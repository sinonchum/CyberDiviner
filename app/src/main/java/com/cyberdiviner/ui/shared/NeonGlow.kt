package com.cyberdiviner.ui.shared

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cyberdiviner.ui.theme.NeonCyan

fun Modifier.neonGlow(
    color: Color = NeonCyan,
    radius: Float = 8f
): Modifier = this.drawBehind {
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = radius * density,
        style = Stroke(width = 2.dp.toPx())
    )
}
