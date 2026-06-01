package com.cyberdiviner.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.cyberdiviner.engine.FortuneEngine
import com.cyberdiviner.ui.localization.CyberCopy
import com.cyberdiviner.ui.localization.LocalAppLanguage

// ── Data model ─────────────────────────────────────────────────────────────

internal data class ArchiveEntry(
    val id: Long,
    val ganzhiDate: String,     // Ganzhi day name (e.g. 丙戌日)
    val solarDate: String,      // Solar date (2026.05.28)
    val type: String,           // Divination type
    val title: String,          // Hexagram/card name or 4-char fortune
    val interpretation: String, // One-line interpretation
    val hash: String            // Anti-tamper hash
)

private data class SharePreview(
    val entry: ArchiveEntry,
    val bitmap: Bitmap
)

/** Clean garbled encoding artifacts from offline LLM output (byte-fallback tokenizer issue) */
private fun cleanGarbledEncoding(text: String): String {
    return text.replace(Regex("[\\u0080-\\u00ff\\u0100-\\u024f\\u0250-\\u02af\\u2000-\\u206f\\u2070-\\u209f\\u20a0-\\u20cf\\u2100-\\u214f]{1,8}(?=[\\u4e00-\\u9fff\\u3400-\\u4dbf])"), "")
}

/** Extract the first sentence from a multi-sentence text, capped at 50 chars */
private fun firstSentence(text: String): String {
    val cleaned = cleanGarbledEncoding(text)
    if (cleaned.isBlank()) return ""
    // Find first sentence-ending punctuation
    val end = cleaned.indexOfFirst { it == '。' || it == '！' || it == '？' || it == '.' || it == '!' || it == '?' }
    val sentence = if (end > 0) cleaned.substring(0, end + 1) else cleaned.take(50)
    // Cap at 50 chars
    return if (sentence.length > 50) sentence.take(47) + "..." else sentence
}

