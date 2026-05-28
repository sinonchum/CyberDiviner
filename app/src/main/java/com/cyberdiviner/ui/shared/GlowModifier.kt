package com.cyberdiviner.ui.shared

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cyberdiviner.ui.theme.CyberWhite

/**
 * Subtle border glow modifier using the new professional palette.
 * Draws a soft outer glow ring around the composable.
 */
fun Modifier.subtleGlow(
    color: Color = CyberWhite,
    glowWidth: Float = 1.5f
): Modifier = this.drawBehind {
    drawCircle(
        color = color.copy(alpha = 0.2f),
        radius = size.width * 0.48f,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = glowWidth.dp.toPx())
    )
}
