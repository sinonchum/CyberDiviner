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
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.LlmService
import com.cyberdiviner.data.remote.PromptManager
import com.cyberdiviner.engine.Persona
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
    val progressMessage: String = ""
)

// ── ViewModel ────────────────────────────────────────────────────────────

@HiltViewModel
class VisionViewModel @Inject constructor(
    application: Application,
    private val llmService: LlmService,
    private val promptManager: PromptManager,
    private val visionDao: VisionDao,
    private val divinationDao: DivinationDao,
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

    /**
     * Scan completion threshold — how many consecutive frames with a face
     * detected before we consider the scan "complete" and auto-capture.
     */
    private var faceDetectedFrameCount = 0
    private val requiredFramesForScan = 8

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
            val config = configManager.buildConfig(systemPrompt = systemPrompt)

            if (config == null) {
                val fallback = buildFallbackInterpretation(featuresJson, question)
                _uiState.value = _uiState.value.copy(
                    interpretation = fallback,
                    phase = VisionPhase.RESULT
                )
                return
            }

            val fullText = llmService.completeStream(config, messages) { chunk ->
                if (chunk.delta.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        streamText = _uiState.value.streamText + chunk.delta
                    )
                }
            }

            _uiState.value = _uiState.value.copy(
                interpretation = com.cyberdiviner.engine.Persona.stripActionDescriptions(fullText).ifBlank { buildFallbackInterpretation(featuresJson, question) },
                phase = VisionPhase.RESULT
            )
        } catch (e: Exception) {
            Log.e(TAG, "Interpretation failed", e)
            val fallback = buildFallbackInterpretation(featuresJson, question)
            _uiState.value = _uiState.value.copy(
                interpretation = fallback,
                phase = VisionPhase.RESULT
            )
        }
    }

    /**
     * Convert raw featuresJson into a human-readable description for the LLM prompt.
     */
    private fun buildFeaturesDescription(featuresJson: String): String {
        return try {
            val features = json.decodeFromString<FacialFeatures>(featuresJson)
            buildString {
                appendLine("面部整体：${features.faceOval.shape}脸型，对称性 ${String.format("%.0f", features.faceOval.symmetry * 100)}%")
                appendLine("额头：${features.forehead.shape}，高度中等")
                appendLine("眼睛：${features.eyes.eyeSize}，${features.eyes.eyeTilt}眼型，间距比 ${String.format("%.2f", features.eyes.eyeSpacing)}")
                appendLine("鼻子：${features.nose.shape}，${features.nose.bridgeDescription}鼻梁")
                appendLine("嘴巴：${features.mouth.shape}，${if (features.mouth.cornerUpturn > 0) "嘴角上扬" else "嘴角平直"}")
                appendLine("眉毛：${features.eyebrows.arch}型，间距 ${String.format("%.2f", features.eyebrows.spacing)}")
                appendLine("下巴：${features.chin.shape}，突出度 ${String.format("%.2f", features.chin.prominence)}")
                appendLine("耳朵：${features.ears.shape}")
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

        return buildString {
            appendLine("👁️ 面相分析")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("【脸型】${features.faceOval.shape}")
            appendLine("对称性：${String.format("%.0f", features.faceOval.symmetry * 100)}%")
            appendLine()
            appendLine("【额头】${features.forehead.shape}额")
            appendLine("【眼睛】${features.eyes.eyeSize}，${features.eyes.eyeTilt}")
            appendLine("【鼻子】${features.nose.shape}，鼻梁${features.nose.bridgeDescription}")
            appendLine("【嘴巴】${features.mouth.shape}")
            appendLine("【下巴】${features.chin.shape}")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            if (question.isNotBlank()) {
                appendLine("💡 你的问题：$question")
                appendLine()
            }
            appendLine("⚡ 信号提示：面相已扫描，但赛博先知暂时离线。")
            appendLine("请在设置中配置 API 密钥以获取完整的解读。")
        }
    }
}
