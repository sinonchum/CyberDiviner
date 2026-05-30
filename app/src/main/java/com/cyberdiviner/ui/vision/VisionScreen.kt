package com.cyberdiviner.ui.vision

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner as ComposeLocalLifecycleOwner
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
 * 1. Screen loads → camera preview starts when permission is available
 * 2. User adjusts angle and taps START to lock the current face
 * 3. ViewModel captures face → DETECTED → ANALYZING
 * 4. Result card shows real [FacialFeatures] and LLM interpretation
 */
@Composable
fun VisionScreen(
    navController: NavController,
    viewModel: VisionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = ComposeLocalLifecycleOwner.current
    val context = LocalContext.current
    android.util.Log.d("VisionScreen", "VisionScreen composable entered")

    // ── Scan trigger state ──
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var scanStarted by remember { mutableStateOf(hasCameraPermission) }
    var cameraFailed by remember { mutableStateOf(false) }

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
        try {
            android.util.Log.d("VisionScreen", "Initializing FaceLandmarker...")
            viewModel.initializeFaceLandmarker()
            android.util.Log.d("VisionScreen", "FaceLandmarker initialized OK")
            if (!hasCameraPermission) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                scanStarted = true
            }
        } catch (e: Throwable) {
            android.util.Log.e("VisionScreen", "FaceLandmarker init failed", e)
        }
    }

    // ── Start camera when scan is triggered ──
    LaunchedEffect(scanStarted) {
        if (scanStarted && !cameraFailed) {
            try {
                viewModel.startCamera(lifecycleOwner, previewView)
            } catch (e: Exception) {
                cameraFailed = true
            }
        }
    }

    // ── Auto-trigger LLM analysis when face is captured ──
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == VisionPhase.DETECTED) {
            try {
                viewModel.analyzeFaceReading()
            } catch (e: Throwable) {
                android.util.Log.e("VisionScreen", "analyzeFaceReading failed", e)
            }
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
    var simPhaseLabel by remember { mutableStateOf("镜阵待启") }
    var simLandmarks by remember { mutableStateOf<List<PointF>>(emptyList()) }
    var simStatusLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var simComplete by remember { mutableStateOf(false) }

    LaunchedEffect(scanStarted) {
        if (!scanStarted) return@LaunchedEffect
        simPhaseLabel = "请将面容置于镜阵"
        simProgress = 0.15f
        simLandmarks = emptyList()
        simStatusLines = listOf("取景中", "调正面容后轻触 START")
        simComplete = false
    }

    // ── Derived display values ──
    val displayProgress: Float
    val displayPhaseLabel: String
    val displayLandmarks: List<PointF>
    val displayStatusLines: List<String>
    val isScanning: Boolean
    val showResult: Boolean

    // Camera is running but hasn't detected a face yet — show simulated animation
    val cameraWaitingForFace = cameraActive && !uiState.faceDetected && uiState.phase != VisionPhase.RESULT

    if (cameraActive && uiState.phase == VisionPhase.RESULT) {
        // Only use real pipeline for the result state
        displayProgress = 1f
        displayPhaseLabel = "面相分析完成"
        displayLandmarks = sampleFaceLandmarks
        displayStatusLines = emptyList()
        isScanning = false
        showResult = true
    } else if (cameraActive && uiState.faceDetected) {
        // Real camera pipeline — face detected, show real progress
        displayProgress = uiState.scanProgress
        displayPhaseLabel = when (uiState.phase) {
            VisionPhase.IDLE -> "请将面容置于镜阵"
            VisionPhase.SCANNING -> "镜中见相，可启签镜"
            VisionPhase.DETECTED -> "面相已入镜"
            VisionPhase.CAPTURING -> "正在取相"
            VisionPhase.ANALYZING -> "先知观相中"
            VisionPhase.RESULT -> "面相批命完成"
            VisionPhase.ERROR -> "镜阵受阻"
        }
        displayLandmarks = sampleFaceLandmarks
        displayStatusLines = when (uiState.phase) {
            VisionPhase.ANALYZING -> listOf(
                "面相已入镜",
                uiState.progressMessage.ifBlank { "先知正在推演..." },
                if (uiState.streamText.isNotEmpty()) uiState.streamText.takeLast(40) else "命盘流转中"
            )
            else -> listOf(
                "镜阵  ██████████ 已见相",
                "相合  ████████░░ ${String.format("%.0f", uiState.scanProgress * 100)}%"
            )
        }
        isScanning = uiState.phase == VisionPhase.SCANNING || uiState.phase == VisionPhase.CAPTURING || uiState.phase == VisionPhase.ANALYZING
        showResult = false
    } else if (scanStarted) {
        // Camera opened but no face yet, OR camera failed — show simulated animation
        displayProgress = simProgress
        displayPhaseLabel = simPhaseLabel
        displayLandmarks = simLandmarks
        displayStatusLines = simStatusLines
        isScanning = scanStarted && !simComplete
        showResult = scanStarted && simComplete
    } else {
        // Not started
        displayProgress = 0f
        displayPhaseLabel = "镜阵待启"
        displayLandmarks = emptyList()
        displayStatusLines = emptyList()
        isScanning = false
        showResult = false
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
                    .background(CyberBlack)
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
            Text(
                text = "< 返回",
                color = GrayCaption,
                fontSize = 13.sp,
                fontFamily = HuiwenFontFamily,
                modifier = Modifier.clickable {
                    if (cameraActive) viewModel.resetScan()
                    navController.popBackStack()
                }
            )
            Spacer(Modifier.width(12.dp))
            // Mode toggle: 基础 / 高级
            Text(
                text = if (uiState.llmEnabled) "✦ 先知" else "◉ 签镜",
                color = if (uiState.llmEnabled) AccentRed else GrayCaption,
                fontSize = 12.sp,
                fontFamily = MonoFontFamily,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = uiState.phase != VisionPhase.ANALYZING) {
                        viewModel.setVisionLlmEnabled(!uiState.llmEnabled)
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "VISION // FACE SCAN",
                color = AccentRed,
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
                            isScanning -> AccentRed
                            else -> GrayMuted
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
                    .background(GraySurface.copy(alpha = 0.95f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "镜阵受阻",
                    color = GrayCaption,
                    fontSize = 16.sp,
                    fontFamily = HuiwenFontFamily
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.errorMessage ?: "Unknown error",
                    color = CyberWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = WenKaiFontFamily
                )
                Spacer(Modifier.height(16.dp))
                CyberButton(
                    text = "[ 再启镜阵 ]",
                    onClick = {
                        viewModel.dismissError()
                        cameraFailed = false
                        scanStarted = false
                    }
                )
            }
        }

        // ── Long-running analysis overlay ──
        AnimatedVisibility(
            visible = uiState.phase == VisionPhase.ANALYZING,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GraySurface.copy(alpha = 0.94f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "先知正在观相",
                    color = GrayTitle,
                    fontSize = 18.sp,
                    fontFamily = HuiwenFontFamily
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = AccentRed,
                    trackColor = GrayBorder
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    uiState.progressMessage.ifBlank { "命理流转中，请稍候片刻。" },
                    color = GrayBody,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = WenKaiFontFamily
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "请勿退出此页，批命成文后将自行显现。",
                    color = GrayCaption,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = WenKaiFontFamily
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
                    .background(GraySurface.copy(alpha = 0.92f))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "面相批命完成",
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
                if (uiState.interpretation.isNotBlank()) {
                    Text(
                        uiState.interpretation,
                        color = CyberWhite,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Left,
                        fontFamily = WenKaiFontFamily
                    )
                } else if (uiState.streamText.isNotBlank()) {
                    Text(
                        uiState.streamText,
                        color = CyberWhite,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Left,
                        fontFamily = WenKaiFontFamily
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Feature badges ──
                val features = uiState.detectedFeatures
                if (features != FacialFeatures()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBadge("FACE", features.faceOval.shape.uppercase(), AccentRed)
                        StatBadge("EYES", features.eyes.eyeSize.uppercase(), GrayCaption)
                        StatBadge("NOSE", features.nose.shape.uppercase(), AccentRed)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBadge("MOUTH", features.mouth.shape.uppercase(), GrayCaption)
                        StatBadge("CHIN", features.chin.shape.uppercase(), AccentRed)
                        StatBadge(
                            "SYMMETRY",
                            "${String.format("%.0f", features.faceOval.symmetry * 100)}%",
                            GrayCaption
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                CyberButton(
                    text = "[ 归档离镜 ]",
                    onClick = {
                        if (cameraActive) viewModel.resetScan()
                        navController.popBackStack()
                    }
                )
            }
        }

        // ── START button — lock current face after preview is ready ──
        if (scanStarted && !showResult && uiState.phase != VisionPhase.ERROR && uiState.phase != VisionPhase.ANALYZING) {
            CyberButton(
                text = if (uiState.faceDetected) "[ START ]" else "[ 请入镜 ]",
                onClick = {
                    viewModel.captureFace()
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp)
            )
        } else if (!scanStarted && !showResult && uiState.phase != VisionPhase.ERROR) {
            CyberButton(
                text = "[ 开启镜阵 ]",
                onClick = {
                    if (hasCameraPermission) scanStarted = true
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp)
            )
        }

    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 16.sp, fontFamily = MonoFontFamily)
        Text(label, color = GrayBody, fontSize = 10.sp, fontFamily = MonoFontFamily)
    }
}
