package com.cyberdiviner.ui.theme

import androidx.compose.ui.graphics.Color

// ── Strictly #000000 / #FFFFFF ──────────────────────────────────────────────
val CyberBlack = Color(0xFF000000)
val CyberWhite = Color(0xFFFFFFFF)

// ── Gray hierarchy — 提亮版本，保证黑色背景上清晰可读 ─────────────────────
val GrayTitle = CyberWhite                  // 标题 — 纯白
val GrayBody = Color(0xFFD0D0D0)            // 正文 — 浅灰（原0xFFAAAAAA）
val GrayCaption = Color(0xFF999999)         // 时间戳/状态 — 中灰（原0xFF777777）
val GrayMuted = Color(0xFF777777)           // 水印/次要信息 — 中灰（原0xFF555555）
val GrayBorder = Color(0xFF555555)          // 边框 — 可见灰（原0xFF333333）
val GraySurface = Color(0xFF1A1A1A)         // 表面 — 微亮（原0xFF111111）

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

// ── Bridgewater accent — 提亮红色 ─────────────────────────────────────────
val AccentRed = Color(0xFFCC3333)           // 提亮红（原0xFF80241E太暗）

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
