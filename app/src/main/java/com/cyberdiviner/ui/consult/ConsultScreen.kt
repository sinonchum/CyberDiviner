package com.cyberdiviner.ui.consult

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.agent.AgentInterviewEngine
import com.cyberdiviner.ui.theme.*
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.shared.DividerLine
import kotlinx.coroutines.launch

/**
 * ConsultScreen -- The Agent interview interface.
 *
 * Terminal-style chat. No decorations. Pure text exchange.
 * AI probes the user through 5 rounds, then generates Soul Hash.
 */

sealed class ChatMessage(val text: String) {
    class User(text: String) : ChatMessage(text)
    class Agent(text: String) : ChatMessage(text)
    class System(text: String) : ChatMessage(text)
}

@Composable
fun ConsultScreen(
    onComplete: (String) -> Unit, // Returns Soul Hash
    onBack: () -> Unit
) {
    val engine = remember { AgentInterviewEngine() }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var started by remember { mutableStateOf(false) }
    var awaitingResponse by remember { mutableStateOf(false) }

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
            .padding(16.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────
        SectionHeader(title = "咨询代理", subtitle = "AGENT CONSULTATION")

        Spacer(modifier = Modifier.height(8.dp))
        DividerLine(color = GrayBorder)
        Spacer(modifier = Modifier.height(12.dp))

        // ── Chat messages ──────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Input ──────────────────────────────────────────────────
        if (!awaitingResponse) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "> ",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = MonoFontFamily
                )
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = CyberWhite,
                        fontSize = 14.sp,
                        fontFamily = MonoFontFamily
                    ),
                    cursorBrush = SolidColor(CyberWhite),
                    singleLine = true,
                    enabled = !awaitingResponse
                )
            }
        } else {
            Text(
                text = "> \u5904\u7406\u4E2D...",
                color = GrayMuted,
                fontSize = 14.sp,
                fontFamily = MonoFontFamily
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        DividerLine(color = GrayBorder)
    }

    // -- Handle send --
    val handleSend: () -> Unit = {
        if (inputText.isNotBlank()) {
            val userMsg = inputText.trim()
            inputText = ""

            if (!started) {
                // First message: start interview
                messages.add(ChatMessage.User(userMsg))
                messages.add(ChatMessage.System("\u2500".repeat(30)))

                val firstPrompt = engine.startInterview(userMsg)
                messages.add(ChatMessage.Agent(firstPrompt))
                started = true
                awaitingResponse = false
            } else {
                messages.add(ChatMessage.User(userMsg))

                val (nextPrompt, isComplete) = engine.processResponse(userMsg)

                if (isComplete) {
                    // Generate Soul Hash
                    val result = engine.completeInterview()
                    messages.add(ChatMessage.System("\u2500".repeat(30)))
                    messages.add(ChatMessage.Agent(
                        "\u7075\u9B42\u54C8\u5E0C\u5DF2\u751F\u6210\u3002\n" +
                        "SOUL HASH: ${result.soulHash}\n\n" +
                        "\u56E0\u679C\u53D8\u91CF\u7EC4\u5DF2\u9501\u5B9A\u3002\u6240\u6709\u540E\u7EED\u7B97\u6CD5\u5C06\u57FA\u4E8E\u6B64\u54C8\u5E0C\u8FD0\u884C\u3002\n\n" +
                        "[ \u8FDB\u5165\u4EEA\u89C4\u6267\u884C ]"
                    ))
                    onComplete(result.soulHash)
                } else {
                    nextPrompt?.let { messages.add(ChatMessage.Agent(it)) }
                }
            }
        }
    }

    // Reassign as a proper lambda
    LaunchedEffect(Unit) {
        // This is a workaround - in production, extract to a ViewModel
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val (prefix, color) = when (msg) {
        is ChatMessage.User -> Pair("\u7528\u6237 > ", CyberWhite)
        is ChatMessage.Agent -> Pair("AGENT > ", CyberWhite)
        is ChatMessage.System -> Pair("", GrayMuted)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = MonoFontFamily
            )
        }
        Text(
            text = msg.text,
            color = color,
            fontSize = 14.sp,
            fontFamily = if (msg is ChatMessage.System) MonoFontFamily else WenKaiFontFamily,
            lineHeight = 22.sp
        )
    }
}
