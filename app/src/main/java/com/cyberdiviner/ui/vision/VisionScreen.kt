package com.cyberdiviner.ui.vision

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.graphics.PointF
import com.cyberdiviner.ui.shared.CyberButton
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.cyberdiviner.ui.theme.*
import kotlinx.coroutines.delay

// ─── Fake facial landmark points (normalised 0..1) — visual decoration only ───
private val sampleFaceLandmarks = listOf(
    // Oval face outline
    PointF(0.50f, 0.18f), PointF(0.38f, 0.22f), PointF(0.30f, 0.30f),
    PointF(0.28f, 0.40f), PointF(0.30f, 0.50f), PointF(0.35f, 0.58f),
    PointF(0.45f, 0.64f), PointF(0.50f, 0.66f), PointF(0.55f, 0.64f),
    PointF(0.65f, 0.58f), PointF(0.70f, 0.50f), PointF(0.72f, 0.40f),
    PointF(0.70f, 0.30f), PointF(0.62f, 0.22f),
    // Left eye
    PointF(0.38f, 0.36f), PointF(0.41f, 0.34f), PointF(0.44f, 0.36f),
    PointF(0.42f, 0.38f), PointF(0.39f, 0.38f),
    // Right eye
    PointF(0.56f, 0.36f), PointF(0.59f, 0.34f), PointF(0.62f, 0.36f),
    PointF(0.61f, 0.38f), PointF(0.58f, 0.38f),
    // Nose
    PointF(0.50f, 0.40f), PointF(0.50f, 0.46f), PointF(0.47f, 0.50f),
    PointF(0.53f, 0.50f),
    // Mouth
    PointF(0.43f, 0.56f), PointF(0.47f, 0.55f), PointF(0.50f, 0.54f),
    PointF(0.53f, 0.55f), PointF(0.57f, 0.56f),
    PointF(0.44f, 0.58f), PointF(0.47f, 0.59f), PointF(0.50f, 0.60f),
    PointF(0.53f, 0.59f), PointF(0.56f, 0.58f),
    // Eyebrows
    PointF(0.36f, 0.30f), PointF(0.40f, 0.28f), PointF(0.44f, 0.30f),
    PointF(0.56f, 0.30f), PointF(0.60f, 0.28f), PointF(0.64f, 0.30f),
)

/**
 * Full-screen vision / face-scanning experience.
 *
 * Wires [VisionViewModel] for real CameraX + MediaPipe FaceLandmarker
 * integration. When the camera is not yet initialised the screen falls
 * back to the original simulated landmark animation so the UI is always
 * functional.
 *
 * Flow:
 * 1. Screen loads → idle state with [CyberButton] "START SCAN"
 * 2. Button press → starts camera via ViewModel + simulated fallback
 * 3. ViewModel auto-detects face → SCANNING → DETECTED → ANALYZING
 * 4. Result card shows real [FacialFeatures] and LLM interpretation
 */
