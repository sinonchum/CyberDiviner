package com.cyberdiviner.ui.muyu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cyberdiviner.R
import com.cyberdiviner.ui.theme.*

/**
 * Electronic Singing Bowl (电子颂钵)
 *
 * Canvas-drawn singing bowl with mallet, spring physics on tap,
 * ripple ring animations, and floating merit text.
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

    // Bowl bounce animation
    var isPressed by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    // Mallet swing animation. Animatable is restarted by hitTrigger so rapid repeated taps
    // still produce a visible strike instead of getting stuck at an unchanged target value.
    val malletAngle = remember { Animatable(0f) }

    // Sequenced press/release for bowl bounce.
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(80)
            isPressed = false
        }
    }

    LaunchedEffect(hitTrigger) {
        if (hitTrigger > 0) {
            malletAngle.stop()
            malletAngle.snapTo(0f)
            malletAngle.animateTo(
                targetValue = 16f,
                animationSpec = tween(durationMillis = 90, easing = FastOutLinearInEasing)
            )
            malletAngle.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        }
    }

    // Floating +1 清音 merit text
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

    // 3-layer ripple ring infinite animation
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
                // 3-layer ripple rings (infinite, expanding, fading)
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

                // Singing bowl + mallet (image asset, inverted on black background)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
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
                    Image(
                        painter = painterResource(id = R.drawable.singing_bowl_body_inverted),
                        contentDescription = "颂钵钵体",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 86.dp, start = 6.dp, end = 6.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.singing_bowl_striker_inverted),
                        contentDescription = "颂钵毡槌",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(width = 128.dp, height = 178.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-28).dp, y = 8.dp)
                            .graphicsLayer {
                                val strikeProgress = (malletAngle.value / 16f).coerceIn(0f, 1f)
                                transformOrigin = TransformOrigin(0.58f, 0.08f)
                                rotationZ = -12f + malletAngle.value
                                translationX = -6f * strikeProgress
                                translationY = 22f * strikeProgress
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Floating +1 清音 merit text ───────────────────────
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
                text = "轻触颂钵，静心调息",
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

// ── Canvas drawing: singing bowl + mallet ───────────────────────

/**
 * Draws a hemispherical singing bowl with mallet, using cubic bezier curves.
 *
 * Bowl: ~260dp wide × ~130dp tall, interior filled light gray, bold white outline.
 * Mallet: positioned diagonally upper-right, angled ~35°, three parts (head, handle, grip).
 * Shadow: small elliptical shadow below the bowl.
 */
