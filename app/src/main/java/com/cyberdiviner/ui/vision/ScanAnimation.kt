package com.cyberdiviner.ui.vision

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.*
import kotlin.math.*

/**
 * Animated scan-ring and status HUD displayed while the camera analyses a face.
 *
 * @param progress 0f → 1f — drives the sweep angle + fill arc.
 * @param phase     human-readable phase label, e.g. "DETECTING FEATURES".
 * @param statusLines optional extra HUD lines drawn inside the ring.
 */
@Composable
fun ScanAnimation(
    progress: Float,           // 0..1
    phase: String = "SCANNING",
    statusLines: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")

    // rotating outer ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing)
        ), label = "rotation"
    )

    // pulse for inner elements
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // ticker dots (animateFloat, cast to int since animateInt doesn't exist)
    val dotCountFloat by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing)
        ), label = "dots"
    )
    val dotCount = dotCountFloat.toInt()

    val cyanColor = GrayBody
    val magentaColor = GrayMuted
    val greenColor = GrayBody
    val goldColor = AccentRed

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = min(cx, cy)
            val sweepAngle = progress * 360f

            // ── Outer rotating ring ──
            rotate(rotation, Offset(cx, cy)) {
                drawArc(
                    color = cyanColor.copy(alpha = 0.3f * pulse),
                    startAngle = 0f, sweepAngle = 270f, useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(4.dp.toPx(), cap = StrokeCap.Square)
                )
                // Bright leading segment
                drawArc(
                    color = cyanColor.copy(alpha = 0.9f * pulse),
                    startAngle = 0f, sweepAngle = 40f, useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(4.dp.toPx(), cap = StrokeCap.Square)
                )
            }

            // ── Inner progress arc ──
            val innerR = radius * 0.75f
            drawArc(
                color = magentaColor.copy(alpha = 0.15f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(cx - innerR, cy - innerR),
                size = Size(innerR * 2, innerR * 2),
                style = Stroke(3.dp.toPx())
            )
            drawArc(
                color = magentaColor.copy(alpha = 0.85f),
                startAngle = -90f, sweepAngle = sweepAngle, useCenter = false,
                topLeft = Offset(cx - innerR, cy - innerR),
                size = Size(innerR * 2, innerR * 2),
                style = Stroke(3.dp.toPx(), cap = StrokeCap.Square)
            )

            // ── Crosshair ──
            val chLen = 14.dp.toPx()
            val chGap = 8.dp.toPx()
            val chColor = cyanColor.copy(alpha = 0.5f * pulse)
            drawLine(chColor, Offset(cx - chGap - chLen, cy), Offset(cx - chGap, cy), strokeWidth = 1.5.dp.toPx())
            drawLine(chColor, Offset(cx + chGap, cy), Offset(cx + chGap + chLen, cy), strokeWidth = 1.5.dp.toPx())
            drawLine(chColor, Offset(cx, cy - chGap - chLen), Offset(cx, cy - chGap), strokeWidth = 1.5.dp.toPx())
            drawLine(chColor, Offset(cx, cy + chGap), Offset(cx, cy + chGap + chLen), strokeWidth = 1.5.dp.toPx())

            // ── Tick marks around ring ──
            val tickR = radius * 0.92f
            val tickLen = 6.dp.toPx()
            for (i in 0..35) {
                val angle = Math.toRadians((i * 10).toDouble())
                val isMajor = i % 5 == 0
                val len = if (isMajor) tickLen * 1.8f else tickLen
                val color = if (isMajor) greenColor.copy(alpha = 0.7f) else greenColor.copy(alpha = 0.3f)
                drawLine(
                    color,
                    Offset(cx + tickR * cos(angle).toFloat(), cy + tickR * sin(angle).toFloat()),
                    Offset(cx + (tickR - len) * cos(angle).toFloat(), cy + (tickR - len) * sin(angle).toFloat()),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // ── Center percentage ──
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = cyanColor.hashCode()
                    textSize = 28.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                drawText("${(progress * 100).toInt()}%", cx, cy + 10.dp.toPx(), paint)
            }
        }

        // ── Phase label below ring ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) {
            Text(
                text = phase + ".".repeat(dotCount % 4),
                color = AccentRed.copy(alpha = pulse),
                fontSize = 13.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 3.sp
            )
            if (statusLines.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                statusLines.forEach { line ->
                    Text(
                        text = line,
                        color = GrayBody,
                        fontSize = 10.sp,
                        fontFamily = MonoFontFamily,
                    )
                }
            }
        }
    }
}
