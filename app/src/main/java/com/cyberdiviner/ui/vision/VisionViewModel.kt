package com.cyberdiviner.ui.vision

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.VisionDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import com.cyberdiviner.data.model.InferenceMode
import com.cyberdiviner.data.model.VisionReading
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.PromptManager
import com.cyberdiviner.engine.offline.InferenceRouter
import com.cyberdiviner.engine.offline.OfflinePromptBuilder
import com.cyberdiviner.engine.Persona
import com.cyberdiviner.engine.FortuneEngine
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// ── UI State ─────────────────────────────────────────────────────────────

enum class VisionPhase {
    IDLE,
    SCANNING,
    DETECTED,
    CAPTURING,
    ANALYZING,
    RESULT,
    ERROR
}

/**
 * Extracted facial features from MediaPipe Face Landmarker, mapped to
 * Chinese physiognomy (面相) concepts.
 */
@Serializable
data class FacialFeatures(
    val faceOval: FaceOval = FaceOval(),
    val forehead: ForeheadFeatures = ForeheadFeatures(),
    val eyes: EyeFeatures = EyeFeatures(),
    val nose: NoseFeatures = NoseFeatures(),
    val mouth: MouthFeatures = MouthFeatures(),
    val ears: EarFeatures = EarFeatures(),
    val chin: ChinFeatures = ChinFeatures(),
    val eyebrows: EyebrowFeatures = EyebrowFeatures()
)

@Serializable
data class FaceOval(
    val shape: String = "oval",       // oval / round / long / square / heart
    val width: Float = 0f,
    val height: Float = 0f,
    val symmetry: Float = 1.0f       // 0-1, 1 = perfectly symmetric
)

@Serializable
data class ForeheadFeatures(
    val height: Float = 0f,
    val width: Float = 0f,
    val shape: String = "broad",      // broad / narrow / rounded / flat
    val lineCount: Int = 0,
    val fullness: String = "normal"   // full / flat / sunken
)

@Serializable
data class EyeFeatures(
    val leftEyeOpenness: Float = 0f,
    val rightEyeOpenness: Float = 0f,
    val eyeSpacing: Float = 0f,       // inter-pupillary relative distance
    val eyeSize: String = "medium",   // small / medium / large
    val eyeTilt: String = "level",    // level / upturned / downturned
    val gazeDirection: String = "center"
)

@Serializable
data class NoseFeatures(
    val bridgeHeight: Float = 0f,
    val tipWidth: Float = 0f,
    val noseLength: Float = 0f,
    val shape: String = "straight",   // straight / pointed / round / broad
    val bridgeDescription: String = "normal" // high / low / normal
)

@Serializable
data class MouthFeatures(
    val width: Float = 0f,
    val lipThickness: Float = 0f,
    val shape: String = "average",    // thin / average / full / cherry
    val smileDepth: Float = 0f,
    val cornerUpturn: Float = 0f      // negative = downturned
)

@Serializable
data class EarFeatures(
    val leftEarSize: Float = 0f,
    val rightEarSize: Float = 0f,
    val shape: String = "medium",     // small / medium / large
    val attachment: String = "detached" // attached / detached
)

@Serializable
data class ChinFeatures(
    val shape: String = "rounded",    // pointed / rounded / square / double
    val prominence: Float = 0f,
    val width: Float = 0f
)

@Serializable
data class EyebrowFeatures(
    val leftThickness: Float = 0f,
    val rightThickness: Float = 0f,
    val arch: String = "natural",     // straight / arched / curved
    val spacing: Float = 0f,
    val shape: String = "standard"
)

data class VisionUiState(
    val phase: VisionPhase = VisionPhase.IDLE,
    val question: String = "",
    val detectedFeatures: FacialFeatures = FacialFeatures(),
    val featuresJson: String = "{}",
    val faceDetected: Boolean = false,
    val scanProgress: Float = 0f,       // 0..1 scan animation progress
    val capturedImageUri: String? = null,
    val interpretation: String = "",
    val streamText: String = "",
    val readingId: Long? = null,
    val errorMessage: String? = null,
    val progressMessage: String = "",
    val fourCharFortune: String = "",
    val fourCharMeaning: String = "",
    val llmEnabled: Boolean = false      // false=基础版, true=高级版(LLM)
)

// ── ViewModel ────────────────────────────────────────────────────────────

