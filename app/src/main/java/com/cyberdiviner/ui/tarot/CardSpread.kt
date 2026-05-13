package com.cyberdiviner.ui.tarot

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.*

// ── Card composable with flip animation ───────────────────────────────────

@Composable
fun TarotCardView(
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
            .width(90.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (isFront) {
            CardFront(card)
        } else {
            CardBack()
        }
    }
}

@Composable
private fun CardFront(card: TarotCard) {
    val borderColor = when (card.suit) {
        "major" -> AccentTarot
        "wands" -> CyberTertiary
        "cups" -> NeonBlue
        "swords" -> CyberPrimary
        "pentacles" -> AccentVision
        else -> CyberSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            // Suit icon
            when (card.suit) {
                "major" -> Text("*", color = borderColor, fontSize = 14.sp)
                "wands" -> Icon(Icons.Default.FlashOn, contentDescription = null, tint = borderColor, modifier = Modifier.size(20.dp))
                "cups" -> Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = borderColor, modifier = Modifier.size(20.dp))
                "swords" -> Icon(Icons.Default.Gavel, contentDescription = null, tint = borderColor, modifier = Modifier.size(20.dp))
                "pentacles" -> Icon(Icons.Default.AttachMoney, contentDescription = null, tint = borderColor, modifier = Modifier.size(20.dp))
                else -> Text("*", color = borderColor, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.nameZh,
                color = borderColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = card.name,
                color = TextSecondary,
                fontSize = 7.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (card.isReversed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("逆位", color = InauspiciousRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .border(1.5.dp, CyberSecondary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("*", color = CyberSecondary.copy(alpha = 0.6f), fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "CYBER\nDIVINER",
                color = CyberSecondary.copy(alpha = 0.4f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 9.sp
            )
        }
    }
}

// ── Spread layouts ────────────────────────────────────────────────────────

@Composable
fun SpreadLayout(
    spread: SpreadType,
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier = Modifier
) {
    when (spread) {
        SpreadType.SINGLE -> SingleSpread(cards, revealedCount, modifier)
        SpreadType.THREE_CARD -> ThreeCardSpread(cards, revealedCount, modifier)
        SpreadType.CELTIC_CROSS -> CelticCrossSpread(cards, revealedCount, modifier)
        SpreadType.HORSESHOE -> HorseshoeSpread(cards, revealedCount, modifier)
    }
}

@Composable
private fun SingleSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        if (cards.isNotEmpty()) {
            TarotCardView(
                card = cards[0],
                isRevealed = revealedCount > 0
            )
            if (revealedCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(cards[0].position, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ThreeCardSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Position labels row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            cards.forEachIndexed { i, card ->
                if (revealedCount > i) {
                    Text(
                        card.position,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(90.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(90.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cards row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            cards.forEachIndexed { i, card ->
                TarotCardView(
                    card = card,
                    isRevealed = revealedCount > i
                )
            }
        }
    }
}

@Composable
private fun CelticCrossSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    // Celtic Cross layout:
    //   4
    // 1 2 3    5 6
    //   7
    //  8 9 10
    val cardSize = Modifier.width(70.dp).height(116.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: card 4 (top of cross)
        Row(horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(100.dp))
            if (cards.size > 4) {
                Box {
                    TarotCardView(cards[4], revealedCount > 4, cardSize)
                    if (revealedCount > 4) {
                        Text(
                            cards[4].position,
                            color = TextMuted,
                            fontSize = 7.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.BottomCenter).width(70.dp)
                        )
                    }
                }
            }
        }

        // Row 2: cards 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.take(3).forEachIndexed { i, card ->
                Box {
                    TarotCardView(card, revealedCount > i, cardSize)
                    if (revealedCount > i) {
                        Text(
                            card.position,
                            color = TextMuted,
                            fontSize = 7.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.BottomCenter).width(70.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // Row 3: card 7 (bottom of cross)
        Row(horizontalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.width(100.dp))
            if (cards.size > 6) {
                Box {
                    TarotCardView(cards[6], revealedCount > 6, cardSize)
                    if (revealedCount > 6) {
                        Text(
                            cards[6].position,
                            color = TextMuted,
                            fontSize = 7.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.BottomCenter).width(70.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Staff: cards 5, 6
        Row(horizontalArrangement = Arrangement.Center) {
            listOf(5).forEach { idx ->
                if (cards.size > idx) {
                    Box {
                        TarotCardView(cards[idx], revealedCount > idx, cardSize)
                        if (revealedCount > idx) {
                            Text(
                                cards[idx].position,
                                color = TextMuted,
                                fontSize = 7.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.BottomCenter).width(70.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Final 3: cards 8, 9, 10
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            for (idx in 7..9) {
                if (cards.size > idx) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TarotCardView(cards[idx], revealedCount > idx, cardSize)
                        if (revealedCount > idx) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                cards[idx].position,
                                color = TextMuted,
                                fontSize = 7.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorseshoeSpread(
    cards: List<TarotCard>,
    revealedCount: Int,
    modifier: Modifier
) {
    // Horseshoe: left side (1,2,3) going up, top (4), right side (5,6,7) going down
    val cardSize = Modifier.width(72.dp).height(120.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: left 1, top 4, right 7
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(0, 3, 6).forEach { idx ->
                if (cards.size > idx) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TarotCardView(cards[idx], revealedCount > idx, cardSize)
                        if (revealedCount > idx) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cards[idx].position, color = TextMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 2: left 2, right 6
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(1, 5).forEach { idx ->
                if (cards.size > idx) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TarotCardView(cards[idx], revealedCount > idx, cardSize)
                        if (revealedCount > idx) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cards[idx].position, color = TextMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 3: left 3, right 5
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(2, 4).forEach { idx ->
                if (cards.size > idx) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TarotCardView(cards[idx], revealedCount > idx, cardSize)
                        if (revealedCount > idx) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cards[idx].position, color = TextMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
