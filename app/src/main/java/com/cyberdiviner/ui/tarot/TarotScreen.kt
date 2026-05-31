package com.cyberdiviner.ui.tarot
import com.cyberdiviner.ui.theme.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.shared.VoiceInputField
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBody
import com.cyberdiviner.ui.theme.GrayBorder
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GrayMuted
import com.cyberdiviner.ui.theme.GrayTitle
import com.cyberdiviner.ui.theme.HuiwenFontFamily
import com.cyberdiviner.ui.theme.AccentRed
import com.cyberdiviner.ui.theme.WenKaiFontFamily
import com.cyberdiviner.ui.theme.MonoFontFamily
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════════════
// TarotScreen — elegant B&W protocol-style layout
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun TarotScreen(
    navController: NavController,
    viewModel: TarotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Plain Column — no Scaffold, no TopAppBar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // ── Minimal navigation bar ──────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "<",
                color = GrayCaption,
                fontSize = 16.sp,
                fontFamily = MonoFontFamily,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "塔罗协议",
                color = GrayCaption,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 3.sp
            )
        }

        // ── Phase content ───────────────────────────────────────────────
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

                TarotPhase.SHUFFLING -> ShufflePhase(
                    onShuffleComplete = viewModel::shuffleAndDraw
                )

                TarotPhase.DRAWING, TarotPhase.REVEALING -> DrawingPhase(uiState)

                TarotPhase.INTERPRETING -> InterpretingPhase(uiState)

                TarotPhase.RESULT -> {
                    val annotations by viewModel.learningAnnotations.collectAsState()
                    ResultPhase(
                        uiState = uiState,
                        annotations = annotations,
                        onNewReading = viewModel::newReading,
                        onBack = { navController.popBackStack() }
                    )
                }

                TarotPhase.ERROR -> ErrorPhase(
                    message = uiState.errorMessage ?: "未知错误",
                    onDismiss = viewModel::dismissError
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Phase composables
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SelectSpreadPhase(
    uiState: TarotUiState,
    onQuestionChange: (String) -> Unit,
    onSelectSpread: (SpreadType) -> Unit,
    onStartReading: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Header ──
        SectionHeader(
            title = "塔罗协议",
            subtitle = "静心凝神，然后输入你的问题",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // ── Question input (shared VoiceInputField) ──────────────────
        VoiceInputField(
            text = uiState.question,
            onTextChange = onQuestionChange,
            onSend = onStartReading,
            placeholder = "你想问什么？",
            modifier = Modifier.fillMaxWidth()
        )

        // ── Error ─────────────────────────────────────────────────────
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = GrayBody,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Spread recommendation ─────────────────────────────────────
        if (uiState.recommendedSpread != null) {
            val rec = uiState.recommendedSpread!!
            Text(
                text = "推荐: ${rec.displayName}（${rec.cardCount}张牌）",
                color = GrayCaption,
                fontSize = 12.sp,
                fontFamily = WenKaiFontFamily,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // ── Spread selection ──────────────────────────────────────────
        Text(
            text = "选择牌阵",
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        SpreadType.entries.forEach { spread ->
            val isSelected = spread == uiState.selectedSpread
            val borderColor = if (isSelected) CyberWhite else GrayBorder
            val bgColor = if (isSelected) Color(0xFF111111) else CyberBlack

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(bgColor)
                    .border(1.dp, borderColor)
                    .clickable { onSelectSpread(spread) }
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spread.displayName,
                        color = if (isSelected) CyberWhite else GrayBody,
                        fontSize = 14.sp,
                        fontFamily = WenKaiFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${spread.cardCount}张牌 · ${spread.positions.joinToString(", ")}",
                        color = GrayMuted,
                        fontSize = 10.sp,
                        fontFamily = WenKaiFontFamily
                    )
                }
                if (isSelected) {
                    Text(
                        text = "|",
                        color = CyberWhite,
                        fontSize = 12.sp,
                        fontFamily = MonoFontFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Start button ──────────────────────────────────────────────
        CyberButton(
            text = "开始占卜",
            onClick = onStartReading,
            enabled = uiState.question.isNotBlank(),
            modifier = Modifier.height(52.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "赛博算命 · 玄学解读",
            color = GrayMuted,
            fontSize = 9.sp,
            fontFamily = WenKaiFontFamily,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Shuffle phase ────────────────────────────────────────────────────────

@Composable
private fun ShufflePhase(
    onShuffleComplete: () -> Unit
) {
    var isShuffling by remember { mutableStateOf(false) }
    var shuffleTextIndex by remember { mutableStateOf(0) }
    val shuffleTexts = listOf("洗牌中", "洗牌中.", "洗牌中..", "洗牌中...")

    val infiniteTransition = rememberInfiniteTransition(label = "shuffle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(animation = tween(200)),
        label = "shuffle_rotation"
    )

    LaunchedEffect(isShuffling) {
        if (isShuffling) {
            // Cycle shuffle text
            repeat(8) {
                delay(125L)
                shuffleTextIndex = (shuffleTextIndex + 1) % shuffleTexts.size
            }
            onShuffleComplete()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (isShuffling) {
            Text(
                text = shuffleTexts[shuffleTextIndex],
                color = GrayCaption,
                fontSize = 16.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 2.sp,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )
        } else {
            Text(
                text = "准备好了吗？",
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "洗 牌",
                color = CyberWhite,
                fontSize = 20.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                modifier = Modifier
                    .border(1.dp, CyberWhite)
                    .clickable { isShuffling = true }
                    .padding(horizontal = 40.dp, vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "轻触牌堆开始",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = WenKaiFontFamily,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Drawing / Revealing phase ─────────────────────────────────────────────

@Composable
private fun DrawingPhase(uiState: TarotUiState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1000)),
        label = "pulse_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = uiState.progressMessage,
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = MonoFontFamily,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CanvasSpreadLayout(
            spread = uiState.selectedSpread,
            cards = uiState.drawnCards,
            revealedCount = uiState.revealedCount
        )

        if (uiState.phase == TarotPhase.DRAWING) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = ". . .",
                color = GrayCaption.copy(alpha = pulseAlpha),
                fontSize = 18.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 8.sp
            )
        }
    }
}

// ── Interpreting phase ────────────────────────────────────────────────────

@Composable
private fun InterpretingPhase(uiState: TarotUiState) {
    val dots by rememberInfiniteTransition(label = "tarot_interpreting_dots")
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
    val animatedMessage = uiState.progressMessage + ".".repeat(dots)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        CanvasSpreadLayout(
            spread = uiState.selectedSpread,
            cards = uiState.drawnCards,
            revealedCount = uiState.drawnCards.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = animatedMessage,
            color = GrayCaption,
            fontSize = 13.sp,
            fontFamily = HuiwenFontFamily,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "牌阵已开，正在等候本地先知落笔",
            color = GrayMuted,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            textAlign = TextAlign.Center
        )

        if (uiState.streamText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .border(1.dp, GrayBorder)
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.streamText,
                    color = GrayBody,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    fontFamily = WenKaiFontFamily
                )
            }
        }
    }
}

// ── Result phase — matches Liuyao ancient-book style ──────────────────────

@Composable
private fun ResultPhase(
    uiState: TarotUiState,
    annotations: List<Pair<String, String>>,
    onNewReading: () -> Unit,
    onBack: () -> Unit
) {
    val cnNums = listOf("壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾")
    val totalPages = if (annotations.isNotEmpty()) 4 else 3 // +1 for learning annotations
    var currentPage by remember { mutableStateOf(0) }

    // Swipe detection
    var swipeOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset < -80f && currentPage < totalPages - 1) {
                            currentPage++
                        } else if (swipeOffset > 80f && currentPage > 0) {
                            currentPage--
                        }
                        swipeOffset = 0f
                    },
                    onDragCancel = { swipeOffset = 0f },
                    onHorizontalDrag = { _, amount -> swipeOffset += amount }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar (same as Liuyao) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< 返回",
                    color = GrayCaption,
                    fontSize = 13.sp,
                    fontFamily = HuiwenFontFamily,
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = "塔罗解读",
                    color = GrayCaption,
                    fontSize = 14.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "${cnNums[currentPage]}/${cnNums[totalPages - 1]}",
                    color = GrayMuted,
                    fontSize = 12.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 2.sp
                )
            }

            // ── Divider ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )

            // ── Card content (scrollable) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Book spine shadow
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (currentPage == 0) {
                        // ── Page 0: Fortune (四字批命) ──
                        Text(
                            text = "批命",
                            color = CyberWhite,
                            fontSize = 22.sp,
                            fontFamily = HuiwenFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        )
                        Text(
                            text = "FORTUNE",
                            color = GrayMuted,
                            fontSize = 10.sp,
                            fontFamily = MonoFontFamily,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.dp)
                                .background(AccentRed)
                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = uiState.fourCharFortune.ifBlank { "顺势而为" },
                            color = GrayTitle,
                            fontSize = 32.sp,
                            fontFamily = HuiwenFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.fourCharMeaning.ifBlank { "天时地利，可以有所作为" },
                            color = GrayBody,
                            fontSize = 14.sp,
                            fontFamily = WenKaiFontFamily,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else if (currentPage == 1) {
                        // ── Page 1: Spread ──
                        Text(
                            text = "牌阵",
                            color = CyberWhite,
                            fontSize = 22.sp,
                            fontFamily = HuiwenFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        )
                        Text(
                            text = "SPREAD",
                            color = GrayMuted,
                            fontSize = 10.sp,
                            fontFamily = MonoFontFamily,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.dp)
                                .background(AccentRed)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Spread layout
                        CanvasSpreadLayout(
                            spread = uiState.selectedSpread,
                            cards = uiState.drawnCards,
                            revealedCount = uiState.drawnCards.size
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Card list
                        uiState.drawnCards.forEach { card ->
                            val orientation = if (card.isReversed) "逆位" else "正位"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = card.position,
                                    color = GrayMuted,
                                    fontSize = 11.sp,
                                    fontFamily = HuiwenFontFamily,
                                    modifier = Modifier.width(72.dp)
                                )
                                Text(
                                    text = card.nameZh,
                                    color = CyberWhite,
                                    fontSize = 16.sp,
                                    fontFamily = HuiwenFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = orientation,
                                    color = if (card.isReversed) AccentRed else GrayBody,
                                    fontSize = 12.sp,
                                    fontFamily = WenKaiFontFamily
                                )
                            }
                        }
                    } else if (currentPage == 2) {
                        // ── Page 2: Interpretation ──
                        Text(
                            text = "解读",
                            color = CyberWhite,
                            fontSize = 22.sp,
                            fontFamily = HuiwenFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        )
                        Text(
                            text = "INTERPRETATION",
                            color = GrayMuted,
                            fontSize = 10.sp,
                            fontFamily = MonoFontFamily,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.dp)
                                .background(AccentRed)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Clean interpretation text
                        val cleanInterp = uiState.interpretation
                            .replace(Regex("[━─═]{4,}"), "")
                            .replace(Regex("━━━ .+ ━━━"), "")
                            .trim()

                        if (cleanInterp.isBlank()) {
                            Text(
                                text = "赛博先知解读中...",
                                color = GrayMuted,
                                fontSize = 13.sp,
                                fontFamily = WenKaiFontFamily
                            )
                        } else {
                            Text(
                                text = cleanInterp,
                                color = GrayBody,
                                fontSize = 14.sp,
                                fontFamily = WenKaiFontFamily,
                                lineHeight = 26.sp
                            )
                        }
                    } else {
                        // ── Page 3: Learning Annotations ──
                        Text(
                            text = "学习",
                            color = CyberWhite,
                            fontSize = 22.sp,
                            fontFamily = HuiwenFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        )
                        Text(
                            text = "LEARNING",
                            color = GrayMuted,
                            fontSize = 11.sp,
                            fontFamily = MonoFontFamily,
                            letterSpacing = 3.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        annotations.forEach { (title, text) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = title,
                                        color = AccentRed,
                                        fontSize = 14.sp,
                                        fontFamily = HuiwenFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = text,
                                        color = GrayBody,
                                        fontSize = 13.sp,
                                        fontFamily = WenKaiFontFamily,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Right page edge shadow
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GrayBorder.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            // ── Bottom divider ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrayBorder)
            )

            // Swipe hint
            Text(
                text = if (currentPage < totalPages - 1) "< 左滑翻页 >" else "< 右滑返回 >",
                color = GrayMuted,
                fontSize = 10.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Page dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalPages) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (i == currentPage) 16.dp else 6.dp)
                            .height(3.dp)
                            .background(
                                if (i == currentPage) CyberWhite else GrayBorder,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            // Action buttons (same as Liuyao)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(CyberWhite, RoundedCornerShape(6.dp))
                        .clickable { onNewReading() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "重新占卜",
                        color = CyberBlack,
                        fontSize = 13.sp,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Error phase ───────────────────────────────────────────────────────────

@Composable
private fun ErrorPhase(message: String, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "错误",
            color = GrayCaption,
            fontSize = 24.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = message,
            color = GrayCaption,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            fontFamily = WenKaiFontFamily,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        CyberButton(
            text = "重新开始",
            onClick = onDismiss
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Canvas geometric card rendering
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GeometricTarotCard(
    card: TarotCard,
    isRevealed: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "card_flip"
    )
    val isFront = rotation > 90f

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, GrayBorder, RoundedCornerShape(6.dp))
                .background(CyberBlack)
        ) {
            if (isFront) {
                GeometricCardFront(card)
            } else {
                GeometricCardBack()
            }
        }
    }
}

@Composable
private fun GeometricCardFront(card: TarotCard) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            val cx = size.width / 2
            val cy = size.height / 2
            val scale = minOf(size.width, size.height) * 0.35f

            if (card.isReversed) {
                rotate(180f, Offset(cx, cy)) {
                    drawSuitGeometry(card.suit, cx, cy, scale)
                }
            } else {
                drawSuitGeometry(card.suit, cx, cy, scale)
            }
        }

        // Card name at bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Text(
                text = card.nameZh,
                color = GrayBody,
                fontSize = 8.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Draw abstract geometric shapes based on card suit.
 * Major = concentric circles + cross, Wands = upward triangle,
 * Cups = inverted triangle, Swords = cross + diamond,
 * Pentacles = circle + diamond.
 */
private fun DrawScope.drawSuitGeometry(
    suit: String,
    cx: Float,
    cy: Float,
    scale: Float
) {
    val shapeColor = GrayBody
    val strokeWidth = 1.dp.toPx()
    val stroke = Stroke(width = strokeWidth)

    when (suit) {
        "major" -> {
            drawCircle(shapeColor, radius = scale, center = Offset(cx, cy), style = stroke)
            drawCircle(shapeColor, radius = scale * 0.55f, center = Offset(cx, cy), style = stroke)
            drawLine(shapeColor, Offset(cx, cy - scale * 0.3f), Offset(cx, cy + scale * 0.3f), strokeWidth)
            drawLine(shapeColor, Offset(cx - scale * 0.3f, cy), Offset(cx + scale * 0.3f, cy), strokeWidth)
        }
        "wands" -> {
            val path = Path().apply {
                moveTo(cx, cy - scale)
                lineTo(cx - scale * 0.87f, cy + scale * 0.5f)
                lineTo(cx + scale * 0.87f, cy + scale * 0.5f)
                close()
            }
            drawPath(path, shapeColor, style = stroke)
        }
        "cups" -> {
            val path = Path().apply {
                moveTo(cx, cy + scale)
                lineTo(cx - scale * 0.87f, cy - scale * 0.5f)
                lineTo(cx + scale * 0.87f, cy - scale * 0.5f)
                close()
            }
            drawPath(path, shapeColor, style = stroke)
        }
        "swords" -> {
            val arm = scale * 0.85f
            drawLine(shapeColor, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth)
            drawLine(shapeColor, Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth)
            val d = scale * 0.2f
            val diamond = Path().apply {
                moveTo(cx, cy - d)
                lineTo(cx + d, cy)
                lineTo(cx, cy + d)
                lineTo(cx - d, cy)
                close()
            }
            drawPath(diamond, shapeColor, style = stroke)
        }
        "pentacles" -> {
            drawCircle(shapeColor, radius = scale * 0.8f, center = Offset(cx, cy), style = stroke)
            val r = scale * 0.5f
            val diamond = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r, cy)
                lineTo(cx, cy + r)
                lineTo(cx - r, cy)
                close()
            }
            drawPath(diamond, shapeColor, style = stroke)
        }
    }
}

@Composable
private fun GeometricCardBack() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
    ) {
        val sw = 0.5.dp.toPx()
        // Grid lines
        val stepX = size.width / 4f
        for (i in 1..3) {
            drawLine(GrayMuted, Offset(stepX * i, 0f), Offset(stepX * i, size.height), sw)
        }
        val stepY = size.height / 6f
        for (i in 1..5) {
            drawLine(GrayMuted, Offset(0f, stepY * i), Offset(size.width, stepY * i), sw)
        }
        // Central diamond
        val cx = size.width / 2
        val cy = size.height / 2
        val r = minOf(size.width, size.height) * 0.12f
        val diamond = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        drawPath(diamond, GrayMuted, style = Stroke(sw))
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Canvas spread layouts
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CanvasSpreadLayout(
    spread: SpreadType,
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier = Modifier
) {
    when (spread) {
        SpreadType.SINGLE -> CanvasSingleSpread(cards, revealedCount, modifier)
        SpreadType.THREE_CARD -> CanvasThreeCardSpread(cards, revealedCount, modifier)
        SpreadType.CELTIC_CROSS -> CanvasCelticCrossSpread(cards, revealedCount, modifier)
        SpreadType.HORSESHOE -> CanvasHorseshoeSpread(cards, revealedCount, modifier)
    }
}

@Composable
private fun CanvasSingleSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        if (cards.isNotEmpty()) {
            GeometricTarotCard(
                card = cards[0],
                isRevealed = revealedCount > 0,
                modifier = Modifier
                    .width(90.dp)
                    .height(150.dp)
            )
        }
    }
}

