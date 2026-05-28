package com.cyberdiviner.ui.muyu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cyberdiviner.ui.theme.*

/**
 * 电子木鱼 — Temple Wooden Fish (圆锤形法器)
 */
@Composable
fun MuyuScreen(
    navController: NavController,
    viewModel: MuyuViewModel = hiltViewModel()
) {
    val totalHits by viewModel.totalHits.collectAsStateWithLifecycle()
    val sessionHits by viewModel.sessionHits.collectAsStateWithLifecycle()
    val hitTrigger by viewModel.hitTrigger.collectAsStateWithLifecycle()

    val hapticFeedback = LocalHapticFeedback.current

    var isPressed by remember { mutableStateOf(false) }

    val bounceScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    // Mallet swing animation
    var malletSwing by remember { mutableStateOf(false) }
    val malletAngle by animateFloatAsState(
        targetValue = if (malletSwing) -25f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "malletAngle"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
            malletSwing = true
            kotlinx.coroutines.delay(200)
            malletSwing = false
        }
    }

    // Floating +1 merit animation
    val meritAnimatable = remember { Animatable(0f) }
    var meritActive by remember { mutableStateOf(false) }

    LaunchedEffect(hitTrigger) {
        if (hitTrigger > 0) {
            meritActive = true
            meritAnimatable.snapTo(0f)
            meritAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
            meritActive = false
        }
    }

    // Ambient glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowPhase"
    )

    // Ripple ring pulse
    val rippleTransition = rememberInfiniteTransition(label = "ripple")
    val rippleProgress by rippleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "< 返回",
                    color = GrayCaption,
                    fontSize = 13.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "电子木鱼",
                    color = GrayCaption,
                    fontSize = 14.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "重置",
                    color = GrayMuted,
                    fontSize = 12.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.clickable { viewModel.newSession() }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Stats row ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(label = "本次功德", value = "$sessionHits")
                StatBadge(label = "总功德", value = "$totalHits")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Temple wooden fish area ──────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                // Ripple rings
                for (i in 1..3) {
                    val ringAlpha = ((1f - rippleProgress) * 0.15f).coerceIn(0f, 0.15f)
                    Canvas(
                        modifier = Modifier
                            .size((180 + i * 40).dp)
                            .graphicsLayer {
                                scaleX = 1f + rippleProgress * 0.2f * i
                                scaleY = 1f + rippleProgress * 0.2f * i
                                alpha = ringAlpha * (1f - i * 0.25f)
                            }
                    ) {
                        drawCircle(
                            color = CyberWhite,
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // Wooden fish body + mallet
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer {
                            scaleX = bounceScale
                            scaleY = bounceScale
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isPressed = true
                            viewModel.hit()
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawTempleWoodenFish(malletAngle)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Floating +1 merit ────────────────────────────────
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (meritActive) {
                    Text(
                        text = "＋1 功德",
                        color = CyberWhite,
                        fontSize = 20.sp,
                        fontFamily = WenKaiFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer {
                            translationY = -50f * meritAnimatable.value
                            alpha = 1f - meritAnimatable.value
                        }
                    )
                }
            }

            Text(
                text = "轻触木鱼，积累功德",
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Bottom ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "session: ${viewModel.sessionId.value.take(8)}…",
                    color = GrayMuted,
                    fontSize = 10.sp,
                    fontFamily = MonoFontFamily
                )
                Text(
                    text = "清除本次",
                    color = GrayMuted,
                    fontSize = 11.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.clickable { viewModel.clearSession() }
                )
            }
        }
    }
}

// ── Canvas: Temple Wooden Fish (寺庙木鱼) ────────────────────────
// A round, hollow percussion instrument with ornamental pattern and a mallet

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTempleWoodenFish(malletAngle: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension * 0.35f  // main radius
    val sw = 2.dp.toPx()
    val color = CyberWhite
    val colorDim = CyberWhite.copy(alpha = 0.3f)
    val colorMid = CyberWhite.copy(alpha = 0.6f)

    // ── Main body: circle (round wooden fish head) ──
    drawCircle(
        color = color,
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(sw, cap = StrokeCap.Round)
    )

    // Inner circle (hollow cavity)
    drawCircle(
        color = colorMid,
        radius = r * 0.75f,
        center = Offset(cx, cy),
        style = Stroke(sw * 0.7f, cap = StrokeCap.Round)
    )

    // Center dot (striking point)
    drawCircle(
        color = color,
        radius = r * 0.08f,
        center = Offset(cx, cy)
    )

    // Cross-hatch pattern inside (traditional ornamental lines)
    val innerR = r * 0.7f
    // Horizontal line
    drawLine(
        color = colorDim,
        start = Offset(cx - innerR, cy),
        end = Offset(cx + innerR, cy),
        strokeWidth = sw * 0.5f,
        cap = StrokeCap.Round
    )
    // Vertical line
    drawLine(
        color = colorDim,
        start = Offset(cx, cy - innerR),
        end = Offset(cx, cy + innerR),
        strokeWidth = sw * 0.5f,
        cap = StrokeCap.Round
    )

    // Diagonal lines (X pattern)
    val diag = innerR * 0.707f
    drawLine(
        color = colorDim.copy(alpha = 0.15f),
        start = Offset(cx - diag, cy - diag),
        end = Offset(cx + diag, cy + diag),
        strokeWidth = sw * 0.4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = colorDim.copy(alpha = 0.15f),
        start = Offset(cx + diag, cy - diag),
        end = Offset(cx - diag, cy + diag),
        strokeWidth = sw * 0.4f,
        cap = StrokeCap.Round
    )

    // ── Mallet (木槌) — positioned at top-right, angled toward center ──
    val malletLen = r * 1.3f
    val malletHeadR = r * 0.12f

    // Mallet handle: from top-right toward center
    val malletStartX = cx + r * 0.8f
    val malletStartY = cy - r * 1.0f
    val malletEndX = cx + r * 0.1f
    val malletEndY = cy - r * 0.1f

    // Apply swing rotation around the mallet's grip end
    val angleRad = Math.toRadians(malletAngle.toDouble())
    val pivotX = malletStartX
    val pivotY = malletStartY

    // Rotate mallet end around pivot
    val dx = malletEndX - pivotX
    val dy = malletEndY - pivotY
    val rotEndX = pivotX + (dx * kotlin.math.cos(angleRad) - dy * kotlin.math.sin(angleRad)).toFloat()
    val rotEndY = pivotY + (dx * kotlin.math.sin(angleRad) + dy * kotlin.math.cos(angleRad)).toFloat()

    // Handle
    drawLine(
        color = colorMid,
        start = Offset(pivotX, pivotY),
        end = Offset(rotEndX, rotEndY),
        strokeWidth = sw * 1.5f,
        cap = StrokeCap.Round
    )

    // Mallet head (round)
    drawCircle(
        color = color,
        radius = malletHeadR,
        center = Offset(rotEndX, rotEndY),
        style = Stroke(sw, cap = StrokeCap.Round)
    )
    drawCircle(
        color = color,
        radius = malletHeadR * 0.4f,
        center = Offset(rotEndX, rotEndY)
    )
}

// ── Stat badge ──────────────────────────────────────────────

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = CyberWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily
        )
        Text(
            text = label,
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily
        )
    }
}
