package com.cyberdiviner.ui.oracle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.ui.shared.TypewriterText
import com.cyberdiviner.ui.shared.VoiceInputField
import com.cyberdiviner.ui.theme.*


/**
 * OracleScreen -- Immersive chat with Eastern aesthetics.
 *
 * AI messages: left-aligned, serif font, plain layout.
 * User messages: right-aligned, monospace, wrapped in a 1dp white border box.
 * Input: VoiceInputField (shared component) with hold-to-record voice input.
 *
 * Keyboard bugs fixed:
 *  - imePadding + systemBarsPadding on outer Column
 *  - Input text hoisted to ViewModel
 *  - Keyboard dismissed on send
 *  - Hold-to-record voice input with offline SpeechRecognizer (shared VoiceInputField)
 */
@Composable
fun OracleScreen(
    onBack: () -> Unit,
    viewModel: OracleViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val round by viewModel.round.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

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
            .imePadding()
            .systemBarsPadding()
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        // -- Header ----------------------------------------------------------
        Text(
            text = "叩问天机",
            color = GrayTitle,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "ROUND ${round}/${viewModel.maxRounds}",
            color = AccentRed,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // -- Chat messages ---------------------------------------------------
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

        // -- Input bar (shared VoiceInputField) ------------------------------
        VoiceInputField(
            text = inputText,
            onTextChange = { viewModel.updateInputText(it) },
            onSend = {
                keyboardController?.hide()
                viewModel.sendMessage(inputText)
                viewModel.clearInput()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── AI Bubble: left-aligned, plain serif ──────────────────────────────────

@Composable
private fun AiBubble(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
    ) {
        TypewriterText(
            text = text,
            style = TextStyle(
                color = GrayTitle,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp
            )
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
