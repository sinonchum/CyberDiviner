package com.cyberdiviner.engine.offline

import android.content.Context
import android.util.Log
import com.cyberdiviner.data.remote.LlmConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
        private const val LEGACY_MODEL_FILENAME = "gemma3_1b_int4.task"

        const val MODEL_DISPLAY_NAME = "Gemma 3 1B"
        const val MODEL_SIZE_BYTES = 1_025_084_110L // Current dynamic-int8 LiteRT bundle
        const val MODEL_SIZE_DISPLAY = "~978 MB"

        // ── Multi-source URLs ──────────────────────────────────────────────
        private const val BASE_REPO_ID = "MiCkSoftware/Gemma3-1B-IT-LiteRT"
        private const val BASE_FILE_PATH = "gemma3-1b-it-int4.task"
        private const val TUNED_REPO_ID = "Sinonchum/cyberdiviner-gemma-3-1b"
        private const val TUNED_FILE_PATH = "gemma3_1b_int4.task"
        private const val BASE_MODEL_SIZE_BYTES = 606_000_000L
        private const val BASE_MODEL_SIZE_DISPLAY = "~578 MB"
        private const val TUNED_MODEL_SIZE_BYTES = MODEL_SIZE_BYTES
        private const val TUNED_MODEL_SIZE_DISPLAY = MODEL_SIZE_DISPLAY

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
        fun buildSources(variant: OfflineModelVariant): List<ModelSource> {
            val sourceHfMirror = ModelSource(
                name = "hf-mirror（国内镜像）",
                url = "https://hf-mirror.com/${variant.repoId}/resolve/main/${variant.filePath}",
                requiresAuth = false,
            )
            val sourceHf = ModelSource(
                name = "HuggingFace",
                url = "https://huggingface.co/${variant.repoId}/resolve/main/${variant.filePath}",
                requiresAuth = true,
            )
            val sourceModelScope = ModelSource(
                name = "ModelScope",
                url = "https://modelscope.cn/models/${variant.repoId}/resolve/main/${variant.filePath}",
                requiresAuth = false,
            )
            return if (detectIsChina()) {
                listOf(sourceHfMirror, sourceModelScope, sourceHf)
            } else {
                listOf(sourceHf, sourceHfMirror, sourceModelScope)
            }
        }
    }

    enum class OfflineModelVariant(
        val displayName: String,
        val description: String,
        val filename: String,
        val tempFilename: String,
        val repoId: String,
        val filePath: String,
        val sizeBytes: Long,
        val sizeDisplay: String,
    ) {
        BASE_GEMMA_3_1B(
            displayName = "Gemma 3 1B",
            description = "Google 原版 LiteRT 模型，体积更小，适合通用离线推理",
            filename = LEGACY_MODEL_FILENAME,
            tempFilename = "$LEGACY_MODEL_FILENAME.tmp",
            repoId = BASE_REPO_ID,
            filePath = BASE_FILE_PATH,
            sizeBytes = BASE_MODEL_SIZE_BYTES,
            sizeDisplay = BASE_MODEL_SIZE_DISPLAY,
        ),
        CYBERDIVINER_GEMMA_3_1B(
            displayName = "CyberDiviner Gemma 3 1B",
            description = "面向签文、解卦、牌义输出优化的 CyberDiviner 端侧模型",
            filename = "cyberdiviner_gemma3_1b_int4.task",
            tempFilename = "cyberdiviner_gemma3_1b_int4.task.tmp",
            repoId = TUNED_REPO_ID,
            filePath = TUNED_FILE_PATH,
            sizeBytes = TUNED_MODEL_SIZE_BYTES,
            sizeDisplay = TUNED_MODEL_SIZE_DISPLAY,
        );

        companion object {
            fun fromName(name: String): OfflineModelVariant =
                entries.firstOrNull { it.name == name } ?: BASE_GEMMA_3_1B
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

    private val configManager = LlmConfigManager(context)

    private val _state = MutableStateFlow<ModelState>(checkInitialState())
    val state: StateFlow<ModelState> = _state.asStateFlow()

    @Volatile
    private var cancelled = false

    // ── Path helpers ──────────────────────────────────────────────────────

    private val modelDir: File
        get() = File(context.filesDir, MODEL_DIR)

    private fun selectedVariant(): OfflineModelVariant = runBlocking {
        OfflineModelVariant.fromName(configManager.offlineModelVariant.first())
    }

    private val modelFile: File
        get() = File(modelDir, selectedVariant().filename)

    private val tempFile: File
        get() = File(modelDir, selectedVariant().tempFilename)

    // ── State check ───────────────────────────────────────────────────────

    private fun checkInitialState(): ModelState {
        return if (modelFile.exists() && modelFile.length() > 100_000_000) {
            ModelState.Ready
        } else {
            ModelState.NotDownloaded
        }
    }

    fun isReady(): Boolean = modelFile.exists() && modelFile.length() > 100_000_000

    suspend fun selectModel(variant: OfflineModelVariant) = withContext(Dispatchers.IO) {
        configManager.setOfflineModelVariant(variant.name)
        _state.value = checkInitialState()
    }

    // ── Multi-source download with fallback ───────────────────────────────

    suspend fun download(hfToken: String? = null) = withContext(Dispatchers.IO) {
        if (isReady()) {
            _state.value = ModelState.Ready
            return@withContext
        }

        modelDir.mkdirs()
        cancelled = false

        val variant = selectedVariant()
        val sources = buildSources(variant)
        val errors = mutableListOf<String>()

        for ((index, source) in sources.withIndex()) {
            if (cancelled) break

            Log.d(TAG, "Trying source ${index + 1}/${sources.size}: ${source.name}")
            _state.value = ModelState.Downloading(
                percent = 0,
                bytesDownloaded = if (tempFile.exists()) tempFile.length() else 0L,
                totalBytes = variant.sizeBytes,
                sourceName = "${variant.displayName} · ${source.name}",
            )

            try {
                downloadFromSource(source, hfToken)
                _state.value = ModelState.Ready
                Log.d(TAG, "${variant.displayName} download complete from ${source.name}")
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

        val variant = selectedVariant()
        val totalBytes = if (responseCode == 206) {
            val contentRange = connection.getHeaderField("Content-Range")
            contentRange?.substringAfter("/")?.toLongOrNull() ?: variant.sizeBytes
        } else {
            connection.contentLengthLong.takeIf { it > 0 } ?: variant.sizeBytes
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
