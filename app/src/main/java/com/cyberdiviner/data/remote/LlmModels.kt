package com.cyberdiviner.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supported LLM providers — each maps to its own API format.
 */
enum class LlmProvider(val displayName: String) {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    OLLAMA("Ollama"),
    OPENAI_COMPATIBLE("OpenAI-Compatible");
}

/**
 * Model identifier + provider pairing.
 */
data class LlmModel(
    val provider: LlmProvider,
    val modelId: String,
    val displayName: String = modelId,
    val baseUrl: String? = null,       // null = provider default
    val maxTokens: Int = 4096,
    val supportsVision: Boolean = false
)

/**
 * Resolved configuration for a single LLM call.
 */
data class LlmConfig(
    val apiKey: String,
    val model: LlmModel,
    val temperature: Double = 0.7,
    val systemPrompt: String? = null,
    val maxTokens: Int = model.maxTokens,
    val timeoutMs: Long = 60_000
)

// ── Request / Response wire models ──────────────────────────────────────────

@Serializable
data class LlmMessage(
    val role: String,           // "system" | "user" | "assistant"
    val content: String
)

/**
 * Unified response regardless of provider.
 */
data class LlmCompletion(
    val text: String,
    val model: String,
    val provider: LlmProvider,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val finishReason: String? = null,
    val latencyMs: Long = 0
)

/**
 * Streaming chunk for SSE-style responses.
 */
data class LlmStreamChunk(
    val delta: String,
    val isFinal: Boolean = false,
    val finishReason: String? = null
)

// ── OpenAI wire models ──────────────────────────────────────────────────────

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<LlmMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

@Serializable
data class OpenAiResponse(
    val id: String = "",
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null
)

@Serializable
data class OpenAiChoice(
    val index: Int = 0,
    val message: LlmMessage? = null,
    @SerialName("delta") val delta: OpenAiDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class OpenAiDelta(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0
)

// ── Anthropic wire models ───────────────────────────────────────────────────

@Serializable
data class AnthropicRequest(
    val model: String,
    val maxTokens: Int = 4096,
    val messages: List<AnthropicMessage>,
    val system: String? = null,
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

@Serializable
data class AnthropicMessage(
    val role: String,   // "user" | "assistant"
    val content: String
)

@Serializable
data class AnthropicResponse(
    val id: String = "",
    val content: List<AnthropicContentBlock> = emptyList(),
    val model: String = "",
    val usage: AnthropicUsage? = null,
    @SerialName("stop_reason") val stopReason: String? = null
)

@Serializable
data class AnthropicContentBlock(
    val type: String = "text",
    val text: String = ""
)

@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0
)

// ── Ollama wire models ──────────────────────────────────────────────────────

@Serializable
data class OllamaRequest(
    val model: String,
    val messages: List<LlmMessage>,
    val stream: Boolean = false,
    val options: OllamaOptions? = null
)

@Serializable
data class OllamaOptions(
    val temperature: Double? = null,
    @SerialName("num_predict") val numPredict: Int? = null
)

@Serializable
data class OllamaResponse(
    val model: String = "",
    val message: LlmMessage? = null,
    @SerialName("total_duration") val totalDuration: Long = 0,
    @SerialName("eval_count") val evalCount: Int = 0,
    @SerialName("prompt_eval_count") val promptEvalCount: Int = 0
)

// ── Predefined popular models ───────────────────────────────────────────────

object LlmModels {
    val GPT4O = LlmModel(LlmProvider.OPENAI, "gpt-4o", "GPT-4o", supportsVision = true)
    val GPT4O_MINI = LlmModel(LlmProvider.OPENAI, "gpt-4o-mini", "GPT-4o Mini", supportsVision = true)
    val GPT4_TURBO = LlmModel(LlmProvider.OPENAI, "gpt-4-turbo", "GPT-4 Turbo", supportsVision = true)
    val CLAUDE_SONNET = LlmModel(LlmProvider.ANTHROPIC, "claude-sonnet-4-20250514", "Claude Sonnet 4", supportsVision = true)
    val CLAUDE_HAIKU = LlmModel(LlmProvider.ANTHROPIC, "claude-3-5-haiku-20241022", "Claude 3.5 Haiku")
    val CLAUDE_OPUS = LlmModel(LlmProvider.ANTHROPIC, "claude-opus-4-20250514", "Claude Opus 4", supportsVision = true)
    val LLAMA3 = LlmModel(LlmProvider.OLLAMA, "llama3", "Llama 3", baseUrl = "http://localhost:11434")
    val MISTRAL = LlmModel(LlmProvider.OLLAMA, "mistral", "Mistral", baseUrl = "http://localhost:11434")
    val QWEN2 = LlmModel(LlmProvider.OLLAMA, "qwen2", "Qwen 2", baseUrl = "http://localhost:11434")

    fun defaultsForProvider(provider: LlmProvider): List<LlmModel> = when (provider) {
        LlmProvider.OPENAI -> listOf(GPT4O, GPT4O_MINI, GPT4_TURBO)
        LlmProvider.ANTHROPIC -> listOf(CLAUDE_SONNET, CLAUDE_HAIKU, CLAUDE_OPUS)
        LlmProvider.OLLAMA -> listOf(LLAMA3, MISTRAL, QWEN2)
        LlmProvider.OPENAI_COMPATIBLE -> listOf(GPT4O)
    }
}
