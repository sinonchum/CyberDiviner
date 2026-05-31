package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.engine.LiuyaoEngine
import com.cyberdiviner.ui.theme.*
import kotlin.math.abs

/**
 * LiuyaoResultScreen — ancient-book style paginated hexagram interpretation
 *
 * Swipe left/right to turn pages, one card per page.
 * Page numbers use Chinese numerals (壹/贰/叁/肆/伍).
 * Card content is vertically scrollable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiuyaoResultScreen(
    navController: NavController,
    viewModel: LiuyaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.divinationResult

    if (result == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(CyberBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("量子因果链运算中...", color = GrayMuted, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
        }
        return
    }

    val annotations by viewModel.learningAnnotations.collectAsState()
    val cards = buildCardList(result, uiState.llmInterpretation.ifBlank { uiState.llmStreamChunks }, uiState.fourCharFortune, uiState.fourCharMeaning, annotations)
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = cards.size

    // Chinese page numerals
    val cnNums = listOf("壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾")

    // Swipe detection
    var swipeOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset < -80f && currentPage < totalPages - 1) {
                            currentPage++
                        } else if (swipeOffset > 80f && currentPage > 0) {
                            currentPage--
                        }
                        swipeOffset = 0f
                    },
                    onDragCancel = { swipeOffset = 0f },
                    onHorizontalDrag = { _, amount -> swipeOffset += amount }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back
                Text(
                    text = "< 返回",
                    color = GrayCaption,
                    fontSize = 13.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )

                // Title
                Text(
                    text = "卦象解读",
                    color = GrayCaption,
                    fontSize = 14.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                // Page number
                Text(
                    text = "${cnNums.getOrElse(currentPage) { "${currentPage + 1}" }}/${cnNums.getOrElse(totalPages - 1) { "$totalPages" }}",
                    color = GrayMuted,
                    fontSize = 12.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 2.sp
                )
            }

            // ── Divider line ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )

            // ── Card content (scrollable) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Page edge shadow effect (left = dark, simulating book spine)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Card body with scroll
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Card header
                    Text(
                        text = cards[currentPage].title,
                        color = CyberWhite,
                        fontSize = 22.sp,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp
                    )
                    Text(
                        text = cards[currentPage].subtitle,
                        color = GrayMuted,
                        fontSize = 10.sp,
                        fontFamily = MonoFontFamily,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(1.dp)
                            .background(AccentRed)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Card content
                    cards[currentPage].content()

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Right page edge shadow (simulating next page peek)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GrayBorder.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            // ── Bottom: page indicator + actions ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )

            // Swipe hint
            Text(
                text = if (currentPage < totalPages - 1) "< 左滑翻页 >" else "< 右滑返回 >",
                color = GrayMuted,
                fontSize = 10.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Page dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalPages) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (i == currentPage) 16.dp else 6.dp)
                            .height(3.dp)
                            .background(
                                if (i == currentPage) CyberWhite else GrayBorder,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 返回 — simple text button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { navController.popBackStack() }
                        .border(1.dp, GrayBorder, RoundedCornerShape(6.dp))
                        .background(CyberBlack, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "返回",
                        color = GrayCaption,
                        fontSize = 13.sp,
                        fontFamily = HuiwenFontFamily
                    )
                }

                // 重新起卦 — white filled
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(CyberWhite, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "重新起卦",
                        color = CyberBlack,
                        fontSize = 13.sp,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Card data ──────────────────────────────────────────────────────────────

private data class CardInfo(
    val title: String,
    val subtitle: String,
    val content: @Composable () -> Unit
)

@Composable
private fun buildCardList(
    result: LiuyaoEngine.DivinationResult,
    llmText: String,
    fourCharFortune: String,
    fourCharMeaning: String,
    annotations: List<Pair<String, String>> = emptyList()
): List<CardInfo> = listOf(
    CardInfo("批命", "FORTUNE") { FortuneCard(fourCharFortune, fourCharMeaning) },
    CardInfo("卦象", "HEXAGRAM") { HexagramCard(result) },
    CardInfo("爻象", "LINES") { LinesCard(result) },
    CardInfo("六神", "SPIRITS") { SpiritsCard(result) }
) + if (llmText.isNotBlank()) listOf(
    CardInfo("解读", "INTERPRETATION") { InterpretationCard(result, llmText) }
) else listOf(
    CardInfo("断卦", "ANALYSIS") { AnalysisCard(result) }
) + if (annotations.isNotEmpty()) listOf(
    CardInfo("学习", "LEARNING") { LearningAnnotationsCard(annotations) }
) else emptyList()

// ── Card 0: Fortune (4-char summary) ─────────────────────────────────────

@Composable
private fun FortuneCard(fortune: String, meaning: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = fortune.ifBlank { "天机莫测" },
            color = GrayTitle,
            fontSize = 32.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = meaning.ifBlank { "卦象已起，静心体悟天机" },
            color = GrayBody,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 22.sp
        )
    }
}

// ── Card 1: Hexagram ──────────────────────────────────────────────────────

@Composable
private fun HexagramCard(result: LiuyaoEngine.DivinationResult) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question
        Text(
            text = result.question,
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Primary hexagram name — hero
        Text(
            text = result.primaryHexagram.chineseName,
            color = CyberWhite,
            fontSize = 48.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp
        )
        Text(
            text = "第${result.primaryHexagram.number}卦 ${result.primaryHexagram.englishName}",
            color = GrayMuted,
            fontSize = 11.sp,
            fontFamily = MonoFontFamily,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hexagram lines — top (上) to bottom (初)
        val lineLabels = listOf("上", "五", "四", "三", "二", "初")

        for (i in 0 until 6) {
            val lineIndex = 5 - i
            val line = result.lines[lineIndex]
            val isYang = line.state == LineState.OLD_YANG || line.state == LineState.YOUNG_YANG
            val isChanging = line.state == LineState.OLD_YANG || line.state == LineState.OLD_YIN

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = lineLabels[i],
                    color = GrayMuted,
                    fontSize = 11.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(12.dp))

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                ) {
                    val sw = 4.dp.toPx()
                    val gap = 14.dp.toPx()
                    val len = size.width * 0.8f
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val color = if (isChanging) AccentRed else CyberWhite

                    if (isYang) {
                        drawLine(color, Offset(cx - len / 2, cy), Offset(cx + len / 2, cy), sw, cap = StrokeCap.Square)
                    } else {
                        drawLine(color, Offset(cx - len / 2, cy), Offset(cx - gap / 2, cy), sw, cap = StrokeCap.Square)
                        drawLine(color, Offset(cx + gap / 2, cy), Offset(cx + len / 2, cy), sw, cap = StrokeCap.Square)
                    }

                    // Changing line: subtle dot
                    if (isChanging) {
                        drawCircle(AccentRed, 3.dp.toPx(), Offset(cx + len / 2 + 14.dp.toPx(), cy))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // World/Response line markers
                when (lineIndex) {
                    result.worldLine -> Tag("世", AccentRed, true)
                    result.responseLine -> Tag("应", AccentRed, false)
                    else -> Spacer(Modifier.width(20.dp))
                }
            }
        }

        // Changed hexagram
        if (result.hasChangingLines()) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
            Spacer(modifier = Modifier.height(16.dp))
            Text("变卦", color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily, letterSpacing = 3.sp)
            Text(
                result.changedHexagram.chineseName,
                color = CyberWhite,
                fontSize = 36.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Text(
                "第${result.changedHexagram.number}卦 ${result.changedHexagram.englishName}",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = MonoFontFamily
            )
        }
    }
}

@Composable
private fun Tag(text: String, color: Color, filled: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .then(
                if (filled) Modifier.background(color, RoundedCornerShape(10.dp))
                else Modifier.border(1.dp, color, RoundedCornerShape(10.dp))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (filled) CyberWhite else color,
            fontSize = 10.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Card 2: Lines table ───────────────────────────────────────────────────

@Composable
private fun LinesCard(result: LiuyaoEngine.DivinationResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("爻", "地支", "六亲", "五行", "世应").forEach { label ->
                Text(label, color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
        Spacer(Modifier.height(4.dp))

        for (i in 5 downTo 0) {
            val line = result.lines[i]
            val el = com.cyberdiviner.engine.HexagramData.BRANCH_TO_WUXING[line.branch]
            val sy = when (i) { result.worldLine -> "世"; result.responseLine -> "应"; else -> "" }
            val bg = when {
                i == result.worldLine -> AccentRed.copy(alpha = 0.08f)
                i == result.responseLine -> GraySurface
                else -> Color.Transparent
            }
            Row(
                Modifier.fillMaxWidth().background(bg, RoundedCornerShape(4.dp)).padding(vertical = 6.dp)
            ) {
                listOf("${i + 1}爻", line.branch, line.relation?.chinese ?: "-", el?.chinese ?: "-", sy).forEach { t ->
                    Text(t, color = if (t == "世" || t == "应") AccentRed else CyberWhite,
                        fontSize = 13.sp, fontFamily = WenKaiFontFamily,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }

        // Hidden lines
        if (result.hiddenLines.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("伏神", color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily, letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
            for (h in result.hiddenLines) {
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text("第${h.position + 1}爻", color = GrayCaption, fontSize = 12.sp, fontFamily = WenKaiFontFamily, modifier = Modifier.width(48.dp))
                    Text("[${h.branch}]", color = GrayBody, fontSize = 12.sp, fontFamily = WenKaiFontFamily, modifier = Modifier.width(40.dp))
                    Text(h.relation?.chinese ?: "", color = CyberWhite, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
                }
            }
        }
    }
}

// ── Card 3: Spirits ───────────────────────────────────────────────────────

@Composable
private fun SpiritsCard(result: LiuyaoEngine.DivinationResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        for (i in 5 downTo 0) {
            val s = result.spirits[i]
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${i + 1}爻", color = GrayCaption, fontSize = 12.sp, fontFamily = WenKaiFontFamily, modifier = Modifier.width(36.dp))
                Text(s.chinese, color = CyberWhite, fontSize = 16.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold, modifier = Modifier.width(56.dp))
                Text(s.animal, color = GrayBody, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
            }
        }

        Spacer(Modifier.height(24.dp))

        // World and Response line markers
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("世爻", color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily)
                val w = result.lines[result.worldLine]
                Text("第${result.worldLine + 1}爻 [${w.branch}]", color = CyberWhite, fontSize = 14.sp, fontFamily = WenKaiFontFamily)
                Text(w.relation?.chinese ?: "", color = AccentRed, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("应爻", color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily)
                val r = result.lines[result.responseLine]
                Text("第${result.responseLine + 1}爻 [${r.branch}]", color = CyberWhite, fontSize = 14.sp, fontFamily = WenKaiFontFamily)
                Text(r.relation?.chinese ?: "", color = GrayBody, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
            }
        }
    }
}

// ── Card 4: Analysis ──────────────────────────────────────────────────────

@Composable
private fun AnalysisCard(result: LiuyaoEngine.DivinationResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("用神  ", color = GrayMuted, fontSize = 13.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold)
            Text(result.analysis.usefulGod, color = CyberWhite, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("旺衰  ", color = GrayMuted, fontSize = 13.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold)
            Text(result.analysis.strength, color = CyberWhite, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
        }

        Spacer(Modifier.height(16.dp))

        Text(result.analysis.interpretation, color = GrayBody, fontSize = 14.sp, fontFamily = WenKaiFontFamily, lineHeight = 24.sp)
        Spacer(Modifier.height(12.dp))
        Text("进退之策", color = AccentRed, fontSize = 13.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text(cleanAdviceText(result.analysis.advice), color = CyberWhite, fontSize = 14.sp, fontFamily = WenKaiFontFamily, lineHeight = 24.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(20.dp))

        Text("卦辞: ${result.primaryHexagram.judgment}", color = GrayCaption, fontSize = 12.sp, fontFamily = WenKaiFontFamily, lineHeight = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text("象辞: ${result.primaryHexagram.image}", color = GrayCaption, fontSize = 12.sp, fontFamily = WenKaiFontFamily, lineHeight = 20.sp)
    }
}

// ── Card 5: LLM Interpretation ────────────────────────────────────────────

@Composable
private fun InterpretationCard(result: LiuyaoEngine.DivinationResult, llmText: String) {
    // Clean LLM text: strip engine divider lines
    val cleanText = llmText
        .replace(Regex("[━─═]{4,}"), "")
        .replace(Regex("六爻占卜 — Liuyao Divination"), "")
        .replace(Regex("━━━ .+ ━━━"), "")
        .replace("【建议】", "【进退之策】")
        .replace("【趋吉避凶】", "【进退之策】")
        .replace("建议：", "进退之策：")
        .replace("建议:", "进退之策：")
        .replace("趋吉避凶", "进退之策")
        .trim()

    if (cleanText.isBlank()) {
        // Fallback: generate structured plain-language summary from engine data
        PlainLanguageSummary(result)
    } else {
        // Show cleaned LLM interpretation
        Text(cleanText, color = GrayBody, fontSize = 14.sp, fontFamily = WenKaiFontFamily, lineHeight = 26.sp)
    }
}

@Composable
private fun PlainLanguageSummary(result: LiuyaoEngine.DivinationResult) {
    val primary = result.primaryHexagram
    val changed = result.changedHexagram
    val hasChanges = result.hasChangingLines()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Hexagram overview
        Text(
            text = "你所问之事，得${primary.chineseName}卦（${primary.englishName}）",
            color = CyberWhite,
            fontSize = 15.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 26.sp
        )
        if (hasChanges) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "动爻变${changed.chineseName}卦（${changed.englishName}），主变化转化之势。",
                color = GrayBody,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Strength assessment
        Text(
            text = result.analysis.strength,
            color = if (result.analysis.strength.contains("旺")) AccentRed else GrayBody,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(12.dp))

        // Overall interpretation
        Text(
            text = result.analysis.interpretation,
            color = GrayBody,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(20.dp))

        // Advice — most important, highlighted
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "指点迷津",
                    color = AccentRed,
                    fontSize = 13.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cleanAdviceText(result.analysis.advice),
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

private fun cleanAdviceText(text: String): String =
    text
        .replace("【建议】", "")
        .replace("建议：", "")
        .replace("建议:", "")
        .replace("建议", "")
        .trim(' ', '\n', '\r', '：', ':')

// ── Card: Learning Annotations ──────────────────────────────────────────

@Composable
private fun LearningAnnotationsCard(annotations: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        annotations.forEach { (title, text) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        color = AccentRed,
                        fontSize = 14.sp,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = text,
                        color = GrayBody,
                        fontSize = 13.sp,
                        fontFamily = WenKaiFontFamily,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
