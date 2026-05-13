package com.cyberdiviner.ui.shared

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBorder
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GrayTitle

/**
 * Shared voice input field: text input + hold-to-mic button + send button.
 *
 * Bugs fixed from the original OracleScreen implementation:
 *  - Mic button is never disabled (was using IconButton enabled=false which blocked pointerInput)
 *  - isRecording state is local to the composable (no stale closure capture from parent scope)
 *  - SpeechRecognizer created via remember{} (guaranteed main thread during composition)
 *  - Listener set BEFORE startListening (was a race condition)
 *  - RECORD_AUDIO permission checked at runtime
 *
 * Usage:
 *   var text by remember { mutableStateOf("") }
 *   VoiceInputField(
 *       text = text,
 *       onTextChange = { text = it },
 *       onSend = { /* send */ }
 *   )
 */
@Composable
fun VoiceInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "输入你的困惑..."
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isRecording by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }

    // Runtime permission check
    LaunchedEffect(Unit) {
        hasPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    // SpeechRecognizer created during composition (main thread) via remember
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }

    // Buffer for recognition results
    var recognizedText by remember { mutableStateOf("") }

    // When recording stops, push recognized text into the input field
    LaunchedEffect(isRecording) {
        if (!isRecording && recognizedText.isNotBlank()) {
            onTextChange(recognizedText)
            recognizedText = ""
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Text input ──────────────────────────────────────────
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
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
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = placeholder,
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

            // ── Mic button: plain Box, never disabled ───────────────
            //  Using IconButton(enabled=false) was the root bug — it
            //  blocks pointerInput so tryAwaitRelease() never fires.
            //  A plain Box with pointerInput always receives touches.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, if (isRecording) CyberWhite else GrayBorder)
                    .background(if (isRecording) CyberWhite else CyberBlack)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (!hasPermission) return@detectTapGestures

                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                isRecording = true

                                // Set listener BEFORE starting (fixes race condition)
                                speechRecognizer.setRecognitionListener(
                                    object : RecognitionListener {
                                        override fun onReadyForSpeech(params: Bundle?) {}
                                        override fun onBeginningOfSpeech() {}
                                        override fun onRmsChanged(rmsdB: Float) {}
                                        override fun onBufferReceived(buffer: ByteArray?) {}
                                        override fun onEndOfSpeech() {
                                            isRecording = false
                                        }
                                        override fun onPartialResults(partialResults: Bundle?) {}
                                        override fun onEvent(eventType: Int, params: Bundle?) {}
                                        override fun onResults(results: Bundle?) {
                                            val matches = results?.getStringArrayList(
                                                SpeechRecognizer.RESULTS_RECOGNITION
                                            )
                                            recognizedText = matches?.firstOrNull() ?: ""
                                            isRecording = false
                                        }
                                        override fun onError(error: Int) {
                                            isRecording = false
                                        }
                                    }
                                )

                                speechRecognizer.startListening(
                                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                    }
                                )

                                // Suspend until finger lifts — this is a suspending call
                                tryAwaitRelease()

                                // Stop listening if still recording (user released early)
                                if (isRecording) {
                                    speechRecognizer.stopListening()
                                    isRecording = false
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "语音输入",
                    tint = if (isRecording) CyberBlack else GrayCaption
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Send button ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, if (text.isNotBlank()) CyberWhite else GrayBorder)
                    .background(if (text.isNotBlank()) CyberWhite else CyberBlack)
                    .pointerInput(text.isNotBlank()) {
                        if (text.isNotBlank()) {
                            detectTapGestures(
                                onPress = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onSend()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u25B6",
                    color = if (text.isNotBlank()) CyberBlack else GrayCaption,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Bottom line / wave ──────────────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        if (isRecording) {
            RecordingWaveLine(
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorder)
            )
        }
    }
}

// ── Recording wave animation ─────────────────────────────────────────────

@Composable
fun RecordingWaveLine(modifier: Modifier = Modifier) {
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

    Canvas(modifier = modifier.clipToBounds()) {
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
                end = Offset(
                    x + segmentWidth,
                    size.height / 2f +
                            kotlin.math.sin(((i + 1).toFloat() / segments + phase) * 2 * Math.PI)
                                .toFloat() * waveHeight
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
    }
}
