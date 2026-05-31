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
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.TimeZone

/**
 * Manages offline model download, storage, and deletion.
 *
 * Smart multi-source download:
 *   - China mainland → hf-mirror → ModelScope → HuggingFace
 *   - Overseas        → HuggingFace → hf-mirror → ModelScope
 *
 * Region is detected via timezone + lightweight HTTP check.
 * Also supports manual .task file import.
 */
class ModelManager(private val context: Context) {

    companion object {
        private const val TAG = "ModelManager"
        private const val MODEL_DIR = "offline_model"
        private const val MODEL_FILENAME = "gemma3_1b_int4.task"
        private const val TEMP_FILENAME = "gemma3_1b_int4.task.tmp"

        const val MODEL_DISPLAY_NAME = "CyberDiviner Gemma 3 1B"
        const val MODEL_SIZE_BYTES = 1_025_084_110L // Current dynamic-int8 LiteRT bundle
        const val MODEL_SIZE_DISPLAY = "~978 MB"

        // ── Multi-source URLs ──────────────────────────────────────────────
        private const val REPO_ID = "MiCkSoftware/Gemma3-1B-IT-LiteRT"
        private const val FILE_PATH = "gemma3-1b-it-int4.task"

        private val SOURCE_HF_MIRROR = ModelSource(
            name = "hf-mirror（国内镜像）",
            url = "https://hf-mirror.com/$REPO_ID/resolve/main/$FILE_PATH",
            requiresAuth = false,
        )
        private val SOURCE_HF = ModelSource(
            name = "HuggingFace",
            url = "https://huggingface.co/$REPO_ID/resolve/main/$FILE_PATH",
            requiresAuth = true,
        )
        private val SOURCE_MODELSCOPE = ModelSource(
            name = "ModelScope",
            url = "https://modelscope.cn/models/$REPO_ID/resolve/main/$FILE_PATH",
            requiresAuth = false,
        )

        /** Quick timezone heuristic — no network needed. */
        private fun isLikelyChinaTimezone(): Boolean {
            val tz = TimeZone.getDefault().id
            return tz in listOf(
                "Asia/Shanghai", "Asia/Chongqing", "Asia/Urumqi",
                "Asia/Harbin", "Asia/Kashgar"
            )
        }

        /**
         * Lightweight HTTP geolocation check.
         * Calls ip-api.com (free, no key, ~50ms response).
         * Returns true if country is CN.
         */
        private fun isChinaByIp(): Boolean {
            return try {
                val conn = URL("http://ip-api.com/json/?fields=countryCode")
                    .openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("User-Agent", "CyberDiviner/10.1")
                conn.connect()
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                body.contains("\"CN\"")
            } catch (e: Exception) {
                Log.w(TAG, "IP check failed: ${e.message}")
                false
            }
        }

        /**
         * Detect if user is in mainland China.
         * Try timezone first (instant), then HTTP fallback.
         */
        private fun detectIsChina(): Boolean {
            if (isLikelyChinaTimezone()) {
                Log.d(TAG, "Region: China (timezone)")
                return true
            }
            val ipResult = isChinaByIp()
            Log.d(TAG, "Region: ${if (ipResult) "China (IP)" else "Overseas"}")
            return ipResult
        }

        /** Build source list in priority order based on region. */
        fun buildSources(): List<ModelSource> {
            return if (detectIsChina()) {
                listOf(SOURCE_HF_MIRROR, SOURCE_MODELSCOPE, SOURCE_HF)
            } else {
                listOf(SOURCE_HF, SOURCE_HF_MIRROR, SOURCE_MODELSCOPE)
            }
        }
    }

    /** Describes one download source. */
    data class ModelSource(
        val name: String,
        val url: String,
        val requiresAuth: Boolean,
    )

    sealed class ModelState {
        data object NotDownloaded : ModelState()
        data class Downloading(
            val percent: Int,
            val bytesDownloaded: Long,
            val totalBytes: Long,
            val sourceName: String = "",
        ) : ModelState()
        data object Ready : ModelState()
        data class Error(val message: String) : ModelState()
    }

    private val _state = MutableStateFlow<ModelState>(checkInitialState())
    val state: StateFlow<ModelState> = _state.asStateFlow()

    @Volatile
    private var cancelled = false

    // ── Path helpers ──────────────────────────────────────────────────────

    private val modelDir: File
        get() = File(context.filesDir, MODEL_DIR)

    private val modelFile: File
        get() = File(modelDir, MODEL_FILENAME)

    private val tempFile: File
        get() = File(modelDir, TEMP_FILENAME)

    // ── State check ───────────────────────────────────────────────────────

    private fun checkInitialState(): ModelState {
        return if (modelFile.exists() && modelFile.length() > 100_000_000) {
            ModelState.Ready
        } else {
            ModelState.NotDownloaded
        }
    }

    fun isReady(): Boolean = modelFile.exists() && modelFile.length() > 100_000_000

    // ── Multi-source download with fallback ───────────────────────────────

