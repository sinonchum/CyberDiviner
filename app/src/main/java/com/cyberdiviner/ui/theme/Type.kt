package com.cyberdiviner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cyberdiviner.R

// ── Serif for titles (古籍刻本感), Monospace for body (终端逻辑感) ──────────

// JetBrains Mono — SIL OFL 1.1, bundled for deterministic terminal-like body text.
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

// 汇文明朝体 — CC0-1.0 (公共领域), bundled for 明朝体 display/titles.
// Source: github.com/bosswnx/huiwenmincho-improved
val HuiWenMingChao = FontFamily(
    Font(R.font.huiwen_mingchao, FontWeight.Normal),
)

// Convenience aliases used throughout the UI layer.
val MonoBody = JetBrainsMono
val SerifDisplay = HuiWenMingChao

val CyberTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SerifDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = 6.sp,
        color = CyberWhite
    ),
    displayMedium = TextStyle(
        fontFamily = SerifDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = 4.sp,
        color = CyberWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = SerifDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 2.sp,
        color = CyberWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = CyberWhite
    ),
    titleLarge = TextStyle(
        fontFamily = MonoBody,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
        color = CyberWhite
    ),
    titleMedium = TextStyle(
        fontFamily = MonoBody,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = CyberWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = MonoBody,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = CyberWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = MonoBody,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = CyberWhite
    ),
    labelLarge = TextStyle(
        fontFamily = MonoBody,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.sp,
        color = CyberWhite
    ),
    labelSmall = TextStyle(
        fontFamily = MonoBody,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = TextSecondary
    )
)
