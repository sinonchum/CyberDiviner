package com.cyberdiviner.ui.shared
import com.cyberdiviner.ui.theme.*
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared voice input field: text input + hold-to-mic button + send button.
 *
 * Uses Android's built-in SpeechRecognizer (zero extra APK size).
 * Requests RECORD_AUDIO permission on first mic tap.
 * Falls back to online recognition if offline model unavailable.
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
    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // SpeechRecognizer created during composition (main thread) via remember
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
        } else null
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.destroy() }
    }

    // Buffer for recognition results
    var recognizedText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // When recording stops, push recognized text into the input field
    if (!isRecording && recognizedText.isNotBlank()) {
        onTextChange(recognizedText)
        recognizedText = ""
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Text input ──────────────────────────────────────────
            BasicTextField(
                value = text,
                onValueChange = { onTextChange(it); errorMsg = null },
                modifier = Modifier
                    .weight(1f)
                    .background(CyberBlack)
                    .padding(vertical = 8.dp),
                textStyle = TextStyle(
                    color = GrayTitle,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily,
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
                                fontFamily = WenKaiFontFamily
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ── Mic button ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, if (isRecording) CyberWhite else GrayBorder)
                    .background(if (isRecording) CyberWhite else CyberBlack)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                // Request permission if not granted
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    return@detectTapGestures
                                }
                                if (speechRecognizer == null) {
                                    errorMsg = "设备不支持语音识别"
                                    return@detectTapGestures
                                }

                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                isRecording = true
                                errorMsg = null

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
                                            errorMsg = when (error) {
                                                SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                                                SpeechRecognizer.ERROR_NETWORK,
                                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络不可用"
                                                else -> null
                                            }
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
                                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                    }
                                )

                                // Suspend until finger lifts
                                tryAwaitRelease()

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

        // ── Bottom line / wave / error ──────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        if (isRecording) {
            RecordingWaveLine(
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        } else if (errorMsg != null) {
            Text(
                text = errorMsg!!,
                color = AccentRed,
                fontFamily = WenKaiFontFamily,
                fontSize = 10.sp
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
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "wave")
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
