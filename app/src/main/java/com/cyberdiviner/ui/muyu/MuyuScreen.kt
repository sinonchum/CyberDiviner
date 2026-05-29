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
 * Electronic Wooden Fish (电子木鱼)
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
                    // Canvas-drawn temple wooden fish — clearer than PNG
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = malletAngle * 0.1f
                            }
                    ) {
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

// ── Temple Wooden Fish ────────────────────────────────────
//
//   Based on real Buddhist temple wooden fish reference:
//   - Nearly spherical hollow wooden body
//   - Horizontal slit (鱼口/开口) at equator — sound resonance opening
//   - Fish scale pattern (鱼鳞) on upper hemisphere — overlapping semicircles
//   - Two decorative eyes on upper section
//   - Central strike point where mallet hits
//   - Rests on a small wooden cradle/cushion (no handle — rests on surface)
//   - Mallet with round bulbous head
//
//            ╭─────────────╮
//          ╱  ◯  ╌╌╌  ◯   ╲       ← eyes
//        ╱  ⌇⌇⌇⌇⌇⌇⌇⌇⌇⌇⌇  ╲     ← fish scales
//       │   ╌╌╌╌╌ ◉ ╌╌╌╌╌  │     ← strike point
//       │   ═══════════════  │     ← mouth slit (equator)
//        ╲                 ╱
//          ╲             ╱
//            ╰─────────╯
//           ───┤     ├───         ← cradle base

private fun DrawScope.drawTempleWoodenFish(malletAngleDeg: Float) {
    // Center the fish body slightly left to make room for the mallet
    val cx = size.width / 2f - size.width * 0.05f
    val cy = size.height / 2f
    val sw = 2.dp.toPx()
    val W = CyberWhite
    val W50 = W.copy(alpha = 0.5f)
    val W30 = W.copy(alpha = 0.3f)

    // ═══════════════════════════════════════════════════
    // 1. PEAR/GOURD SHAPE — the body of the wooden fish
    //    Wider at top, narrower at bottom, smooth organic curve
    // ═══════════════════════════════════════════════════
    val bodyW = size.minDimension * 0.36f   // half-width at widest
    val bodyH = size.minDimension * 0.42f   // half-height
    val stemW = size.minDimension * 0.04f   // stem width
    val stemH = size.minDimension * 0.06f   // stem height

    val bodyPath = Path().apply {
        // Start at bottom center (stem junction)
        moveTo(cx, cy + bodyH * 0.85f)
        // Right side — smooth curve outward then back in
        cubicTo(
            cx + bodyW * 0.6f, cy + bodyH * 0.85f,   // control 1: out to the right
            cx + bodyW, cy + bodyH * 0.2f,             // control 2: wide at upper-right
            cx + bodyW * 0.95f, cy - bodyH * 0.1f     // end: near top-right
        )
        // Top — round dome
        cubicTo(
            cx + bodyW * 0.85f, cy - bodyH * 0.7f,    // control 1
            cx - bodyW * 0.85f, cy - bodyH * 0.7f,    // control 2
            cx - bodyW * 0.95f, cy - bodyH * 0.1f     // end: top-left
        )
        // Left side — mirror of right
        cubicTo(
            cx - bodyW, cy + bodyH * 0.2f,
            cx - bodyW * 0.6f, cy + bodyH * 0.85f,
            cx, cy + bodyH * 0.85f
        )
        close()
    }
    drawPath(bodyPath, W, style = Stroke(sw * 1.5f, cap = StrokeCap.Round))

    // ═══════════════════════════════════════════════════
    // 2. MOUTH SLIT (开口) — curved horizontal opening
    //    Follows the curvature of the pear shape
    // ═══════════════════════════════════════════════════
    val slitY = cy - bodyH * 0.05f  // slightly above center
    val slitW = bodyW * 0.75f       // how wide the slit extends

    // The slit is a curved line (arc) that follows the sphere's surface
    val slitPath = Path().apply {
        moveTo(cx - slitW, slitY + bodyH * 0.06f)  // left end, slightly lower
        // Arc upward in the middle, following the sphere curvature
        quadraticBezierTo(
            cx, slitY - bodyH * 0.12f,              // peak of the arc (curves up)
            cx + slitW, slitY + bodyH * 0.06f       // right end, slightly lower
        )
    }
    drawPath(slitPath, W, style = Stroke(sw * 1.4f, cap = StrokeCap.Round))

    // Second line slightly below — creates the "gap" / depth of the slit
    val slitPath2 = Path().apply {
        moveTo(cx - slitW * 0.85f, slitY + bodyH * 0.12f)
        quadraticBezierTo(
            cx, slitY - bodyH * 0.04f,
            cx + slitW * 0.85f, slitY + bodyH * 0.12f
        )
    }
    drawPath(slitPath2, W30, style = Stroke(sw * 0.8f, cap = StrokeCap.Round))

    // ═══════════════════════════════════════════════════
    // 3. STEM (茎) — small tiered detail at the bottom
    // ═══════════════════════════════════════════════════
    val stemTop = cy + bodyH * 0.85f
    // Stepped ridges
    drawLine(W50, Offset(cx - stemW * 1.5f, stemTop), Offset(cx + stemW * 1.5f, stemTop), sw, StrokeCap.Round)
    drawLine(W50, Offset(cx - stemW, stemTop + stemH * 0.4f), Offset(cx + stemW, stemTop + stemH * 0.4f), sw, StrokeCap.Round)
    drawLine(W50, Offset(cx - stemW * 0.5f, stemTop + stemH * 0.8f), Offset(cx + stemW * 0.5f, stemTop + stemH * 0.8f), sw, StrokeCap.Round)

    // ═══════════════════════════════════════════════════
    // 4. MALLET (木槌) — separate stick with round head
    // ═══════════════════════════════════════════════════
    val malletPivotX = cx + bodyW * 1.8f
    val malletPivotY = cy - bodyH * 0.9f
    val malletTipX0 = cx + bodyW * 0.2f
    val malletTipY0 = cy + bodyH * 0.15f

    val angleRad = Math.toRadians(malletAngleDeg.toDouble())
    val dx = malletTipX0 - malletPivotX
    val dy = malletTipY0 - malletPivotY
    val tipX = malletPivotX + (dx * kotlin.math.cos(angleRad) - dy * kotlin.math.sin(angleRad)).toFloat()
    val tipY = malletPivotY + (dx * kotlin.math.sin(angleRad) + dy * kotlin.math.cos(angleRad)).toFloat()

    // Handle
    drawLine(W50, Offset(malletPivotX, malletPivotY), Offset(tipX, tipY), sw * 2f, StrokeCap.Round)
    // Head — solid round bulb
    val headR = bodyW * 0.1f
    drawCircle(W, radius = headR, center = Offset(tipX, tipY))
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

