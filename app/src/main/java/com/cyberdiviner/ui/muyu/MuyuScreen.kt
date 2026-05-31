package com.cyberdiviner.ui.muyu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
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
 * Electronic Singing Bowl (电子颂钵)
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

    // Striker animation
    var malletStrike by remember { mutableStateOf(false) }
    val malletAngle by animateFloatAsState(
        targetValue = if (malletStrike) -30f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "strikerAngle"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(80)
            isPressed = false
                malletStrike = true
                kotlinx.coroutines.delay(320)
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
                    text = "电子颂钵",
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
                StatBadge(label = "本次清音", value = "$sessionHits")
                StatBadge(label = "总清音", value = "$totalHits")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Singing bowl area ─────────────────────────────────
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

                // Singing bowl + striker
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
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = malletAngle * 0.1f
                            }
                    ) {
                        drawSingingBowl(malletAngle)
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
                        text = "＋1 清音",
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
                text = "轻触颂钵，听一声清响",
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

private fun DrawScope.drawSingingBowl(malletAngleDeg: Float) {
    val cx = size.width / 2f - size.width * 0.05f
    val cy = size.height / 2f + size.height * 0.05f
    val sw = 2.dp.toPx()
    val W = CyberWhite
    val W70 = W.copy(alpha = 0.7f)
    val W50 = W.copy(alpha = 0.5f)
    val W30 = W.copy(alpha = 0.3f)
    val W18 = W.copy(alpha = 0.18f)
    val bowlW = size.minDimension * 0.78f
    val bowlH = size.minDimension * 0.42f
    val rimY = cy - bowlH * 0.42f
    val baseY = cy + bowlH * 0.46f

    val bowlPath = Path().apply {
        moveTo(cx - bowlW * 0.5f, rimY)
        cubicTo(cx - bowlW * 0.48f, cy + bowlH * 0.16f, cx - bowlW * 0.27f, baseY, cx, baseY)
        cubicTo(cx + bowlW * 0.27f, baseY, cx + bowlW * 0.48f, cy + bowlH * 0.16f, cx + bowlW * 0.5f, rimY)
    }
    drawOval(
        color = W18,
        topLeft = Offset(cx - bowlW * 0.46f, rimY + bowlH * 0.05f),
        size = Size(bowlW * 0.92f, bowlH * 0.82f),
        style = Stroke(sw * 0.9f)
    )
    drawPath(bowlPath, W, style = Stroke(sw * 1.5f, cap = StrokeCap.Round))
    drawOval(
        color = W,
        topLeft = Offset(cx - bowlW * 0.52f, rimY - bowlH * 0.18f),
        size = Size(bowlW * 1.04f, bowlH * 0.36f),
        style = Stroke(sw * 1.8f)
    )
    drawOval(
        color = W50,
        topLeft = Offset(cx - bowlW * 0.42f, rimY - bowlH * 0.09f),
        size = Size(bowlW * 0.84f, bowlH * 0.18f),
        style = Stroke(sw * 0.9f)
    )
    drawLine(W30, Offset(cx - bowlW * 0.34f, cy + bowlH * 0.12f), Offset(cx + bowlW * 0.34f, cy + bowlH * 0.12f), sw * 0.8f, StrokeCap.Round)
    drawLine(W30, Offset(cx - bowlW * 0.2f, baseY + bowlH * 0.08f), Offset(cx + bowlW * 0.2f, baseY + bowlH * 0.08f), sw, StrokeCap.Round)
    drawArc(W30, 205f, 130f, false, Offset(cx - bowlW * 0.73f, rimY - bowlH * 0.46f), Size(bowlW * 1.46f, bowlH * 1.46f), style = Stroke(sw * 0.8f, cap = StrokeCap.Round))
    drawArc(W18, 212f, 116f, false, Offset(cx - bowlW * 0.86f, rimY - bowlH * 0.6f), Size(bowlW * 1.72f, bowlH * 1.72f), style = Stroke(sw * 0.6f, cap = StrokeCap.Round))

    val malletPivotX = cx + bowlW * 0.52f
    val malletPivotY = cy - bowlH * 1.18f
    val malletTipX0 = cx + bowlW * 0.33f
    val malletTipY0 = rimY - bowlH * 0.12f

    val angleRad = Math.toRadians(malletAngleDeg.toDouble())
    val dx = malletTipX0 - malletPivotX
    val dy = malletTipY0 - malletPivotY
    val tipX = malletPivotX + (dx * kotlin.math.cos(angleRad) - dy * kotlin.math.sin(angleRad)).toFloat()
    val tipY = malletPivotY + (dx * kotlin.math.sin(angleRad) + dy * kotlin.math.cos(angleRad)).toFloat()

    drawLine(W70, Offset(malletPivotX, malletPivotY), Offset(tipX, tipY), sw * 2.2f, StrokeCap.Round)
    rotate(degrees = malletAngleDeg - 28f, pivot = Offset(tipX, tipY)) {
        drawRoundRect(
            color = W,
            topLeft = Offset(tipX - bowlW * 0.08f, tipY - bowlH * 0.055f),
            size = Size(bowlW * 0.16f, bowlH * 0.11f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bowlH * 0.05f, bowlH * 0.05f)
        )
        drawRoundRect(
            color = W30,
            topLeft = Offset(tipX - bowlW * 0.095f, tipY - bowlH * 0.068f),
            size = Size(bowlW * 0.19f, bowlH * 0.136f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bowlH * 0.06f, bowlH * 0.06f),
            style = Stroke(sw * 0.7f)
        )
    }
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
