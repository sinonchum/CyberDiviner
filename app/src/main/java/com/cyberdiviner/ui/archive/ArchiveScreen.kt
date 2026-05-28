package com.cyberdiviner.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.*
import com.cyberdiviner.ui.shared.SectionHeader

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
        type = "叩问天机",
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
 * Bridgewater-inspired: red accent on section labels, larger serif titles,
 * thinner borders, more generous padding (24dp internal).
 * Each record is a card with thin white border, featuring:
 * - Top: lunar date + type
 * - Center: AI summary in 汇文明朝体 (large, 32sp)
 * - Body: plain text interpretation
 * - Bottom-right: hash watermark (7sp, dark gray)
 */
@Composable
fun ArchiveScreen(@Suppress("UNUSED_PARAMETER") onBack: () -> Unit) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──
            SectionHeader(title = "因果命簿", subtitle = "CAUSAL LEDGER")
            Spacer(modifier = Modifier.height(48.dp))

            // ── Card stream ─────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────

@Composable
private fun EmptyArchive() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "因果链为空",
            color = GrayCaption,
            fontFamily = HuiwenFontFamily,
            fontSize = 18.sp,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NO CAUSAL RECORDS",
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = MonoFontFamily,
            letterSpacing = 2.sp
        )
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
            .padding(24.dp)
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
                    fontFamily = MonoFontFamily
                )
                Text(
                    text = entry.type,
                    color = AccentRed,
                    fontSize = 11.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Center: AI summary title (汇文明朝体, large) ──
            Text(
                text = entry.title,
                color = GrayTitle,
                fontSize = 32.sp,
                fontFamily = HuiwenFontFamily,
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
                fontFamily = WenKaiFontFamily,
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
                        fontFamily = WenKaiFontFamily,
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
                    fontSize = 7.sp,
                    fontFamily = MonoFontFamily
                )
            }
        }
    }
}
