package com.cyberdiviner.ui.almanac

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.AlmanacEngine
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * AlmanacScreen -- 赛博黄历 (Cyber Almanac)
 *
 * Poster-quality, share-worthy card-based layout.
 * Designed for "screenshot → share to WeChat Moments" aesthetic.
 *
 * Pure B&W aesthetic with AccentRed accents.
 * No emoji, no Material ripple. StrokeCap.Square only.
 */
@Composable
fun AlmanacScreen(
    onBack: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }

    // Generate a deterministic hash for the watermark
    val dayHash = remember {
        val seed = today.toEpochDay()
        String.format("0x%08X", seed.hashCode())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ════════════════════════════════════════════════════════════════
        // HERO SECTION — Day GanZhi as the dominant visual element
        // ════════════════════════════════════════════════════════════════

        // Year / Month GanZhi (smaller, above the day)
        Text(
            text = "${reading.yearGanzhi.combined}年  ${reading.monthGanzhi.combined}月",
            color = GrayCaption,
            fontFamily = HuiwenFontFamily,
            fontSize = 20.sp,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Day GanZhi — HERO ELEMENT
        Text(
            text = reading.dayGanzhi.combined,
            color = GrayTitle,
            fontFamily = HuiwenFontFamily,
            fontSize = 56.sp,
            letterSpacing = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )

        // Hour GanZhi if available
        reading.hourGanzhi?.let { hour ->
            Text(
                text = hour.combined + "时",
                color = GrayCaption,
                fontFamily = HuiwenFontFamily,
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Solar date in monospace, small and muted
        Text(
            text = today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            color = GrayMuted,
            fontFamily = MonoFontFamily,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        // Lunar date
        Text(
            text = "农历 ${reading.lunarDate.monthName}${reading.lunarDate.dayName}",
            color = GrayMuted,
            fontFamily = WenKaiFontFamily,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Thin AccentRed divider
        AccentDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════════════════
        // CARD: 天文 — Zodiac & Solar Term
        // ════════════════════════════════════════════════════════════════
        AlmanacCard(title = "天文") {
            CardInfoRow(label = "生肖", value = reading.zodiac)
            CardInfoRow(label = "EN", value = reading.zodiacEnglish, isMono = true)
            reading.currentSolarTerm?.let { term ->
                Spacer(modifier = Modifier.height(4.dp))
                CardInfoRow(label = "节气", value = term.name)
                CardInfoRow(label = "EN", value = term.englishName, isMono = true)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ════════════════════════════════════════════════════════════════
        // CARD: 签语 — Daily Quote
        // ════════════════════════════════════════════════════════════════
        AlmanacCard(title = "签语") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GraySurface, RoundedCornerShape(0.dp))
                    .border(1.dp, GrayBorder, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "「${reading.dailyQuote.text}」",
                    color = GrayTitle,
                    fontSize = 15.sp,
                    fontFamily = WenKaiFontFamily,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ════════════════════════════════════════════════════════════════
        // CARD: 宜 — Auspicious Activities
        // ════════════════════════════════════════════════════════════════
        AlmanacCard(title = "宜") {
            ActivityGrid(
                activities = reading.auspiciousActivities,
                isAuspicious = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ════════════════════════════════════════════════════════════════
        // CARD: 忌 — Inauspicious Activities
        // ════════════════════════════════════════════════════════════════
        AlmanacCard(title = "忌") {
            ActivityGrid(
                activities = reading.inauspiciousActivities,
                isAuspicious = false
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ════════════════════════════════════════════════════════════════
        // CARD: 五行 — Energy, Elements, Lucky
        // ════════════════════════════════════════════════════════════════
        AlmanacCard(title = "五行") {
            CardInfoRow(label = "能量", value = reading.dailyEnergy)
            CardInfoRow(label = "五行", value = reading.elementAdvice)

            Spacer(modifier = Modifier.height(8.dp))

            // Lucky Colors — displayed with small bordered squares
            Text(
                text = "吉祥色",
                color = GrayCaption,
                fontFamily = WenKaiFontFamily,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                reading.luckyColors.forEach { colorName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Small bordered square (B&W aesthetic — shows color name)
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(1.dp, GrayBorder, RoundedCornerShape(0.dp))
                                .background(GraySurface, RoundedCornerShape(0.dp))
                        )
                        Text(
                            text = colorName,
                            color = GrayBody,
                            fontFamily = WenKaiFontFamily,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lucky Numbers
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "吉祥数",
                    color = GrayCaption,
                    fontFamily = WenKaiFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = reading.luckyNumbers.joinToString("  "),
                    color = GrayBody,
                    fontFamily = MonoFontFamily,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }

            // Warnings
            if (reading.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                reading.warnings.forEach { w ->
                    Text(
                        text = w,
                        color = GrayCaption,
                        fontFamily = WenKaiFontFamily,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ════════════════════════════════════════════════════════════════
        // CARD: 综合 — Overview
        // ════════════════════════════════════════════════════════════════
        AlmanacCard(title = "综合") {
            Text(
                text = reading.overview,
                color = GrayBody,
                fontSize = 13.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ════════════════════════════════════════════════════════════════
        // WATERMARK HASH — authenticity feel
        // ════════════════════════════════════════════════════════════════
        Text(
            text = "SIG:${dayHash}  EPOCH:${today.toEpochDay()}  GZ:${reading.dayGanzhi.combined}",
            color = GrayMuted,
            fontFamily = MonoFontFamily,
            fontSize = 7.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════════════════
        // FOOTER — AccentRed line + version
        // ════════════════════════════════════════════════════════════════
        AccentDivider()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "CYBERDIVINER v6.0",
            color = GrayMuted,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Back button
        CyberButton(
            text = "[ 返回 ]",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ════════════════════════════════════════════════════════════════════════
// REUSABLE COMPOSABLES
// ════════════════════════════════════════════════════════════════════════

/**
 * Card container with GrayBorder, sharp corners, internal padding.
 * Each card has a section title with AccentRed indicator line.
 */
@Composable
private fun AlmanacCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder, RoundedCornerShape(0.dp))
            .padding(16.dp)
    ) {
        // Section header with red indicator line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            // Small red square indicator
            Canvas(modifier = Modifier.size(8.dp)) {
                drawRect(
                    color = AccentRed,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = AccentRed,
                fontFamily = HuiwenFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }

        // Card content
        content()
    }
}

/**
 * Thin AccentRed horizontal divider line (Canvas-based, square caps).
 */
@Composable
private fun AccentDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = AccentRed,
            start = Offset.Zero,
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Square
        )
    }
}

/**
 * Key:value info row inside a card.
 */
@Composable
private fun CardInfoRow(
    label: String,
    value: String,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            modifier = Modifier.width(48.dp),
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = if (isMono) MonoFontFamily else WenKaiFontFamily,
            letterSpacing = if (isMono) 1.sp else 0.5.sp
        )
    }
}

/**
 * Grid-like layout for activities (2 columns).
 * Each item shows Chinese name + English name below in smaller muted text.
 */
@Composable
private fun ActivityGrid(
    activities: List<AlmanacEngine.DailyActivity>,
    isAuspicious: Boolean
) {
    // Split into pairs for 2-column layout
    val pairs = activities.chunked(2)

    pairs.forEach { pair ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            pair.forEach { activity ->
                ActivityItem(
                    name = activity.name,
                    english = activity.englishName,
                    isAuspicious = isAuspicious,
                    modifier = Modifier.weight(1f)
                )
            }
            // Fill remaining space if odd number
            if (pair.size < 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Single activity item: Chinese name on top, English below in muted mono.
 * Preceded by a small square indicator.
 */
@Composable
private fun ActivityItem(
    name: String,
    english: String,
    isAuspicious: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Small square indicator
        Canvas(modifier = Modifier.padding(top = 5.dp).size(5.dp)) {
            drawRect(
                color = if (isAuspicious) GrayBody else GrayCaption,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }
        Column {
            Text(
                text = name,
                color = GrayBody,
                fontSize = 13.sp,
                fontFamily = WenKaiFontFamily,
                letterSpacing = 0.5.sp
            )
            Text(
                text = english,
                color = GrayMuted,
                fontSize = 10.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 0.5.sp
            )
        }
    }
}
