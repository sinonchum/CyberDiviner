package com.cyberdiviner.engine.offline

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Wraps MediaPipe LLM Inference API for on-device Gemma inference.
 *
 * Lifecycle:
 *   initialize() → generate() → release()
 *
 * The engine is lazily initialized on first generate() call.
 * Resources are released when the app enters background or memory is low.
 */
class GemmaEngine(private val context: Context) {

    companion object {
        private const val TAG = "GemmaEngine"
        private const val MODEL_FILENAME = "qwen25_1b.task"
    }

    @Volatile
    private var llmInference: LlmInference? = null

    @Volatile
    private var modelReady = false

    private val initMutex = Mutex()

    // ── Model path ────────────────────────────────────────────────────────────

    /**
     * Returns the path to the downloaded .task model file,
     * or null if the model is not yet downloaded.
     */
    fun getModelPath(): String? {
        val file = File(context.filesDir, "offline_model/$MODEL_FILENAME")
        return if (file.exists() && file.length() > 100_000_000) file.absolutePath else null
    }

    // ── Initialization ────────────────────────────────────────────────────────

    /**
     * Initialize the MediaPipe LLM Inference engine.
     * Thread-safe: concurrent calls will wait for the first initialization.
     * Returns true if the engine is ready, false if model not found or init failed.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (modelReady && llmInference != null) return@withContext true

        initMutex.withLock {
            // Double-check after acquiring lock
            if (modelReady && llmInference != null) return@withLock true

            val modelPath = getModelPath()
            if (modelPath == null) {
                Log.w(TAG, "Model file not found at expected path")
                return@withLock false
            }

            // Check available memory before loading
            if (!hasEnoughMemory()) {
                Log.e(TAG, "Insufficient memory to load model")
                return@withLock false
            }

            try {
                Log.d(TAG, "Initializing LLM Inference from $modelPath")
                val options = LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .setMaxTopK(64)
                    .build()

                val inference = LlmInference.createFromOptions(context, options)

                llmInference = inference
                modelReady = true
                Log.d(TAG, "Engine initialized successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Engine initialization failed", e)
                modelReady = false
                false
            }
        }
    }

    // ── Inference ─────────────────────────────────────────────────────────────

    /**
     * Generate a response from the on-device model.
     * Non-streaming — returns the complete response text.
     *
     * @param systemInstruction System instruction prepended to user prompt.
     * @param userPrompt The user's input.
     * @param maxTokens Maximum tokens to generate.
     * @return The generated text, or null if generation failed.
     */
    suspend fun generate(
        systemInstruction: String,
        userPrompt: String,
        maxTokens: Int = 200,
        temperature: Double = 0.7
    ): String? = withContext(Dispatchers.IO) {
        if (!initialize()) {
            Log.e(TAG, "Cannot generate: engine not initialized")
            return@withContext null
        }

        val inference = llmInference ?: return@withContext null

        try {
            Log.d(TAG, "Generating offline response (maxTokens=$maxTokens)")

            // MediaPipe LLM Inference doesn't have a separate system instruction field.
            // We prepend it as context to the user prompt.
            val fullPrompt = if (systemInstruction.isNotBlank()) {
                "$systemInstruction\n\n$userPrompt"
            } else {
                userPrompt
            }

            val response = inference.generateResponse(fullPrompt)
            Log.d(TAG, "Offline generation complete: ${response.length} chars")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Offline generation failed", e)
            null
        }
    }

    /**
     * Generate a streaming response. Calls onPartialResult for each chunk.
     * Returns the full accumulated text.
     */
    suspend fun generateStream(
        systemInstruction: String,
        userPrompt: String,
        maxTokens: Int = 200,
        temperature: Double = 0.7,
        onPartialResult: (String) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        if (!initialize()) {
            Log.e(TAG, "Cannot generate: engine not initialized")
            return@withContext null
        }

        val inference = llmInference ?: return@withContext null

        try {
            Log.d(TAG, "Generating offline stream response")

            val fullPrompt = if (systemInstruction.isNotBlank()) {
                "$systemInstruction\n\n$userPrompt"
            } else {
                userPrompt
            }

            // Use async generation with callback
            val resultBuilder = StringBuilder()
            val latch = java.util.concurrent.CountDownLatch(1)
            var error: Exception? = null

            inference.generateResponseAsync(fullPrompt) { partialResult, done ->
                resultBuilder.append(partialResult)
                onPartialResult(partialResult)
                if (done) {
                    latch.countDown()
                }
            }

            latch.await() // Wait for completion
            val result = resultBuilder.toString()
            Log.d(TAG, "Offline stream complete: ${result.length} chars")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Offline stream generation failed", e)
            null
        }
    }

    // ── Resource management ───────────────────────────────────────────────────

    /**
     * Release engine resources. Safe to call multiple times.
     * The engine will be lazily re-initialized on next generate() call.
     */
    fun release() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing engine", e)
        }
        llmInference = null
        modelReady = false
        Log.d(TAG, "Engine released")
    }

    /**
     * Whether the engine is currently loaded and ready for inference.
     */
    fun isReady(): Boolean = modelReady && llmInference != null

    /**
     * Whether the model file has been downloaded.
     */
    fun isModelDownloaded(): Boolean = getModelPath() != null

    // ── Memory check ──────────────────────────────────────────────────────────

    /**
     * Check if the device has enough available memory to load the model.
     * Requires at least 2.5GB free to safely load a ~2GB model.
     */
    private fun hasEnoughMemory(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val availableMB = memInfo.availMem / (1024 * 1024)
        Log.d(TAG, "Available memory: ${availableMB}MB, threshold: 1500MB")
        return availableMB > 1500
    }
}
