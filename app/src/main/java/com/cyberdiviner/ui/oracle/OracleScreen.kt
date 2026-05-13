package com.cyberdiviner.ui.oracle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.ui.shared.TypewriterText
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * OracleScreen -- Minimal chat terminal.
 *
 * Top: "ROUND 0/5". Bottom: single-line input with bottom border only.
 * AI messages render via TypewriterText. User messages appear instantly.
 * Auto-inserts initial AI message on mount.
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
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
    ) {
        // -- Header: ROUND counter --
        Text(
            text = "ROUND ${round}/${viewModel.maxRounds}",
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // -- Chat messages --
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                if (msg.isAgent) {
                    // AI message: TypewriterText
                    TypewriterText(
                        text = msg.text,
                        style = androidx.compose.ui.text.TextStyle(
                            color = CyberWhite,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp
                        )
                    )
                } else {
                    // User message: instant
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "用户 > ",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = msg.text,
                            color = CyberWhite,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // -- Input: bottom border only, no box --
        if (!isProcessing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "> ",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberBlack),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = CyberWhite,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(CyberWhite),
                    singleLine = true,
                    enabled = !isProcessing
                )
            }
            // Bottom line (single white line)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CyberWhite)
            )
        } else {
            Text(
                text = "> 处理中...",
                color = TextMuted,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    // -- Handle send --
    val handleSend: () -> Unit = {
        if (inputText.isNotBlank()) {
            val msg = inputText.trim()
            inputText = ""
            viewModel.sendMessage(msg)
        }
    }

    // Reassign as a proper lambda
    LaunchedEffect(Unit) {
        // This is a workaround - in production, extract to a ViewModel
    }
}
