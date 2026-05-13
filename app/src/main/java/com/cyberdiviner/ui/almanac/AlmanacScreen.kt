package com.cyberdiviner.ui.almanac

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.AlmanacEngine
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.theme.*
import java.time.LocalDate

/**
 * AlmanacScreen -- 赛博黄历 (Cyber Almanac)
 *
 * Displays the full daily almanac reading:
 *   - Lunar date + Ganzhi
 *   - Solar term
 *   - Zodiac
 *   - Auspicious/inauspicious activities
 *   - Daily quote
 *   - Element advice + lucky colors/numbers
 *
 * Pure B&W aesthetic. No emoji, no neon.
 */
@Composable
fun AlmanacScreen(
    onBack: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──
        Text(
            text = "赛博黄历",
            color = GrayTitle,
            fontFamily = HuiwenFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Canvas(
            modifier = Modifier
                .width(120.dp)
                .padding(top = 4.dp)
                .height(2.dp)
        ) {
            drawRect(
                color = AccentRed,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "通书算法 · 全本地化",
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // ── Date Block ──
        SectionTitle("日期")
        InfoRow("阳历", "${today.year}年${today.monthValue}月${today.dayOfMonth}日")
        InfoRow("农历", "${reading.lunarDate.monthName}${reading.lunarDate.dayName}")
        InfoRow("干支", "${reading.yearGanzhi.combined}年 ${reading.monthGanzhi.combined}月 ${reading.dayGanzhi.combined}日")
        if (reading.hourGanzhi != null) {
            InfoRow("时辰", reading.hourGanzhi.combined)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Zodiac & Solar Term ──
        SectionTitle("天文")
        InfoRow("生肖", "${reading.zodiac} (${reading.zodiacEnglish})")
        reading.currentSolarTerm?.let { term ->
            InfoRow("节气", "${term.name} (${term.englishName})")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Daily Quote ──
        SectionTitle("签语")
        QuoteBlock(reading.dailyQuote.text)
        Spacer(modifier = Modifier.height(16.dp))

        // ── Auspicious Activities ──
        SectionTitle("今日宜")
        reading.auspiciousActivities.forEach { act ->
            ActivityRow(name = act.name, english = act.englishName, isAuspicious = true)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Inauspicious Activities ──
        SectionTitle("今日忌")
        reading.inauspiciousActivities.forEach { act ->
            ActivityRow(name = act.name, english = act.englishName, isAuspicious = false)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Energy & Advice ──
        SectionTitle("运势")
        InfoRow("能量", reading.dailyEnergy)
        InfoRow("五行", reading.elementAdvice)
        InfoRow("吉祥色", reading.luckyColors.joinToString(" / "))
        InfoRow("吉祥数", reading.luckyNumbers.joinToString(" / "))
        if (reading.warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            reading.warnings.forEach { w ->
                WarningLine(w)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Overview ──
        SectionTitle("综合")
        Text(
            text = reading.overview,
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // ── Back ──
        CyberButton(
            text = "[ 返回 ]",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Reusable composables ──────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = AccentRed,
        fontFamily = HuiwenFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = GrayCaption,
            fontSize = 12.sp,
            fontFamily = WenKaiFontFamily,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily
        )
    }
}

@Composable
private fun QuoteBlock(text: String) {
    Text(
        text = text,
        color = GrayTitle,
        fontSize = 14.sp,
        fontFamily = WenKaiFontFamily,
        lineHeight = 22.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(GraySurface, RoundedCornerShape(4.dp))
            .padding(12.dp)
    )
}

@Composable
private fun ActivityRow(name: String, english: String, isAuspicious: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isAuspicious) "+" else "-",
            color = if (isAuspicious) GrayBody else GrayCaption,
            fontSize = 12.sp,
            fontFamily = MonoFontFamily,
            modifier = Modifier.width(16.dp)
        )
        Text(
            text = name,
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = english,
            color = GrayCaption,
            fontSize = 11.sp,
            fontFamily = MonoFontFamily
        )
    }
}

@Composable
private fun WarningLine(text: String) {
    Text(
        text = text,
        color = GrayCaption,
        fontSize = 12.sp,
        fontFamily = WenKaiFontFamily,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}
