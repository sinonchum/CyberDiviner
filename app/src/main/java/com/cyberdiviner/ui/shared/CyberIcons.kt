package com.cyberdiviner.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cyberdiviner.ui.theme.CyberWhite

// ── Canvas Icon Library ─────────────────────────────────────────────────────
// Pure geometric icons drawn on Canvas. No Material Icons. No ASCII.
// Strict B&W. StrokeCap.Square only. StrokeWidth 1–2.dp.

/**
 * I Ching trigram icon — three horizontal lines stacked vertically.
 * Top: yang (solid), Middle: yin (broken), Bottom: yang (solid).
 */
@Composable
fun IChingIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(48.dp)) {
        val strokeWidth = 2.dp.toPx()
        val lineLength = size.width * 0.85f
        val centerGap = 8.dp.toPx()
        val centerX = size.width / 2f
        val topY = size.height * 0.2f
        val midY = size.height * 0.5f
        val botY = size.height * 0.8f

        // Top line — yang (solid)
        drawLine(
            color = CyberWhite,
            start = Offset(centerX - lineLength / 2f, topY),
            end = Offset(centerX + lineLength / 2f, topY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )

        // Middle line — yin (broken with center gap)
        val leftStart = centerX - lineLength / 2f
        val leftEnd = centerX - centerGap / 2f
        val rightStart = centerX + centerGap / 2f
        val rightEnd = centerX + lineLength / 2f

        drawLine(
            color = CyberWhite,
            start = Offset(leftStart, midY),
            end = Offset(leftEnd, midY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
        drawLine(
            color = CyberWhite,
            start = Offset(rightStart, midY),
            end = Offset(rightEnd, midY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )

        // Bottom line — yang (solid)
        drawLine(
            color = CyberWhite,
            start = Offset(centerX - lineLength / 2f, botY),
            end = Offset(centerX + lineLength / 2f, botY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

/**
 * Tarot card icon — rectangular card outline with a diamond inside.
 * Drawn with straight lines (StrokeCap.Square, no rounded corners).
 */
@Composable
fun TarotIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(48.dp)) {
        val strokeWidth = 1.5.dp.toPx()
        val padding = 2.dp.toPx()

        // Card outline (straight-line rectangle, not rounded)
        val cardLeft = padding
        val cardTop = padding
        val cardRight = size.width - padding
        val cardBottom = size.height - padding

        // Top edge
        drawLine(CyberWhite, Offset(cardLeft, cardTop), Offset(cardRight, cardTop), strokeWidth, cap = StrokeCap.Square)
        // Right edge
        drawLine(CyberWhite, Offset(cardRight, cardTop), Offset(cardRight, cardBottom), strokeWidth, cap = StrokeCap.Square)
        // Bottom edge
        drawLine(CyberWhite, Offset(cardRight, cardBottom), Offset(cardLeft, cardBottom), strokeWidth, cap = StrokeCap.Square)
        // Left edge
        drawLine(CyberWhite, Offset(cardLeft, cardBottom), Offset(cardLeft, cardTop), strokeWidth, cap = StrokeCap.Square)

        // Diamond (rotated square) centered inside
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val diamondRadius = size.width * 0.22f

        val top = Offset(centerX, centerY - diamondRadius)
        val right = Offset(centerX + diamondRadius, centerY)
        val bottom = Offset(centerX, centerY + diamondRadius)
        val left = Offset(centerX - diamondRadius, centerY)

        drawLine(CyberWhite, top, right, strokeWidth, cap = StrokeCap.Square)
        drawLine(CyberWhite, right, bottom, strokeWidth, cap = StrokeCap.Square)
        drawLine(CyberWhite, bottom, left, strokeWidth, cap = StrokeCap.Square)
        drawLine(CyberWhite, left, top, strokeWidth, cap = StrokeCap.Square)
    }
}

/**
 * Vision / radar icon — two concentric circles with a horizontal scan line.
 */
@Composable
fun VisionIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(48.dp)) {
        val strokeWidth = 1.5.dp.toPx()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val outerRadius = size.width / 2f - strokeWidth
        val innerRadius = size.width * 0.25f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Square)

        // Outer circle
        drawCircle(
            color = CyberWhite,
            radius = outerRadius,
            center = Offset(centerX, centerY),
            style = stroke
        )

        // Inner circle
        drawCircle(
            color = CyberWhite,
            radius = innerRadius,
            center = Offset(centerX, centerY),
            style = stroke
        )

        // Horizontal scan line across center
        drawLine(
            color = CyberWhite,
            start = Offset(centerX - outerRadius, centerY),
            end = Offset(centerX + outerRadius, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

/**
 * Full hexagram icon — 6 lines drawn bottom to top.
 * @param yaoLines list of 6 booleans: true = yang (solid), false = yin (broken).
 * Each yin line has an 8.dp center gap.
 */
@Composable
fun HexagramIcon(
    yaoLines: List<Boolean>,
    modifier: Modifier = Modifier
) {
    require(yaoLines.size == 6) { "HexagramIcon requires exactly 6 yao lines" }

    Canvas(modifier = modifier.size(48.dp)) {
        val strokeWidth = 2.dp.toPx()
        val lineLength = size.width * 0.85f
        val centerGap = 8.dp.toPx()
        val centerX = size.width / 2f

        // 6 lines from bottom (index 5 in list) to top (index 0 in list)
        // Even spacing: divide vertical space into 7 zones, place lines at 1/7..6/7
        val lineCount = 6
        val verticalMargin = size.height * 0.08f
        val usableHeight = size.height - 2 * verticalMargin
        val spacing = usableHeight / (lineCount - 1)

        for (i in 0 until lineCount) {
            val y = verticalMargin + i * spacing
            val isYang = yaoLines[5 - i]  // index 5 = bottom, index 0 = top

            if (isYang) {
                // Solid line
                drawLine(
                    color = CyberWhite,
                    start = Offset(centerX - lineLength / 2f, y),
                    end = Offset(centerX + lineLength / 2f, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
            } else {
                // Broken line — two segments with centerGap
                val leftStart = centerX - lineLength / 2f
                val leftEnd = centerX - centerGap / 2f
                val rightStart = centerX + centerGap / 2f
                val rightEnd = centerX + lineLength / 2f

                drawLine(
                    color = CyberWhite,
                    start = Offset(leftStart, y),
                    end = Offset(leftEnd, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
                drawLine(
                    color = CyberWhite,
                    start = Offset(rightStart, y),
                    end = Offset(rightEnd, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
            }
        }
    }
}
