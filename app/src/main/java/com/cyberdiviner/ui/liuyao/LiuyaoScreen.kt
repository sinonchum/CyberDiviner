package com.cyberdiviner.ui.liuyao

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.ui.theme.*

/**
 * LiuyaoScreen — 六爻起卦 main screen.
 *
 * Features:
 * - Question input with cyberpunk-styled text field
 * - Animated "起卦" button with neon glow
 * - Coin toss animation sequence (6 rounds)
 * - Progress indicator through tossing phases
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "六爻起卦",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBlack
                )
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    CyberBlack
                )
        ) {
            when (uiState.phase) {
                LiuyaoPhase.INPUT -> InputPhase(
                    question = uiState.question,
                    errorMessage = uiState.errorMessage,
                    onQuestionChange = viewModel::updateQuestion,
                    onStartDivination = {
                        focusManager.clearFocus()
                        viewModel.startDivination()
                    }
                )

                LiuyaoPhase.TOSSING, LiuyaoPhase.COMPUTING, LiuyaoPhase.INTERPRETING -> TossingPhase(
                    uiState = uiState
                )

                LiuyaoPhase.ERROR -> ErrorPhase(
                    message = uiState.errorMessage ?: "未知错误",
                    onDismiss = viewModel::dismissError
                )

                LiuyaoPhase.RESULT -> {
                    // Shouldn't reach here — LaunchedEffect handles navigation
                }
            }
        }
    }
}

// ── Input Phase ──────────────────────────────────────────────────────────

@Composable
private fun InputPhase(
    question: String,
    errorMessage: String?,
    onQuestionChange: (String) -> Unit,
    onStartDivination: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Decorative header
        Icon(
            imageVector = Icons.Default.Explore,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "心诚则灵",
            color = TextSecondary,
            fontSize = 16.sp,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "静心冥想，然后输入你的问题",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Question input
        OutlinedTextField(
            value = question,
            onValueChange = onQuestionChange,
            label = {
                Text("你想问什么？", color = TextSecondary)
            },
            placeholder = {
                Text("例如：我的事业前景如何？", color = TextMuted)
            },
            textStyle = LocalTextStyle.current.copy(
                color = TextPrimary,
                fontSize = 16.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CyberGray,
                cursorColor = NeonCyan,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onStartDivination() }),
            maxLines = 3
        )

        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = InauspiciousRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start button
        Button(
            onClick = onStartDivination,
            enabled = question.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = CyberBlack,
                disabledContainerColor = CyberGray,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "起卦",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Method description
        Text(
            text = "三钱法 · 六次抛掷 · 三枚铜钱",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Tossing Phase ────────────────────────────────────────────────────────

@Composable
private fun TossingPhase(
    uiState: LiuyaoUiState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Status message
        Text(
            text = uiState.progressMessage,
            color = NeonCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Coin animation
        SixTossAnimation(
            tossResults = uiState.tossResults,
            currentTossIndex = uiState.currentTossIndex,
            currentCoins = uiState.currentCoins,
            isAnimating = uiState.isCoinAnimating
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Computing / interpreting indicator
        if (uiState.phase == LiuyaoPhase.COMPUTING || uiState.phase == LiuyaoPhase.INTERPRETING) {
            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ── Error Phase ──────────────────────────────────────────────────────────

@Composable
private fun ErrorPhase(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = InauspiciousRed,
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "起卦失败",
            color = InauspiciousRed,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = message,
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = CyberBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("重新开始", fontWeight = FontWeight.Bold)
        }
    }
}
