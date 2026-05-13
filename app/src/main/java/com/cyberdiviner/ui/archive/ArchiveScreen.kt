package com.cyberdiviner.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.theme.*

// ── Data model ─────────────────────────────────────────────────────────────

private data class ArchiveEntry(
    val lunarDate: String,      // 农历日期
    val type: String,           // 测算类型
    val title: String,          // AI 四字/六字雅称
    val interpretation: String, // 白话文解读
    val hash: String            // 防伪哈希
)

// ── Mock data ──────────────────────────────────────────────────────────────

private val mockArchive = listOf(
    ArchiveEntry(
        lunarDate = "丙戌日",
        type = "周易起卦",
        title = "泽水困卦",
        interpretation = "当前局势阻滞，切忌盲目投资，需等待时机。",
        hash = "0x8F9A3C7E1B2D4460"
    ),
    ArchiveEntry(
        lunarDate = "乙酉日",
        type = "赛博塔罗",
        title = "愚者逆位",
        interpretation = "冲动行事将导致失控，应回归理性审慎的判断。",
        hash = "0x2E7B1FA9D3C86052"
    ),
    ArchiveEntry(
        lunarDate = "甲申日",
        type = "视界扫描",
        title = "破局之象",
        interpretation = "灵子共振率达标，深层结构已解析，可推进下一阶段。",
        hash = "0xD4A06B3E91F72C88"
    ),
    ArchiveEntry(
        lunarDate = "癸未日",
        type = "Agent访谈",
        title = "灵魂校准",
        interpretation = "人格频谱已锁定，因果变量组已写入永久链。",
        hash = "0x7C3E2A8B1D64F091"
    ),
    ArchiveEntry(
        lunarDate = "壬午日",
        type = "周易起卦",
        title = "地天泰卦",
        interpretation = "天地交泰，万事亨通。宜积极行动，把握当前良机。",
        hash = "0x1A5E8D2F6B73C940"
    )
)

// ── Screen ─────────────────────────────────────────────────────────────────

/**
 * ArchiveScreen -- Elegant card stream layout.
 *
 * Each record is a card with thin white border, featuring:
 * - Top: lunar date + type
 * - Center: AI summary in 汇文明朝体 (large)
 * - Body: plain text interpretation
 * - Bottom-right: hash watermark (8sp, dark gray)
 */
@Composable
fun ArchiveScreen(onBack: () -> Unit) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──────────────────────────────
            Text(
                text = "因果命簿",
                color = GrayTitle,
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CAUSAL LEDGER",
                color = GrayCaption,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Card stream ─────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockArchive.size) { index ->
                    val entry = mockArchive[index]
                    val isExpanded = expandedIndex == index

                    ArchiveCard(
                        entry = entry,
                        isExpanded = isExpanded,
                        onClick = {
                            expandedIndex = if (isExpanded) null else index
                        }
                    )
                }
            }

            // ── Footer ──────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            CyberButton(
                text = "[ 返回 ]",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Card composable ────────────────────────────────────────────────────────

@Composable
private fun ArchiveCard(
    entry: ArchiveEntry,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder)
            .background(if (isExpanded) GraySurface else CyberBlack)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(20.dp)
    ) {
        Column {
            // ── Top: lunar date + type ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.lunarDate,
                    color = GrayCaption,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = entry.type,
                    color = GrayCaption,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Center: AI summary title (汇文明朝体, large) ──
            Text(
                text = entry.title,
                color = GrayTitle,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Body: interpretation ────────────────
            Text(
                text = entry.interpretation,
                color = GrayBody,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp
            )

            // ── Expanded details ────────────────────
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "完整解读已归档。后续算法将基于此哈希运行。",
                        color = GrayCaption,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 20.sp
                    )
                }
            }

            // ── Bottom-right: hash watermark ────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = entry.hash,
                    color = GrayMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