/** Strip known LLM template section headers from liuyao interpretation text */
private fun cleanLiuyaoInterpretation(text: String): String {
    if (text.isBlank()) return text
    val headerPatterns = listOf(
        "卦象识别与起卦", "卦象识别", "本卦意义分析", "变爻解读",
        "五行生克动态", "断卦——最终指引", "断卦",
        "上卦解读", "下卦解读", "综合分析"
    )
    var cleaned = text.trim()
    for (header in headerPatterns) {
        // Remove "N. header" or "N、header" patterns (with optional trailing punctuation/spaces)
        cleaned = cleaned.replace(Regex("\\d+[.、．]\\s*${Regex.escape(header)}[^\\n]*\\n?"), "")
        // Remove bare header lines
        cleaned = cleaned.replace(Regex("(?m)^\\s*${Regex.escape(header)}\\s*$"), "")
    }
    // Remove leading separator lines (━━━, ───, ===)
    cleaned = cleaned.replace(Regex("(?m)^\\s*[━─═=]{3,}\\s*$"), "")
    // Remove blank lines at the start
    cleaned = cleaned.trimStart('\n', '\r', ' ')
    return cleaned
}

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun ArchiveScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val readings by viewModel.readings.collectAsState()
    val learningStats by viewModel.learningStats.collectAsState()
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    var sharePreview by remember { mutableStateOf<SharePreview?>(null) }
    val lang = LocalAppLanguage.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──
            SectionHeader(title = CyberCopy.archiveTitle(lang), subtitle = CyberCopy.archiveSubtitle(lang))
            Spacer(modifier = Modifier.height(24.dp))

            // ── Learning review card ──
            val stats = learningStats
            if (stats != null && stats.totalXp > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentRed)
                        .clickable { /* Navigate to learn tab */ }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = CyberCopy.archiveLearningReview(lang),
                            color = AccentRed,
                            fontSize = 11.sp,
                            fontFamily = HuiwenFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${stats.totalXp} XP",
                                    color = CyberWhite,
                                    fontSize = 18.sp,
                                    fontFamily = MonoFontFamily
                                )
                                Text(
                                    text = stats.title,
                                    color = GrayBody,
                                    fontSize = 13.sp,
                                    fontFamily = HuiwenFontFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CyberCopy.archiveDayStreak(lang, stats.currentStreak),
                                    color = CyberWhite,
                                    fontSize = 14.sp,
                                    fontFamily = MonoFontFamily
                                )
                                Text(
                                    text = CyberCopy.archiveBestStreak(lang, stats.bestStreak),
                                    color = GrayCaption,
                                    fontSize = 12.sp,
                                    fontFamily = MonoFontFamily
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Card stream ─────────────────────────
            if (readings.isEmpty()) {
                EmptyArchive(lang)
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
                                    val summary = viewModel.getLiuyaoSummary(reading.id)
                                    title = summary?.title
                                        ?: reading.question.takeIf { it.length in 2..6 }
                                        ?: "I Ching Reading"
                                    interp = summary?.interpretation ?: "Hexagram cast, contemplate the omens"
                                }
                                DivinationType.TAROT -> {
                                    val summary = viewModel.getTarotSummary(reading.id)
                                    title = summary?.title
                                        ?: reading.question.takeIf { it.length <= 8 }
                                        ?: "Tarot Reading"
                                    interp = firstSentence(summary?.interpretation ?: "")
                                }
                                DivinationType.VISION -> {
                                    val summary = viewModel.getVisionSummary(reading.id)
                                    title = summary?.first ?: "Face Reading"
                                    interp = firstSentence(summary?.second ?: "")
                                }
                                else -> {
                                    // ORACLE/MUYU: question field is already 4-char summary
                                    val base = reading.toDisplayEntry()
                                    title = base.title
                                    interp = firstSentence(base.interpretation)
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
                        val interpState = produceState<String>(initialValue = "", key1 = isExpanded) {
                            if (isExpanded) {
                                value = viewModel.getInterpretation(reading.id, reading.type)
                            }
                        }

                        SwipeToDeleteCard(lang = lang,
                            entry = entry,
                            isExpanded = isExpanded,
                            expandedText = interpState.value,
                            onClick = {
                                expandedIndex = if (isExpanded) null else index
                            },
                            onShare = {
                                sharePreview = SharePreview(
                                    entry = entry,
                                    bitmap = ArchiveShareGenerator.generate(context, entry)
                                )
                            },
                            onDelete = {
                                viewModel.deleteReading(reading)
                            }
                        )
                    }
                }
            }
        }

        sharePreview?.let { preview ->
            AlertDialog(
                onDismissRequest = { sharePreview = null },
                containerColor = CyberBlack,
                titleContentColor = CyberWhite,
                textContentColor = GrayBody,
                title = {
                    Text(
                        text = CyberCopy.archiveCausalCard(lang),
                        fontFamily = HuiwenFontFamily,
                        color = CyberWhite
                    )
                },
                text = {
                    Image(
                        bitmap = preview.bitmap.asImageBitmap(),
                        contentDescription = "Causal Card Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(preview.bitmap.width.toFloat() / preview.bitmap.height.toFloat())
                            .border(1.dp, GrayBorder)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        ArchiveShareGenerator.share(context, preview.bitmap, preview.entry)
                        sharePreview = null
                    }) {
                        Text(CyberCopy.archiveSend(lang), color = AccentRed, fontFamily = HuiwenFontFamily)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            val saved = ArchiveShareGenerator.saveToGallery(context, preview.bitmap)
                            Toast.makeText(
                                context,
                                if (saved) CyberCopy.archiveSavedGallery(lang) else CyberCopy.archiveSaveFailed(lang),
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Text(CyberCopy.archiveSave(lang), color = CyberWhite, fontFamily = HuiwenFontFamily)
                        }
                        TextButton(onClick = { sharePreview = null }) {
                            Text(CyberCopy.archiveCancel(lang), color = GrayCaption, fontFamily = HuiwenFontFamily)
                        }
                    }
                }
            )
        }
    }
}

// ── Swipe-to-delete card wrapper ──────────────────────────────────────────

