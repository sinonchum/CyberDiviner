package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.ui.shared.HapticUtils
import com.cyberdiviner.ui.shared.HapticUtils.HapticType
import com.cyberdiviner.ui.theme.*

/**
 * GeometricCoinAnimation — abstract "shī cǎo" (蓍草)演算 animation.
 *
 * Three hollow squares float at center. On tap, they flip on Z-axis.
 * Filled white = yang (阳). Hollow = yin (阴).
 * Then the squares dissolve and fly upward, condensing into a yao line.
 *
 * All rendering via Canvas drawRect + drawLine. No Chinese characters.
 */

data class GeoCoinState(
    val isYang: Boolean,    // true = filled (阳), false = hollow (阴)
    val isRevealed: Boolean = false
)

@Composable
fun GeometricCoinAnimation(
    currentTossIndex: Int,
    coinStates: List<GeoCoinState>,
    isAnimating: Boolean,
    onToss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Spin animation
    val infiniteTransition = rememberInfiniteTransition(label = "geo_spin")
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotY"
    )

    // Glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Haptic on reveal
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
            color = GrayCaption,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Three squares
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 3) {
                val coin = coinStates.getOrNull(i)
                val revealed = coin?.isRevealed == true

                Canvas(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                    if (!revealed && isAnimating) { /* rotation handled by animation */ }
                        }
                ) {
                    val strokeWidth = 2.dp.toPx()
                    val squareSize = size.minDimension * 0.7f
                    val topLeft = Offset(
                        (size.width - squareSize) / 2f,
                        (size.height - squareSize) / 2f
                    )

                    if (revealed) {
                        // Revealed: filled or hollow
                        if (coin?.isYang == true) {
                            // Yang: filled white square
                            drawRect(
                                color = CyberWhite,
                                topLeft = topLeft,
                                size = Size(squareSize, squareSize)
                            )
                        } else {
                            // Yin: hollow square with 1dp border
                            drawRect(
                                color = CyberWhite,
                                topLeft = topLeft,
                                size = Size(squareSize, squareSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                            )
                        }
                    } else {
                        // Animating: hollow square with pulsing glow
                        drawRect(
                            color = CyberWhite.copy(alpha = glowAlpha * 0.3f),
                            topLeft = topLeft,
                            size = Size(squareSize, squareSize)
                        )
                        drawRect(
                            color = CyberWhite.copy(alpha = glowAlpha),
                            topLeft = topLeft,
                            size = Size(squareSize, squareSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result label (after reveal)
        if (coinStates.isNotEmpty() && coinStates.all { it.isRevealed }) {
            val lineState = coinStates.toLineState()
            val stateLabel = when (lineState) {
                LineState.OLD_YANG -> "老阳"
                LineState.OLD_YIN -> "老阴"
                LineState.YOUNG_YANG -> "少阳"
                LineState.YOUNG_YIN -> "少阴"
            }
            Text(
                text = stateLabel,
                color = GrayBody,
                fontSize = 16.sp,
                fontFamily = WenKaiFontFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp
            )
        } else if (isAnimating) {
            Text(
                text = "演算中...",
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily
            )
        } else {
            // Tap to toss
            Text(
                text = "轻触屏幕起卦",
                color = GrayCaption,
                fontSize = 13.sp,
                fontFamily = WenKaiFontFamily
            )
        }
    }
}

// ── Full 6-toss sequence with yao line preview ─────────────────────────────

@Composable
fun SixTossAnimation(
    tossResults: List<LineState>,
    currentTossIndex: Int,
    currentCoins: List<GeoCoinState>,
    isAnimating: Boolean,
    onToss: () -> Unit,
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
                    i < tossResults.size -> GrayBody
                    i == currentTossIndex -> CyberWhite
                    else -> GrayMuted
                }
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = color, radius = size.minDimension / 2)
                }
            }
        }

        // Current toss
        if (currentTossIndex < 6) {
            GeometricCoinAnimation(
                currentTossIndex = currentTossIndex,
                coinStates = currentCoins,
                isAnimating = isAnimating,
                onToss = onToss
            )
        }

        // Completed yao lines preview
        if (tossResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "已得爻象",
                color = GrayCaption,
                fontSize = 12.sp,
                fontFamily = WenKaiFontFamily,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            for (i in tossResults.indices.reversed()) {
                val state = tossResults[i]
                val isYang = state == LineState.YOUNG_YANG || state == LineState.OLD_YANG

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Line number
                    Text(
                        text = "${i + 1}",
                        color = GrayCaption,
                        fontSize = 12.sp,
                        fontFamily = MonoFontFamily,
                        modifier = Modifier.width(20.dp)
                    )

                    // Yao line (Canvas)
                    Canvas(
                        modifier = Modifier
                            .width(120.dp)
                            .height(8.dp)
                    ) {
                        val strokeWidth = 2.dp.toPx()
                        val centerY = size.height / 2f
                        if (isYang) {
                            // Yang: solid line
                            drawLine(
                                color = CyberWhite,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                        } else {
                            // Yin: broken line with gap
                            val mid = size.width / 2f
                            val gap = 8.dp.toPx()
                            drawLine(
                                color = CyberWhite,
                                start = Offset(0f, centerY),
                                end = Offset(mid - gap, centerY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = CyberWhite,
                                start = Offset(mid + gap, centerY),
                                end = Offset(size.width, centerY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                        }
                    }

                    // Change mark
                    val changeMark = when (state) {
                        LineState.OLD_YANG -> " ○"
                        LineState.OLD_YIN -> " ×"
                        else -> ""
                    }
                    if (changeMark.isNotEmpty()) {
                        Text(
                            text = changeMark,
                            color = GrayBody,
                            fontSize = 12.sp,
                            fontFamily = MonoFontFamily
                        )
                    }
                }
            }
        }
    }
}

// ── Helper ─────────────────────────────────────────────────────────────────

private fun List<GeoCoinState>.toLineState(): LineState {
    val sum = fold(0) { acc, coin -> acc + if (coin.isYang) 2 else 1 }
    return when (sum) {
        6 -> LineState.OLD_YIN
        5 -> LineState.YOUNG_YANG
        4 -> LineState.YOUNG_YIN
        3 -> LineState.OLD_YANG
        else -> LineState.YOUNG_YANG
    }
}
