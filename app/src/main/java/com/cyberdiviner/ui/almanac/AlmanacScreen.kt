package com.cyberdiviner.ui.almanac

import com.cyberdiviner.BuildConfig
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
 * AlmanacScreen -- 赛博黄历
 *
 * 海报级卡片布局，截图分享友好。
 * 纯黑白 + AccentRed 点缀。无 emoji。
 */
@Composable
fun AlmanacScreen(
    onBack: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }

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
        // ══════════════════════════════════════════════════════════════
        // HERO SECTION
        // ══════════════════════════════════════════════════════════════

        // 年月 — 汇文明朝体
        Text(
            text = "${reading.yearGanzhi.combined}年  ${reading.monthGanzhi.combined}月",
            color = CyberWhite,
            fontFamily = HuiwenFontFamily,
            fontSize = 20.sp,
            letterSpacing = 6.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 日干支 — HERO
        Text(
            text = reading.dayGanzhi.combined,
            color = CyberWhite,
            fontFamily = HuiwenFontFamily,
            fontSize = 58.sp,
            letterSpacing = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )

        // 时辰
        reading.hourGanzhi?.let { hour ->
            Text(
                text = hour.combined + "时",
                color = GrayCaption,
                fontFamily = HuiwenFontFamily,
                fontSize = 16.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 阳历
        Text(
            text = today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            color = GrayCaption,
            fontFamily = MonoFontFamily,
            fontSize = 12.sp,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center
        )

        // 农历
        Text(
            text = "农历 ${reading.lunarDate.monthName}${reading.lunarDate.dayName}",
            color = GrayCaption,
            fontFamily = WenKaiFontFamily,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
        AccentDivider()
        Spacer(modifier = Modifier.height(20.dp))

        // ══════════════════════════════════════════════════════════════
        // CARD: 天文
        // ══════════════════════════════════════════════════════════════
        AlmanacCard(title = "天文") {
            CardInfoRow(label = "生肖", value = reading.zodiac)
            CardInfoRow(label = "EN", value = reading.zodiacEnglish, isMono = true)
            reading.currentSolarTerm?.let { term ->
                Spacer(modifier = Modifier.height(4.dp))
                CardInfoRow(label = "节气", value = term.name)
                CardInfoRow(label = "EN", value = term.englishName, isMono = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ══════════════════════════════════════════════════════════════
        // CARD: 签语
        // ══════════════════════════════════════════════════════════════
        AlmanacCard(title = "签语") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBlack, RoundedCornerShape(0.dp))
                    .border(1.dp, GrayBorder, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "「${reading.dailyQuote.text}」",
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontFamily = WenKaiFontFamily,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ══════════════════════════════════════════════════════════════
        // CARD: 宜
        // ══════════════════════════════════════════════════════════════
        AlmanacCard(title = "宜") {
            ActivityGrid(
                activities = reading.auspiciousActivities,
                isAuspicious = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ══════════════════════════════════════════════════════════════
        // CARD: 忌
        // ══════════════════════════════════════════════════════════════
        AlmanacCard(title = "忌") {
            ActivityGrid(
                activities = reading.inauspiciousActivities,
                isAuspicious = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ══════════════════════════════════════════════════════════════
        // CARD: 五行
        // ══════════════════════════════════════════════════════════════
        AlmanacCard(title = "五行") {
            CardInfoRow(label = "能量", value = reading.dailyEnergy)
            CardInfoRow(label = "五行", value = reading.elementAdvice)

            Spacer(modifier = Modifier.height(10.dp))

            // 吉祥色
            Text(
                text = "吉祥色",
                color = CyberWhite,
                fontFamily = HuiwenFontFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                reading.luckyColors.forEach { colorName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(1.dp, CyberWhite, RoundedCornerShape(0.dp))
                        )
                        Text(
                            text = colorName,
                            color = CyberWhite,
                            fontFamily = WenKaiFontFamily,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 吉祥数
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "吉祥数",
                    color = CyberWhite,
                    fontFamily = HuiwenFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = reading.luckyNumbers.joinToString("  "),
                    color = CyberWhite,
                    fontFamily = MonoFontFamily,
                    fontSize = 15.sp,
                    letterSpacing = 3.sp
                )
            }

            // 警告
            if (reading.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // ══════════════════════════════════════════════════════════════
        // CARD: 综合
        // ══════════════════════════════════════════════════════════════
        AlmanacCard(title = "综合") {
            Text(
                text = reading.overview,
                color = CyberWhite,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 哈希水印
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

        // Footer
        AccentDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "CYBERDIVINER v${BuildConfig.VERSION_NAME}",
            color = GrayMuted,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        CyberButton(
            text = "[ 返回 ]",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ════════════════════════════════════════════════════════════════════
// 组件
// ════════════════════════════════════════════════════════════════════

/**
 * 卡片容器 — 1dp CyberWhite 边框 + AccentRed 标题指示器
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
        // 红色方块指示器 + 标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawRect(
                    color = AccentRed,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = AccentRed,
                fontFamily = HuiwenFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 3.sp
            )
        }
        content()
    }
}

/**
 * AccentRed 水平分割线
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
 * 键值对信息行
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
            .padding(vertical = 3.dp),
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
            color = CyberWhite,
            fontSize = 14.sp,
            fontFamily = if (isMono) MonoFontFamily else WenKaiFontFamily,
            letterSpacing = if (isMono) 1.sp else 0.5.sp
        )
    }
}

/**
 * 双列活动网格
 */
@Composable
private fun ActivityGrid(
    activities: List<AlmanacEngine.DailyActivity>,
    isAuspicious: Boolean
) {
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
            if (pair.size < 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * 单个活动项 — 方块指示器 + 中文名 + 英文名
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
        Canvas(modifier = Modifier.padding(top = 5.dp).size(5.dp)) {
            drawRect(
                color = if (isAuspicious) CyberWhite else GrayCaption,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }
        Column {
            Text(
                text = name,
                color = CyberWhite,
                fontSize = 14.sp,
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