@Composable
private fun SwipeToDeleteCard(
    lang: com.cyberdiviner.ui.settings.AppLanguage,
    entry: ArchiveEntry,
    isExpanded: Boolean,
    expandedText: String,
    onClick: () -> Unit,
    onShare: () -> Unit,
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
                    text = CyberCopy.archiveDelete(lang),
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

                    // ── Center: 4-char fortune summary ──────────────
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
                                    text = CyberCopy.archiveFullInterp(lang),
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
                                    text = CyberCopy.archiveNoInterp(lang),
                                    color = GrayMuted,
                                    fontSize = 12.sp,
                                    fontFamily = WenKaiFontFamily
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isExpanded) {
                            Text(
                                text = CyberCopy.archiveShare(lang),
                                color = AccentRed,
                                fontSize = 11.sp,
                                fontFamily = HuiwenFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                modifier = Modifier
                                    .border(1.dp, AccentRed.copy(alpha = 0.7f))
                                    .clickable { onShare() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
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
            // resultJson is plain text summary, contains "本卦 (Primary): X hexagram name"
            titleText = try {
                val m = Regex("本卦\\s*\\(Primary\\):\\s*\\S+\\s+(\\S+)").find(resultJson)
                val name = m?.groupValues?.get(1) ?: ""
                if (name.isNotEmpty()) name else question.takeIf { it.length <= 6 } ?: "I Ching Reading"
            } catch (e: Exception) { "I Ching Reading" }
            interpretationText = try {
                val m = Regex("卦象:\\s*(.+)").find(resultJson)
                m?.groupValues?.get(1)?.take(60) ?: ""
            } catch (e: Exception) { "" }
        }
        DivinationType.TAROT -> {
            // resultJson is JSON array: [{"card_zh":"愚者","isReversed":"true",...}]
            val firstCard = try {
                val arr = resultJson.trim()
                if (arr.startsWith("[")) {
                    val cardZh = Regex("\"card_zh\"\\s*:\\s*\"([^\"]+)\"").find(arr)?.groupValues?.get(1) ?: ""
                    val reversed = Regex("\"isReversed\"\\s*:\\s*\"true\"").containsMatchIn(arr)
                    cardZh to reversed
                } else "" to false
            } catch (e: Exception) { "" to false }
            titleText = if (firstCard.first.isNotBlank()) {
                FortuneEngine.tarotFortune(firstCard.first, firstCard.second)
            } else "Tarot Reading"
            interpretationText = if (firstCard.first.isNotBlank()) {
                FortuneEngine.tarotMeaning(firstCard.first, firstCard.second)
            } else "Cards archived, awaiting review."
        }
        DivinationType.VISION -> {
            titleText = try {
                val m = Regex("\"fortune\"\\s*:\\s*\"([^\"]+)\"").find(resultJson)
                m?.groupValues?.get(1) ?: question.ifBlank { "Face Analysis" }
            } catch (e: Exception) { question.ifBlank { "Face Analysis" } }
            interpretationText = FortuneEngine.visionMeaning(titleText)
        }
        else -> {
            // ORACLE / MUYU: question field is already 4-char fortune summary
            // resultJson may be plain text (new) or JSON (old format)
            // Collapsed card: only show first sentence as one-line interpretation
            titleText = if (question.length <= 8) question else question.take(4)
            interpretationText = try {
                val raw = resultJson.trim()
                val fullText = if (raw.startsWith("{")) {
                    // Old JSON format: extract "response" field
                    val m = Regex("\"response\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(raw)
                    m?.groupValues?.get(1)
                        ?.replace("\\\"", "\"")
                        ?.replace("\\n", "\n") ?: ""
                } else {
                    // New plain text format
                    raw
                }
                // Strip all bracket headers and take first sentence from remaining content
                val cleaned = fullText
                    .replace(Regex("\\[[^\\]]*\\]"), "") // Remove any [ ... ] markers
                    .replace(Regex("\\n+"), " ")         // Flatten to single line
                    .trim()
                if (cleaned.isNotBlank()) firstSentence(cleaned) else ""
            } catch (e: Exception) { "" }
        }
    }

    return ArchiveEntry(
        id = id,
        ganzhiDate = "${gz.combined}日",
        solarDate = solarDateStr,
        type = type.displayName,
        title = titleText.ifEmpty { type.displayName },
        interpretation = interpretationText.ifEmpty { notes.ifEmpty { "No reading" } },
        hash = String.format("0x%08X", id.hashCode())
    )
}

@Composable
private fun EmptyArchive(lang: com.cyberdiviner.ui.settings.AppLanguage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = CyberCopy.archiveEmpty(lang),
            color = GrayCaption,
            fontFamily = HuiwenFontFamily,
            fontSize = 18.sp,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NO CAUSAL RECORDS",
            color = GrayMuted,
            fontSize = 11.sp,
            fontFamily = MonoFontFamily,
            letterSpacing = 2.sp
        )
    }
}