@Composable
private fun CanvasThreeCardSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Position labels
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            cards.forEachIndexed { i, card ->
                if (revealedCount > i) {
                    Text(
                        card.position,
                        color = GrayMuted,
                        fontSize = 9.sp,
                        fontFamily = MonoFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(90.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(90.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cards
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            cards.forEachIndexed { i, card ->
                GeometricTarotCard(
                    card = card,
                    isRevealed = revealedCount > i,
                    modifier = Modifier
                        .width(90.dp)
                        .height(150.dp)
                )
            }
        }
    }
}

@Composable
private fun CanvasCelticCrossSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    val cardMod = Modifier
        .width(70.dp)
        .height(116.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Card 4 (top of cross)
        Row(horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(80.dp))
            if (cards.size > 4) {
                GeometricTarotCard(cards[4], revealedCount > 4, cardMod)
            }
        }

        // Cards 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.take(3).forEachIndexed { i, card ->
                GeometricTarotCard(card, revealedCount > i, cardMod)
                if (i < 2) Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // Card 7 (bottom of cross)
        Row(horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(80.dp))
            if (cards.size > 6) {
                GeometricTarotCard(cards[6], revealedCount > 6, cardMod)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card 5 (staff)
        if (cards.size > 5) {
            GeometricTarotCard(cards[5], revealedCount > 5, cardMod)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cards 8, 9, 10
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (idx in 7..9) {
                if (cards.size > idx) {
                    GeometricTarotCard(cards[idx], revealedCount > idx, cardMod)
                }
            }
        }
    }
}

@Composable
private fun CanvasHorseshoeSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    val cardMod = Modifier
        .width(72.dp)
        .height(120.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: cards 0, 3, 6
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(0, 3, 6).forEach { idx ->
                if (cards.size > idx) {
                    GeometricTarotCard(cards[idx], revealedCount > idx, cardMod)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 2: cards 1, 5
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(1, 5).forEach { idx ->
                if (cards.size > idx) {
                    GeometricTarotCard(cards[idx], revealedCount > idx, cardMod)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 3: cards 2, 4
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(2, 4).forEach { idx ->
                if (cards.size > idx) {
                    GeometricTarotCard(cards[idx], revealedCount > idx, cardMod)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shared UI elements
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun DashedSeparator() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = GrayBorder,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        )
    }
}
