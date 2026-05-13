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
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted

// ── Data model ─────────────────────────────────────────

private data class ArchiveEntry(
    val timestamp: String,
    val hash: String,
    val status: String,
    val details: String
)

// ── Mock archive data ──────────────────────────────────

private val mockArchive = listOf(
    ArchiveEntry(
        timestamp = "2026.05.13 14:02",
        hash = "0x8F9A3C7E1B2D4460",
        status = "周易归档",
        details = "乾卦 · 飞龙在天\n变爻: 九五 → 用九\n排盘参数已归档, 卦象哈希已写入永久链。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.12 23:58",
        hash = "0x2E7B1FA9D3C86052",
        status = "塔罗归档",
        details = "THE LOVERS (VI) ↓\n正逆位: 逆位\n牌阵: 凯尔特十字 · 四层映射完成。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.12 18:32",
        hash = "0xD4A06B3E91F72C88",
        status = "视界扫描",
        details = "视界深度: LAYER_2\n扫描节点: 0x3F...C2\n灵子共振率: 94.7% · 扫描摘要已存档。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.11 15:07",
        hash = "0x7C3E2A8B1D64F091",
        status = "Agent访谈",
        details = "Agent: Hermes-7B\n访谈轮次: 12\n灵魂哈希已生成: 0x7C...91\n人格频谱已锁定。"
    ),
    ArchiveEntry(
        timestamp = "2026.05.10 11:20",
        hash = "0x1A5E8D2F6B73C940",
        status = "周易归档",
        details = "坤卦 · 厚德载物\n变爻: 六二 → 六五\n问事业, 大吉, 周期归档完成。"
    )
)

// ── Screen ─────────────────────────────────────────────

/**
 * ArchiveScreen -- Terminal log style.
 *
 * Single-line log entries with JetBrainsMono. Click to expand.
 * Reverse color on click. Format: [ timestamp ] HASH:0x8F9... | 周易归档
 */
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
                text = "[ 因果命簿 ]",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "─".repeat(44),
                color = TextMuted,
                fontSize = 8.sp,
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
                                .background(if (isExpanded) CyberWhite else CyberBlack)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    expandedIndex = if (isExpanded) null else index
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "[ ${entry.timestamp} ] HASH:${entry.hash} | ${entry.status}",
                                color = if (isExpanded) CyberBlack else CyberWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // ── Expanded details ─────────
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 4.dp, bottom = 8.dp)
                            ) {
                                entry.details.lines().forEach { line ->
                                    Text(
                                        text = "  $line",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "  SIGNATURE: ${entry.hash}",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Separator
                        if (index < mockArchive.size - 1) {
                            Text(
                                text = "─".repeat(44),
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // ── Footer ──────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            CyberButton(
                text = "[ 返回 ]",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