@Composable
fun VisionScreen(
    navController: NavController,
    viewModel: VisionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // ── Scan trigger state ──
    var scanStarted by remember { mutableStateOf(false) }
    var cameraFailed by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            scanStarted = true
        }
    }

    // Whether the ViewModel camera pipeline is active — show preview
    // as soon as scan starts so the user sees the camera feed immediately.
    val cameraActive = scanStarted && !cameraFailed

    // ── CameraX PreviewView (created once, reused) ──
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // ── Initialise MediaPipe FaceLandmarker on screen entry ──
    LaunchedEffect(Unit) {
        viewModel.initializeFaceLandmarker()
    }

    // ── Start camera when scan is triggered ──
    LaunchedEffect(scanStarted) {
        if (scanStarted && !cameraFailed) {
            viewModel.startCamera(lifecycleOwner, previewView)
        }
    }

    // ── Auto-trigger LLM analysis when face is captured ──
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == VisionPhase.DETECTED) {
            viewModel.analyzeFaceReading()
        }
    }

    // ── Detect camera failure → fall back to simulation ──
    LaunchedEffect(uiState.phase, uiState.errorMessage) {
        if (scanStarted && uiState.phase == VisionPhase.ERROR) {
            cameraFailed = true
        }
    }

    // ── Simulated fallback state ──
    var simProgress by remember { mutableFloatStateOf(0f) }
    var simPhaseLabel by remember { mutableStateOf("初始化传感器阵列") }
    var simLandmarks by remember { mutableStateOf<List<PointF>>(emptyList()) }
    var simStatusLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var simComplete by remember { mutableStateOf(false) }

    // Simulated scan progression (runs when camera is NOT active)
    LaunchedEffect(scanStarted, cameraFailed) {
        if (!scanStarted || cameraActive) return@LaunchedEffect

        // Phase 0 – init
        simPhaseLabel = "初始化传感器阵列"
        delay(800)
        simPhaseLabel = "面部识别中"
        delay(600)

        // Phase 1 – face detection: reveal landmarks gradually
        val landmarksPerStep = 3
        for (i in sampleFaceLandmarks.indices step landmarksPerStep) {
            simLandmarks = sampleFaceLandmarks.take(i + landmarksPerStep)
            simProgress = (i.toFloat() / sampleFaceLandmarks.size).coerceIn(0f, 0.7f)
            delay(120)
        }

        // Phase 2 – mapping
        simPhaseLabel = "绘制面相拓扑"
        simStatusLines = listOf(
            "天庭  ████████░░ 78%",
            "眼睛  ██████████ 100%",
            "鼻子  ███████░░░ 72%",
            "嘴巴  █████████░ 95%",
        )
        for (p in 70..90) {
            simProgress = p / 100f
            delay(80)
        }

        // Phase 3 – analysis
        simPhaseLabel = "分析气场能量"
        simStatusLines = listOf(
            "五行平衡: 木=3 火=5 土=2 金=4 水=6",
            "气场频率: 432 Hz",
            "面相评级: S+",
        )
        for (p in 90..100) {
            simProgress = p / 100f
            delay(100)
        }

        // Phase 4 – complete
        simPhaseLabel = "面相分析完成"
        simProgress = 1f
        simComplete = true
    }

    // ── Derived display values ──
    val displayProgress: Float
    val displayPhaseLabel: String
    val displayLandmarks: List<PointF>
    val displayStatusLines: List<String>
    val isScanning: Boolean
    val showResult: Boolean

    if (cameraActive) {
        // Real camera pipeline
        displayProgress = uiState.scanProgress
        displayPhaseLabel = when (uiState.phase) {
            VisionPhase.IDLE -> "初始化传感器阵列"
            VisionPhase.SCANNING -> "面部识别中"
            VisionPhase.DETECTED -> "面部已捕捉"
            VisionPhase.CAPTURING -> "采集面部数据"
            VisionPhase.ANALYZING -> "分析气场能量"
            VisionPhase.RESULT -> "面相分析完成"
            VisionPhase.ERROR -> "系统错误"
        }
        // Use simulated landmarks for visual overlay (MediaPipe 478-point
        // data is processed internally by the ViewModel into FacialFeatures)
        displayLandmarks = if (uiState.faceDetected) sampleFaceLandmarks else emptyList()
        displayStatusLines = when {
            uiState.faceDetected -> listOf(
                "FACE  ██████████ DETECTED",
                "CONF  ████████░░ ${String.format("%.0f", uiState.scanProgress * 100)}%"
            )
            uiState.progressMessage.isNotBlank() -> listOf(uiState.progressMessage)
            else -> emptyList()
        }
        isScanning = uiState.phase == VisionPhase.SCANNING
                || uiState.phase == VisionPhase.CAPTURING
        showResult = uiState.phase == VisionPhase.RESULT
    } else {
        // Simulated fallback
        displayProgress = simProgress
        displayPhaseLabel = simPhaseLabel
        displayLandmarks = simLandmarks
        displayStatusLines = simStatusLines
        isScanning = scanStarted && !simComplete
        showResult = scanStarted && simComplete
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // ── Camera feed background ──
        if (cameraActive) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberDark)
            )
        }

        // ── AR Overlay ──
        AROverlay(
            scanProgress = displayProgress,
            isScanning = isScanning,
            detectedPoints = displayLandmarks,
        )

        // ── Scan ring in center ──
        ScanAnimation(
            progress = displayProgress,
            phase = displayPhaseLabel,
            statusLines = displayStatusLines,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .align(Alignment.Center)
        )

        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (cameraActive) viewModel.resetScan()
                navController.popBackStack()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AccentVision
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "VISION // FACE SCAN",
                color = AccentVision,
                fontSize = 14.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.weight(1f))
            // Status indicator
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            showResult -> GrayCaption
                            cameraActive && uiState.faceDetected -> GrayCaption
                            isScanning -> AccentVision
                            else -> TextMuted
                        }
                    )
            )
        }

        // ── Error overlay (camera / MediaPipe failure) ──
        if (cameraActive && uiState.phase == VisionPhase.ERROR) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberSurface.copy(alpha = 0.95f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "系统错误",
                    color = GrayCaption,
                    fontSize = 16.sp,
                    fontFamily = HuiwenFontFamily
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.errorMessage ?: "Unknown error",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = WenKaiFontFamily
                )
                Spacer(Modifier.height(16.dp))
                CyberButton(
                    text = "[ RETRY ]",
                    onClick = {
                        viewModel.dismissError()
                        cameraFailed = false
                        scanStarted = false
                    }
                )
            }
        }

        // ── Result card ──
        AnimatedVisibility(
            visible = showResult,
            enter = fadeIn(tween(600)),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberSurface.copy(alpha = 0.92f))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "面相分析完成",
                    color = GrayCaption,
                    fontSize = 18.sp,
                    fontFamily = HuiwenFontFamily
                )
                Spacer(Modifier.height(12.dp))

                // ── 四字批命 fortune ──
                if (uiState.fourCharFortune.isNotBlank()) {
                    Text(
                        text = uiState.fourCharFortune,
                        color = GrayTitle,
                        fontSize = 32.sp,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 8.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.fourCharMeaning,
                        color = GrayBody,
                        fontSize = 14.sp,
                        fontFamily = WenKaiFontFamily,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    // Thin divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(1.dp)
                            .background(GrayBorder)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── Interpretation text ──
                if (cameraActive && uiState.interpretation.isNotBlank()) {
                    // Real LLM interpretation from VisionViewModel
                    Text(
                        uiState.interpretation,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Left,
                        fontFamily = WenKaiFontFamily
                    )
                } else if (cameraActive && uiState.streamText.isNotBlank()) {
                    // Streaming LLM text (in case interpretation isn't final yet)
                    Text(
                        uiState.streamText,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Left,
                        fontFamily = WenKaiFontFamily
                    )
                } else {
                    // Simulated fallback interpretation
                    Text(
                        "此面相气场充沛，五行水旺而木辅，\n"
                                + "主智慧深远，贵人运旺。\n"
                                + "近期宜静心修炼，把握机遇。",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = WenKaiFontFamily
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Feature badges ──
                if (cameraActive && uiState.detectedFeatures != FacialFeatures()) {
                    // Real extracted features from MediaPipe
                    val features = uiState.detectedFeatures
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBadge("FACE", features.faceOval.shape.uppercase(), AccentVision)
                        StatBadge("EYES", features.eyes.eyeSize.uppercase(), GrayCaption)
                        StatBadge("NOSE", features.nose.shape.uppercase(), AccentVision)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBadge("MOUTH", features.mouth.shape.uppercase(), GrayCaption)
                        StatBadge("CHIN", features.chin.shape.uppercase(), AccentVision)
                        StatBadge(
                            "SYMMETRY",
                            "${String.format("%.0f", features.faceOval.symmetry * 100)}%",
                            GrayCaption
                        )
                    }
                } else {
                    // Simulated fallback badges
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBadge("RATING", "S+", AccentVision)
                        StatBadge("FIELD", "432Hz", GrayCaption)
                        StatBadge("ELEMENT", "WATER", AccentVision)
                    }
                }

                Spacer(Modifier.height(16.dp))
                CyberButton(
                    text = "[ RETURN ]",
                    onClick = {
                        if (cameraActive) viewModel.resetScan()
                        navController.popBackStack()
                    }
                )
            }
        }

        // ── START SCAN button (idle state only) ──
        if (!scanStarted && !showResult && uiState.phase != VisionPhase.ERROR) {
            CyberButton(
                text = "[ START SCAN ]",
                onClick = {
                    if (hasCameraPermission) {
                        scanStarted = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.Center)
            )
        }

    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 16.sp, fontFamily = MonoFontFamily)
        Text(label, color = TextSecondary, fontSize = 10.sp, fontFamily = MonoFontFamily)
    }
}


