package com.cyberdiviner.ui.vision

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.graphics.PointF
import com.cyberdiviner.ui.theme.*
import kotlinx.coroutines.delay

// ─── Fake facial landmark points (normalised 0..1) ───
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

private enum class ScanPhase(val label: String) {
    INIT("INITIALIZING SENSOR ARRAY"),
    DETECT("FACE DETECTION IN PROGRESS"),
    MAPPING("MAPPING FACIAL TOPOLOGY"),
    ANALYZING("ANALYZING ENERGY SIGNATURES"),
    COMPLETE("DIVINATION COMPLETE"),
}

/**
 * Full-screen vision / face-scanning experience.
 * Combines [AROverlay] (HUD wireframe) with [ScanAnimation] (progress ring).
 */
@Composable
fun VisionScreen(navController: NavController) {
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var phase by remember { mutableStateOf(ScanPhase.INIT) }
    var visibleLandmarks by remember { mutableStateOf<List<PointF>>(emptyList()) }
    var statusLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var showResult by remember { mutableStateOf(false) }

    // Simulated scan progression
    LaunchedEffect(Unit) {
        // Phase 0 – init
        delay(800)
        phase = ScanPhase.DETECT
        delay(600)

        // Phase 1 – face detection: reveal landmarks gradually
        val landmarksPerStep = 3
        for (i in sampleFaceLandmarks.indices step landmarksPerStep) {
            visibleLandmarks = sampleFaceLandmarks.take(i + landmarksPerStep)
            scanProgress = (i.toFloat() / sampleFaceLandmarks.size).coerceIn(0f, 0.7f)
            phase = ScanPhase.DETECT
            delay(120)
        }

        // Phase 2 – mapping
        phase = ScanPhase.MAPPING
        statusLines = listOf(
            "FOREHEAD  ████████░░ 78%",
            "EYES      ██████████ 100%",
            "NOSE      ███████░░░ 72%",
            "MOUTH     █████████░ 95%",
        )
        for (p in 70..90) {
            scanProgress = p / 100f
            delay(80)
        }

        // Phase 3 – analysis
        phase = ScanPhase.ANALYZING
        statusLines = listOf(
            "五行平衡: 木=3 火=5 土=2 金=4 水=6",
            "气场频率: 432 Hz",
            "面相评级: S+",
        )
        for (p in 90..100) {
            scanProgress = p / 100f
            delay(100)
        }

        // Phase 4 – complete
        phase = ScanPhase.COMPLETE
        scanProgress = 1f
        delay(400)
        showResult = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // ── Camera feed background ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberDark)
        )

        // ── AR Overlay ──
        AROverlay(
            scanProgress = scanProgress,
            isScanning = !showResult,
            detectedPoints = visibleLandmarks,
        )

        // ── Scan ring in center ──
        ScanAnimation(
            progress = scanProgress,
            phase = phase.label,
            statusLines = statusLines,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .align(Alignment.Center)
        )

        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
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
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.weight(1f))
            // Status indicator
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (!showResult) AccentVision else FortuneGold)
            )
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface.copy(alpha = 0.92f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "✓ FACE ANALYSIS COMPLETE",
                    color = FortuneGold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "此面相气场充沛，五行水旺而木辅，\n"
                        + "主智慧深远，贵人运旺。\n"
                        + "近期宜静心修炼，把握机遇。",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBadge("RATING", "S+", AccentVision)
                    StatBadge("FIELD", "432Hz", CyberSecondary)
                    StatBadge("ELEMENT", "WATER", AccentVision)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentVision)
                ) {
                    Text("BACK", color = CyberBlack, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // ── Bottom HUD bar ──
        if (!showResult) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurface.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HUDItem("FRAME", "30 FPS")
                HUDItem("SENSOR", "IR + UV")
                HUDItem("TEMP", "36.4°C")
                HUDItem("MODE", "FORTUNE")
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun HUDItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = AccentVision, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(label, color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
    }
}
