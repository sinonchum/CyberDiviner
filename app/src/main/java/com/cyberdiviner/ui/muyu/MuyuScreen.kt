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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
 * 电子木鱼 — Temple Wooden Fish
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
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    // Mallet strike animation
    var malletStrike by remember { mutableStateOf(false) }
    val malletAngle by animateFloatAsState(
        targetValue = if (malletStrike) -30f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "malletAngle"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(80)
            isPressed = false
            malletStrike = true
            kotlinx.coroutines.delay(250)
            malletStrike = false
        }
    }

    // Floating +1 merit
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

    // Ripple
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

            // ── Stats ────────────────────────────────────────────
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
                modifier = Modifier.size(320.dp)
            ) {
                // Ripple rings
                for (i in 1..3) {
                    val ringAlpha = ((1f - rippleProgress) * 0.12f).coerceIn(0f, 0.12f)
                    Canvas(
                        modifier = Modifier
                            .size((160 + i * 50).dp)
                            .graphicsLayer {
                                scaleX = 1f + rippleProgress * 0.15f * i
                                scaleY = 1f + rippleProgress * 0.15f * i
                                alpha = ringAlpha * (1f - i * 0.2f)
                            }
                    ) {
                        drawCircle(
                            color = CyberWhite,
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // Wooden fish + mallet
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            // ── Floating +1 ──────────────────────────────────────
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

// ── Temple Wooden Fish (寺庙木鱼) ────────────────────────────────
//
//   Shape: Rounded square body with slightly concave top edge,
//          decorative "scale" pattern, central strike point,
//          short handle stem at bottom, and a mallet to the side.
//
//        ╭──────────────╮
//       │  ╱╲  ╱╲  ╱╲   │
//       │ ╱  ╲╱  ╲╱  ╲  │   ← scale pattern
//       │   ── ◉ ──     │   ← strike point
//       │ ╲  ╱╲  ╱╲  ╱  │
//       │  ╲╱  ╲╱  ╲╱   │
//        ╰──────┬───────╯
//               │            ← handle stem
//               ▼

private fun DrawScope.drawTempleWoodenFish(malletAngleDeg: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val bodyR = size.minDimension * 0.34f
    val sw = 2.dp.toPx()
    val color = CyberWhite
    val colorMid = CyberWhite.copy(alpha = 0.5f)
    val colorDim = CyberWhite.copy(alpha = 0.2f)

    // ── Body: rounded square (superellipse approximation) ──
    // Draw as a rounded rectangle path
    val bodyW = bodyR * 1.7f
    val bodyH = bodyR * 1.5f
    val cornerR = bodyR * 0.4f

    val bodyPath = Path().apply {
        // Start at top-left corner
        moveTo(cx - bodyW + cornerR, cy - bodyH)
        // Top edge
        lineTo(cx + bodyW - cornerR, cy - bodyH)
        // Top-right corner
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                cx + bodyW - cornerR * 2, cy - bodyH,
                cx + bodyW, cy - bodyH + cornerR * 2
            ),
            startAngleDegrees = -90f, sweepAngleDegrees = 90f, forceMoveTo = false
        )
        // Right edge
        lineTo(cx + bodyW, cy + bodyH - cornerR)
        // Bottom-right corner
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                cx + bodyW - cornerR * 2, cy + bodyH - cornerR * 2,
                cx + bodyW, cy + bodyH
            ),
            startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false
        )
        // Bottom edge (with gap for handle)
        lineTo(cx + bodyW * 0.3f, cy + bodyH)
        moveTo(cx - bodyW * 0.3f, cy + bodyH)
        lineTo(cx - bodyW + cornerR, cy + bodyH)
        // Bottom-left corner
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                cx - bodyW, cy + bodyH - cornerR * 2,
                cx - bodyW + cornerR * 2, cy + bodyH
            ),
            startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false
        )
        // Left edge
        lineTo(cx - bodyW, cy - bodyH + cornerR)
        // Top-left corner
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                cx - bodyW, cy - bodyH,
                cx - bodyW + cornerR * 2, cy - bodyH + cornerR * 2
            ),
            startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false
        )
        close()
    }
    drawPath(bodyPath, color, style = Stroke(sw, cap = StrokeCap.Round))

    // ── Top slit (开口 / mouth of the fish) ──
    val slitW = bodyW * 0.5f
    drawLine(
        color = colorMid,
        start = Offset(cx - slitW, cy - bodyH + bodyH * 0.25f),
        end = Offset(cx + slitW, cy - bodyH + bodyH * 0.25f),
        strokeWidth = sw * 1.2f,
        cap = StrokeCap.Round
    )

    // ── Scale pattern (鱼鳞纹) — concentric arcs inside ──
    val scaleRows = 3
    val scaleCols = 4
    val scaleStartY = cy - bodyH * 0.1f
    val scaleSpacingX = bodyW * 1.4f / (scaleCols + 1)
    val scaleSpacingY = bodyH * 1.2f / (scaleRows + 1)

    for (row in 0 until scaleRows) {
        for (col in 0 until scaleCols) {
            val sx = cx - bodyW * 0.7f + (col + 1) * scaleSpacingX
            val sy = scaleStartY + row * scaleSpacingY
            val arcR = scaleSpacingX * 0.35f

            // Alternating direction for fish-scale effect
            if ((row + col) % 2 == 0) {
                drawArc(
                    color = colorDim,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(sx - arcR, sy - arcR),
                    size = Size(arcR * 2, arcR * 2),
                    style = Stroke(sw * 0.5f, cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    color = colorDim,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(sx - arcR, sy - arcR),
                    size = Size(arcR * 2, arcR * 2),
                    style = Stroke(sw * 0.5f, cap = StrokeCap.Round)
                )
            }
        }
    }

    // ── Central strike point (圆心 / 击打点) ──
    val dotR = bodyR * 0.06f
    drawCircle(color, radius = dotR, center = Offset(cx, cy))
    drawCircle(color, radius = dotR * 2.5f, center = Offset(cx, cy), style = Stroke(sw * 0.6f))

    // ── Handle stem (把手) at bottom ──
    val handleW = bodyW * 0.2f
    val handleH = bodyH * 0.45f
    val handleTop = cy + bodyH
    val handlePath = Path().apply {
        moveTo(cx - handleW, handleTop)
        lineTo(cx - handleW * 0.7f, handleTop + handleH)
        lineTo(cx + handleW * 0.7f, handleTop + handleH)
        lineTo(cx + handleW, handleTop)
        close()
    }
    drawPath(handlePath, color, style = Stroke(sw, cap = StrokeCap.Round))

    // ── Mallet (木槌) — top-right, angled toward strike point ──
    val malletLen = bodyR * 1.4f
    val malletHeadR = bodyR * 0.1f
    val malletPivotX = cx + bodyW * 1.1f
    val malletPivotY = cy - bodyH * 1.0f
    val malletTipX0 = cx + bodyW * 0.2f
    val malletTipY0 = cy - bodyH * 0.1f

    // Rotate mallet around pivot
    val angleRad = Math.toRadians(malletAngleDeg.toDouble())
    val dx = malletTipX0 - malletPivotX
    val dy = malletTipY0 - malletPivotY
    val tipX = malletPivotX + (dx * kotlin.math.cos(angleRad) - dy * kotlin.math.sin(angleRad)).toFloat()
    val tipY = malletPivotY + (dx * kotlin.math.sin(angleRad) + dy * kotlin.math.cos(angleRad)).toFloat()

    // Handle
    drawLine(
        color = colorMid,
        start = Offset(malletPivotX, malletPivotY),
        end = Offset(tipX, tipY),
        strokeWidth = sw * 1.5f,
        cap = StrokeCap.Round
    )
    // Head
    drawCircle(color, radius = malletHeadR, center = Offset(tipX, tipY), style = Stroke(sw))
    drawCircle(color, radius = malletHeadR * 0.35f, center = Offset(tipX, tipY))
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