private fun DrawScope.drawSingingBowl(malletAngleDeg: Float) {
    val sw = 2.dp.toPx()
    val W = CyberWhite
    val W80 = W.copy(alpha = 0.8f)
    val W70 = W.copy(alpha = 0.7f)
    val W50 = W.copy(alpha = 0.5f)
    val W35 = W.copy(alpha = 0.35f)
    val W20 = W.copy(alpha = 0.2f)
    val W12 = W.copy(alpha = 0.12f)
    val InteriorFill = Color(0xFF1A1A1A) // very dark gray interior

    // Bowl geometry
    val cx = size.width / 2f
    val cy = size.height / 2f + size.height * 0.06f
    val bowlW = size.minDimension * 0.82f  // ~213dp effective width
    val bowlH = size.minDimension * 0.44f  // ~114dp effective height

    val rimY = cy - bowlH * 0.5f          // top of rim
    val baseY = cy + bowlH * 0.48f        // bottom of bowl
    val rimHalfW = bowlW * 0.5f           // half-width at rim
    val baseHalfW = bowlW * 0.08f         // half-width at base

    // ── Shadow below bowl ────────────────────────────────────────
    drawOval(
        color = W12,
        topLeft = Offset(cx - rimHalfW * 0.65f, baseY + bowlH * 0.06f),
        size = Size(rimHalfW * 1.3f, bowlH * 0.14f)
    )

    // ── Bowl body (bezier curve path) ────────────────────────────
    val bowlPath = Path().apply {
        // Left rim → bottom-left curve
        moveTo(cx - rimHalfW, rimY)
        cubicTo(
            cx - rimHalfW * 0.95f, cy + bowlH * 0.18f,   // control 1: slight inward from rim
            cx - baseHalfW * 2.5f, baseY,                  // control 2: curves toward base
            cx, baseY                                       // end: bottom center
        )
        // Bottom-right curve → right rim
        cubicTo(
            cx + baseHalfW * 2.5f, baseY,                  // control 1: mirror of left
            cx + rimHalfW * 0.95f, cy + bowlH * 0.18f,    // control 2: slight inward from rim
            cx + rimHalfW, rimY                              // end: right rim
        )
        close()
    }

    // Fill interior with dark gray
    drawPath(bowlPath, InteriorFill)

    // Bowl body outline (bold white)
    drawPath(
        bowlPath,
        W,
        style = Stroke(
            width = sw * 1.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // ── Rim (elliptical) ─────────────────────────────────────────
    // Outer rim edge
    drawOval(
        color = W,
        topLeft = Offset(cx - rimHalfW * 1.04f, rimY - bowlH * 0.16f),
        size = Size(rimHalfW * 2.08f, bowlH * 0.32f),
        style = Stroke(sw * 1.8f)
    )
    // Inner rim highlight
    drawOval(
        color = W50,
        topLeft = Offset(cx - rimHalfW * 0.88f, rimY - bowlH * 0.08f),
        size = Size(rimHalfW * 1.76f, bowlH * 0.16f),
        style = Stroke(sw * 0.8f)
    )

    // ── Decorative lines on bowl body ────────────────────────────
    // Upper interior line
    drawLine(
        W35,
        Offset(cx - rimHalfW * 0.62f, cy + bowlH * 0.08f),
        Offset(cx + rimHalfW * 0.62f, cy + bowlH * 0.08f),
        sw * 0.7f,
        StrokeCap.Round
    )
    // Lower base accent line
    drawLine(
        W20,
        Offset(cx - rimHalfW * 0.32f, baseY + bowlH * 0.05f),
        Offset(cx + rimHalfW * 0.32f, baseY + bowlH * 0.05f),
        sw * 0.8f,
        StrokeCap.Round
    )

    // ── Mallet ──────────────────────────────────────────────────
    // Pivot point: upper-right of bowl
    val malletPivotX = cx + rimHalfW * 0.75f
    val malletPivotY = cy - bowlH * 1.45f

    // Rest position tip: near the rim right side
    val malletTipX0 = cx + rimHalfW * 0.35f
    val malletTipY0 = rimY - bowlH * 0.08f

    // Rotate mallet around pivot by malletAngleDeg
    val angleRad = Math.toRadians(malletAngleDeg.toDouble())
    val dx = malletTipX0 - malletPivotX
    val dy = malletTipY0 - malletPivotY
    val cosA = kotlin.math.cos(angleRad).toFloat()
    val sinA = kotlin.math.sin(angleRad).toFloat()
    val tipX = malletPivotX + (dx * cosA - dy * sinA)
    val tipY = malletPivotY + (dx * sinA + dy * cosA)

    // Compute mallet direction angle for orienting head and grip
    val malletDirAngle = Math.toDegrees(
        kotlin.math.atan2((tipY - malletPivotY).toDouble(), (tipX - malletPivotX).toDouble())
    ).toFloat()

    // Mallet line (handle shaft)
    drawLine(
        W70,
        Offset(malletPivotX, malletPivotY),
        Offset(tipX, tipY),
        sw * 1.8f,
        StrokeCap.Round
    )

    // Mallet head: rounded felt shape at the tip end
    val headW = bowlW * 0.13f
    val headH = bowlH * 0.10f
    rotate(degrees = malletDirAngle, pivot = Offset(tipX, tipY)) {
        // Head body (filled rounded rect)
        drawRoundRect(
            color = W,
            topLeft = Offset(tipX - headW * 0.5f, tipY - headH * 0.5f),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(headH * 0.4f, headH * 0.4f)
        )
        // Head outline
        drawRoundRect(
            color = W50,
            topLeft = Offset(tipX - headW * 0.5f, tipY - headH * 0.5f),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(headH * 0.4f, headH * 0.4f),
            style = Stroke(sw * 0.6f)
        )
        // Hatching lines on head (felt texture)
        val hatchCount = 4
        val hatchSpacing = headW / (hatchCount + 1)
        for (i in 1..hatchCount) {
            val lx = tipX - headW * 0.5f + hatchSpacing * i
            drawLine(
                W20,
                Offset(lx, tipY - headH * 0.32f),
                Offset(lx, tipY + headH * 0.32f),
                sw * 0.4f,
                StrokeCap.Round
            )
        }
    }

    // Grip: solid white cylinder at the pivot end
    val gripLen = bowlW * 0.09f
    val gripW = sw * 2.8f
    rotate(degrees = malletDirAngle, pivot = Offset(malletPivotX, malletPivotY)) {
        drawRoundRect(
            color = W,
            topLeft = Offset(malletPivotX - gripLen * 0.1f, malletPivotY - gripW * 0.5f),
            size = Size(gripLen, gripW),
            cornerRadius = CornerRadius(gripW * 0.35f, gripW * 0.35f)
        )
    }
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
