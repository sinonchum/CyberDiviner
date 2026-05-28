package com.cyberdiviner.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import com.cyberdiviner.ui.theme.*
import com.cyberdiviner.ui.shared.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// ── Data model ─────────────────────────────────────────────────────────────

private data class ArchiveEntry(
    val id: Long,
    val lunarDate: String,      // 日期
    val type: String,           // 测算类型
    val title: String,          // AI 四字批命
    val interpretation: String, // 白话文解读
    val hash: String            // 防伪哈希
)

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun ArchiveScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val readings by viewModel.readings.collectAsState()
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
            if (readings.isEmpty()) {
                EmptyArchive()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(
                        items = readings,
                        key = { _, reading -> reading.id }
                    ) { index, reading ->
                        val entry = reading.toDisplayEntry()
                        val isExpanded = expandedIndex == index

                        SwipeToDeleteCard(
                            entry = entry,
                            isExpanded = isExpanded,
                            onClick = {
                                expandedIndex = if (isExpanded) null else index
                            },
                            onDelete = {
                                viewModel.deleteReading(reading)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Swipe-to-delete card wrapper ──────────────────────────────────────────

@Composable
private fun SwipeToDeleteCard(
    entry: ArchiveEntry,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isRemoving by remember { mutableStateOf(false) }

    val swipeThreshold = -150f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < swipeThreshold) {
                            isRemoving = true
                            onDelete()
                        } else {
                            offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceAtMost(0f) // only swipe left
                    }
                )
            }
    ) {
        // Red delete hint behind the card
        if (offsetX < -20f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(AccentRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "删除",
                    color = AccentRed,
                    fontSize = 14.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }

        // The actual card
        AnimatedVisibility(
            visible = !isRemoving,
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .offset(x = offsetX.dp)
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
                    // ── Top: date + type ──────────────
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

                    // ── Center: 四字批命 ──────────────
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

                    // ── Body: interpretation ────────────
                    Text(
                        text = entry.interpretation,
                        color = GrayBody,
                        fontSize = 14.sp,
                        fontFamily = WenKaiFontFamily,
                        lineHeight = 24.sp
                    )

                    // ── Expanded details ────────────────
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

                    // ── Bottom-right: hash watermark ────
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
    }
}

// ── Empty state ───────────────────────────────────────────────────────

private fun DivinationReading.toDisplayEntry(): ArchiveEntry {
    val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(timestamp))

    // 从 resultJson 提取四字批命
    val summary = try {
        val json = resultJson
        val summaryStart = json.indexOf("\"summary\": \"") + 12
        if (summaryStart > 12) {
            val summaryEnd = json.indexOf("\"", summaryStart).coerceAtMost(summaryStart + 20)
            json.substring(summaryStart, summaryEnd).trim()
        } else ""
    } catch (e: Exception) { "" }

    // 从 resultJson 提取解读
    val briefResult = try {
        val json = resultJson
        val responseStart = json.indexOf("\"response\": \"") + 13
        if (responseStart > 13) {
            val responseEnd = json.indexOf("\"", responseStart).coerceAtMost(responseStart + 80)
            json.substring(responseStart, responseEnd).trim()
        } else ""
    } catch (e: Exception) { "" }

    // 优先用 summary（四字批命），fallback 到 question 字段（Oracle已存summary）
    val titleText = summary.ifEmpty {
        // question字段在Oracle流程中已经存的是四字批命
        if (question.length <= 6) question else question.take(4)
    }.ifEmpty { type.displayName }

    return ArchiveEntry(
        id = id,
        lunarDate = dateStr,
        type = type.displayName,
        title = titleText,
        interpretation = briefResult.ifEmpty { notes.ifEmpty { "暂无解读" } },
        hash = String.format("0x%08X", id.hashCode())
    )
}

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
