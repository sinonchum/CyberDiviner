package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.engine.HexagramData.SixRelation
import com.cyberdiviner.engine.HexagramData.SixSpirit
import com.cyberdiviner.engine.LiuyaoEngine
import com.cyberdiviner.ui.theme.*

/**
 * LiuyaoResultScreen — displays the full divination result including:
 * - Hexagram visual diagram (6 lines, top to bottom)
 * - Primary & changed hexagram names
 * - World line (世爻) & Response line (应爻)
 * - Six Relations (六亲) & Earthly Branches (地支)
 * - Six Spirits (六神)
 * - Hidden lines (伏神)
 * - Engine analysis + LLM interpretation
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
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = NeonCyan
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
                            tint = NeonMagenta
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        if (result == null) {
            // No result yet — show loading or error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    CyberBlack
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Question banner ──
            QuestionBanner(question = result.question)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hexagram names ──
            HexagramHeader(result = result)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hexagram diagram ──
            HexagramDiagram(result = result)

            Spacer(modifier = Modifier.height(20.dp))

            // ── World & Response lines ──
            WorldResponseSection(result = result)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Six Relations & Branches ──
            RelationsBranchesSection(result = result)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Six Spirits ──
            SpiritsSection(result = result)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hidden Lines ──
            if (result.hiddenLines.isNotEmpty()) {
                HiddenLinesSection(result = result)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Engine Analysis ──
            AnalysisSection(result = result)

            Spacer(modifier = Modifier.height(20.dp))

            // ── LLM Interpretation ──
            LlmInterpretationSection(
                interpretation = uiState.llmInterpretation.ifBlank { uiState.llmStreamChunks }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Action buttons ──
            ActionButtons(
                onNewReading = {
                    viewModel.newReading()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Sub-sections ─────────────────────────────────────────────────────────

@Composable
private fun QuestionBanner(question: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CyberSurface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("问事", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(question, color = TextPrimary, fontSize = 16.sp)
        }
    }
}

@Composable
private fun HexagramHeader(result: LiuyaoEngine.DivinationResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Primary hexagram
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("本卦", color = TextSecondary, fontSize = 12.sp)
            Text(
                result.primaryHexagram.chineseName,
                color = NeonCyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                result.primaryHexagram.englishName,
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                "第${result.primaryHexagram.number}卦",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        // Arrow
        if (result.hasChangingLines()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("→", color = NeonMagenta, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            // Changed hexagram
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("变卦", color = TextSecondary, fontSize = 12.sp)
                Text(
                    result.changedHexagram.chineseName,
                    color = NeonMagenta,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    result.changedHexagram.englishName,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    "第${result.changedHexagram.number}卦",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun HexagramDiagram(result: LiuyaoEngine.DivinationResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CyberDark.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "━━━ 六爻排列 ━━━",
                color = TextMuted,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Canvas-based hexagram diagram — draws all 6 lines bottom to top
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val strokeWidth = 2.dp.toPx()
                val lineLength = size.width * 0.75f
                val centerGap = 8.dp.toPx()
                val centerX = size.width / 2f
                val lineCount = 6
                val verticalMargin = size.height * 0.1f
                val usableHeight = size.height - 2 * verticalMargin
                val spacing = usableHeight / (lineCount - 1)

                for (i in 0 until lineCount) {
                    // y increases downward in Canvas; i=0 is top line (yao 6), i=5 is bottom line (yao 1)
                    val y = verticalMargin + i * spacing
                    val lineIndex = 5 - i  // lineIndex 5 = yao 1 (bottom), lineIndex 0 = yao 6 (top)
                    val line = result.lines[lineIndex]
                    val isYang = line.state == LineState.OLD_YANG || line.state == LineState.YOUNG_YANG
                    val isChanging = line.state == LineState.OLD_YANG || line.state == LineState.OLD_YIN

                    val lineColor = when {
                        isChanging -> NeonOrange
                        lineIndex == result.worldLine -> NeonCyan
                        lineIndex == result.responseLine -> NeonMagenta
                        line.isHidden -> TextMuted
                        else -> CyberWhite
                    }

                    if (isYang) {
                        // Yang (solid) line — single贯通线
                        drawLine(
                            color = lineColor,
                            start = Offset(centerX - lineLength / 2f, y),
                            end = Offset(centerX + lineLength / 2f, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Square
                        )
                    } else {
                        // Yin (broken) line — two segments with centerGap
                        val leftStart = centerX - lineLength / 2f
                        val leftEnd = centerX - centerGap / 2f
                        val rightStart = centerX + centerGap / 2f
                        val rightEnd = centerX + lineLength / 2f

                        drawLine(
                            color = lineColor,
                            start = Offset(leftStart, y),
                            end = Offset(leftEnd, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Square
                        )
                        drawLine(
                            color = lineColor,
                            start = Offset(rightStart, y),
                            end = Offset(rightEnd, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Square
                        )
                    }

                    // Moving line indicator — small circles at line ends
                    if (isChanging) {
                        val markerRadius = 3.dp.toPx()
                        drawCircle(
                            color = lineColor,
                            radius = markerRadius,
                            center = Offset(centerX - lineLength / 2f - markerRadius * 2, y)
                        )
                        drawCircle(
                            color = lineColor,
                            radius = markerRadius,
                            center = Offset(centerX + lineLength / 2f + markerRadius * 2, y)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldResponseSection(result: LiuyaoEngine.DivinationResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // World line
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("世爻 (Self)", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val worldLine = result.lines[result.worldLine]
                Text(
                    "第${result.worldLine + 1}爻 [${worldLine.branch}]",
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    worldLine.relation?.chinese ?: "",
                    color = NeonCyan,
                    fontSize = 13.sp
                )
            }
        }

        // Response line
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("应爻 (Other)", color = NeonMagenta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val responseLine = result.lines[result.responseLine]
                Text(
                    "第${result.responseLine + 1}爻 [${responseLine.branch}]",
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    responseLine.relation?.chinese ?: "",
                    color = NeonMagenta,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun RelationsBranchesSection(result: LiuyaoEngine.DivinationResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "━━━ 地支 · 六亲 ━━━",
                color = TextMuted,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Table header
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("爻", "地支", "六亲", "五行", "世应").forEach { label ->
                    Text(
                        text = label,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            HorizontalDivider(color = CyberGray, modifier = Modifier.padding(vertical = 4.dp))

            // Rows
            for (i in 5 downTo 0) {
                val line = result.lines[i]
                val branchElement = com.cyberdiviner.engine.HexagramData.BRANCH_TO_WUXING[line.branch]
                val 世应 = when (i) {
                    result.worldLine -> "世"
                    result.responseLine -> "应"
                    else -> ""
                }
                val rowColor = when {
                    i == result.worldLine -> NeonCyan.copy(alpha = 0.15f)
                    i == result.responseLine -> NeonMagenta.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowColor, RoundedCornerShape(4.dp))
                        .padding(vertical = 4.dp)
                ) {
                    listOf(
                        "${i + 1}爻",
                        line.branch,
                        line.relation?.chinese ?: "-",
                        branchElement?.chinese ?: "-",
                       世应
                    ).forEach { text ->
                        Text(
                            text = text,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpiritsSection(result: LiuyaoEngine.DivinationResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "━━━ 六神 ━━━",
                color = TextMuted,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            for (i in 5 downTo 0) {
                val spirit = result.spirits[i]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        "${i + 1}爻",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.width(32.dp)
                    )
                    Text(
                        spirit.chinese,
                        color = NeonPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        spirit.animal,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenLinesSection(result: LiuyaoEngine.DivinationResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "━━━ 伏神 (Hidden Lines) ━━━",
                color = TextMuted,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            for (hidden in result.hiddenLines) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        "第${hidden.position + 1}爻",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        "[${hidden.branch}]",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        hidden.relation?.chinese ?: "",
                        color = NeonOrange.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisSection(result: LiuyaoEngine.DivinationResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberDark.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "━━━ 分析 (Analysis) ━━━",
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            AnalysisRow("用神", result.analysis.usefulGod)
            AnalysisRow("旺衰", result.analysis.strength)

            Spacer(modifier = Modifier.height(8.dp))

            // Interpretation text
            Text(
                text = result.analysis.interpretation,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Advice
            Text(
                text = result.analysis.advice,
                color = AuspiciousGreen,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            // Judgment text
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CyberGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "卦辞: ${result.primaryHexagram.judgment}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Text(
                text = "象辞: ${result.primaryHexagram.image}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun AnalysisRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            "  $label: ",
            color = TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            value,
            color = TextPrimary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun LlmInterpretationSection(interpretation: String) {
    if (interpretation.isBlank()) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = NeonCyan.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "赛博先知的解读",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = interpretation,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onNewReading: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("返回")
        }

        Button(
            onClick = onNewReading,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = CyberBlack
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("重新起卦", fontWeight = FontWeight.Bold)
        }
    }
}
