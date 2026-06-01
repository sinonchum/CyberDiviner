package com.cyberdiviner.ui.oracle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.ui.shared.TypewriterText
import com.cyberdiviner.ui.shared.VoiceInputField
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.localization.CyberCopy
import com.cyberdiviner.ui.localization.LocalAppLanguage
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
    val lang = LocalAppLanguage.current

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
        SectionHeader(title = CyberCopy.oracleTitle(lang), subtitle = CyberCopy.oracleRound(lang, round, viewModel.maxRounds))
        Spacer(modifier = Modifier.height(24.dp))

        // -- Chat messages ---------------------------------------------------
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(messages) { msg ->
                if (msg.isAgent) {
                    AiBubble(text = msg.text)
                } else {
                    UserBubble(text = msg.text)
                }
            }

            // Loading indicator when processing
            if (isProcessing) {
                item {
                    LoadingIndicator()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp
            )
        )
    }
}

// ── Loading indicator: animated dots ─────────────────────────────────────

@Composable
private fun LoadingIndicator() {
    val lang = LocalAppLanguage.current
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dotCount by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    val dots = ".".repeat(dotCount)
    val symbols = listOf("|", "/", "—", "\\")
    val spinIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Text(
        text = "${CyberCopy.oracleComputing(lang)} ${symbols[spinIndex]}$dots",
        color = GrayMuted,
        fontSize = 14.sp,
        fontFamily = MonoFontFamily,
        modifier = Modifier.padding(vertical = 4.dp)
    )
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
                fontFamily = WenKaiFontFamily,
                lineHeight = 20.sp
            )
        }
    }
}
