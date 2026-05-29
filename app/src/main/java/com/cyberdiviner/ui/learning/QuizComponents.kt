package com.cyberdiviner.ui.learning

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.DesignTokens
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
 * Binary classify quiz — classify each item as true or false.
 * Uses AccentRed highlight on the selected classification for each item.
 *
 * @param prompt Instruction text
 * @param items List of (text, _) pairs — the Boolean is the correct answer (unused for display)
 * @param onAnswerSelected Callback returning list of Boolean choices (true=yes, false=no)
 */
@Composable
fun BinaryClassifyQuiz(
    prompt: String,
    items: List<Pair<String, Boolean>>,
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

                // True / False buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BinaryButton(
                        label = "✓ 正确",
                        isSelected = currentSelection == true,
                        accentColor = CyberWhite,
                        modifier = Modifier.weight(1f)
                    ) {
                        selections = selections.toMutableList().apply { set(index, true) }
                        if (selections.all { it != null }) {
                            onAnswerSelected(selections.map { it!! })
                        }
                    }

                    BinaryButton(
                        label = "✗ 错误",
                        isSelected = currentSelection == false,
                        accentColor = AccentRed,
                        modifier = Modifier.weight(1f)
                    ) {
                        selections = selections.toMutableList().apply { set(index, false) }
                        if (selections.all { it != null }) {
                            onAnswerSelected(selections.map { it!! })
                        }
                    }
                }
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
