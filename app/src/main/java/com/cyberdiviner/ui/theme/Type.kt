package com.cyberdiviner.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cyberdiviner.R

// ── Three-font system: 赛博古迹 (Cyber Relic, unearthed 2026) ──────────────

/**
 * HuiWen MingChao (汇文明朝体) — CC0-1.0
 * 线条最硬，古意最浓。
 * Purpose: 大标题与干支 (titles, GanZhi, hexagram names)
 */
val HuiwenFontFamily = FontFamily(
    Font(R.font.huiwen_mingchao, FontWeight.Normal),
)

/**
 * LXGW WenKai (霞鹜文楷) — SIL OFL 1.1
 * 人文气息，温暖感，舒缓算法的冰冷。
 * Purpose: 判词与对话 (predictions, oracle dialogue)
 */
val WenKaiFontFamily = FontFamily(
    Font(R.font.lxgw_wenkai_regular, FontWeight.Normal),
)

/**
 * JetBrains Mono — SIL OFL 1.1
 * 数据本质，终端逻辑。
 * Purpose: 存根信息与哈希值 (stubs, hashes, metadata, system text)
 */
val MonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

// ── Typography ─────────────────────────────────────────────────────────────

val CyberTypography = Typography(
    // ── 大标题: 汇文明朝体 (titles, GanZhi) ──
    displayLarge = TextStyle(
        fontFamily = HuiwenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        letterSpacing = 8.sp,
        color = CyberWhite
    ),
    displayMedium = TextStyle(
        fontFamily = HuiwenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        letterSpacing = 6.sp,
        color = CyberWhite
    ),
    displaySmall = TextStyle(
        fontFamily = HuiwenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        letterSpacing = 4.sp,
        color = CyberWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = HuiwenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        letterSpacing = 4.sp,
        color = CyberWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = HuiwenFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        letterSpacing = 2.sp,
        color = CyberWhite
    ),

    // ── AI判词与对话: 霞鹜文楷 (oracle, predictions) ──
    titleLarge = TextStyle(
        fontFamily = WenKaiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.sp,
        color = CyberWhite
    ),
    titleMedium = TextStyle(
        fontFamily = WenKaiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = CyberWhite
    ),
    titleSmall = TextStyle(
        fontFamily = WenKaiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = CyberWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = WenKaiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        color = CyberWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = WenKaiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = CyberWhite
    ),
    bodySmall = TextStyle(
        fontFamily = WenKaiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),

    // ── 存根/元数据/系统: JetBrains Mono (data, hashes, system) ──
    labelLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.sp,
        color = CyberWhite
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = TextSecondary
    )
)