@HiltViewModel
class VisionViewModel @Inject constructor(
    application: Application,
    private val inferenceRouter: InferenceRouter,
    private val offlinePromptBuilder: OfflinePromptBuilder,
    private val divinationDao: DivinationDao,
    private val visionDao: VisionDao,
    private val promptManager: PromptManager,
    private val configManager: LlmConfigManager
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "VisionViewModel"
    }

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var faceLandmarker: FaceLandmarker? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val analysisExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

    /**
     * Lock protecting faceLandmarker access to prevent use-after-free.
     * analyzeFrame() holds this during detect(); releaseFaceLandmarker() acquires
     * it before closing the native resource.
     */
    private val landmarkerLock = java.util.concurrent.locks.ReentrantLock()

    /** Guard flag — set true before releasing resources to stop frame processing */
    @Volatile
    private var isReleasing = false

    /**
     * Scan completion threshold — how many consecutive frames with a face
     * detected before we consider the scan "complete" and auto-capture.
     */
    private var faceDetectedFrameCount = 0
    private val requiredFramesForScan = 5
    @Volatile
    private var captureRequested = false

    // ── Init ─────────────────────────────────────────────────────────────

    init {
        // Load persisted vision LLM preference
        viewModelScope.launch {
            configManager.visionLlmEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(llmEnabled = enabled)
            }
        }
    }

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Initialize MediaPipe Face Landmarker. Call once on screen entry.
     */
    fun initializeFaceLandmarker() {
        if (faceLandmarker != null) return
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setOutputFaceBlendshapes(true)
                .setOutputFacialTransformationMatrixes(true)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.5f)
                .setMinFacePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(
                getApplication(), options
            )
            Log.d(TAG, "FaceLandmarker initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FaceLandmarker", e)
            _uiState.value = _uiState.value.copy(
                phase = VisionPhase.ERROR,
                errorMessage = "无法初始化面相识别引擎: ${e.message}"
            )
        }
    }

    /**
     * Set up CameraX preview and bind image analysis.
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val context = getApplication<Application>()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            // Preview use case
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            // Image analysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(analysisExecutor) {
                analyzeFrame(it)
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
                Log.d(TAG, "CameraX bound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "CameraX bind failed", e)
                _uiState.value = _uiState.value.copy(
                    phase = VisionPhase.ERROR,
                    errorMessage = "摄像头启动失败: ${e.message}"
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * User manually triggers a scan (captures current face features).
     */
    fun captureFace() {
        val state = _uiState.value
        if (state.featuresJson == "{}") {
            _uiState.value = state.copy(
                phase = VisionPhase.SCANNING,
                progressMessage = "镜阵取相中，请正对镜头稍候。",
                errorMessage = "请先将面容置于镜阵之中"
            )
            return
        }
        captureRequested = false
        _uiState.value = state.copy(
            phase = VisionPhase.DETECTED,
            scanProgress = 1f,
            progressMessage = "面相已入镜"
        )
    }

    /**
     * Analyze captured features with the LLM for physiognomy interpretation.
     */
    fun analyzeFaceReading() {
        val state = _uiState.value
        if (state.phase != VisionPhase.DETECTED && state.featuresJson == "{}") {
            _uiState.value = state.copy(errorMessage = "请先扫描面部")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val readingId = saveReading(state.featuresJson, state.question)
                _uiState.value = _uiState.value.copy(
                    phase = VisionPhase.ANALYZING,
                    readingId = readingId,
                    streamText = "",
                    progressMessage = if (state.llmEnabled) {
                        "本地先知正在观骨听相，约需片刻..."
                    } else {
                        "本地签镜正在排布面相..."
                    }
                )
                streamInterpretation(state.featuresJson, state.question)
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "OOM during face analysis", e)
                _uiState.value = _uiState.value.copy(
                    phase = VisionPhase.ERROR,
                    errorMessage = "内存不足，请关闭其他应用后重试"
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Face analysis failed", e)
                _uiState.value = _uiState.value.copy(
                    phase = VisionPhase.ERROR,
                    errorMessage = "分析失败: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = VisionUiState()
    }

    /** Toggle between basic (local) and advanced (LLM) vision mode */
    fun setVisionLlmEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(llmEnabled = enabled)
        viewModelScope.launch {
            configManager.setVisionLlmEnabled(enabled)
        }
    }

    /**
     * Trigger face analysis — saves reading, tries LLM, falls back to local engine.
     */
    fun triggerFallbackAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            val features = FacialFeatures()
            val featuresJson = json.encodeToString(features)
            // Save reading first (like analyzeFaceReading does)
            val readingId = saveReading(featuresJson, "")
            _uiState.value = _uiState.value.copy(readingId = readingId)
            // Then run interpretation
            streamInterpretation(featuresJson, "")
        }
    }

    fun resetScan() {
        isReleasing = false
        captureRequested = false
        _uiState.value = _uiState.value.copy(
            phase = VisionPhase.IDLE,
            faceDetected = false,
            scanProgress = 0f,
            capturedImageUri = null,
            detectedFeatures = FacialFeatures(),
            featuresJson = "{}"
        )
        faceDetectedFrameCount = 0
    }

    /** Release face landmarker to free memory before LLM inference */
    private suspend fun releaseFaceLandmarker() {
        isReleasing = true
        // Stop camera to prevent new frames
        releaseCamera()
        // Wait for analysis executor to drain all queued frames
        withContext(Dispatchers.IO) {
            val latch = java.util.concurrent.CountDownLatch(1)
            analysisExecutor.submit { latch.countDown() }
            latch.await(3, java.util.concurrent.TimeUnit.SECONDS)
        }
        // Acquire lock to ensure no in-flight frame is using the landmarker
        withContext(Dispatchers.IO) {
            landmarkerLock.lock()
            try {
                faceLandmarker?.close()
                faceLandmarker = null
                Log.d(TAG, "FaceLandmarker released to free memory")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to release FaceLandmarker", e)
                faceLandmarker = null
            } finally {
                landmarkerLock.unlock()
            }
        }
        // Final GC before LLM load
        System.gc()
        kotlinx.coroutines.delay(500)
        Log.d(TAG, "Memory freed, ready for LLM inference")
    }

    /** Unbind camera to free memory before LLM inference */
    private suspend fun releaseCamera() {
        try {
            withContext(Dispatchers.Main) {
                cameraProvider?.unbindAll()
                cameraProvider = null
            }
            Log.d(TAG, "Camera unbound to free memory for LLM inference")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unbind camera", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        isReleasing = true
        landmarkerLock.lock()
        try {
            faceLandmarker?.close()
            faceLandmarker = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing landmarker in onCleared", e)
        } finally {
            landmarkerLock.unlock()
        }
        cameraProvider?.unbindAll()
        analysisExecutor.shutdown()
    }

    // ── Frame Analysis ─────────────────────────────────────────────────

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        // Bail immediately if resources are being released
        if (isReleasing) {
            imageProxy.close()
            return
        }

        // Only process frames when scanning or capturing
        val currentState = _uiState.value.phase
        if (currentState != VisionPhase.IDLE && currentState != VisionPhase.SCANNING
            && currentState != VisionPhase.CAPTURING
        ) {
            imageProxy.close()
            return
        }

        try {
            // Create ARGB_8888 bitmap explicitly for MediaPipe compatibility
            // (imageProxy.toBitmap() may produce incompatible format with RGBA_8888)
            val width = imageProxy.width
            val height = imageProxy.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            try {
                val buffer = imageProxy.planes[0].buffer
                val stride = imageProxy.planes[0].rowStride
                val pixelStride = imageProxy.planes[0].pixelStride

                if (stride == width * pixelStride) {
                    // No padding — direct copy
                    buffer.rewind()
                    bitmap.copyPixelsFromBuffer(buffer)
                } else {
                    // Row padding — copy row by row
                    val rowBytes = ByteArray(width * pixelStride)
                    for (row in 0 until height) {
                        buffer.position(row * stride)
                        buffer.get(rowBytes, 0, rowBytes.size)
                        val pixels = IntArray(width)
                        for (col in 0 until width) {
                            val offset = col * pixelStride
                            val r = rowBytes[offset].toInt() and 0xFF
                            val g = rowBytes[offset + 1].toInt() and 0xFF
                            val b = rowBytes[offset + 2].toInt() and 0xFF
                            val a = rowBytes[offset + 3].toInt() and 0xFF
                            pixels[col] = (a shl 24) or (r shl 16) or (g shl 8) or b
                        }
                        bitmap.setPixels(pixels, 0, width, row, 0, width, 1)
                    }
                }

                // CRITICAL: Hold lock during detect() to prevent use-after-free.
                // releaseFaceLandmarker() must acquire this lock before closing.
                val result: FaceLandmarkerResult? = if (landmarkerLock.tryLock()) {
                    try {
                        if (isReleasing || faceLandmarker == null) {
                            null
                        } else {
                            val mpImage = BitmapImageBuilder(bitmap).build()
                            faceLandmarker!!.detect(mpImage)
                        }
                    } finally {
                        landmarkerLock.unlock()
                    }
                } else {
                    // Lock held by release — skip this frame
                    null
                }

                if (result == null) {
                    imageProxy.close()
                    return
                }

                if (result.faceLandmarks().isNotEmpty()) {
                    val landmarks = result.faceLandmarks()[0]
                    val features = extractFeatures(landmarks, result, imageProxy)

                    faceDetectedFrameCount++

                    if (currentState == VisionPhase.CAPTURING && captureRequested) {
                        // Extract image for storage
                        val imageUri = captureAndSaveImage(imageProxy)

                        _uiState.value = _uiState.value.copy(
                            phase = VisionPhase.DETECTED,
                            faceDetected = true,
                            scanProgress = 1f,
                            detectedFeatures = features,
                            featuresJson = json.encodeToString(features),
                            capturedImageUri = imageUri,
                            progressMessage = "面相已入镜。"
                        )
                        captureRequested = false
                        faceDetectedFrameCount = 0
                    } else {
                        // SCANNING or IDLE — update live preview
                        val progress = (faceDetectedFrameCount.toFloat() / requiredFramesForScan).coerceAtMost(1f)
                        _uiState.value = _uiState.value.copy(
                            phase = VisionPhase.SCANNING,
                            faceDetected = true,
                            scanProgress = progress,
                            detectedFeatures = features,
                            featuresJson = json.encodeToString(features)
                        )
                    }
                } else {
                    faceDetectedFrameCount = 0
                    _uiState.value = _uiState.value.copy(
                        faceDetected = false,
                        scanProgress = 0f
                    )
                }
            } finally {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Face analysis error", e)
        } finally {
            imageProxy.close()
        }
    }

    // ── Feature Extraction ──────────────────────────────────────────────

    /**
     * Map MediaPipe 478 face landmarks to Chinese physiognomy features.
     *
     * Key landmark indices:
     * - Face oval: 10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109
     * - Eyes: 33, 133, 159, 145 (left); 362, 263, 386, 374 (right)
     * - Nose bridge: 6, 197, 195, 5; tip: 1, 2, 98, 327
     * - Mouth: 61, 291, 0, 17, 18, 178
     * - Forehead: 10 (top), 151 (between brows), lateral expansion via face width
     * - Chin: 152, 377, 400
     * - Eyebrows: 66, 105, 63, 70, 300, 293, 334, 296
     */
    private fun extractFeatures(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        result: FaceLandmarkerResult,
        imageProxy: ImageProxy
    ): FacialFeatures {
        val imgW = imageProxy.width.toFloat()
        val imgH = imageProxy.height.toFloat()

        // Helper to convert normalized landmark to pixel coords
        fun px(idx: Int) = Pair(landmarks[idx].x() * imgW, landmarks[idx].y() * imgH)
        fun dist(a: Pair<Float, Float>, b: Pair<Float, Float>): Float {
            val dx = a.first - b.first
            val dy = a.second - b.second
            return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }

        // ── Face Oval ──
        val faceTop = px(10)
        val faceBottom = px(152)
        val faceLeft = px(234)
        val faceRight = px(454)
        val faceHeight = dist(faceTop, faceBottom)
        val faceWidth = dist(faceLeft, faceRight)
        val aspectRatio = faceWidth / faceHeight.coerceAtLeast(1f)

        val ovalShape = when {
            aspectRatio > 0.85f -> "round"
            aspectRatio < 0.65f -> "long"
            aspectRatio > 0.75f -> "square"
            else -> "oval"
        }

        // Symmetry: compare left-right midpoint distances
        val midLeft = dist(faceTop, faceLeft)
        val midRight = dist(faceTop, faceRight)
        val symmetry = 1f - Math.abs(midLeft - midRight) / maxOf(midLeft, midRight).coerceAtLeast(1f)

        val faceOval = FaceOval(
            shape = ovalShape,
            width = faceWidth,
            height = faceHeight,
            symmetry = symmetry.coerceIn(0f, 1f)
        )

        // ── Forehead ──
        val foreheadHeight = dist(faceTop, px(9))  // top of face to brow ridge (index ~9)
        val foreheadWidthRatio = faceWidth / imgW

        // Horizontal forehead wrinkles via blendshapes (if available)
        val foreheadLineCount = 0 // Simplified; advanced detection would use blendshapes

        val forehead = ForeheadFeatures(
            height = foreheadHeight,
            width = foreheadWidthRatio,
            shape = when {
                foreheadWidthRatio > 0.35f -> "broad"
                foreheadWidthRatio < 0.2f -> "narrow"
                else -> "medium"
            },
            lineCount = foreheadLineCount,
            fullness = "normal"
        )

        // ── Eyes ──
        val leftEyeTop = px(159)
        val leftEyeBottom = px(145)
        val leftEyeOpenness = dist(leftEyeTop, leftEyeBottom) / faceHeight

        val rightEyeTop = px(386)
        val rightEyeBottom = px(374)
        val rightEyeOpenness = dist(rightEyeTop, rightEyeBottom) / faceHeight

        val leftEyeCenter = px(33)
        val rightEyeCenter = px(362)
        val eyeSpacing = dist(leftEyeCenter, rightEyeCenter) / faceWidth

        val avgEyeOpenness = (leftEyeOpenness + rightEyeOpenness) / 2f

        // Eye tilt: compare outer vs inner corners
        val leftOuter = px(33)
        val leftInner = px(133)
        val rightInner = px(362)
        val rightOuter = px(263)
        val leftTilt = leftOuter.second - leftInner.second
        val rightTilt = rightOuter.second - rightInner.second
        val avgTilt = (leftTilt + rightTilt) / 2f

        val eyeTilt = when {
            avgTilt > 2f -> "upturned"
            avgTilt < -2f -> "downturned"
            else -> "level"
        }

        val eyeFeatures = EyeFeatures(
            leftEyeOpenness = leftEyeOpenness,
            rightEyeOpenness = rightEyeOpenness,
            eyeSpacing = eyeSpacing,
            eyeSize = when {
                avgEyeOpenness > 0.06f -> "large"
                avgEyeOpenness < 0.03f -> "small"
                else -> "medium"
            },
            eyeTilt = eyeTilt,
            gazeDirection = "center"
        )

        // ── Nose ──
        val noseBridgeTop = px(6)
        val noseTip = px(2)
        val noseLength = dist(noseBridgeTop, noseTip)
        val noseWidth = dist(px(31), px(291))

        val nose = NoseFeatures(
            bridgeHeight = dist(px(168), px(6)),  // bridge protrusion proxy
            tipWidth = noseWidth / faceWidth,
            noseLength = noseLength / faceHeight,
            shape = when {
                noseWidth / faceWidth > 0.25f -> "broad"
                noseLength / faceHeight > 0.35f -> "pointed"
                else -> "straight"
            },
            bridgeDescription = "normal"
        )

        // ── Mouth ──
        val mouthLeft = px(61)
        val mouthRight = px(291)
        val mouthWidth = dist(mouthLeft, mouthRight)
        val upperLipTop = px(13)
        val lowerLipBottom = px(14)
        val lipThickness = dist(upperLipTop, lowerLipBottom)

        val mouthCornerLeft = px(61)
        val mouthCornerRight = px(291)
        val mouthCenterTop = px(13)
        val upturn = ((mouthCornerLeft.second + mouthCornerRight.second) / 2f) - mouthCenterTop.second

        val mouth = MouthFeatures(
            width = mouthWidth / faceWidth,
            lipThickness = lipThickness / faceHeight,
            shape = when {
                lipThickness / faceHeight > 0.06f -> "full"
                lipThickness / faceHeight < 0.02f -> "thin"
                else -> "average"
            },
            smileDepth = 0f,
            cornerUpturn = upturn
        )

        // ── Ears (approximated from face side landmarks) ──
        val leftEar = dist(px(127), px(234))  // vertical span of left face region
        val rightEar = dist(px(356), px(454))

        val earFeatures = EarFeatures(
            leftEarSize = leftEar / faceHeight,
            rightEarSize = rightEar / faceHeight,
            shape = when {
                (leftEar + rightEar) / 2f / faceHeight > 0.5f -> "large"
                (leftEar + rightEar) / 2f / faceHeight < 0.3f -> "small"
                else -> "medium"
            },
            attachment = "detached"
        )

        // ── Chin ──
        val chinWidth = dist(px(148), px(176))
        val chinProminence = dist(faceBottom, px(18))

        val chin = ChinFeatures(
            shape = when {
                chinWidth / faceWidth < 0.3f -> "pointed"
                chinWidth / faceWidth > 0.6f -> "square"
                else -> "rounded"
            },
            prominence = chinProminence / faceHeight,
            width = chinWidth / faceWidth
        )

        // ── Eyebrows ──
        val leftBrowInner = px(70)
        val leftBrowOuter = px(66)
        val rightBrowInner = px(300)
        val rightBrowOuter = px(296)

        val leftBrowThickness = dist(px(105), leftBrowInner)
        val rightBrowThickness = dist(px(334), rightBrowInner)

        val browSpacing = dist(leftBrowInner, rightBrowInner) / faceWidth

        val browArch = dist(leftBrowInner, px(63)) / dist(leftBrowInner, leftBrowOuter).coerceAtLeast(1f)

        val eyebrows = EyebrowFeatures(
            leftThickness = leftBrowThickness / faceHeight,
            rightThickness = rightBrowThickness / faceHeight,
            arch = when {
                browArch > 0.6f -> "arched"
                browArch < 0.3f -> "straight"
                else -> "natural"
            },
            spacing = browSpacing,
            shape = "standard"
        )

        return FacialFeatures(
            faceOval = faceOval,
            forehead = forehead,
            eyes = eyeFeatures,
            nose = nose,
            mouth = mouth,
            ears = earFeatures,
            chin = chin,
            eyebrows = eyebrows
        )
    }

    // ── Image Capture ──────────────────────────────────────────────────

    @OptIn(ExperimentalGetImage::class)
    private fun captureAndSaveImage(imageProxy: ImageProxy): String? {
        return try {
            val mediaImage = imageProxy.image ?: return null
            val buffer = mediaImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

            // Mirror horizontally for front camera
            val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width.toFloat(), bitmap.height.toFloat()) }
            val mirrored = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Save to app internal storage
            val dir = File(getApplication<Application>().filesDir, "vision_captures")
            dir.mkdirs()
            val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                mirrored.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            mirrored.recycle()
            bitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Image capture failed", e)
            null
        }
    }

    // ── Database ───────────────────────────────────────────────────────

    private suspend fun saveReading(featuresJson: String, question: String): Long =
        withContext(Dispatchers.IO) {
            try {
                val reading = DivinationReading(
                    type = DivinationType.VISION,
                    question = question.ifBlank { "面相分析" },
                    resultJson = featuresJson
                )
                val readingId = divinationDao.insert(reading)

                val visionReading = VisionReading(
                    readingId = readingId,
                    imageUri = _uiState.value.capturedImageUri,
                    featuresJson = featuresJson
                )
                visionDao.insert(visionReading)

                readingId
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save reading", e)
                -1L
            }
        }

    // ── LLM Interpretation ─────────────────────────────────────────────

    private suspend fun streamInterpretation(
        featuresJson: String,
        question: String
    ) {
        try {
            val systemPrompt = promptManager.resolveSystem(
                feature = "vision",
                persona = Persona.DEFAULT
            )

            val featuresText = buildFeaturesDescription(featuresJson)

            val userPrompt = promptManager.resolveUser(
                feature = "vision",
                variables = mapOf(
                    "face_description" to featuresText,
                    "context" to question.ifBlank { "请分析此面相" }
                )
            )

            val messages = listOf(LlmMessage(role = "user", content = userPrompt))
            val offlinePrompt = offlinePromptBuilder.buildVisionPrompt(
                faceDescription = featuresText
            )

            val fullText = try {
                // Release camera + landmarker, wait for memory to settle
                _uiState.value = _uiState.value.copy(
                    progressMessage = "镜阵已收，先知将启..."
                )
                releaseFaceLandmarker()

                val mode = inferenceRouter.currentMode()
                if (!_uiState.value.llmEnabled && mode != InferenceMode.OFFLINE) {
                    // 基础版 — 直接用本地面相引擎，不加载 LLM
                    Log.d(TAG, "Basic mode: using local face reading engine")
                    throw IllegalStateException("Basic mode — LLM skipped")
                }

                // 高级版 — 尝试 LLM 推理
                // Check available memory before loading LLM model
                val runtime = Runtime.getRuntime()
                val usedMem = runtime.totalMemory() - runtime.freeMemory()
                val maxMem = runtime.maxMemory()
                val availMem = maxMem - usedMem
                val availMB = availMem / (1024 * 1024)
                Log.d(TAG, "Available heap before LLM: ${availMB}MB")

                if (availMB < 300 && mode != InferenceMode.OFFLINE) {
                    // Not enough memory for LLM — use fallback directly
                    Log.w(TAG, "Skipping LLM inference, low memory: ${availMB}MB")
                    throw IllegalStateException("Low memory: ${availMB}MB")
                }

                _uiState.value = _uiState.value.copy(
                    progressMessage = "先知正在观相成文，请稍候..."
                )
                inferenceRouter.completeStream(
                    feature = "vision",
                    messages = messages,
                    offlineUserPrompt = offlinePrompt
                ) { delta ->
                    _uiState.value = _uiState.value.copy(
                        streamText = _uiState.value.streamText + delta
                    )
                }.text
            } catch (e: Throwable) {
                Log.e(TAG, "Vision inference failed", e)
                buildFallbackInterpretation(featuresJson, question)
            }

            val candidateText = if (inferenceRouter.isOfflineAvailable() && !inferenceRouter.isOnlineAvailable()) {
                com.cyberdiviner.engine.Persona.cleanOfflineOutput(fullText)
            } else {
                com.cyberdiviner.engine.Persona.stripActionDescriptions(fullText)
            }
            val fallbackText = buildFallbackInterpretation(featuresJson, question)
            val finalText = if (candidateText.isBlank() || isLowQualityVisionOutput(candidateText)) {
                fallbackText
            } else {
                normalizeVisionInterpretation(candidateText, fallbackText)
            }
            val fortune = FortuneEngine.visionFortune(finalText)
            val meaning = FortuneEngine.visionMeaning(fortune)
            _uiState.value = _uiState.value.copy(
                interpretation = finalText,
                phase = VisionPhase.RESULT,
                fourCharFortune = fortune,
                fourCharMeaning = meaning
            )
            // Persist complete result to database
            persistResult(fortune, meaning, finalText, featuresJson)
        } catch (e: Exception) {
            Log.e(TAG, "Interpretation failed", e)
            val fallback = buildFallbackInterpretation(featuresJson, question)
            val fortune = FortuneEngine.visionFortune(fallback)
            val meaning = FortuneEngine.visionMeaning(fortune)
            _uiState.value = _uiState.value.copy(
                interpretation = fallback,
                phase = VisionPhase.RESULT,
                fourCharFortune = fortune,
                fourCharMeaning = meaning
            )
            persistResult(fortune, meaning, fallback, featuresJson)
        }
    }

    private fun normalizeVisionInterpretation(candidate: String, fallback: String): String {
        val cleaned = candidate
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()

        if (cleaned.isBlank() || isLowQualityVisionOutput(cleaned)) {
            return fallback
        }

        return cleaned
    }

    private fun isLowQualityVisionOutput(text: String): Boolean {
        val rawDataMarkers = listOf(
            "分析面形、额头", "每个部位1-2句话", "脸型：", "面部宽度", "宽高比",
            "左右对称性", "左眼开合度", "右眼开合度", "鼻梁高度", "鼻头宽度比",
            "眉间距比", "px", "broad额", "straight眉", "medium", "standard",
            "normal", "level"
        )
        val markerHits = rawDataMarkers.count { text.contains(it) }
        val numericDensity = Regex("""\d+(\.\d+)?""").findAll(text).count()
        val repeatedUnits = text
            .split(Regex("[。！？!?\\n]+"))
            .map { it.trim() }
            .filter { it.length >= 12 }
            .groupingBy { it }
            .eachCount()
            .count { it.value >= 2 }
        val hasRequiredReadingShape =
            text.contains("面形总论") ||
                text.contains("逐部位详析") ||
                text.contains("运势总判") ||
                text.contains("事业运")

        return markerHits >= 2 ||
            numericDensity >= 8 ||
            repeatedUnits >= 2 ||
            !hasRequiredReadingShape ||
            text.length < 160
    }

    /** Persist vision result (fortune + interpretation) to database */
    private suspend fun persistResult(
        fortune: String,
        meaning: String,
        interpretation: String,
        featuresJson: String
    ) {
        try {
            val rid = _uiState.value.readingId ?: return
            // Update DivinationReading with fortune as question + structured resultJson
            val existingReading = divinationDao.getById(rid)
            if (existingReading != null) {
                val resultData = """{"fortune":"$fortune","meaning":"$meaning","interpretation":${Json.encodeToString(interpretation.take(500))},"features":$featuresJson}"""
                divinationDao.update(existingReading.copy(
                    question = fortune,
                    resultJson = resultData
                ))
            }
            // Update VisionReading with interpretation
            val existing = visionDao.getByReadingId(rid)
            if (existing != null) {
                visionDao.update(existing.copy(interpretation = interpretation))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist vision result", e)
        }
    }

    /**
     * Convert raw featuresJson into a human-readable description for the LLM prompt.
     */
    private fun buildFeaturesDescription(featuresJson: String): String {
        return try {
            val features = json.decodeFromString<FacialFeatures>(featuresJson)
            val face = when (features.faceOval.shape) {
                "round" -> "水形圆面"
                "long" -> "木形长面"
                "square" -> "金形方面"
                "heart" -> "上宽下收之面"
                else -> "端正椭圆面"
            }
            val forehead = when (features.forehead.shape) {
                "broad" -> "天庭开阔"
                "narrow" -> "天庭略窄"
                "rounded" -> "额圆而润"
                else -> "额相平正"
            }
            val brow = when (features.eyebrows.arch) {
                "straight" -> "眉直"
                "arched" -> "眉弓"
                else -> "眉形自然"
            }
            val eyes = when (features.eyes.eyeSize) {
                "large" -> "眼大有神"
                "small" -> "眼小聚光"
                else -> "眼势平和"
            }
            val nose = when (features.nose.shape) {
                "broad" -> "鼻头丰"
                "pointed" -> "鼻梁锐"
                "round" -> "鼻形圆"
                else -> "鼻形直"
            }
            val mouth = when (features.mouth.shape) {
                "full" -> "唇厚"
                "thin" -> "唇薄"
                else -> "口形端"
            }
            val chin = when (features.chin.shape) {
                "square" -> "地阁方"
                "pointed" -> "地阁尖"
                else -> "地阁圆"
            }
            val symmetry = if (features.faceOval.symmetry > 0.9f) "左右均衡" else "左右略偏"
            "$face，$symmetry；$forehead；$brow，$eyes；$nose，$mouth；$chin。"
        } catch (e: Exception) {
            "面形端正，五官可辨，气色待察。"
        }
    }

    private fun buildFallbackInterpretation(
        featuresJson: String,
        question: String
    ): String {
        val features = try {
            json.decodeFromString<FacialFeatures>(featuresJson)
        } catch (_: Exception) {
            FacialFeatures()
        }

        val shapeFiveElement = when (features.faceOval.shape) {
            "round" -> "水形面"
            "long" -> "木形面"
            "square" -> "金形面"
            else -> "土形面"
        }
        val foreheadShape = when (features.forehead.shape) {
            "broad" -> "宽额"
            "narrow" -> "窄额"
            "rounded" -> "圆额"
            "flat" -> "平额"
            else -> "平满之额"
        }

        val noseWealth = when (features.nose.shape) {
            "broad" -> "鼻头丰隆，主财运亨通，中年后财源广进"
            "pointed" -> "鼻梁挺直，主事业心强，凭技艺生财"
            else -> "鼻形端正，主财运平稳，理财有道"
        }

        val eyeInsight = when (features.eyes.eyeSize) {
            "large" -> "眼大有神，主心胸开阔，洞察力强，感情丰富"
            "small" -> "眼小聚光，主心思缜密，观察入微，行事谨慎"
            else -> "眼中平正，主性情平和，处事稳重"
        }

        val browTemper = when (features.eyebrows.arch) {
            "arched" -> "眉形如弓，主性情温和，人缘佳，善交际"
            "straight" -> "眉直如剑，主性格刚直，行事果断，有领导力"
            else -> "眉形自然，主性情随和，进退有度"
        }

        val chinFortune = when (features.chin.shape) {
            "pointed" -> "下巴尖削，主晚年须早做打算，宜广结善缘"
            "square" -> "下巴方正，主晚年安稳，有田产之福"
            else -> "下巴圆润，主晚年安乐，子女有靠"
        }

        val mouthFortune = when (features.mouth.shape) {
            "full" -> "唇厚饱满，主食禄丰厚，口福不浅，言语有信"
            "thin" -> "唇薄紧抿，主言辞犀利，善于表达，宜从事口才相关之业"
            else -> "口形端正，主言而有信，食禄平顺"
        }

        val earWisdom = when (features.ears.shape) {
            "large" -> "耳大厚实，主肾气充足，先天禀赋佳，少年运好"
            "small" -> "耳小精致，主心思灵敏，悟性高，宜后天修养"
            else -> "耳形端正，主聪慧稳重，少年平顺"
        }

        val foreheadCareer = when (features.forehead.shape) {
            "broad" -> "天庭开阔饱满，主智慧过人，事业根基深厚，少年得志"
            "narrow" -> "天庭略窄，主早年辛苦，但中年后运势渐开"
            else -> "天庭平满，主少年运势平顺，学业有成"
        }

        val symmetryBonus = if (features.faceOval.symmetry > 0.9f) {
            "面部左右对称度极高，主心性端正，处事公允，贵人运旺"
        } else if (features.faceOval.symmetry > 0.8f) {
            "面部对称度良好，主性情稳定，运势平顺"
        } else {
            "面部略有不对称，主性格中有矛盾面，需注意内外平衡"
        }

        return buildString {
            appendLine("面相详析")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("一、面形总论")
            appendLine("此面属$shapeFiveElement，${when (features.faceOval.shape) {
                "round" -> "面圆肉丰，主性格圆融，善于变通，人缘极佳"
                "long" -> "面长有骨，主性格坚韧，有远见，适合长线发展"
                "square" -> "面方有棱，主性格刚正，做事有原则，事业心强"
                else -> "面形端正，主性格均衡，适应力强"
            }}。$symmetryBonus。")
            appendLine()
            appendLine("二、逐部位详析")
            appendLine()
            appendLine("天庭：$foreheadCareer")
            appendLine()
            appendLine("眉：$browTemper。眉间距${if (features.eyebrows.spacing > 0.3f) "较宽，主心胸豁达，不拘小节" else "适中，主思虑周全"}。")
            appendLine()
            appendLine("眼：$eyeInsight。眼尾${when (features.eyes.eyeTilt) {
                "upturned" -> "上扬，主桃花运旺，异性缘佳"
                "downturned" -> "下垂，主性情温柔，重感情"
                else -> "平正，主性情端正，理性与感性兼顾"
            }}。")
            appendLine()
            appendLine("鼻：$noseWealth。鼻梁${when (features.nose.bridgeDescription) {
                "high" -> "高挺"
                "low" -> "偏低"
                else -> "平顺"
            }}，山根${if (features.nose.bridgeHeight > 20) "高挺，主中年运势强劲" else "平顺，主中年稳步发展"}。")
            appendLine()
            appendLine("口：$mouthFortune。${if (features.mouth.cornerUpturn > 0) "嘴角自然上扬，天生笑相，主乐观积极，逢凶化吉" else "嘴角平直，主性格沉稳，不轻易表露情绪"}。")
            appendLine()
            appendLine("耳：$earWisdom")
            appendLine()
            appendLine("地阁：$chinFortune")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("三、运势总判")
            appendLine()
            appendLine("事业运：${if (features.forehead.shape == "broad" && features.nose.shape != "broad") "天庭开阔配挺鼻，主事业有成，适合管理或创业" else "面相主稳，事业宜循序渐进，厚积薄发"}。")
            appendLine("财运：$noseWealth。")
            appendLine("感情运：${if (features.eyes.eyeSize == "large") "眼大有神，感情丰富，桃花运旺，但需防多情" else "眼神内敛，感情专一，重质不重量"}。")
            appendLine("健康运：${if (features.ears.shape == "large") "耳大肾气足，先天体质好" else "体质中平，宜注意作息养生"}。")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            if (question.isNotBlank()) {
                appendLine("你的问题：$question")
                appendLine()
            }
            appendLine("请知会本地专属先知：此判为签镜初断，宜作趋吉避凶之参考，不作定命之论。")
        }
            .replace(features.forehead.shape + "额", foreheadShape)
            .replace("upturned", "上扬")
            .replace("downturned", "下垂")
            .replace("level", "平正")
            .replace("broad", "宽阔")
            .replace("straight", "直")
            .replace("arched", "弓")
            .replace("curved", "弯")
            .replace("natural", "自然")
            .replace("medium", "中等")
            .replace("standard", "端正")
            .replace("normal", "平顺")
    }
}
