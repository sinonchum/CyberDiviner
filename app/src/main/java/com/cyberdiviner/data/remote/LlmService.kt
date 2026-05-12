package com.cyberdiviner.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Model-agnostic LLM service. Routes requests to the correct provider wire format,
 * parses responses into a unified [LlmCompletion].
 *
 * Usage:
 *   val service = LlmService()
 *   val response = service.complete(config, messages)
 */
class LlmService(
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) {
    companion object {
        private const val TAG = "LlmService"

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    // ── Non-streaming completion ────────────────────────────────────────────

    suspend fun complete(
        config: LlmConfig,
        messages: List<LlmMessage>
    ): LlmCompletion = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "→ ${config.model.provider.name}/${config.model.modelId} (${messages.size} messages)")

        val request = buildRequest(config, messages, stream = false)
        val responseBody = executeRequest(request)
        val latency = System.currentTimeMillis() - startTime

        val completion = when (config.model.provider) {
            LlmProvider.OPENAI, LlmProvider.OPENAI_COMPATIBLE ->
                parseOpenAi(responseBody, config, latency)
            LlmProvider.ANTHROPIC ->
                parseAnthropic(responseBody, config, latency)
            LlmProvider.OLLAMA ->
                parseOllama(responseBody, config, latency)
        }

        Log.d(TAG, "← ${completion.completionTokens} tokens in ${completion.latencyMs}ms")
        completion
    }

    // ── Streaming completion ────────────────────────────────────────────────

    /**
     * Yields [LlmStreamChunk] via OkHttp's streaming response body.
     * Returns the full accumulated text when the stream finishes.
     */
    suspend fun completeStream(
        config: LlmConfig,
        messages: List<LlmMessage>,
        onChunk: (LlmStreamChunk) -> Unit
    ): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "→ STREAM ${config.model.provider.name}/${config.model.modelId}")

        val request = buildRequest(config, messages, stream = true)
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val body = response.body?.string() ?: ""
            throw LlmException(response.code, "Stream request failed: ${response.code} $body")
        }

        val body = response.body ?: throw LlmException(-1, "Empty response body")
        val source = body.source()
        val buffer = StringBuilder()

        try {
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break

                val chunk = parseStreamChunk(data, config.model.provider)
                if (chunk.delta.isNotEmpty()) {
                    buffer.append(chunk.delta)
                    onChunk(chunk)
                }
                if (chunk.isFinal) break
            }
        } finally {
            body.close()
        }

        onChunk(LlmStreamChunk("", isFinal = true))
        buffer.toString()
    }

    // ── Request building ────────────────────────────────────────────────────

    private fun buildRequest(
        config: LlmConfig,
        messages: List<LlmMessage>,
        stream: Boolean
    ): Request {
        val (url, body) = when (config.model.provider) {
            LlmProvider.OPENAI, LlmProvider.OPENAI_COMPATIBLE -> {
                val baseUrl = config.model.baseUrl ?: "https://api.openai.com/v1"
                val allMessages = mutableListOf<LlmMessage>()
                config.systemPrompt?.let { allMessages.add(LlmMessage("system", it)) }
                allMessages.addAll(messages)

                "$baseUrl/chat/completions" to json.encodeToString(
                    OpenAiRequest(
                        model = config.model.modelId,
                        messages = allMessages,
                        maxTokens = config.maxTokens,
                        temperature = config.temperature,
                        stream = stream
                    )
                )
            }
            LlmProvider.ANTHROPIC -> {
                val baseUrl = config.model.baseUrl ?: "https://api.anthropic.com/v1"
                val apiMessages = messages.filter { it.role != "system" }.map {
                    AnthropicMessage(role = it.role, content = it.content)
                }

                "$baseUrl/messages" to json.encodeToString(
                    AnthropicRequest(
                        model = config.model.modelId,
                        maxTokens = config.maxTokens,
                        messages = apiMessages,
                        system = config.systemPrompt,
                        temperature = config.temperature,
                        stream = stream
                    )
                )
            }
            LlmProvider.OLLAMA -> {
                val baseUrl = config.model.baseUrl ?: "http://localhost:11434"
                val allMessages = mutableListOf<LlmMessage>()
                config.systemPrompt?.let { allMessages.add(LlmMessage("system", it)) }
                allMessages.addAll(messages)

                "$baseUrl/api/chat" to json.encodeToString(
                    OllamaRequest(
                        model = config.model.modelId,
                        messages = allMessages,
                        stream = stream,
                        options = OllamaOptions(
                            temperature = config.temperature,
                            numPredict = config.maxTokens
                        )
                    )
                )
            }
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_MEDIA))

        // Auth headers per provider
        when (config.model.provider) {
            LlmProvider.OPENAI, LlmProvider.OPENAI_COMPATIBLE ->
                requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
            LlmProvider.ANTHROPIC -> {
                requestBuilder.addHeader("x-api-key", config.apiKey)
                requestBuilder.addHeader("anthropic-version", "2023-06-01")
            }
            LlmProvider.OLLAMA -> { /* no auth needed */ }
        }

        requestBuilder.addHeader("Content-Type", "application/json")

        return requestBuilder.build()
    }

    // ── HTTP execution ──────────────────────────────────────────────────────

    private fun executeRequest(request: Request): String {
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e(TAG, "HTTP ${response.code}: $body")
            throw LlmException(response.code, "Request failed: ${response.code} $body")
        }

        return body
    }

    private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (cont.isActive) cont.resume(response)
            }
        })
    }

    // ── Response parsing ────────────────────────────────────────────────────

    private fun parseOpenAi(
        body: String,
        config: LlmConfig,
        latency: Long
    ): LlmCompletion {
        val response = json.decodeFromString<OpenAiResponse>(body)
        val choice = response.choices.firstOrNull()
        val text = choice?.message?.content ?: ""
        val usage = response.usage

        return LlmCompletion(
            text = text,
            model = config.model.modelId,
            provider = LlmProvider.OPENAI,
            promptTokens = usage?.promptTokens ?: 0,
            completionTokens = usage?.completionTokens ?: 0,
            finishReason = choice?.finishReason,
            latencyMs = latency
        )
    }

    private fun parseAnthropic(
        body: String,
        config: LlmConfig,
        latency: Long
    ): LlmCompletion {
        val response = json.decodeFromString<AnthropicResponse>(body)
        val text = response.content.firstOrNull { it.type == "text" }?.text ?: ""
        val usage = response.usage

        return LlmCompletion(
            text = text,
            model = config.model.modelId,
            provider = LlmProvider.ANTHROPIC,
            promptTokens = usage?.inputTokens ?: 0,
            completionTokens = usage?.outputTokens ?: 0,
            finishReason = response.stopReason,
            latencyMs = latency
        )
    }

    private fun parseOllama(
        body: String,
        config: LlmConfig,
        latency: Long
    ): LlmCompletion {
        val response = json.decodeFromString<OllamaResponse>(body)

        return LlmCompletion(
            text = response.message?.content ?: "",
            model = config.model.modelId,
            provider = LlmProvider.OLLAMA,
            promptTokens = response.promptEvalCount,
            completionTokens = response.evalCount,
            finishReason = "stop",
            latencyMs = latency
        )
    }

    // ── SSE stream chunk parsing ────────────────────────────────────────────

    private fun parseStreamChunk(
        data: String,
        provider: LlmProvider
    ): LlmStreamChunk = when (provider) {
        LlmProvider.OPENAI, LlmProvider.OPENAI_COMPATIBLE -> {
            val chunk = json.decodeFromString<OpenAiResponse>(data)
            val delta = chunk.choices.firstOrNull()?.delta?.content ?: ""
            val finish = chunk.choices.firstOrNull()?.finishReason
            LlmStreamChunk(delta = delta, isFinal = finish != null, finishReason = finish)
        }
        LlmProvider.ANTHROPIC -> {
            // Anthropic SSE: {"type":"content_block_delta","delta":{"type":"text_delta","text":"..."}}
            // Simplified parsing — extract "text" field
            val textMatch = Regex(""""text"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(data)
            val delta = textMatch?.groupValues?.get(1)?.replace("\\n", "\n")?.replace("\\\"", "\"") ?: ""
            val isStop = data.contains("\"stop_reason\"") || data.contains("\"type\":\"message_stop\"")
            LlmStreamChunk(delta = delta, isFinal = isStop)
        }
        LlmProvider.OLLAMA -> {
            val chunk = json.decodeFromString<OllamaResponse>(data)
            LlmStreamChunk(
                delta = chunk.message?.content ?: "",
                isFinal = false // Ollama sends empty message at end
            )
        }
    }
}

/**
 * Exception carrying the HTTP status and error body from an LLM provider.
 */
class LlmException(
    val statusCode: Int,
    override val message: String
) : Exception(message)
