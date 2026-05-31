package com.cyberdiviner.engine.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.cyberdiviner.data.model.InferenceMode
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.LlmService
import com.cyberdiviner.engine.Persona
import kotlinx.coroutines.flow.first

/**
 * Central inference router — decides whether to use online (LlmService)
 * or offline (GemmaEngine) for each inference request.
 *
 * Decision logic:
 *   AUTO:    online if network + API key available, else offline
 *   ONLINE:  always online (fallback to offline on network error)
 *   OFFLINE: always offline
 *
 * Usage:
 *   val response = router.complete(
 *       feature = "oracle",
 *       messages = listOf(LlmMessage("user", question)),
 *       offlineUserPrompt = question
 *   )
 */
class InferenceRouter(
    private val context: Context,
    private val llmService: LlmService,
    private val gemmaEngine: GemmaEngine,
    private val configManager: LlmConfigManager,
    private val offlinePromptBuilder: OfflinePromptBuilder
) {
    companion object {
        private const val TAG = "InferenceRouter"
    }

    // ── Result wrapper ────────────────────────────────────────────────────────

    data class InferenceResult(
        val text: String,
        val isOffline: Boolean
    )

    // ── Main entry point ──────────────────────────────────────────────────────

    /**
     * Route an inference request to the appropriate engine.
     *
     * @param feature Feature key ("oracle", "liuyao", "tarot", "vision").
     * @param messages Full conversation history for online mode.
     * @param offlineUserPrompt Concise user prompt for offline mode.
     * @param persona Current persona for online system prompt.
     * @return InferenceResult with text and a flag indicating the source.
     */
    suspend fun complete(
        feature: String,
        messages: List<LlmMessage>,
        offlineUserPrompt: String,
        persona: Persona = Persona.DEFAULT
    ): InferenceResult {
        val mode = getInferenceMode()
        val offlineEnabled = isOfflineModelEnabled()

        return when (mode) {
            InferenceMode.ONLINE -> completeOnline(feature, messages, persona)
                ?: throw Exception("Online inference failed. Please check API key and network.")

            InferenceMode.OFFLINE -> {
                Log.i(TAG, "Inference mode OFFLINE: forcing local model for $feature")
                completeOffline(feature, offlineUserPrompt)
                    ?: throw Exception("Offline inference failed. Please download the model in settings.")
            }

            InferenceMode.AUTO -> {
                // Try online first
                val onlineResult = completeOnline(feature, messages, persona)
                if (onlineResult != null) return onlineResult

                // Fallback to offline
                if (!offlineEnabled) {
                    throw Exception("Online inference failed and offline model is disabled.")
                }
                Log.d(TAG, "Online failed, falling back to offline")
                completeOffline(feature, offlineUserPrompt)
                    ?: throw Exception("Both online and offline inference failed.")
            }
        }
    }

    // ── Online inference ──────────────────────────────────────────────────────

    private suspend fun completeOnline(
        feature: String,
        messages: List<LlmMessage>,
        persona: Persona
    ): InferenceResult? {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available")
            return null
        }

        val config = configManager.buildConfig(
            systemPrompt = com.cyberdiviner.data.remote.PromptManager().resolveSystem(feature, persona)
        ) ?: return null // No API key configured

        return try {
            val response = llmService.complete(config, messages)
            InferenceResult(text = response.text, isOffline = false)
        } catch (e: Exception) {
            Log.e(TAG, "Online inference failed", e)
            null
        }
    }

    // ── Offline inference ─────────────────────────────────────────────────────

    private suspend fun completeOffline(
        feature: String,
        offlineUserPrompt: String
    ): InferenceResult? {
        if (!gemmaEngine.isModelDownloaded()) {
            Log.d(TAG, "Offline model not downloaded")
            return null
        }

        val systemInstruction = offlinePromptBuilder.buildSystemInstruction(feature)
        val maxTokens = offlinePromptBuilder.getMaxTokens(feature)

        val response = gemmaEngine.generate(
            systemInstruction = systemInstruction,
            userPrompt = offlineUserPrompt,
            maxTokens = maxTokens
        ) ?: return null

        return InferenceResult(text = response, isOffline = true)
    }

    // ── Streaming support (for Oracle chat) ───────────────────────────────────

    /**
     * Stream an inference response. For online mode, streams via SSE.
     * For offline mode, returns the full response at once (no streaming).
     */
    suspend fun completeStream(
        feature: String,
        messages: List<LlmMessage>,
        offlineUserPrompt: String,
        persona: Persona = Persona.DEFAULT,
        onChunk: (String) -> Unit
    ): InferenceResult {
        val mode = getInferenceMode()

        return when (mode) {
            InferenceMode.ONLINE -> {
                val config = configManager.buildConfig(
                    systemPrompt = com.cyberdiviner.data.remote.PromptManager().resolveSystem(feature, persona)
                ) ?: throw Exception("API key not configured")

                val fullText = llmService.completeStream(config, messages) { chunk ->
                    if (chunk.delta.isNotEmpty()) onChunk(chunk.delta)
                }
                InferenceResult(text = fullText, isOffline = false)
            }

            InferenceMode.OFFLINE -> {
                Log.i(TAG, "Inference mode OFFLINE: forcing local stream model for $feature")
                val result = completeOffline(feature, offlineUserPrompt)
                    ?: throw Exception("Offline inference failed")
                onChunk(result.text) // Emit all at once (no streaming for offline)
                result
            }

            InferenceMode.AUTO -> {
                // Try online streaming first
                try {
                    val config = configManager.buildConfig(
                        systemPrompt = com.cyberdiviner.data.remote.PromptManager().resolveSystem(feature, persona)
                    ) ?: throw Exception("No API key")

                    if (!isNetworkAvailable()) throw Exception("No network")

                    val fullText = llmService.completeStream(config, messages) { chunk ->
                        if (chunk.delta.isNotEmpty()) onChunk(chunk.delta)
                    }
                    InferenceResult(text = fullText, isOffline = false)
                } catch (e: Exception) {
                    Log.d(TAG, "Online streaming failed, falling back to offline: ${e.message}")
                    if (!isOfflineModelEnabled()) {
                        throw Exception("Online streaming failed and offline model is disabled.")
                    }
                    val result = completeOffline(feature, offlineUserPrompt)
                        ?: throw Exception("Both online and offline inference failed")
                    onChunk(result.text)
                    result
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    suspend fun currentMode(): InferenceMode = getInferenceMode()

    private suspend fun getInferenceMode(): InferenceMode {
        val modeName = configManager.inferenceMode.first()
        return InferenceMode.fromName(modeName)
    }

    private suspend fun isOfflineModelEnabled(): Boolean {
        return configManager.offlineModelEnabled.first()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Whether offline mode is currently usable (model downloaded + engine ready or can initialize).
     */
    suspend fun isOfflineAvailable(): Boolean {
        return (getInferenceMode() == InferenceMode.OFFLINE || isOfflineModelEnabled()) &&
            gemmaEngine.isModelDownloaded()
    }

    /**
     * Whether online mode is currently usable (API key configured + network available).
     */
    suspend fun isOnlineAvailable(): Boolean {
        val config = configManager.buildConfig()
        return config != null && isNetworkAvailable()
    }
}
