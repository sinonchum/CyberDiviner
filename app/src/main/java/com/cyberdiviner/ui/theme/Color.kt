package com.cyberdiviner.ui.theme

import androidx.compose.ui.graphics.Color

// ── Professional cyberpunk palette ────────────────────────────────────────
// Primary: muted teal/cyan
val CyberPrimary = Color(0xFF5EEAD4)
// Secondary: muted lavender
val CyberSecondary = Color(0xFFA78BFA)
// Tertiary: warm amber
val CyberTertiary = Color(0xFFF59E0B)

// ── Background layers (softened dark tones) ───────────────────────────────
val CyberBlack = Color(0xFF0D0D12)
val CyberDark = Color(0xFF15151E)
val CyberGray = Color(0xFF1E1E2E)
val CyberSurface = Color(0xFF1A2332)

// ── Feature accent colors (subtle, not neon) ──────────────────────────────
val AccentLiuYao = Color(0xFF5EEAD4)   // teal – hexagram divination
val AccentTarot = Color(0xFFA78BFA)    // lavender – tarot reading
val AccentVision = Color(0xFF6EE7B7)   // mint – face scanning
val AccentMuyu = Color(0xFFF59E0B)     // amber – wooden fish

// ── Text ──────────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFF8888AA)
val TextMuted = Color(0xFF555577)

// ── Fortune specific ──────────────────────────────────────────────────────
val AuspiciousGreen = Color(0xFF34D399)   // muted green, not screaming
val InauspiciousRed = Color(0xFFEF4444)   // clean red
val FortuneGold = Color(0xFFFBBF24)       // warm gold

// ── Backward-compat aliases (legacy neon names → professional palette) ────
val NeonCyan = CyberPrimary
val NeonMagenta = CyberSecondary
val NeonGreen = CyberTertiary
