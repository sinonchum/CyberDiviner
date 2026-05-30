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
import com.cyberdiviner.data.model.VisionReading
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
    val fourCharMeaning: String = ""
)

// ── ViewModel ────────────────────────────────────────────────────────────

@HiltViewModel
class VisionViewModel @Inject constructor(
    application: Application,
    private val inferenceRouter: InferenceRouter,
    private val offlinePromptBuilder: OfflinePromptBuilder,
    private val divinationDao: DivinationDao,
    private val visionDao: VisionDao,
    private val promptManager: PromptManager
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "VisionViewModel"
    }

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var faceLandmarker: FaceLandmarker? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * Scan completion threshold — how many consecutive frames with a face
     * detected before we consider the scan "complete" and auto-capture.
     */
    private var faceDetectedFrameCount = 0
    private val requiredFramesForScan = 5

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

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) {
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
        if (!_uiState.value.faceDetected) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请先将面部置于取景框中"
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            phase = VisionPhase.CAPTURING,
            progressMessage = "正在捕捉面部特征..."
        )
        // The capture happens via the latest analyzed frame; 
        // we transition to CAPTURING so the next analysis frame performs extraction.
    }

    /**
     * Update the user's question.
     */
    fun updateQuestion(question: String) {
        _uiState.value = _uiState.value.copy(question = question)
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

        viewModelScope.launch {
            // Save reading
            val readingId = saveReading(state.featuresJson, state.question)

            _uiState.value = _uiState.value.copy(
                phase = VisionPhase.ANALYZING,
                readingId = readingId,
                progressMessage = "赛博先知正在观面相..."
            )

            streamInterpretation(state.featuresJson, state.question)
        }
    }

    fun dismissError() {
        _uiState.value = VisionUiState()
    }

    /**
     * Trigger face analysis — saves reading, tries LLM, falls back to local engine.
     */
    fun triggerFallbackAnalysis() {
        viewModelScope.launch {
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
    private fun releaseFaceLandmarker() {
        try {
            faceLandmarker?.close()
            faceLandmarker = null
            Log.d(TAG, "FaceLandmarker released to free memory")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release FaceLandmarker", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        faceLandmarker?.close()
        faceLandmarker = null
        cameraProvider?.unbindAll()
    }

    // ── Frame Analysis ─────────────────────────────────────────────────

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val landmarker = faceLandmarker
        if (landmarker == null) {
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
            // Use Bitmap conversion for RGBA_8888 format compatibility with MediaPipe
            val bitmap = imageProxy.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = landmarker.detect(mpImage)

            if (result.faceLandmarks().isNotEmpty()) {
                val landmarks = result.faceLandmarks()[0]
                val features = extractFeatures(landmarks, result, imageProxy)

                faceDetectedFrameCount++

                if (currentState == VisionPhase.CAPTURING) {
                    // Extract image for storage
                    val imageUri = captureAndSaveImage(imageProxy)

                    _uiState.value = _uiState.value.copy(
                        phase = VisionPhase.DETECTED,
                        faceDetected = true,
                        scanProgress = 1f,
                        detectedFeatures = features,
                        featuresJson = json.encodeToString(features),
                        capturedImageUri = imageUri,
                        progressMessage = "面部特征已捕捉！"
                    )
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

                    if (faceDetectedFrameCount >= requiredFramesForScan) {
                        // Auto-complete scan
                        val imageUri = captureAndSaveImage(imageProxy)
                        _uiState.value = _uiState.value.copy(
                            phase = VisionPhase.DETECTED,
                            scanProgress = 1f,
                            capturedImageUri = imageUri,
                            progressMessage = "面部特征已捕捉！"
                        )
                        faceDetectedFrameCount = 0
                    }
                }
            } else {
                faceDetectedFrameCount = 0
                _uiState.value = _uiState.value.copy(
                    faceDetected = false,
                    scanProgress = 0f
                )
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
                // Release face landmarker before loading LLM to free memory
                releaseFaceLandmarker()
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

            val finalText = if (inferenceRouter.isOfflineAvailable() && !inferenceRouter.isOnlineAvailable()) {
                val cleaned = com.cyberdiviner.engine.Persona.cleanOfflineOutput(fullText)
                cleaned.ifBlank { buildFallbackInterpretation(featuresJson, question) }
            } else {
                com.cyberdiviner.engine.Persona.stripActionDescriptions(fullText).ifBlank { buildFallbackInterpretation(featuresJson, question) }
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
            buildString {
                // ── 面形总论 ──
                appendLine("【面形数据】")
                val shapeDesc = when (features.faceOval.shape) {
                    "round" -> "圆面（水形面）"
                    "long" -> "长面（木形面）"
                    "square" -> "方面（金形面）"
                    else -> "椭圆面（标准面形）"
                }
                appendLine("脸型：$shapeDesc")
                appendLine("面部宽度：${String.format("%.1f", features.faceOval.width)}px，高度：${String.format("%.1f", features.faceOval.height)}px")
                appendLine("宽高比：${String.format("%.2f", features.faceOval.width / features.faceOval.height.coerceAtLeast(1f))}")
                appendLine("左右对称性：${String.format("%.0f", features.faceOval.symmetry * 100)}%")
                appendLine()

                // ── 三停分析 ──
                appendLine("【三停比例】")
                appendLine("上停（发际至眉）：${features.forehead.shape}额，额头高度 ${String.format("%.1f", features.forehead.height)}px")
                appendLine("中停（眉至鼻尖）：鼻子长度占比 ${String.format("%.0f", features.nose.noseLength * 100)}%")
                appendLine("下停（鼻尖至下巴）：下巴突出度 ${String.format("%.2f", features.chin.prominence)}")
                appendLine()

                // ── 天庭 ──
                appendLine("【天庭（额头）】")
                appendLine("形态：${features.forehead.shape}额")
                appendLine("宽度比例：${String.format("%.2f", features.forehead.width)}")
                appendLine("饱满度：${features.forehead.fullness}")
                appendLine("纹路：${features.forehead.lineCount}条")
                appendLine()

                // ── 眉 ──
                appendLine("【眉（保寿官）】")
                appendLine("眉形：${features.eyebrows.arch}眉")
                appendLine("左眉厚度：${String.format("%.3f", features.eyebrows.leftThickness)}")
                appendLine("右眉厚度：${String.format("%.3f", features.eyebrows.rightThickness)}")
                appendLine("眉间距比：${String.format("%.2f", features.eyebrows.spacing)}")
                appendLine("整体形态：${features.eyebrows.shape}")
                appendLine()

                // ── 眼 ──
                appendLine("【眼（监察官）】")
                appendLine("眼型大小：${features.eyes.eyeSize}")
                appendLine("眼尾走向：${features.eyes.eyeTilt}")
                appendLine("左眼开合度：${String.format("%.3f", features.eyes.leftEyeOpenness)}")
                appendLine("右眼开合度：${String.format("%.3f", features.eyes.rightEyeOpenness)}")
                appendLine("两眼间距比：${String.format("%.2f", features.eyes.eyeSpacing)}")
                appendLine("目光方向：${features.eyes.gazeDirection}")
                appendLine()

                // ── 鼻 ──
                appendLine("【鼻（审辨官）】")
                appendLine("鼻形：${features.nose.shape}")
                appendLine("鼻梁描述：${features.nose.bridgeDescription}")
                appendLine("鼻梁高度：${String.format("%.1f", features.nose.bridgeHeight)}px")
                appendLine("鼻头宽度比：${String.format("%.2f", features.nose.tipWidth)}")
                appendLine("鼻子长度比：${String.format("%.2f", features.nose.noseLength)}")
                appendLine()

                // ── 口 ──
                appendLine("【口（出纳官）】")
                appendLine("唇形：${features.mouth.shape}")
                appendLine("嘴宽比：${String.format("%.2f", features.mouth.width)}")
                appendLine("唇厚比：${String.format("%.3f", features.mouth.lipThickness)}")
                val cornerDesc = if (features.mouth.cornerUpturn > 0) "嘴角上扬（笑相）" else "嘴角平直"
                appendLine("嘴角走势：$cornerDesc")
                appendLine()

                // ── 地阁 ──
                appendLine("【地阁（下巴）】")
                appendLine("下巴形态：${features.chin.shape}")
                appendLine("下巴宽度比：${String.format("%.2f", features.chin.width)}")
                appendLine("下巴突出度：${String.format("%.2f", features.chin.prominence)}")
                appendLine()

                // ── 耳 ──
                appendLine("【耳（采听官）】")
                appendLine("耳朵大小：${features.ears.shape}")
                appendLine("左耳比例：${String.format("%.2f", features.ears.leftEarSize)}")
                appendLine("右耳比例：${String.format("%.2f", features.ears.rightEarSize)}")
                appendLine("耳型：${features.ears.attachment}")
            }
        } catch (e: Exception) {
            featuresJson
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
            appendLine("鼻：$noseWealth。鼻梁${features.nose.bridgeDescription}，山根${if (features.nose.bridgeHeight > 20) "高挺，主中年运势强劲" else "平顺，主中年稳步发展"}。")
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
            appendLine("注：以上为本地面相引擎分析。配置API密钥后可获得更深入的十二宫位详析与个性化运势指引。")
        }
    }
}
