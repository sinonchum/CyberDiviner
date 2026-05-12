package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.ui.theme.*
import com.cyberdiviner.ui.shared.HapticUtils
import com.cyberdiviner.ui.shared.HapticUtils.HapticType
import androidx.compose.ui.platform.LocalContext
import kotlin.math.cos
import kotlin.math.sin

/**
 * CoinAnimation — animated 3-coin toss visualization for Liuyao divination.
 *
 * Renders three coins that spin, glow, and land showing 字/花 faces.
 * Each coin can land on yang (字) or yin (花) with optional changing-line indicator.
 */
data class CoinState(
    val index: Int,
    val isHeads: Boolean,      // true = 字面 (yang), false = 花面 (yin)
    val isChanging: Boolean = false, // old yang/old yin
    val isRevealed: Boolean = false
)

@Composable
fun CoinTossAnimation(
    currentTossIndex: Int,   // 0-5 (which of the 6 tosses)
    coinStates: List<CoinState>, // 3 coin states for current toss
    isAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_spin")

    // Global rotation for spinning effect
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Bob animation
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    val context = LocalContext.current
    LaunchedEffect(coinStates) {
        if (coinStates.isNotEmpty() && coinStates.all { it.isRevealed }) {
            HapticUtils.vibrate(context, HapticType.COIN_LAND)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Toss counter
        Text(
            text = "第 ${currentTossIndex + 1} / 6 爻",
            color = NeonCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Three coins in a row
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 3) {
                val coin = coinStates.getOrNull(i)
                val revealed = coin?.isRevealed == true

                Box(contentAlignment = Alignment.Center) {
                    Canvas(
                        modifier = Modifier
                            .size(80.dp)
                            .offset(y = if (!revealed && isAnimating) bobOffset.dp else 0.dp)
                    ) {
                        drawCoin(
                            rotation = if (revealed) 0f else rotation,
                            isHeads = coin?.isHeads ?: true,
                            isRevealed = revealed,
                            isChanging = coin?.isChanging ?: false,
                            glowAlpha = if (revealed) 0.6f else glowAlpha,
                            coinIndex = i
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result label
        if (coinStates.all { it.isRevealed }) {
            val resultText = coinStates.joinToString(" + ") { if (it.isHeads) "字" else "花" }
            val lineState = coinStates.toLineState()
            val stateLabel = when (lineState) {
                LineState.OLD_YANG -> "老阳 ○"
                LineState.OLD_YIN -> "老阴 ×"
                LineState.YOUNG_YANG -> "少阳 —"
                LineState.YOUNG_YIN -> "少阴 - -"
            }
            val stateColor = when (lineState) {
                LineState.OLD_YANG -> NeonOrange
                LineState.OLD_YIN -> NeonPurple
                LineState.YOUNG_YANG -> NeonCyan
                LineState.YOUNG_YIN -> NeonBlue
            }

            Text(
                text = "$resultText → $stateLabel",
                color = stateColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (isAnimating) {
            Text(
                text = "抛掷铜钱中...",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

private fun DrawScope.drawCoin(
    rotation: Float,
    isHeads: Boolean,
    isRevealed: Boolean,
    isChanging: Boolean,
    glowAlpha: Float,
    coinIndex: Int
) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2, size.height / 2)

    // Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                NeonCyan.copy(alpha = glowAlpha * 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.3f
        ),
        radius = radius * 1.3f
    )

    // Coin body
    rotate(rotation, pivot = center) {
        // Gold gradient background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    FortuneGold,
                    Color(0xFFB8860B),
                    Color(0xFF8B6914)
                ),
                center = center,
                radius = radius
            ),
            radius = radius
        )

        // Inner ring
        drawCircle(
            color = Color(0xFFDAA520).copy(alpha = 0.6f),
            radius = radius * 0.85f,
            style = Stroke(width = 2.dp.toPx())
        )

        // Square hole in center (traditional Chinese coin)
        val holeSize = radius * 0.3f
        drawRect(
            color = CyberBlack.copy(alpha = 0.8f),
            topLeft = Offset(center.x - holeSize / 2, center.y - holeSize / 2),
            size = androidx.compose.ui.geometry.Size(holeSize, holeSize)
        )
    }

    // Face text (shown when revealed, no rotation)
    if (isRevealed) {
        val faceText = if (isHeads) "字" else "花"
        val faceColor = if (isHeads) NeonCyan else NeonMagenta

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = faceColor.hashCode()
                    textSize = radius * 0.7f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                // Shadow
                paint.color = android.graphics.Color.argb(100, 0, 0, 0)
                drawText(faceText, center.x + 1f, center.y + radius * 0.25f + 1f, paint)
                // Face
                paint.color = faceColor.hashCode()
                drawText(faceText, center.x, center.y + radius * 0.25f, paint)
            }
        }
    }

    // Changing line indicator
    if (isRevealed && isChanging) {
        val indicatorColor = if (isHeads) NeonOrange else NeonPurple
        drawCircle(
            color = indicatorColor,
            radius = radius * 1.1f,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

/**
 * Full 6-toss animation sequence composable.
 * Shows progress through all 6 line tosses.
 */
@Composable
fun SixTossAnimation(
    tossResults: List<LineState>,     // completed toss results so far
    currentTossIndex: Int,            // which toss we're on (0-5, or 6 = done)
    currentCoins: List<CoinState>,    // coins for current toss
    isAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            for (i in 0 until 6) {
                val color = when {
                    i < tossResults.size -> {
                        when (tossResults[i]) {
                            LineState.OLD_YANG -> NeonOrange
                            LineState.OLD_YIN -> NeonPurple
                            LineState.YOUNG_YANG -> NeonCyan
                            LineState.YOUNG_YIN -> NeonBlue
                        }
                    }
                    i == currentTossIndex -> NeonCyan
                    else -> TextMuted
                }
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = color, radius = size.minDimension / 2)
                }
            }
        }

        // Current toss animation
        if (currentTossIndex < 6) {
            CoinTossAnimation(
                currentTossIndex = currentTossIndex,
                coinStates = currentCoins,
                isAnimating = isAnimating
            )
        }

        // Completed lines preview
        if (tossResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "━━━ 已得爻象 ━━━",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            for (i in tossResults.indices.reversed()) {
                val state = tossResults[i]
                val bar = when (state) {
                    LineState.YOUNG_YANG -> "━━━━━"
                    LineState.OLD_YANG -> "━━━━━"
                    LineState.YOUNG_YIN -> "━   ━"
                    LineState.OLD_YIN -> "━   ━"
                }
                val changeMark = when (state) {
                    LineState.OLD_YANG -> " ○"
                    LineState.OLD_YIN -> " ×"
                    else -> ""
                }
                val stateColor = when (state) {
                    LineState.OLD_YANG -> NeonOrange
                    LineState.OLD_YIN -> NeonPurple
                    LineState.YOUNG_YANG -> NeonCyan
                    LineState.YOUNG_YIN -> NeonBlue
                }
                Text(
                    text = "  ${i + 1}爻 $bar$changeMark",
                    color = stateColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// ── Helper: convert 3 coin states to a LineState ─────────────────────────

private fun List<CoinState>.toLineState(): LineState {
    val sum = fold(0) { acc, coin -> acc + if (coin.isHeads) 2 else 1 }
    return when (sum) {
        6 -> LineState.OLD_YIN
        5 -> LineState.YOUNG_YANG
        4 -> LineState.YOUNG_YIN
        3 -> LineState.OLD_YANG
        else -> LineState.YOUNG_YANG
    }
}
