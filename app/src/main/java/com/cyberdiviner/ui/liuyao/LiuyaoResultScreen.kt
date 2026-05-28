package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.engine.LiuyaoEngine
import com.cyberdiviner.ui.theme.*
import kotlinx.coroutines.launch

/**
 * LiuyaoResultScreen — 卡片堆叠式卦象解读
 *
 * 使用 HorizontalPager 实现左右翻转的卡片 stack。
 * 卡片顺序：卦象 → 爻表 → 六神 → 分析 → 解读
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiuyaoResultScreen(
    navController: NavController,
    viewModel: LiuyaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.divinationResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "卦象解读",
                        color = GrayCaption,
                        fontFamily = HuiwenFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = GrayCaption
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.newReading()
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重新起卦",
                            tint = GrayCaption
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GrayCaption)
            }
            return@Scaffold
        }

        val cards = buildCardList(result, uiState.llmInterpretation.ifBlank { uiState.llmStreamChunks })
        val pagerState = rememberPagerState(pageCount = { cards.size })
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CyberBlack)
        ) {
            // ── Card pager ──
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                pageSpacing = 12.dp
            ) { page ->
                ResultCard(
                    title = cards[page].title,
                    subtitle = cards[page].subtitle
                ) {
                    cards[page].content()
                }
            }

            // ── Page indicator + nav ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left arrow
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) {
                    Text("<", color = if (pagerState.currentPage > 0) CyberWhite else GrayMuted, fontSize = 20.sp, fontFamily = MonoFontFamily)
                }

                // Page dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(cards.size) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == pagerState.currentPage) CyberWhite else GrayBorder)
                        )
                    }
                }

                // Right arrow
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < cards.size - 1) pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    enabled = pagerState.currentPage < cards.size - 1
                ) {
                    Text(">", color = if (pagerState.currentPage < cards.size - 1) CyberWhite else GrayMuted, fontSize = 20.sp, fontFamily = MonoFontFamily)
                }
            }

            // ── Bottom action buttons ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayCaption),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GrayBorder)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("返回", fontFamily = HuiwenFontFamily, fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        viewModel.newReading()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberWhite, contentColor = CyberBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重新起卦", fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    llmText: String
): List<CardInfo> = listOf(
    CardInfo("卦象", "HEXAGRAM") {
        HexagramCard(result)
    },
    CardInfo("爻象", "LINES") {
        LinesCard(result)
    },
    CardInfo("六神", "SPIRITS") {
        SpiritsCard(result)
    },
    CardInfo("断卦", "ANALYSIS") {
        AnalysisCard(result)
    },
    CardInfo("解读", "INTERPRETATION") {
        InterpretationCard(result, llmText)
    }
)

// ── Generic card wrapper ───────────────────────────────────────────────────

@Composable
private fun ResultCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, GrayBorder, RoundedCornerShape(0.dp))
            .background(CyberBlack)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Card header
            Text(
                text = title,
                color = CyberWhite,
                fontSize = 20.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Text(
                text = subtitle,
                color = GrayMuted,
                fontSize = 10.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(AccentRed)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Card body
            content()
        }
    }
}

// ── Card 1: Hexagram ──────────────────────────────────────────────────────

@Composable
private fun HexagramCard(result: LiuyaoEngine.DivinationResult) {
    Column(
        modifier = Modifier.fillMaxSize(),
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

        Spacer(modifier = Modifier.height(24.dp))

        // Primary hexagram name
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

        Spacer(modifier = Modifier.height(24.dp))

        // Hexagram diagram — from top (爻6) to bottom (爻1)
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
                    val centerGap = 14.dp.toPx()
                    val lineLength = size.width * 0.8f
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val color = if (isChanging) AccentRed else CyberWhite

                    if (isYang) {
                        drawLine(
                            color = color,
                            start = Offset(cx - lineLength / 2f, cy),
                            end = Offset(cx + lineLength / 2f, cy),
                            strokeWidth = sw,
                            cap = StrokeCap.Square
                        )
                    } else {
                        drawLine(
                            color = color,
                            start = Offset(cx - lineLength / 2f, cy),
                            end = Offset(cx - centerGap / 2f, cy),
                            strokeWidth = sw,
                            cap = StrokeCap.Square
                        )
                        drawLine(
                            color = color,
                            start = Offset(cx + centerGap / 2f, cy),
                            end = Offset(cx + lineLength / 2f, cy),
                            strokeWidth = sw,
                            cap = StrokeCap.Square
                        )
                    }

                    // Changing line: subtle circle dot instead of red cross
                    if (isChanging) {
                        drawCircle(
                            color = AccentRed,
                            radius = 3.dp.toPx(),
                            center = Offset(cx + lineLength / 2f + 14.dp.toPx(), cy)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // World/Response markers
                when (lineIndex) {
                    result.worldLine -> {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(AccentRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("世", color = CyberWhite, fontSize = 10.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                    result.responseLine -> {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(1.dp, AccentRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("应", color = AccentRed, fontSize = 10.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Changed hexagram (if any)
        if (result.hasChangingLines()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "变卦",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 3.sp
            )
            Text(
                text = result.changedHexagram.chineseName,
                color = CyberWhite,
                fontSize = 36.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Text(
                text = "第${result.changedHexagram.number}卦 ${result.changedHexagram.englishName}",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = MonoFontFamily
            )
        }
    }
}

// ── Card 2: Lines table ───────────────────────────────────────────────────

@Composable
private fun LinesCard(result: LiuyaoEngine.DivinationResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("爻", "地支", "六亲", "五行", "世应").forEach { label ->
                Text(
                    text = label,
                    color = GrayMuted,
                    fontSize = 11.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
        Spacer(modifier = Modifier.height(4.dp))

        for (i in 5 downTo 0) {
            val line = result.lines[i]
            val branchElement = com.cyberdiviner.engine.HexagramData.BRANCH_TO_WUXING[line.branch]
            val shiYing = when (i) {
                result.worldLine -> "世"
                result.responseLine -> "应"
                else -> ""
            }
            val rowBg = when {
                i == result.worldLine -> AccentRed.copy(alpha = 0.08f)
                i == result.responseLine -> GraySurface
                else -> Color.Transparent
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg, RoundedCornerShape(4.dp))
                    .padding(vertical = 6.dp)
            ) {
                listOf(
                    "${i + 1}爻",
                    line.branch,
                    line.relation?.chinese ?: "-",
                    branchElement?.chinese ?: "-",
                    shiYing
                ).forEach { text ->
                    Text(
                        text = text,
                        color = if (text == "世" || text == "应") AccentRed else CyberWhite,
                        fontSize = 13.sp,
                        fontFamily = WenKaiFontFamily,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hidden lines
        if (result.hiddenLines.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "伏神",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            for (hidden in result.hiddenLines) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(
                        "第${hidden.position + 1}爻",
                        color = GrayCaption,
                        fontSize = 12.sp,
                        fontFamily = WenKaiFontFamily,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        "[${hidden.branch}]",
                        color = GrayBody,
                        fontSize = 12.sp,
                        fontFamily = WenKaiFontFamily,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        hidden.relation?.chinese ?: "",
                        color = CyberWhite,
                        fontSize = 13.sp,
                        fontFamily = WenKaiFontFamily
                    )
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
            val spirit = result.spirits[i]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    "${i + 1}爻",
                    color = GrayCaption,
                    fontSize = 12.sp,
                    fontFamily = WenKaiFontFamily,
                    modifier = Modifier.width(36.dp)
                )
                Text(
                    spirit.chinese,
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(56.dp)
                )
                Text(
                    spirit.animal,
                    color = GrayBody,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // World & Response summary
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("世爻", color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily)
                val w = result.lines[result.worldLine]
                Text(
                    "第${result.worldLine + 1}爻 [${w.branch}]",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily
                )
                Text(
                    w.relation?.chinese ?: "",
                    color = AccentRed,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("应爻", color = GrayMuted, fontSize = 11.sp, fontFamily = HuiwenFontFamily)
                val r = result.lines[result.responseLine]
                Text(
                    "第${result.responseLine + 1}爻 [${r.branch}]",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily
                )
                Text(
                    r.relation?.chinese ?: "",
                    color = GrayBody,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily
                )
            }
        }
    }
}

// ── Card 4: Analysis ──────────────────────────────────────────────────────

@Composable
private fun AnalysisCard(result: LiuyaoEngine.DivinationResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Key analysis
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("用神  ", color = GrayMuted, fontSize = 13.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold)
            Text(result.analysis.usefulGod, color = CyberWhite, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("旺衰  ", color = GrayMuted, fontSize = 13.sp, fontFamily = HuiwenFontFamily, fontWeight = FontWeight.Bold)
            Text(result.analysis.strength, color = CyberWhite, fontSize = 13.sp, fontFamily = WenKaiFontFamily)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
        Spacer(modifier = Modifier.height(12.dp))

        // Interpretation
        Text(
            text = result.analysis.interpretation,
            color = GrayBody,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Advice
        Text(
            text = result.analysis.advice,
            color = CyberWhite,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorder))
        Spacer(modifier = Modifier.height(12.dp))

        // Judgment & Image
        Text(
            text = "卦辞: ${result.primaryHexagram.judgment}",
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "象辞: ${result.primaryHexagram.image}",
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 20.sp
        )
    }
}

// ── Card 5: LLM Interpretation ────────────────────────────────────────────

@Composable
private fun InterpretationCard(result: LiuyaoEngine.DivinationResult, llmText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (llmText.isBlank()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "量子因果链运算中...",
                    color = GrayMuted,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily
                )
            }
        } else {
            Text(
                text = llmText,
                color = GrayBody,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp
            )
        }
    }
}
