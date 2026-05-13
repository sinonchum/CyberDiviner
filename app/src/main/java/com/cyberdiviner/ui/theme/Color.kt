package com.cyberdiviner.ui.theme

import androidx.compose.ui.graphics.Color

// ── Strictly #000000 / #FFFFFF ──────────────────────────────────────────────
val CyberBlack = Color(0xFF000000)
val CyberWhite = Color(0xFFFFFFFF)

// ── Gray hierarchy (visual depth without color) ─────────────────────────────
val GrayTitle = CyberWhite                  // Primary titles — pure white
val GrayBody = Color(0xFFAAAAAA)            // Body / interpretation text
val GrayCaption = Color(0xFF777777)         // Timestamps, status, secondary
val GrayMuted = Color(0xFF555555)           // Hash watermarks, dividers
val GrayBorder = Color(0xFF333333)          // Card borders, separators
val GraySurface = Color(0xFF111111)         // Subtle surface differentiation

// ── Legacy aliases (map to B&W) ───────────────────────────────────────────
val CyberPrimary = CyberWhite
val CyberSecondary = CyberWhite
val CyberTertiary = CyberWhite
val TextPrimary = CyberWhite
val TextSecondary = GrayCaption
val TextMuted = GrayMuted
val BorderColor = GrayBorder
val CyberSurface = GraySurface
val CyberGray = GraySurface
val CyberDark = CyberBlack

// ── Bridgewater accent ─────────────────────────────────────────────────────
val AccentRed = Color(0xFF80241E)     // Bridgewater deep red — section labels, category text

// ── Legacy accent aliases (for existing screens) ───────────────────────────
val AccentVision = CyberWhite
val AccentLiuYao = CyberWhite
val AccentTarot = CyberWhite
val AccentMuyu = CyberWhite
val NeonCyan = CyberWhite
val NeonMagenta = CyberWhite
val NeonGreen = CyberWhite
val NeonPurple = CyberWhite
val NeonOrange = CyberWhite
val NeonBlue = CyberWhite
val NeonYellow = CyberWhite
val AuspiciousGreen = CyberWhite
val InauspiciousRed = CyberWhite
val FortuneGold = CyberWhite
