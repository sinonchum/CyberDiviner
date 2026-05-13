package com.cyberdiviner.ui.oracle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.ui.shared.TypewriterText
import com.cyberdiviner.ui.theme.*

/**
 * OracleScreen -- Immersive chat with Eastern aesthetics.
 *
 * AI messages: left-aligned, 汇文明朝体, with a thin vertical anchor line.
 * User messages: right-aligned, JetBrainsMono, wrapped in a 1dp white border box.
 * Input: bottom bar with send button (white triangle).
 */
@Composable
fun OracleScreen(
    onBack: () -> Unit,
    viewModel: OracleViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val round by viewModel.round.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // ── Header ──────────────────────────────────────
        Text(
            text = "叩问天机",
            color = GrayTitle,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ROUND ${round}/${viewModel.maxRounds}",
            color = GrayCaption,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ── Chat messages ───────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(messages) { msg ->
                if (msg.isAgent) {
                    AiBubble(text = msg.text)
                } else {
                    UserBubble(text = msg.text)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Input bar ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(CyberBlack)
                    .padding(vertical = 8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = GrayTitle,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(CyberWhite),
                singleLine = true,
                enabled = !isProcessing,
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "输入你的困惑...",
                                color = GrayCaption,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Serif
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Send button: white triangle
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, GrayBorder)
                    .background(if (inputText.isNotBlank()) CyberWhite else CyberBlack)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // ▶ triangle drawn via Canvas would be ideal, but Text is simpler
                Text(
                    text = "\u25B6",
                    color = if (inputText.isNotBlank()) CyberBlack else GrayCaption,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom line
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GrayBorder)
        )
    }

    // ── Handle send (wired to ViewModel) ─────────────────
    // The send button click is handled inline above
}

// ── AI Bubble: left-aligned with vertical anchor ───────────────────────────

@Composable
private fun AiBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Thin vertical anchor line (2dp, height matches text)
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(24.dp)
                .background(GrayBorder)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // AI message text (汇文明朝体)
        TypewriterText(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                color = GrayTitle,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

// ── User Bubble: right-aligned with border box ─────────────────────────────

@Composable
private fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, GrayBorder, RoundedCornerShape(0.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                color = GrayTitle,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            )
        }
    }
}
