package com.cyberdiviner.ui.vision

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import android.graphics.PointF
import com.cyberdiviner.ui.theme.*
import kotlin.math.*

/**
 * Cyberpunk AR overlay drawn on top of the camera view.
 * Renders a wireframe face mesh, floating HUD data, scan-line grid,
 * and corner brackets — all with pulsing neon glow effects.
 */
@Composable
fun AROverlay(
    modifier: Modifier = Modifier,
    scanProgress: Float = 0f,       // 0..1 overall scan progress
    isScanning: Boolean = true,
    detectedPoints: List<PointF> = emptyList(), // facial landmark coords normalised 0..1
) {
    // Infinite pulse animation for glow lines
    val infiniteTransition = rememberInfiniteTransition(label = "ar_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val gridScroll by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ), label = "grid_scroll"
    )

    val cyanColor = GrayBody
    val magentaColor = GrayMuted
    val greenColor = GrayBody
    val purpleColor = GrayMuted

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val strokeAlpha = pulseAlpha

        // ── Corner brackets ──
        val bracketLen = 40.dp.toPx()
        val bracketStroke = Stroke(3.dp.toPx(), cap = StrokeCap.Square)
        val bracketInset = 32.dp.toPx()
        val corners = listOf(
            // top-left
            listOf(
                Offset(bracketInset, bracketInset),
                Offset(bracketInset + bracketLen, bracketInset),
                Offset(bracketInset, bracketInset),
                Offset(bracketInset, bracketInset + bracketLen),
            ),
            // top-right
            listOf(
                Offset(w - bracketInset, bracketInset),
                Offset(w - bracketInset - bracketLen, bracketInset),
                Offset(w - bracketInset, bracketInset),
                Offset(w - bracketInset, bracketInset + bracketLen),
            ),
            // bottom-left
            listOf(
                Offset(bracketInset, h - bracketInset),
                Offset(bracketInset + bracketLen, h - bracketInset),
                Offset(bracketInset, h - bracketInset),
                Offset(bracketInset, h - bracketInset - bracketLen),
            ),
            // bottom-right
            listOf(
                Offset(w - bracketInset, h - bracketInset),
                Offset(w - bracketInset - bracketLen, h - bracketInset),
                Offset(w - bracketInset, h - bracketInset),
                Offset(w - bracketInset, h - bracketInset - bracketLen),
            ),
        )
        corners.forEach { pts ->
            drawLine(cyanColor, pts[0], pts[1], strokeWidth = bracketStroke.width, cap = bracketStroke.cap, alpha = strokeAlpha)
            drawLine(cyanColor, pts[2], pts[3], strokeWidth = bracketStroke.width, cap = bracketStroke.cap, alpha = strokeAlpha)
        }

        // ── Horizontal scan lines ──
        val lineColor = cyanColor.copy(alpha = 0.08f * pulseAlpha)
        val lineSpacing = 12.dp.toPx()
        var y = 0f
        while (y <= h) {
            drawLine(lineColor, Offset(0f, y), Offset(w, y), 1f)
            y += lineSpacing
        }

        // ── Vertical scrolling grid ──
        val gridColor = magentaColor.copy(alpha = 0.05f * pulseAlpha)
        val gridSpacing = 60.dp.toPx()
        val gridOffset = gridScroll * gridSpacing
        var x = -gridSpacing
        while (x <= w + gridSpacing) {
            drawLine(gridColor, Offset(x + gridOffset, 0f), Offset(x + gridOffset, h), 1f)
            x += gridSpacing
        }
        var gy = -gridSpacing
        while (gy <= h + gridSpacing) {
            drawLine(gridColor, Offset(0f, gy + gridOffset), Offset(w, gy + gridOffset), 1f)
            gy += gridSpacing
        }

        // ── Face wireframe mesh ──
        if (detectedPoints.isNotEmpty()) {
            val meshColor = greenColor.copy(alpha = 0.6f * pulseAlpha)
            val meshStroke = Stroke(1.5.dp.toPx())
            // Connect nearby points
            for (i in detectedPoints.indices) {
                for (j in i + 1 until detectedPoints.size) {
                    val a = detectedPoints[i]
                    val b = detectedPoints[j]
                    val dist = sqrt((a.x - b.x).pow(2) + (a.y - b.y).pow(2))
                    if (dist < 0.12f) {
                        drawLine(
                            meshColor,
                            Offset(a.x * w, a.y * h),
                            Offset(b.x * w, b.y * h),
                            strokeWidth = 1f,
                            cap = StrokeCap.Square
                        )
                    }
                }
            }
            // Draw nodes at each point
            detectedPoints.forEach { pt ->
                drawCircle(
                    meshColor, radius = 3.dp.toPx(),
                    center = Offset(pt.x * w, pt.y * h)
                )
            }
        }

        // ── HUD data bars (left side) ──
        val barColor = purpleColor.copy(alpha = 0.5f * pulseAlpha)
        val barX = 20.dp.toPx()
        val barTop = h * 0.15f
        val barWidth = 4.dp.toPx()
        val barMaxHeight = h * 0.3f
        val barSpacing = 10.dp.toPx()
        for (i in 0..7) {
            val barH = barMaxHeight * (0.3f + 0.7f * abs(sin((i * 0.7f + gridScroll * PI * 2).toFloat())))
            drawRect(
                barColor,
                Offset(barX + i * (barWidth + barSpacing), barTop + barMaxHeight - barH),
                Size(barWidth, barH)
            )
        }

        // ── Scan progress beam ──
        if (isScanning) {
            val beamY = h * (1f - scanProgress)
            val beamGrad = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    cyanColor.copy(alpha = 0.7f),
                    Color.Transparent
                ),
                startX = 0f, endX = w
            )
            drawLine(
                brush = beamGrad,
                start = Offset(0f, beamY),
                end = Offset(w, beamY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Square
            )
            // Glow below beam
            val glowGrad = Brush.verticalGradient(
                colors = listOf(cyanColor.copy(alpha = 0.15f), Color.Transparent),
                startY = beamY, endY = beamY + 60.dp.toPx()
            )
            drawRect(glowGrad, Offset(0f, beamY), Size(w, 60.dp.toPx()))
        }
    }
}
