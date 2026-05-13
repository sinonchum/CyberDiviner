package com.cyberdiviner.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberGray
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted
import com.cyberdiviner.ui.theme.TextPrimary

// ─────────────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────────────

private data class ArchiveEntry(
    val timestamp: String,
    val hash: String,
    val status: String,
    val details: String
)

// ─────────────────────────────────────────────────────
//  Mock archive data
// ─────────────────────────────────────────────────────

private val mockArchive = listOf(
    ArchiveEntry(
        timestamp = "2026.05.13 09:14:07",
        hash = "0x8F9A3C7E1B2D4460",
        status = "周易归档",
        details = "乾卦 · 飞龙在天\n变爻: 九五 → 用九\n排盘参数已归档, 卦象哈希已写入永久链。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.12 23:58:41",
        hash = "0x2E7B1FA9D3C86052",
        status = "塔罗归档",
        details = "THE LOVERS (VI) ↓\n正逆位: 逆位\n牌阵: 凯尔特十字 · 四层映射完成。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.12 18:32:19",
        hash = "0xD4A06B3E91F72C88",
        status = "视界扫描",
        details = "视界深度: LAYER_2\n扫描节点: 0x3F...C2\n灵子共振率: 94.7% · 扫描摘要已存档。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.11 15:07:55",
        hash = "0x7C3E2A8B1D64F091",
        status = "Agent访谈",
        details = "Agent: Hermes-7B\n访谈轮次: 12\n灵魂哈希已生成: 0x7C...91\n人格频谱已锁定。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.10 11:20:03",
        hash = "0x1A5E8D2F6B73C940",
        status = "周易归档",
        details = "坤卦 · 厚德载物\n变爻: 六二 → 六五\n问事业, 大吉, 周期归档完成。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.09 20:45:38",
        hash = "0x9F0B7E4A3C2D1856",
        status = "视界扫描",
        details = "视界深度: LAYER_3\n扫描节点: 0xA1...F7\n灵子共振率: 88.2% · 异常波动已标记。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.08 14:11:22",
        hash = "0x3D6CA0F8E51B2974",
        status = "塔罗归档",
        details = "THE STAR (XVII) ↑\n正逆位: 正位\n牌阵: 三牌展开 · 潜意识/现状/结果。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.07 08:03:16",
        hash = "0xE249D7BA6C85F301",
        status = "Agent访谈",
        details = "Agent: Hermes-7B\n访谈轮次: 8\n灵魂哈希已生成: 0xE2...01\n初始人格校准完成。"
    )
)

// ─────────────────────────────────────────────────────
//  Screen composable
// ─────────────────────────────────────────────────────

@Composable
fun ArchiveScreen(onBack: () -> Unit) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header ──────────────────────────────
            Text(
                text = "[ \u5B58\u6863\u68C0\u7D22 ]",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ARCHIVE RETRIEVAL",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u2500".repeat(44),
                color = CyberGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ── Log stream ──────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(mockArchive.size) { index ->
                    val entry = mockArchive[index]
                    val isExpanded = expandedIndex == index

                    Column {
                        // ── Single-line log row ──────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    expandedIndex = if (isExpanded) null else index
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Bracket icon for status category
                            val bracket = when (entry.status) {
                                "周易归档" -> "\u300A\u5468\u6613\u300B"
                                "塔罗归档" -> "\u300A\u5854\u7F57\u300B"
                                "视界扫描" -> "\u300A\u89C6\u754C\u300B"
                                else       -> "\u300AAgent\u300B"
                            }
                            Text(
                                text = bracket,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            // Main line
                            Text(
                                text = "[ ${entry.timestamp} ] | HASH: ${entry.hash} | \u72B6\u6001: ${entry.status}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // ── Expanded details ─────────
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp, end = 4.dp, bottom = 8.dp)
                            ) {
                                // Electronic stub border box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CyberBlack)
                                        .padding(1.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberBlack)
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            // Stub header
                                            Text(
                                                text = "\u250C${"\u2500".repeat(36)}\u2510",
                                                color = CyberGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "|\u7535\u5B50\u5B58\u6839 / ELECTRONIC STUB \u2003\u2003|",
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "|\u2500${"\u2500".repeat(34)}\u2500|",
                                                color = CyberGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Detail lines
                                            entry.details.lines().forEach { line ->
                                                Text(
                                                    text = "| $line",
                                                    color = TextMuted,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Hash footer
                                            Text(
                                                text = "|\u7B7E\u540D: ${entry.hash}",
                                                color = CyberGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "\u2514${"\u2500".repeat(36)}\u2518",
                                                color = CyberGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Separator
                        if (index < mockArchive.size - 1) {
                            Text(
                                text = "\u2500".repeat(44),
                                color = CyberGray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // ── Footer ──────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "\u2500".repeat(44),
                color = CyberGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            CyberButton(
                text = "[ RETURN ]",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
