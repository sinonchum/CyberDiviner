package com.cyberdiviner.ui.oracle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
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
 * AI messages: left-aligned, serif font, with a thin vertical anchor line.
 * User messages: right-aligned, monospace, wrapped in a 1dp white border box.
 * Input: bottom bar with mic button (hold to record) and send button.
 *
 * Keyboard bugs fixed:
 *  - imePadding + systemBarsPadding on outer Column
 *  - Input text hoisted to ViewModel
 *  - Keyboard dismissed on send
 *  - Hold-to-record voice input with offline SpeechRecognizer
 *  - Canvas wave animation during recording
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
    val isRecording by viewModel.isRecording.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── Voice recognition setup ───────────────────────────────────────────
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var recognizedText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer
        onDispose {
            recognizer.destroy()
        }
    }

    // Fill recognized text into input when recording stops
    LaunchedEffect(isRecording) {
        if (!isRecording && recognizedText.isNotBlank()) {
            viewModel.updateInputText(recognizedText)
            recognizedText = ""
        }
    }

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
            .padding(horizontal = 24.dp, vertical = 32.dp)
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ROUND ${round}/${viewModel.maxRounds}",
            color = GrayCaption,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

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

        // -- Input bar -------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { viewModel.updateInputText(it) },
                modifier = Modifier
                    .weight(1f)
                    .background(CyberBlack)
                    .padding(vertical = 8.dp),
                textStyle = TextStyle(
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

            Spacer(modifier = Modifier.width(12.dp))

            // Mic button: hold to record
            MicButton(
                isRecording = isRecording,
                onRecognitionResult = { text ->
                    recognizedText = text
                },
                speechRecognizer = speechRecognizer,
                context = context,
                onRecordingStarted = { viewModel.setRecording(true) },
                onRecordingStopped = { viewModel.setRecording(false) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button: white triangle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, GrayBorder)
                    .background(if (inputText.isNotBlank()) CyberWhite else CyberBlack)
                    .padding(8.dp)
                    .pointerInput(inputText.isNotBlank()) {
                        if (inputText.isNotBlank()) {
                            detectTapGestures(
                                onPress = {
                                    keyboardController?.hide()
                                    viewModel.sendMessage(inputText)
                                    viewModel.clearInput()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u25B6",
                    color = if (inputText.isNotBlank()) CyberBlack else GrayCaption,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom line (or recording wave)
        Spacer(modifier = Modifier.height(8.dp))
        if (isRecording) {
            RecordingWaveLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )
        }
    }
}

// ── Mic button with hold-to-record gesture ──────────────────────────────────

@Composable
private fun MicButton(
    isRecording: Boolean,
    onRecognitionResult: (String) -> Unit,
    speechRecognizer: SpeechRecognizer?,
    context: Context,
    onRecordingStarted: () -> Unit,
    onRecordingStopped: () -> Unit
) {
    val buttonColor = if (isRecording) CyberWhite else GrayCaption

    IconButton(
        onClick = { /* No-op: use press gesture below */ },
        modifier = Modifier
            .size(36.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onRecordingStarted()
                        speechRecognizer?.startListening(
                            buildRecognitionIntent()
                        )

                        // Setup listener if not already set
                        speechRecognizer?.setRecognitionListener(
                            buildRecognitionListener(
                                onResult = { text ->
                                    onRecognitionResult(text)
                                    onRecordingStopped()
                                },
                                onError = {
                                    onRecordingStopped()
                                }
                            )
                        )

                        // Wait for finger lift
                        tryAwaitRelease()

                        // Stop if still recording
                        if (isRecording) {
                            speechRecognizer?.stopListening()
                            onRecordingStopped()
                        }
                    }
                )
            },
        enabled = !isRecording
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Voice input",
            tint = buttonColor
        )
    }
}

// ── Build the recognition intent for offline speech ─────────────────────────

private fun buildRecognitionIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
}

// ── Build a RecognitionListener that forwards results ───────────────────────

private fun buildRecognitionListener(
    onResult: (String) -> Unit,
    onError: () -> Unit
): RecognitionListener = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        onResult(text)
    }

    override fun onError(error: Int) {
        onError()
    }
}

// ── Recording wave animation (Canvas) ──────────────────────────────────────

@Composable
private fun RecordingWaveLine(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier.clipToBounds()
    ) {
        val waveHeight = size.height / 2f
        val waveWidth = size.width
        val segments = 80
        val segmentWidth = waveWidth / segments

        for (i in 0 until segments) {
            val x = i * segmentWidth
            val progress = (i.toFloat() / segments) + phase
            val yOffset = kotlin.math.sin(progress * 2 * Math.PI).toFloat() * waveHeight

            drawLine(
                color = CyberWhite,
                start = Offset(x, size.height / 2f + yOffset),
                end = Offset(x + segmentWidth, size.height / 2f +
                    kotlin.math.sin(((i + 1).toFloat() / segments + phase) * 2 * Math.PI).toFloat() * waveHeight
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
    }
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

        // AI message text (serif font)
        TypewriterText(
            text = text,
            style = TextStyle(
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
