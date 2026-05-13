package com.cyberdiviner.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
 * Subtle static radial gradient background.
 * Replaces the old animated acid sweep gradient with a clean, professional dark gradient.
 */
@Composable
fun SubtleBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    CyberGray,
                    CyberBlack
                ),
                center = Offset(size.width * 0.5f, size.height * 0.3f),
                radius = size.width * 0.8f
            )
        )
    }
}

// Backward-compat alias
@Composable
fun AcidBackground(modifier: Modifier = Modifier) {
    SubtleBackground(modifier = modifier)
}
