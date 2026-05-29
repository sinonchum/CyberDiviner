package com.cyberdiviner.engine.offline

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages offline model download, storage, and deletion.
 *
 * Model is stored in: context.filesDir/offline_model/gemma_litertlm.litertlm
 * Download progress is exposed as a StateFlow for UI binding.
 */
class ModelManager(private val context: Context) {

    companion object {
        private const val TAG = "ModelManager"
        private const val MODEL_DIR = "offline_model"
        private const val MODEL_FILENAME = "qwen25_1b.task"
        private const val TEMP_FILENAME = "qwen25_1b.task.tmp"

        // HuggingFace direct download — Qwen2.5-1.5B int8 (no token needed, Apache 2.0)
        const val MODEL_URL =
            "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv1280.task"
        const val MODEL_DISPLAY_NAME = "Qwen2.5 1.5B (int8)"
        const val MODEL_SIZE_BYTES = 1_600_000_000L // ~1.6 GB
        const val MODEL_SIZE_DISPLAY = "~1.6 GB"
    }

    sealed class ModelState {
        data object NotDownloaded : ModelState()
        data class Downloading(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : ModelState()
        data object Ready : ModelState()
        data class Error(val message: String) : ModelState()
    }

    private val _state = MutableStateFlow<ModelState>(checkInitialState())
    val state: StateFlow<ModelState> = _state.asStateFlow()

    // ── Path helpers ──────────────────────────────────────────────────────────

    private val modelDir: File
        get() = File(context.filesDir, MODEL_DIR)

    private val modelFile: File
        get() = File(modelDir, MODEL_FILENAME)

    private val tempFile: File
        get() = File(modelDir, TEMP_FILENAME)

    // ── State check ───────────────────────────────────────────────────────────

    private fun checkInitialState(): ModelState {
        return if (modelFile.exists() && modelFile.length() > 100_000_000) {
            ModelState.Ready
        } else {
            ModelState.NotDownloaded
        }
    }

    fun isReady(): Boolean = modelFile.exists() && modelFile.length() > 100_000_000

    // ── Download ──────────────────────────────────────────────────────────────

    /**
     * Download the model from HuggingFace.
     * Supports resume — if a partial .tmp file exists, continues from where it left off.
     * On completion, renames .tmp to the final filename.
     */
    suspend fun download(hfToken: String? = null) = withContext(Dispatchers.IO) {
        if (isReady()) {
            _state.value = ModelState.Ready
            return@withContext
        }

        modelDir.mkdirs()

        val existingBytes = if (tempFile.exists()) tempFile.length() else 0L

        try {
            _state.value = ModelState.Downloading(0, existingBytes, MODEL_SIZE_BYTES)

            val url = URL(MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "CyberDiviner/10.1")
            if (!hfToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $hfToken")
            }

            // Resume support
            if (existingBytes > 0) {
                connection.setRequestProperty("Range", "bytes=$existingBytes-")
                Log.d(TAG, "Resuming download from byte $existingBytes")
            }

            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299 && responseCode != 206) {
                throw Exception("HTTP $responseCode: ${connection.responseMessage}")
            }

            val totalBytes = if (responseCode == 206) {
                // Partial content — get total from Content-Range header
                val contentRange = connection.getHeaderField("Content-Range")
                val total = contentRange?.substringAfter("/")?.toLongOrNull() ?: MODEL_SIZE_BYTES
                existingBytes + (connection.contentLengthLong.takeIf { it > 0 } ?: (total - existingBytes))
                total
            } else {
                connection.contentLengthLong.takeIf { it > 0 } ?: MODEL_SIZE_BYTES
            }

            val inputStream = BufferedInputStream(connection.inputStream, 8192)
            val outputStream = FileOutputStream(tempFile, existingBytes > 0)

            val buffer = ByteArray(8192)
            var downloaded = existingBytes
            var bytesRead: Int
            var lastProgressUpdate = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloaded += bytesRead

                // Update progress at most every 500ms
                val now = System.currentTimeMillis()
                if (now - lastProgressUpdate > 500) {
                    val percent = ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                    _state.value = ModelState.Downloading(percent, downloaded, totalBytes)
                    lastProgressUpdate = now
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()

            // Rename temp file to final name
            if (modelFile.exists()) modelFile.delete()
            tempFile.renameTo(modelFile)

            _state.value = ModelState.Ready
            Log.d(TAG, "Model download complete: ${modelFile.absolutePath} (${modelFile.length()} bytes)")

        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            _state.value = ModelState.Error(e.message ?: "Download failed")
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Delete the downloaded model file.
     */
    suspend fun delete() = withContext(Dispatchers.IO) {
        try {
            if (modelFile.exists()) modelFile.delete()
            if (tempFile.exists()) tempFile.delete()
            _state.value = ModelState.NotDownloaded
            Log.d(TAG, "Model deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete model", e)
            _state.value = ModelState.Error("Failed to delete: ${e.message}")
        }
    }

    // ── Cancel download ───────────────────────────────────────────────────────

    /**
     * Cancel an in-progress download. Partial file is kept for resume.
     */
    fun cancelDownload() {
        // The download coroutine will be cancelled by the caller's scope.
        // Partial .tmp file is preserved for resume.
        if (_state.value is ModelState.Downloading) {
            _state.value = ModelState.NotDownloaded
        }
    }
}
