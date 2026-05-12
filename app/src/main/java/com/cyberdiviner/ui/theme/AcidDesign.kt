package com.cyberdiviner.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.sin

@Composable
fun AcidBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.sweepGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.15f + 0.05f * sin(phase)),
                    NeonMagenta.copy(alpha = 0.1f + 0.05f * sin(phase + 1f)),
                    NeonGreen.copy(alpha = 0.08f + 0.04f * sin(phase + 2f)),
                    NeonCyan.copy(alpha = 0.15f + 0.05f * sin(phase))
                ),
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
        )
    }
}
