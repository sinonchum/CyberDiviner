package com.cyberdiviner.ui.muyu

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cyberdiviner.ui.theme.*

/**
 * 电子木鱼 — Meditation Wooden Fish screen.
 * A cyberpunk-styled digital wooden fish the user taps to accumulate merit (功德).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        targetValue = if (isPressed) 0.88f else 1f,
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
    val glowAlpha = 0.08f + 0.04f * kotlin.math.sin(glowPhase)

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
            // ── Top bar ──────────────────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        "电子木鱼",
                        color = AccentMuyu,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = AccentMuyu
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.newSession() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "新 session",
                            tint = AccentMuyu
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberDark)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Stats row ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(label = "本次功德", value = "$sessionHits", color = AccentMuyu)
                StatBadge(label = "总功德", value = "$totalHits", color = FortuneGold)
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Wooden fish area ─────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Ripple rings
                for (i in 1..3) {
                    val ringAlpha = ((1f - rippleProgress) * 0.3f).coerceIn(0f, 0.3f)
                    Box(
                        modifier = Modifier
                            .size((160 + i * 40).dp)
                            .graphicsLayer {
                                scaleX = 1f + rippleProgress * 0.3f * i
                                scaleY = 1f + rippleProgress * 0.3f * i
                                alpha = ringAlpha * (1f - i * 0.25f)
                            }
                            .clip(CircleShape)
                            .background(AccentMuyu.copy(alpha = 0.06f))
                    )
                }

                // Main fish circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            scaleX = bounceScale
                            scaleY = bounceScale
                        }
                        .clip(CircleShape)
                        .background(AccentMuyu.copy(alpha = 0.15f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isPressed = true
                            viewModel.hit()
                        }
                ) {
                    // Wooden fish symbol — simple text character
                    Text(
                        text = "◎",
                        fontSize = 64.sp,
                        color = AccentMuyu,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Floating +1 merit text ───────────────────────────
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (meritActive) {
                    Text(
                        text = "＋1 功德",
                        color = AccentMuyu,
                        fontSize = 22.sp,
                        fontFamily = WenKaiFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer {
                            translationY = -50f * meritAnimatable.value
                            alpha = 1f - meritAnimatable.value
                        }
                    )
                }
            }

            // ── Instruction hint ─────────────────────────────────
            Text(
                text = "轻触木鱼，积累功德",
                color = TextSecondary,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Bottom session info ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "session: ${viewModel.sessionId.value.take(8)}…",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = MonoFontFamily
                )
                TextButton(onClick = { viewModel.clearSession() }) {
                    Text("清除本次", color = AccentMuyu, fontSize = 12.sp, fontFamily = WenKaiFontFamily)
                }
            }
        }
    }
}

// ── Stat badge composable ──────────────────────────────────────
@Composable
private fun StatBadge(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonoFontFamily
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily
        )
    }
}
