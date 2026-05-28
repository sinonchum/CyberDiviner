package com.cyberdiviner.ui.muyu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * 电子木鱼 — Digital Wooden Fish screen.
 * Matches the app's B&W institutional design with Canvas-drawn wooden fish icon.
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

    // ── Hit animation state ──────────────────────────────────────
    var isPressed by remember { mutableStateOf(false) }

    val bounceScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(80)
            isPressed = false
        }
    }

    // ── Floating +1 merit animation ──────────────────────────────
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

    // ── Ambient glow pulse ───────────────────────────────────────
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
    val glowAlpha = 0.06f + 0.03f * kotlin.math.sin(glowPhase)

    // ── Ripple ring pulse ────────────────────────────────────────
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
            // ── Top bar (matching app style) ──────────────────────
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
                // Reset button
                Text(
                    text = "重置",
                    color = GrayMuted,
                    fontSize = 12.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.clickable { viewModel.newSession() }
                )
            }

            // Divider
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

            // ── Wooden fish area ──────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Ripple rings
                for (i in 1..3) {
                    val ringAlpha = ((1f - rippleProgress) * 0.2f).coerceIn(0f, 0.2f)
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
                            color = AccentMuyu,
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // Main wooden fish — Canvas icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
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
                    Canvas(modifier = Modifier.size(160.dp)) {
                        drawWoodenFish()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Floating +1 merit text ────────────────────────────
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (meritActive) {
                    Text(
                        text = "＋1 功德",
                        color = AccentMuyu,
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

            // ── Instruction ───────────────────────────────────────
            Text(
                text = "轻触木鱼，积累功德",
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Bottom session info ───────────────────────────────
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

// ── Canvas wooden fish icon ────────────────────────────────────────

private fun DrawScope.drawWoodenFish() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val scale = size.minDimension * 0.42f
    val sw = 2.dp.toPx()
    val color = AccentMuyu
    val colorDim = AccentMuyu.copy(alpha = 0.4f)

    // Fish body — rounded oval shape (木鱼 is traditionally a hollowed fish-shaped block)
    // Draw the main body as an elliptical outline
    drawOval(
        color = color,
        topLeft = Offset(cx - scale * 0.8f, cy - scale * 0.55f),
        size = androidx.compose.ui.geometry.Size(scale * 1.6f, scale * 1.1f),
        style = Stroke(sw, cap = StrokeCap.Round)
    )

    // Inner body line (the "split" down the middle of a wooden fish)
    drawLine(
        color = colorDim,
        start = Offset(cx - scale * 0.5f, cy),
        end = Offset(cx + scale * 0.5f, cy),
        strokeWidth = sw * 0.8f,
        cap = StrokeCap.Round
    )

    // Tail fin — V-shape at the right
    val tailPath = Path().apply {
        moveTo(cx + scale * 0.7f, cy - scale * 0.2f)
        lineTo(cx + scale * 1.1f, cy - scale * 0.45f)
        moveTo(cx + scale * 0.7f, cy + scale * 0.2f)
        lineTo(cx + scale * 1.1f, cy + scale * 0.45f)
    }
    drawPath(tailPath, color, style = Stroke(sw, cap = StrokeCap.Round))

    // Mouth — small opening at the left (where the mallet strikes)
    drawArc(
        color = color,
        startAngle = -45f,
        sweepAngle = -90f,
        useCenter = false,
        topLeft = Offset(cx - scale * 1.0f, cy - scale * 0.2f),
        size = androidx.compose.ui.geometry.Size(scale * 0.4f, scale * 0.4f),
        style = Stroke(sw, cap = StrokeCap.Round)
    )

    // Eye — small circle on the upper left
    drawCircle(
        color = color,
        radius = scale * 0.06f,
        center = Offset(cx - scale * 0.35f, cy - scale * 0.25f)
    )

    // Scales — decorative arcs on the body
    for (i in 0..2) {
        val sx = cx + scale * (i * 0.2f - 0.15f)
        drawArc(
            color = colorDim,
            startAngle = -30f,
            sweepAngle = -120f,
            useCenter = false,
            topLeft = Offset(sx - scale * 0.12f, cy - scale * 0.35f),
            size = androidx.compose.ui.geometry.Size(scale * 0.24f, scale * 0.24f),
            style = Stroke(sw * 0.6f, cap = StrokeCap.Round)
        )
    }

    // Mallet — small diagonal line near the fish (suggesting the striker)
    drawLine(
        color = color.copy(alpha = 0.3f),
        start = Offset(cx - scale * 0.9f, cy - scale * 0.65f),
        end = Offset(cx - scale * 0.6f, cy - scale * 0.15f),
        strokeWidth = sw * 1.2f,
        cap = StrokeCap.Round
    )
}

// ── Stat badge composable ──────────────────────────────────────

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
