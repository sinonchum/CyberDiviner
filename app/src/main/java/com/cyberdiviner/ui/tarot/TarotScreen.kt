package com.cyberdiviner.ui.tarot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(
    navController: NavController,
    viewModel: TarotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "塔罗占卜",
                        color = AccentTarot,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = AccentTarot
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CyberBlack)
        ) {
            AnimatedContent(
                targetState = uiState.phase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tarot_phase"
            ) { phase ->
                when (phase) {
                    TarotPhase.SELECT_SPREAD -> SelectSpreadPhase(
                        uiState = uiState,
                        onQuestionChange = viewModel::updateQuestion,
                        onSelectSpread = viewModel::selectSpread,
                        onStartReading = viewModel::startReading
                    )

                    TarotPhase.DRAWING, TarotPhase.REVEALING -> DrawingPhase(uiState)

                    TarotPhase.INTERPRETING -> InterpretingPhase(uiState)

                    TarotPhase.RESULT -> ResultPhase(
                        uiState = uiState,
                        onNewReading = viewModel::newReading,
                        onBack = { navController.popBackStack() }
                    )

                    TarotPhase.ERROR -> ErrorPhase(
                        message = uiState.errorMessage ?: "未知错误",
                        onDismiss = viewModel::dismissError
                    )
                }
            }
        }
    }
}

// ── Phase composables ─────────────────────────────────────────────────────

@Composable
private fun SelectSpreadPhase(
    uiState: TarotUiState,
    onQuestionChange: (String) -> Unit,
    onSelectSpread: (SpreadType) -> Unit,
    onStartReading: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "—",
            fontSize = 48.sp,
            color = AccentTarot,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "聆听数据流中的神谕",
            color = TextSecondary,
            fontSize = 14.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "静心凝神，然后输入你的问题",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Question input
        OutlinedTextField(
            value = uiState.question,
            onValueChange = onQuestionChange,
            label = { Text("你想问什么？", color = TextSecondary) },
            placeholder = { Text("例如：我的感情运势如何？", color = TextMuted) },
            textStyle = LocalTextStyle.current.copy(
                color = TextPrimary,
                fontSize = 16.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentTarot,
                unfocusedBorderColor = CyberGray,
                cursorColor = AccentTarot,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            maxLines = 3
        )

        // Error
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = InauspiciousRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spread recommendation
        if (uiState.recommendedSpread != null) {
            val rec = uiState.recommendedSpread!!
            Text(
                text = "AI 推荐: ${rec.displayName}（${rec.cardCount}张牌）",
                color = AccentTarot.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Spread selection
        Text(
            text = "选择牌阵",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        SpreadType.entries.forEach { spread ->
            val isSelected = spread == uiState.selectedSpread
            val borderColor = if (isSelected) AccentTarot else CyberGray
            val bgColor = if (isSelected) AccentTarot.copy(alpha = 0.1f) else CyberDark

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSelectSpread(spread) }
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spread.displayName,
                        color = if (isSelected) AccentTarot else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${spread.cardCount}张牌 · ${spread.positions.joinToString(", ")}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                if (isSelected) {
                    Text("●", color = AccentTarot, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start button
        Button(
            onClick = onStartReading,
            enabled = uiState.question.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentTarot,
                contentColor = CyberBlack,
                disabledContainerColor = CyberGray,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                "开始占卜",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "赛博塔罗 · AI 增强解读",
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DrawingPhase(uiState: TarotUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = uiState.progressMessage,
            color = AccentTarot,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Show spread with cards
        SpreadLayout(
            spread = uiState.selectedSpread,
            cards = uiState.drawnCards,
            revealedCount = uiState.revealedCount
        )

        if (uiState.phase == TarotPhase.DRAWING) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = AccentTarot,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun InterpretingPhase(uiState: TarotUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Show revealed cards
        SpreadLayout(
            spread = uiState.selectedSpread,
            cards = uiState.drawnCards,
            revealedCount = uiState.drawnCards.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = uiState.progressMessage,
            color = AccentTarot,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        CircularProgressIndicator(
            color = AccentTarot,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
        )

        // Stream text preview
        if (uiState.streamText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                Text(
                    text = uiState.streamText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultPhase(
    uiState: TarotUiState,
    onNewReading: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Show cards at top
        SpreadLayout(
            spread = uiState.selectedSpread,
            cards = uiState.drawnCards,
            revealedCount = uiState.drawnCards.size
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Interpretation card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentTarot.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "赛博先知解读",
                    color = AccentTarot,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = uiState.interpretation,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("返回首页")
            }

            Button(
                onClick = onNewReading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentTarot,
                    contentColor = CyberBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("重新占卜", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ErrorPhase(message: String, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Text(text = "⚠", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
        Text(
            text = "占卜失败",
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
                containerColor = AccentTarot,
                contentColor = CyberBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("重新开始", fontWeight = FontWeight.Bold)
        }
    }
}
