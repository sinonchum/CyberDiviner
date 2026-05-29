package com.cyberdiviner.ui.learning

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.DesignTokens
import com.cyberdiviner.data.model.learning.MatchItem
import com.cyberdiviner.ui.theme.AccentRed
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBody
import com.cyberdiviner.ui.theme.GrayBorder
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GrayMuted
import com.cyberdiviner.ui.theme.GraySurface
import com.cyberdiviner.ui.theme.HuiwenFontFamily
import com.cyberdiviner.ui.theme.MonoFontFamily
import com.cyberdiviner.ui.theme.WenKaiFontFamily

/**
 * Single-choice quiz — select one option from a list.
 * Highlights selected option with AccentRed border.
 *
 * @param question The question text
 * @param options List of option strings
 * @param onAnswerSelected Callback returning the selected index
 */
@Composable
fun SingleChoiceQuiz(
    question: String,
    options: List<String>,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.ItemSpacing)
    ) {
        Text(
            text = question,
            color = CyberWhite,
            fontFamily = WenKaiFontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) AccentRed else GrayBorder,
                animationSpec = tween(200),
                label = "border_$index"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) CyberWhite else GrayBody,
                animationSpec = tween(200),
                label = "text_$index"
            )
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) GraySurface else CyberBlack,
                animationSpec = tween(200),
                label = "bg_$index"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(bgColor)
                    .clickable {
                        selectedIndex = index
                        onAnswerSelected(index)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Border drawn via Canvas overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(2f, 2f),
                        style = Stroke(width = 1.5f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Radio indicator
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer circle
                        Canvas(modifier = Modifier.size(14.dp)) {
                            drawCircle(
                                color = if (isSelected) AccentRed else GrayBorder,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 1.5f)
                            )
                        }
                        // Inner filled circle
                        if (isSelected) {
                            Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(color = AccentRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = option,
                        color = textColor,
                        fontFamily = WenKaiFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

/**
 * Binary classify quiz — categorize each item into one of two categories.
 * Buttons show category labels (e.g. 阳/阴) with distinct styling.
 *
 * @param prompt Instruction text
 * @param items List of (text, _) pairs — the Boolean is the correct answer
 * @param categoryLabels Pair of (trueLabel, falseLabel) for the two categories
 * @param onAnswerSelected Callback returning list of Boolean choices
 */
@Composable
fun BinaryClassifyQuiz(
    prompt: String,
    items: List<Pair<String, Boolean>>,
    categoryLabels: Pair<String, String>,
    onAnswerSelected: (List<Boolean>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selections by remember { mutableStateOf(List(items.size) { null as Boolean? }) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.ItemSpacing)
    ) {
        Text(
            text = prompt,
            color = CyberWhite,
            fontFamily = WenKaiFontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEachIndexed { index, (text, _) ->
            val currentSelection = selections[index]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(GraySurface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(2.dp))
                    .padding(16.dp)
            ) {
                // Item text
                Text(
                    text = text,
                    color = CyberWhite,
                    fontFamily = WenKaiFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // True / False buttons — side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 正确 button — white accent
                    val trueBg = animateColorAsState(
                        targetValue = when (currentSelection) {
                            true -> CyberWhite.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        },
                        animationSpec = tween(200), label = "trueBg"
                    )
                    val trueBorder = animateColorAsState(
                        targetValue = when (currentSelection) {
                            true -> CyberWhite
                            false -> GrayBorder  // dimmed when other is selected
                            null -> GrayBorder
                        },
                        animationSpec = tween(200), label = "trueBorder"
                    )
                    val trueText = animateColorAsState(
                        targetValue = when (currentSelection) {
                            true -> CyberWhite
                            else -> GrayMuted
                        },
                        animationSpec = tween(200), label = "trueText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(trueBg.value)
                            .border(1.5.dp, trueBorder.value, RoundedCornerShape(2.dp))
                            .clickable {
                                selections = selections.toMutableList().apply { set(index, true) }
                                if (selections.all { it != null }) {
                                    onAnswerSelected(selections.map { it!! })
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryLabels.first,
                            color = trueText.value,
                            fontFamily = WenKaiFontFamily,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // 错误 button — red accent
                    val falseBg = animateColorAsState(
                        targetValue = when (currentSelection) {
                            false -> AccentRed.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        },
                        animationSpec = tween(200), label = "falseBg"
                    )
                    val falseBorder = animateColorAsState(
                        targetValue = when (currentSelection) {
                            false -> AccentRed
                            true -> GrayBorder  // dimmed when other is selected
                            null -> GrayBorder
                        },
                        animationSpec = tween(200), label = "falseBorder"
                    )
                    val falseText = animateColorAsState(
                        targetValue = when (currentSelection) {
                            false -> AccentRed
                            else -> GrayMuted
                        },
                        animationSpec = tween(200), label = "falseText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(falseBg.value)
                            .border(1.5.dp, falseBorder.value, RoundedCornerShape(2.dp))
                            .clickable {
                                selections = selections.toMutableList().apply { set(index, false) }
                                if (selections.all { it != null }) {
                                    onAnswerSelected(selections.map { it!! })
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryLabels.second,
                            color = falseText.value,
                            fontFamily = WenKaiFontFamily,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draw 6 hexagram lines for a given trigram combination (e.g. "乾上坤下").
 * Upper trigram on top (lines 4-6), lower trigram on bottom (lines 1-3).
 * Yang = solid line ━━━━━, Yin = broken line ━━  ━━
 */
@Composable
private fun HexagramLines(
    trigramCombo: String,  // e.g. "乾上坤下"
    modifier: Modifier = Modifier,
    lineColor: Color = AccentRed,
    lineWidth: Float = 28f,
    lineHeight: Float = 4f,
    gap: Float = 3f
) {
    // Parse "X上Y下" → upper trigram X, lower trigram Y
    val trigramLines = mapOf(
        "乾" to listOf(true, true, true),
        "坤" to listOf(false, false, false),
        "震" to listOf(false, false, true),
        "巽" to listOf(true, true, false),
        "坎" to listOf(false, true, false),
        "离" to listOf(true, false, true),
        "艮" to listOf(true, false, false),
        "兑" to listOf(false, true, true)
    )
    val upper = trigramCombo.substringBefore("上").trim()
    val lower = trigramCombo.substringAfter("上").substringBefore("下").trim()
    val upperLines = trigramLines[upper] ?: return
    val lowerLines = trigramLines[lower] ?: return
    // Draw bottom→top: lower(1-3), then upper(4-6), displayed top→bottom visually
    val allLines = upperLines.reversed() + lowerLines.reversed()  // top line first for display

    val totalHeight = allLines.size * lineHeight + (allLines.size - 1) * gap
    Canvas(
        modifier = modifier.size(
            width = (lineWidth + 8).dp,
            height = (totalHeight + 4).dp
        )
    ) {
        val lp = lineHeight.dp.toPx()
        val gp = gap.dp.toPx()
        val wp = lineWidth.dp.toPx()
        val startX = 4.dp.toPx()
        var y = 2.dp.toPx()
        for (isYang in allLines) {
            if (isYang) {
                // Solid line
                drawRect(
                    color = lineColor,
                    topLeft = Offset(startX, y),
                    size = Size(wp, lp)
                )
            } else {
                // Broken line — two halves with gap
                val halfW = (wp - 4.dp.toPx()) / 2
                drawRect(
                    color = lineColor,
                    topLeft = Offset(startX, y),
                    size = Size(halfW, lp)
                )
                drawRect(
                    color = lineColor,
                    topLeft = Offset(startX + halfW + 4.dp.toPx(), y),
                    size = Size(halfW, lp)
                )
            }
            y += lp + gp
        }
    }
}

/**
 * Draw 3-line trigram symbol for a single trigram name.
 * Used in matching quizzes to show 卦象 next to the trigram text.
 */
@Composable
private fun TrigramLines(
    trigramName: String,  // e.g. "乾", "坤", "坎"
    modifier: Modifier = Modifier,
    lineColor: Color = AccentRed,
    lineWidth: Float = 20f,
    lineHeight: Float = 3f,
    gap: Float = 2f
) {
    val trigramMap = mapOf(
        "乾" to listOf(true, true, true),     // ☰ 阳阳阳
        "坤" to listOf(false, false, false),   // ☷ 阴阴阴
        "震" to listOf(false, false, true),    // ☳ 阴阴阳
        "巽" to listOf(true, true, false),     // ☴ 阳阳阴
        "坎" to listOf(false, true, false),    // ☵ 阴阳阴
        "离" to listOf(true, false, true),     // ☲ 阳阴阳
        "離" to listOf(true, false, true),     // ☲ 阳阴阳 (繁体)
        "艮" to listOf(true, false, false),    // ☶ 阳阴阴
        "兑" to listOf(false, true, true),     // ☱ 阴阳阳
        "兌" to listOf(false, true, true),     // ☱ 阴阳阳 (繁体)
    )
    val lines = trigramMap[trigramName] ?: return

    val totalHeight = lines.size * lineHeight + (lines.size - 1) * gap
    Canvas(
        modifier = modifier.size(
            width = (lineWidth + 4).dp,
            height = (totalHeight + 2).dp
        )
    ) {
        val lp = lineHeight.dp.toPx()
        val gp = gap.dp.toPx()
        val wp = lineWidth.dp.toPx()
        val startX = 2.dp.toPx()
        var y = 1.dp.toPx()
        for (isYang in lines) {
            if (isYang) {
                drawRect(
                    color = lineColor,
                    topLeft = Offset(startX, y),
                    size = Size(wp, lp)
                )
            } else {
                val halfW = (wp - 3.dp.toPx()) / 2
                drawRect(
                    color = lineColor,
                    topLeft = Offset(startX, y),
                    size = Size(halfW, lp)
                )
                drawRect(
                    color = lineColor,
                    topLeft = Offset(startX + halfW + 3.dp.toPx(), y),
                    size = Size(halfW, lp)
                )
            }
            y += lp + gp
        }
    }
}

/**
 * Matching quiz — show key-value pairs, user confirms the mapping.
 * Displays items as a list of key → value pairs with a confirm button.
 * If value matches "X上Y下" pattern, also draws hexagram lines.
 *
 * @param prompt Instruction text
 * @param items List of MatchItem (key-value pairs)
 * @param explanation Explanation text shown after answering
 * @param onAnswerSelected Callback with whether the answer was correct
 */
@Composable
fun MatchingQuiz(
    prompt: String,
    items: List<MatchItem>,
    explanation: String,
    onAnswerSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Shuffle values once for the options pool
    val shuffledValues = remember { items.map { it.value }.shuffled() }
    // User selections: index → selected value (null = not selected)
    var selections by remember { mutableStateOf(List(items.size) { null as String? }) }
    var confirmed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.ItemSpacing)
    ) {
        Text(
            text = prompt,
            color = CyberWhite,
            fontFamily = WenKaiFontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Each item: show key + selectable value options
        items.forEachIndexed { index, item ->
            val selected = selections[index]
            val hasHexagram = item.value.contains("上") && item.value.contains("下")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(GraySurface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(2.dp))
                    .padding(12.dp)
            ) {
                // Key text + trigram symbol
                val knownTrigrams = setOf("乾","坤","震","巽","坎","离","離","艮","兑","兌")
                val isTrigramKey = item.key in knownTrigrams || item.key.length <= 2 && item.key.any { it in knownTrigrams.flatMap { c -> c.toList() } }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    if (isTrigramKey) {
                        TrigramLines(
                            trigramName = item.key,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = item.key,
                        color = CyberWhite,
                        fontFamily = WenKaiFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasHexagram && selected == item.value) {
                        HexagramLines(
                            trigramCombo = item.value,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Shuffled value options as tappable chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shuffledValues.forEach { value ->
                        val isSelected = selected == value
                        val isUsedElsewhere = selections.any { it == value && selections.indexOf(it) != index }
                        val chipBg = animateColorAsState(
                            targetValue = when {
                                isSelected -> AccentRed.copy(alpha = 0.2f)
                                isUsedElsewhere -> GraySurface.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            },
                            animationSpec = tween(200), label = "chipBg"
                        )
                        val chipBorder = animateColorAsState(
                            targetValue = when {
                                isSelected -> AccentRed
                                isUsedElsewhere -> GrayBorder.copy(alpha = 0.3f)
                                else -> GrayBorder
                            },
                            animationSpec = tween(200), label = "chipBorder"
                        )
                        val chipText = animateColorAsState(
                            targetValue = when {
                                isSelected -> CyberWhite
                                isUsedElsewhere -> GrayMuted.copy(alpha = 0.4f)
                                else -> GrayMuted
                            },
                            animationSpec = tween(200), label = "chipText"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(chipBg.value)
                                .border(1.dp, chipBorder.value, RoundedCornerShape(2.dp))
                                .clickable(enabled = !confirmed && !isUsedElsewhere) {
                                    selections = selections.toMutableList().apply {
                                        set(index, if (isSelected) null else value)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = chipText.value,
                                fontFamily = WenKaiFontFamily,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // Confirm button
        if (!confirmed) {
            val allSelected = selections.all { it != null }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (allSelected) GraySurface else GraySurface.copy(alpha = 0.5f))
                    .clickable(enabled = allSelected) {
                        confirmed = true
                        val allCorrect = items.zip(selections).all { (item, sel) -> sel == item.value }
                        onAnswerSelected(allCorrect)
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "确认",
                    color = if (allSelected) CyberWhite else GrayMuted,
                    fontFamily = HuiwenFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

/**
 * Ordering quiz — user arranges items in correct order by tapping to swap.
 * Items are initially shuffled. User taps two items to swap them, then confirms.
 *
 * @param prompt Instruction text
 * @param items List of MatchItem (key = display text, value = sort order string "1","2",...)
 * @param onAnswerSelected Callback with whether the ordering was correct
 */
@Composable
fun OrderingQuiz(
    prompt: String,
    items: List<MatchItem>,
    onAnswerSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val correctOrder = items.sortedBy { it.value.toIntOrNull() ?: 0 }
    var shuffled by remember {
        mutableStateOf(items.shuffled().map { it.key }.toMutableList())
    }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var confirmed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.ItemSpacing)
    ) {
        Text(
            text = prompt,
            color = CyberWhite,
            fontFamily = WenKaiFontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        shuffled.forEachIndexed { index, itemText ->
            val isSelected = selectedIndex == index
            val borderColor = when {
                isSelected -> AccentRed
                confirmed -> GrayBorder
                else -> GrayBorder
            }
            val bgColor = if (isSelected) GraySurface else CyberBlack

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(bgColor)
                    .clickable {
                        if (!confirmed) {
                            val prev = selectedIndex
                            if (prev == null) {
                                selectedIndex = index
                            } else if (prev == index) {
                                selectedIndex = null
                            } else {
                                // Swap
                                val newList = shuffled.toMutableList()
                                val tmp = newList[prev]
                                newList[prev] = newList[index]
                                newList[index] = tmp
                                shuffled = newList
                                selectedIndex = null
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(2f, 2f),
                        style = Stroke(width = 1.5f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${index + 1}.",
                        color = if (isSelected) AccentRed else GrayMuted,
                        fontFamily = MonoFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = itemText,
                        color = if (isSelected) CyberWhite else GrayBody,
                        fontFamily = WenKaiFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (!confirmed) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .clickable {
                        confirmed = true
                        val isCorrect = shuffled == correctOrder.map { it.key }
                        onAnswerSelected(isCorrect)
                    }
                    .background(GraySurface)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "确认排序",
                    color = CyberWhite,
                    fontFamily = HuiwenFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

/**
 * Internal composable for a binary true/false button.
 */
@Composable
private fun BinaryButton(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else GrayBorder,
        animationSpec = tween(200),
        label = "binary_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else GrayMuted,
        animationSpec = tween(200),
        label = "binary_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Border
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = borderColor,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = 1f)
            )
        }

        Text(
            text = label,
            color = textColor,
            fontFamily = MonoFontFamily,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}
