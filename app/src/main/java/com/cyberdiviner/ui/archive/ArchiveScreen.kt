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
import java.time.Instant
import java.time.ZoneId
import com.cyberdiviner.engine.AlmanacEngine

// ── Data model ─────────────────────────────────────────────────────────────

private data class ArchiveEntry(
    val id: Long,
    val ganzhiDate: String,     // 天干地支日名 (e.g. 丙戌日)
    val solarDate: String,      // 公历日期 (2026.05.28)
    val type: String,           // 测算类型
    val title: String,          // 卦名 / 卡名 / 四字批命
    val interpretation: String, // 一句解读
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
                        // Fetch sub-reading data asynchronously
                        val entryState = produceState<ArchiveEntry>(
                            initialValue = reading.toDisplayEntry()
                        ) {
                            val title: String
                            val interp: String
                            when (reading.type) {
                                DivinationType.LIUYAO -> {
                                    val hexName = viewModel.getHexagramName(reading.id)
                                    title = hexName?.takeIf { it.isNotBlank() }
                                        ?: reading.question.takeIf { it.length in 2..6 }
                                        ?: "六爻占卜"
                                    interp = ""
                                }
                                DivinationType.TAROT -> {
                                    val summary = viewModel.getTarotSummary(reading.id)
                                    title = summary?.title
                                        ?: reading.question.takeIf { it.length <= 8 }
                                        ?: "塔罗占卜"
                                    interp = summary?.interpretation ?: ""
                                }
                                DivinationType.VISION -> {
                                    val summary = viewModel.getVisionSummary(reading.id)
                                    title = summary?.first ?: "面相玄机"
                                    interp = summary?.second ?: ""
                                }
                                else -> {
                                    // ORACLE/MUYU: question field is already 4-char summary
                                    val base = reading.toDisplayEntry()
                                    title = base.title
                                    interp = base.interpretation
                                }
                            }
                            value = reading.toDisplayEntry().copy(
                                title = title.ifEmpty { reading.type.displayName },
                                interpretation = interp.ifEmpty { value.interpretation }
                            )
                        }
                        val entry = entryState.value
                        val isExpanded = expandedIndex == index

                        // Fetch full interpretation when expanded
                        val interpState = produceState<String>(initialValue = "") {
                            if (isExpanded) {
                                value = viewModel.getInterpretation(reading.id, reading.type)
                            }
                        }

                        SwipeToDeleteCard(
                            entry = entry,
                            isExpanded = isExpanded,
                            expandedText = interpState.value,
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
    expandedText: String,
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
                        Column {
                            Text(
                                text = entry.ganzhiDate,
                                color = CyberWhite,
                                fontSize = 14.sp,
                                fontFamily = HuiwenFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = entry.solarDate,
                                color = GrayMuted,
                                fontSize = 10.sp,
                                fontFamily = MonoFontFamily,
                                letterSpacing = 1.sp
                            )
                        }
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

                    // ── Expanded details: full interpretation ────────
                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            if (expandedText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(GrayBorder)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "完整解读",
                                    color = AccentRed,
                                    fontSize = 11.sp,
                                    fontFamily = HuiwenFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = expandedText,
                                    color = GrayBody,
                                    fontSize = 13.sp,
                                    fontFamily = WenKaiFontFamily,
                                    lineHeight = 22.sp
                                )
                            } else {
                                Text(
                                    text = "暂无详细解读",
                                    color = GrayMuted,
                                    fontSize = 12.sp,
                                    fontFamily = WenKaiFontFamily
                                )
                            }
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
    val localDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    val gz = AlmanacEngine.calculateGanzhi(localDate)
    val solarDateStr = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(timestamp))

    val titleText: String
    val interpretationText: String

    when (type) {
        DivinationType.LIUYAO -> {
            // resultJson 是纯文本 summary，含 "本卦 (Primary): X 卦名"
            titleText = try {
                val m = Regex("本卦\\s*\\(Primary\\):\\s*\\S+\\s+(\\S+)").find(resultJson)
                val name = m?.groupValues?.get(1) ?: ""
                if (name.isNotEmpty()) name else question.takeIf { it.length <= 6 } ?: "六爻占卜"
            } catch (e: Exception) { "六爻占卜" }
            interpretationText = try {
                val m = Regex("卦象:\\s*(.+)").find(resultJson)
                m?.groupValues?.get(1)?.take(60) ?: ""
            } catch (e: Exception) { "" }
        }
        DivinationType.TAROT -> {
            // resultJson 是 JSON array: [{"card_zh":"愚者","isReversed":"true",...}]
            titleText = try {
                val arr = resultJson.trim()
                if (arr.startsWith("[")) {
                    val cardZh = Regex("\"card_zh\"\\s*:\\s*\"([^\"]+)\"").find(arr)?.groupValues?.get(1) ?: ""
                    val reversed = Regex("\"isReversed\"\\s*:\\s*\"true\"").containsMatchIn(arr)
                    if (cardZh.isNotEmpty()) {
                        cardZh + if (reversed) "逆位" else "正位"
                    } else question.takeIf { it.length <= 6 } ?: "塔罗占卜"
                } else question.takeIf { it.length <= 6 } ?: "塔罗占卜"
            } catch (e: Exception) { "塔罗占卜" }
            interpretationText = try {
                val names = Regex("\"card_zh\"\\s*:\\s*\"([^\"]+)\"").findAll(resultJson)
                    .map { it.groupValues[1] }.toList()
                if (names.size > 1) names.joinToString(" · ") else ""
            } catch (e: Exception) { "" }
        }
        DivinationType.VISION -> {
            titleText = question.ifBlank { "面相分析" }
            interpretationText = try {
                val m = Regex("\"conclusion\"\\s*:\\s*\"([^\"]+)\"").find(resultJson)
                m?.groupValues?.get(1)?.take(60) ?: ""
            } catch (e: Exception) { "" }
        }
        else -> {
            // ORACLE / MUYU: question 字段已是四字批命
            titleText = if (question.length <= 8) question else question.take(4)
            interpretationText = try {
                val m = Regex("\"response\"\\s*:\\s*\"([^\"]{1,80})\"").find(resultJson)
                m?.groupValues?.get(1)?.trim() ?: ""
            } catch (e: Exception) { "" }
        }
    }

    return ArchiveEntry(
        id = id,
        ganzhiDate = "${gz.combined}日",
        solarDate = solarDateStr,
        type = type.displayName,
        title = titleText.ifEmpty { type.displayName },
        interpretation = interpretationText.ifEmpty { notes.ifEmpty { "暂无解读" } },
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
