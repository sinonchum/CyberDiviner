package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.shared.VoiceInputField
import com.cyberdiviner.ui.theme.*

/**
 * LiuyaoScreen -- I-Ching divination with geometric animation.
 *
 * Clean B&W aesthetic. Generous padding. Gray hierarchy.
 * No neon colors, no Material ripple, no emoji.
 */
@Composable
fun LiuyaoScreen(
    navController: NavController,
    viewModel: LiuyaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Navigate to result when interpretation is complete
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == LiuyaoPhase.RESULT) {
            navController.navigate("liuyao_result") {
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        when (uiState.phase) {
            LiuyaoPhase.INPUT -> InputPhase(
                question = uiState.question,
                errorMessage = uiState.errorMessage,
                onQuestionChange = viewModel::updateQuestion,
                onStartDivination = {
                    focusManager.clearFocus()
                    viewModel.startDivination()
                },
                onBack = { navController.popBackStack() }
            )

            LiuyaoPhase.TOSSING, LiuyaoPhase.COMPUTING, LiuyaoPhase.INTERPRETING -> {
                // Map old CoinState to GeoCoinState for the new animation
                val geoCoins = uiState.currentCoins.map { coin ->
                    GeoCoinState(
                        isYang = coin.isHeads,
                        isRevealed = coin.isRevealed
                    )
                }
                TossingPhase(
                    uiState = uiState,
                    geoCoins = geoCoins
                )
            }

            LiuyaoPhase.ERROR -> ErrorPhase(
                message = uiState.errorMessage ?: "未知错误",
                onDismiss = viewModel::dismissError
            )

            LiuyaoPhase.RESULT -> {
                // Handled by LaunchedEffect above
            }
        }
    }
}

// ── Input Phase ────────────────────────────────────────────────────────────

@Composable
private fun InputPhase(
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
        Text(
            text = "周易起卦",
            color = GrayTitle,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "三钱法 · 六次演算",
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Prompt
        Text(
            text = "心诚则灵",
            color = GrayBody,
            fontSize = 16.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "静心冥想，然后输入你的问题",
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Question input (shared VoiceInputField with hold-to-record)
        VoiceInputField(
            text = question,
            onTextChange = onQuestionChange,
            onSend = onStartDivination,
            placeholder = "例如：我的事业前景如何？",
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
            text = "起卦",
            onClick = onStartDivination,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // Back
        CyberButton(
            text = "[ 返回 ]",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Tossing Phase ──────────────────────────────────────────────────────────

@Composable
private fun TossingPhase(
    uiState: LiuyaoUiState,
    geoCoins: List<GeoCoinState>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        SixTossAnimation(
            tossResults = uiState.tossResults,
            currentTossIndex = uiState.currentTossIndex,
            currentCoins = geoCoins,
            isAnimating = uiState.isCoinAnimating,
            onToss = { /* Handled by ViewModel */ }
        )
    }
}

// ── Error Phase ────────────────────────────────────────────────────────────

@Composable
private fun ErrorPhase(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "起卦失败",
            color = GrayTitle,
            fontSize = 20.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = message,
            color = GrayBody,
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        CyberButton(
            text = "重新开始",
            onClick = onDismiss
        )
    }
}
