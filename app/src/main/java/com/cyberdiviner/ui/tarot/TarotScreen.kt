package com.cyberdiviner.ui.tarot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.cyberdiviner.ui.shared.VoiceInputField
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBody
import com.cyberdiviner.ui.theme.GrayBorder
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GrayMuted
import com.cyberdiviner.ui.theme.GrayTitle
import com.cyberdiviner.ui.theme.SerifDisplay

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
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "TAROT PROTOCOL",
                color = GrayCaption,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
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

        // ── Header ────────────────────────────────────────────────────
        Text(
            text = "聆听数据流中的神谕",
            color = GrayCaption,
            fontSize = 13.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "静心凝神，然后输入你的问题",
            color = GrayMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 24.dp)
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
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // ── Spread selection ──────────────────────────────────────────
        Text(
            text = "选择牌阵",
            color = GrayTitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable { onSelectSpread(spread) }
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spread.displayName,
                        color = if (isSelected) CyberWhite else GrayBody,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${spread.cardCount}张牌 · ${spread.positions.joinToString(", ")}",
                        color = GrayMuted,
                        fontSize = 10.sp
                    )
                }
                if (isSelected) {
                    Text(
                        text = "|",
                        color = CyberWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
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
            text = "CYBER DIVINER · AI ENHANCED READING",
            color = GrayMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
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
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
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
                fontFamily = FontFamily.Monospace,
                letterSpacing = 8.sp
            )
        }
    }
}

// ── Interpreting phase ────────────────────────────────────────────────────

@Composable
private fun InterpretingPhase(uiState: TarotUiState) {
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
            text = uiState.progressMessage,
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
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
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    fontFamily = SerifDisplay
                )
            }
        }
    }
}

// ── Result phase ──────────────────────────────────────────────────────────

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
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Archive header ────────────────────────────────────────────
        Text(
            text = "[ ARCHIVE: TAROT PROTOCOL ]",
            color = GrayCaption,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Dashed separator ──────────────────────────────────────────
        DashedSeparator()

        Spacer(modifier = Modifier.height(20.dp))

        // ── Geometric card spread ─────────────────────────────────────
        CanvasSpreadLayout(
            spread = uiState.selectedSpread,
            cards = uiState.drawnCards,
            revealedCount = uiState.drawnCards.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Card titles in serif ──────────────────────────────────────
        uiState.drawnCards.forEach { card ->
            val orientation = if (card.isReversed) "逆位" else "正位"
            Text(
                text = "${card.nameZh} · $orientation",
                color = GrayTitle,
                fontSize = 18.sp,
                fontFamily = SerifDisplay,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (card.position.isNotEmpty()) {
                Text(
                    text = card.position,
                    color = GrayMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Dashed separator ──────────────────────────────────────────
        DashedSeparator()

        Spacer(modifier = Modifier.height(20.dp))

        // ── Interpretation body ───────────────────────────────────────
        Text(
            text = uiState.interpretation,
            color = GrayBody,
            fontSize = 14.sp,
            fontFamily = SerifDisplay,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Action buttons ────────────────────────────────────────────
        CyberButton(
            text = "重新占卜",
            onClick = onNewReading
        )

        Spacer(modifier = Modifier.height(12.dp))

        CyberButton(
            text = "返回",
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(24.dp))
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
            text = "ERROR",
            color = GrayBody,
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = message,
            color = GrayCaption,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
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
                        fontFamily = FontFamily.Monospace,
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