    suspend fun download(hfToken: String? = null) = withContext(Dispatchers.IO) {
        if (isReady()) {
            _state.value = ModelState.Ready
            return@withContext
        }

        modelDir.mkdirs()
        cancelled = false

        val sources = buildSources()
        val errors = mutableListOf<String>()

        for ((index, source) in sources.withIndex()) {
            if (cancelled) break

            Log.d(TAG, "Trying source ${index + 1}/${sources.size}: ${source.name}")
            _state.value = ModelState.Downloading(
                percent = 0,
                bytesDownloaded = if (tempFile.exists()) tempFile.length() else 0L,
                totalBytes = MODEL_SIZE_BYTES,
                sourceName = source.name,
            )

            try {
                downloadFromSource(source, hfToken)
                _state.value = ModelState.Ready
                Log.d(TAG, "Model download complete from ${source.name}")
                return@withContext
            } catch (e: Exception) {
                val msg = "${source.name}: ${e.message}"
                Log.w(TAG, "Source failed: $msg")
                errors.add(msg)
            }
        }

        val errorMsg = if (cancelled) "下载已取消"
        else "所有下载源均失败:\n${errors.joinToString("\n")}"
        Log.e(TAG, errorMsg)
        _state.value = ModelState.Error(errorMsg)
    }

    private fun downloadFromSource(source: ModelSource, hfToken: String?) {
        val existingBytes = if (tempFile.exists()) tempFile.length() else 0L

        val url = URL(source.url)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "CyberDiviner/10.1")

        if (source.requiresAuth && !hfToken.isNullOrBlank()) {
            connection.setRequestProperty("Authorization", "Bearer $hfToken")
        }

        if (existingBytes > 0) {
            connection.setRequestProperty("Range", "bytes=$existingBytes-")
            Log.d(TAG, "Resuming from byte $existingBytes")
        }

        connection.instanceFollowRedirects = true
        connection.connectTimeout = 30_000
        connection.readTimeout = 60_000
        connection.connect()

        val responseCode = connection.responseCode
        if (responseCode !in 200..299 && responseCode != 206) {
            connection.disconnect()
            throw Exception("HTTP $responseCode: ${connection.responseMessage}")
        }

        val totalBytes = if (responseCode == 206) {
            val contentRange = connection.getHeaderField("Content-Range")
            contentRange?.substringAfter("/")?.toLongOrNull() ?: MODEL_SIZE_BYTES
        } else {
            connection.contentLengthLong.takeIf { it > 0 } ?: MODEL_SIZE_BYTES
        }

        val inputStream = BufferedInputStream(connection.inputStream, 8192)
        val outputStream = FileOutputStream(tempFile, existingBytes > 0)

        try {
            copyStream(inputStream, outputStream, existingBytes, totalBytes, source.name)
        } finally {
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()
        }

        val finalSize = tempFile.length()
        if (finalSize < 100_000_000) {
            throw Exception("文件过小 (${finalSize} bytes)，可能不是完整模型")
        }

        if (modelFile.exists()) modelFile.delete()
        if (!tempFile.renameTo(modelFile)) {
            throw Exception("无法重命名临时文件")
        }
    }

    private fun copyStream(
        input: InputStream,
        output: FileOutputStream,
        startBytes: Long,
        totalBytes: Long,
        sourceName: String,
    ) {
        val buffer = ByteArray(8192)
        var downloaded = startBytes
        var bytesRead: Int
        var lastUpdate = 0L

        while (input.read(buffer).also { bytesRead = it } != -1) {
            if (cancelled) throw Exception("下载已取消")

            output.write(buffer, 0, bytesRead)
            downloaded += bytesRead

            val now = System.currentTimeMillis()
            if (now - lastUpdate > 500) {
                val percent = ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                _state.value = ModelState.Downloading(
                    percent = percent,
                    bytesDownloaded = downloaded,
                    totalBytes = totalBytes,
                    sourceName = sourceName,
                )
                lastUpdate = now
            }
        }
    }

    // ── Manual import ─────────────────────────────────────────────────────

    suspend fun importFromStream(inputStream: InputStream) = withContext(Dispatchers.IO) {
        modelDir.mkdirs()

        try {
            _state.value = ModelState.Downloading(0, 0, 0, sourceName = "本地导入")

            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(8192)
            var totalRead = 0L
            var bytesRead: Int
            var lastUpdate = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastUpdate > 300) {
                    _state.value = ModelState.Downloading(
                        percent = -1,
                        bytesDownloaded = totalRead,
                        totalBytes = 0,
                        sourceName = "本地导入",
                    )
                    lastUpdate = now
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            if (totalRead < 100_000_000) {
                tempFile.delete()
                throw Exception("文件过小 (${totalRead} bytes)，请选择有效的 .task 模型文件")
            }

            if (modelFile.exists()) modelFile.delete()
            if (!tempFile.renameTo(modelFile)) {
                throw Exception("无法写入模型目录")
            }

            _state.value = ModelState.Ready
            Log.d(
                TAG,
                "Model imported: ${modelFile.absolutePath} " +
                    "(read=$totalRead bytes, final=${modelFile.length()} bytes)"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            _state.value = ModelState.Error("导入失败: ${e.message}")
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────

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

    // ── Cancel ────────────────────────────────────────────────────────────

    fun cancelDownload() {
        cancelled = true
    }
}
