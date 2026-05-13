package com.cyberdiviner.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BinaryColorScheme = darkColorScheme(
    primary = CyberWhite,
    secondary = CyberWhite,
    tertiary = CyberWhite,
    background = CyberBlack,
    surface = CyberDark,
    surfaceVariant = CyberGray,
    onPrimary = CyberBlack,
    onSecondary = CyberBlack,
    onTertiary = CyberBlack,
    onBackground = CyberWhite,
    onSurface = CyberWhite,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

@Composable
fun CyberDivinerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BinaryColorScheme,
        typography = CyberTypography,
        content = content
    )
}
