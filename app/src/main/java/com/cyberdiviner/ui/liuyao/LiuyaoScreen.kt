package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.ui.localization.CyberCopy
import com.cyberdiviner.ui.localization.LocalAppLanguage
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.shared.VoiceInputField
import com.cyberdiviner.ui.theme.*

/**
 * LiuyaoScreen — I-Ching divination with physical shake interaction.
 *
 * Flow:
 *   1. INPUT — enter question
 *   2. TOSSING — shake phone 6 times to generate 6 lines
 *   3. COMPUTING — engine calculates hexagram
 *   4. INTERPRETING → RESULT — LLM interpretation
 *
 * Clean B&W aesthetic. No emoji, no neon.
 */
@Composable
fun LiuyaoScreen(
    navController: NavController,
    viewModel: LiuyaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val lang = LocalAppLanguage.current

    // Show result screen inline when interpretation is complete
    if (uiState.phase == LiuyaoPhase.RESULT) {
        LiuyaoResultScreen(
            navController = navController,
            viewModel = viewModel
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        when (uiState.phase) {
            LiuyaoPhase.INPUT -> InputPhase(
                lang = lang,
                question = uiState.question,
                errorMessage = uiState.errorMessage,
                onQuestionChange = viewModel::updateQuestion,
                onStartDivination = {
                    focusManager.clearFocus()
                    viewModel.startDivination()
                },
                onBack = { navController.popBackStack() }
            )

            LiuyaoPhase.TOSSING -> ShakePhase(
                lang = lang,
                uiState = uiState
            )

            LiuyaoPhase.COMPUTING, LiuyaoPhase.INTERPRETING -> ComputingPhase(
                lang = lang,
                message = uiState.progressMessage
            )

            LiuyaoPhase.ERROR -> ErrorPhase(
                lang = lang,
                message = uiState.errorMessage ?: CyberCopy.liuyaoUnknownError(lang),
                onDismiss = viewModel::dismissError
            )

            LiuyaoPhase.RESULT -> { /* Handled above */ }
        }
    }
}

// ── Input Phase ────────────────────────────────────────────────────────────

@Composable
private fun InputPhase(
    lang: com.cyberdiviner.ui.settings.AppLanguage,
    question: String,
    errorMessage: String?,
    onQuestionChange: (String) -> Unit,
    onStartDivination: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        SectionHeader(
            title = CyberCopy.liuyaoTitle(lang),
            subtitle = CyberCopy.liuyaoSubtitle(lang)
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Prompt
        Text(
            text = CyberCopy.liuyaoSincerity(lang),
            color = GrayBody,
            fontSize = 16.sp,
            fontFamily = WenKaiFontFamily,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = CyberCopy.liuyaoPrompt(lang),
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Question input
        VoiceInputField(
            text = question,
            onTextChange = onQuestionChange,
            onSend = onStartDivination,
            placeholder = CyberCopy.liuyaoPlaceholder(lang),
            modifier = Modifier.fillMaxWidth()
        )

        // Error
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = GrayBody,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start button
        CyberButton(
            text = CyberCopy.liuyaoCast(lang),
            onClick = onStartDivination,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // Back
        CyberButton(
            text = CyberCopy.liuyaoBack(lang),
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Shake Phase ────────────────────────────────────────────────────────────

@Composable
private fun ShakePhase(
    lang: com.cyberdiviner.ui.settings.AppLanguage,
    uiState: LiuyaoUiState
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Haptic feedback on each new toss result
    LaunchedEffect(uiState.tossResults.size) {
        if (uiState.tossResults.isNotEmpty()) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Pulsing animation for the shake instruction
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        SectionHeader(title = CyberCopy.liuyaoTitle(lang))
        Spacer(modifier = Modifier.height(64.dp))

        // Shake instruction — pulsing
        Text(
            text = uiState.shakeProgress,
            color = GrayTitle.copy(alpha = alpha),
            fontSize = 24.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = CyberCopy.liuyaoShake(lang),
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Show generated lines so far
        if (uiState.tossResults.isNotEmpty()) {
            Text(
                text = CyberCopy.liuyaoObtained(lang),
                color = GrayCaption,
                fontSize = 11.sp,
                fontFamily = WenKaiFontFamily,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            uiState.tossResults.forEachIndexed { index, lineState ->
                val bar = when (lineState) {
                    LineState.YOUNG_YANG, LineState.OLD_YANG -> "━━━━━"
                    LineState.YOUNG_YIN, LineState.OLD_YIN -> "━   ━"
                }
                val mark = when (lineState) {
                    LineState.OLD_YANG -> " ○"
                    LineState.OLD_YIN -> " ×"
                    else -> ""
                }
                val label = when (lineState) {
                    LineState.YOUNG_YANG -> CyberCopy.liuyaoYoungYang(lang)
                    LineState.YOUNG_YIN -> CyberCopy.liuyaoYoungYin(lang)
                    LineState.OLD_YANG -> CyberCopy.liuyaoOldYang(lang)
                    LineState.OLD_YIN -> CyberCopy.liuyaoOldYin(lang)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        color = GrayMuted,
                        fontSize = 11.sp,
                        fontFamily = MonoFontFamily,
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = "$bar$mark",
                        color = GrayBody,
                        fontSize = 14.sp,
                        fontFamily = MonoFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = GrayCaption,
                        fontSize = 11.sp,
                        fontFamily = WenKaiFontFamily
                    )
                }
            }

            // Placeholder lines for remaining
            repeat(6 - uiState.tossResults.size) { idx ->
                val lineNum = uiState.tossResults.size + idx + 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$lineNum",
                        color = GrayMuted,
                        fontSize = 11.sp,
                        fontFamily = MonoFontFamily,
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = "- - - - -",
                        color = GrayMuted.copy(alpha = 0.3f),
                        fontSize = 14.sp,
                        fontFamily = MonoFontFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress counter
        Text(
            text = "${uiState.currentTossIndex} / 6",
            color = GrayMuted,
            fontSize = 12.sp,
            fontFamily = MonoFontFamily
        )
    }
}

// ── Computing Phase ────────────────────────────────────────────────────────

@Composable
private fun ComputingPhase(
    lang: com.cyberdiviner.ui.settings.AppLanguage,
    message: String
) {
    val dots by rememberInfiniteTransition(label = "liuyao_waiting_dots")
        .animateValue(
            initialValue = 0,
            targetValue = 3,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dots"
        )
    val baseMessage = message.trimEnd('.')
    val animatedMessage = if (baseMessage.contains("正在召唤赛博先知")) {
        baseMessage.removeSuffix("...").removeSuffix("…") + ".".repeat(dots)
    } else {
        message
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = CyberCopy.liuyaoCastComplete(lang),
            color = GrayTitle,
            fontSize = 20.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = animatedMessage,
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily,
            textAlign = TextAlign.Center
        )
    }
}

// ── Error Phase ────────────────────────────────────────────────────────────

@Composable
private fun ErrorPhase(
    lang: com.cyberdiviner.ui.settings.AppLanguage,
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = CyberCopy.liuyaoCastFailed(lang),
            color = GrayTitle,
            fontSize = 20.sp,
            fontFamily = WenKaiFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = message,
            color = GrayBody,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        CyberButton(
            text = CyberCopy.liuyaoRestart(lang),
            onClick = onDismiss
        )
    }
}
