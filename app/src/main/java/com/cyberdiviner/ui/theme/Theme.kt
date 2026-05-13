package com.cyberdiviner.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBlack,
    surface = CyberDark,
    surfaceVariant = CyberGray,
    onPrimary = CyberBlack,
    onSecondary = CyberBlack,
    onTertiary = CyberBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun CyberDivinerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = CyberTypography,
        content = content
    )
}
