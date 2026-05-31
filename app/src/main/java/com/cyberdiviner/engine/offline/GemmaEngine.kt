package com.cyberdiviner.engine.offline

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Wraps LiteRT-LM for on-device Gemma inference.
 *
 * Uses a Java reflection bridge so Kotlin sources do not directly import
 * LiteRT-LM classes with newer Kotlin metadata.
 *
 * Uses Gemma 3 1B int4 (~529MB) for lower memory pressure.
 * Lifecycle: initialize() → generate() → release()
 * The engine is lazily initialized on first generate() call.
 */
class GemmaEngine(private val context: Context) {

    companion object {
        private const val TAG = "GemmaEngine"
        private const val MODEL_FILENAME = "gemma3_1b_int4.task"

        @Volatile
        private var activeInstance: GemmaEngine? = null

        fun forceReleaseActive() {
            activeInstance?.release()
            activeInstance = null
        }
    }

    @Volatile
    private var bridge: LiteRtLmBridge? = null

    @Volatile
    private var modelReady = false

    private val initMutex = Mutex()
    private val inferenceLock = java.util.concurrent.locks.ReentrantLock()

    init {
        activeInstance = this
    }

    // ── Model path ────────────────────────────────────────────────────────────

    fun getModelPath(): String? {
        val file = File(context.filesDir, "offline_model/$MODEL_FILENAME")
        return if (file.exists() && file.length() > 100_000_000) file.absolutePath else null
    }

    // ── Initialization ────────────────────────────────────────────────────────

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (modelReady && bridge?.isReady == true) return@withContext true

        initMutex.withLock {
            if (modelReady && bridge?.isReady == true) return@withLock true

            val modelPath = getModelPath()
            if (modelPath == null) {
                Log.w(TAG, "Model file not found at expected path")
                return@withLock false
            }

            if (!hasEnoughMemory()) {
                Log.e(TAG, "Insufficient memory to load model")
                return@withLock false
            }

            try {
                Log.d(TAG, "Initializing LiteRT-LM bridge from $modelPath")
                val nextBridge = LiteRtLmBridge()
                nextBridge.initialize(modelPath, context.cacheDir.path, 1024)
                bridge = nextBridge
                modelReady = true
                Log.d(TAG, "LiteRT-LM bridge initialized successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Engine initialization failed", e)
                bridge?.close()
                bridge = null
                modelReady = false
                false
            }
        }
    }

    // ── Inference ─────────────────────────────────────────────────────────────

    suspend fun generate(
        systemInstruction: String,
        userPrompt: String,
        maxTokens: Int = 200,
        temperature: Double = 0.35
    ): String? = withContext(Dispatchers.IO) {
        if (!initialize()) {
            Log.e(TAG, "Cannot generate: engine not initialized")
            return@withContext null
        }

        val localBridge = bridge ?: return@withContext null

        try {
            Log.d(TAG, "Generating offline response (maxTokens=$maxTokens)")

            val fullPrompt = buildGemmaPrompt(systemInstruction, userPrompt)

            val response: String? = try {
                inferenceLock.lock()
                try {
                    localBridge.generate(fullPrompt, temperature, 8)
                } finally {
                    inferenceLock.unlock()
                }
            } catch (e: InterruptedException) {
                Log.w(TAG, "Inference interrupted")
                null
            }
            if (response == null) return@withContext null
            Log.d(TAG, "Offline generation complete: ${response.length} chars")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Offline generation failed", e)
            null
        }
    }

    suspend fun generateStream(
        systemInstruction: String,
        userPrompt: String,
        maxTokens: Int = 200,
        temperature: Double = 0.35,
        onPartialResult: (String) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        if (!initialize()) {
            Log.e(TAG, "Cannot generate: engine not initialized")
            return@withContext null
        }

        val localBridge = bridge ?: return@withContext null

        try {
            Log.d(TAG, "Generating offline response through bridge")
            val fullPrompt = buildGemmaPrompt(systemInstruction, userPrompt)

            val result: String?
            inferenceLock.lock()
            try {
                result = localBridge.generate(fullPrompt, temperature, 8)
            } finally {
                inferenceLock.unlock()
            }

            if (!result.isNullOrBlank()) onPartialResult(result)
            Log.d(TAG, "Offline bridge generation complete: ${result?.length ?: 0} chars")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Offline stream generation failed", e)
            null
        }
    }

    // ── Resource management ───────────────────────────────────────────────────

    fun release() {
        inferenceLock.lock()
        try {
            try {
                bridge?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing engine", e)
            }
            bridge = null
            modelReady = false
            Log.d(TAG, "Engine released")
        } finally {
            inferenceLock.unlock()
        }
    }

    fun isReady(): Boolean = modelReady && bridge?.isReady == true

    fun isModelDownloaded(): Boolean = getModelPath() != null

    private fun buildGemmaPrompt(systemInstruction: String, userPrompt: String): String {
        return if (systemInstruction.isNotBlank()) {
            "$systemInstruction\n\n$userPrompt"
        } else {
            userPrompt
        }
    }

    // ── Memory check ──────────────────────────────────────────────────────────

    /**
     * Check available memory. Gemma 3 1B int4 is the low-memory path, so avoid
     * rejecting normal devices just because Android reports conservative free RAM.
     */
    private fun hasEnoughMemory(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val availableMB = memInfo.availMem / (1024 * 1024)
        val thresholdMB = 450L
        Log.d(TAG, "Available memory: ${availableMB}MB, threshold: ${thresholdMB}MB, lowMemory=${memInfo.lowMemory}")
        return availableMB > thresholdMB && !memInfo.lowMemory
    }
}
