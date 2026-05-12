package com.cyberdiviner.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import com.cyberdiviner.engine.AlmanacEngine
import com.cyberdiviner.ui.navigation.Routes
import com.cyberdiviner.ui.shared.BinaryClock
import com.cyberdiviner.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Feature entry data ───────────────────────────────────────────────────

private data class FeatureEntry(
    val route: String,
    val icon: String,
    val label: String,
    val englishLabel: String,
    val description: String,
    val accentColor: Color,
)

private val FEATURE_ENTRIES = listOf(
    FeatureEntry(
        Routes.LIUYAO, "☯", "六爻", "Liu Yao",
        "量子卜卦 · Quantum I Ching",
        NeonCyan
    ),
    FeatureEntry(
        Routes.TAROT, "🃏", "塔罗", "Tarot",
        "赛博塔罗 · Neon Card Reading",
        NeonMagenta
    ),
    FeatureEntry(
        Routes.VISION, "👁️", "面相", "Vision",
        "面相扫描 · Neural Physiognomy",
        NeonGreen
    ),
    FeatureEntry(
        Routes.MUYU, "🔔", "木鱼", "Muyu",
        "电子木鱼 · Digital Zen",
        NeonOrange
    ),
)

// ── HomeScreen ───────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val dayReading by viewModel.dayReading.collectAsState()
    val ganzhiDate by viewModel.ganzhiDate.collectAsState()
    val shichenName by viewModel.shichenName.collectAsState()
    val currentDateFormatted by viewModel.currentDateFormatted.collectAsState()
    val recentReadings by viewModel.recentReadings.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val greeting by viewModel.greeting.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(CyberBlack)) {
        // Animated acid background
        AcidBackground(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header: App Title + Binary Clock ──
            item {
                HeaderSection(currentDateFormatted)
            }

            // ── Ganzhi + Shichen Info ──
            item {
                GanzhiInfoCard(ganzhiDate, shichenName)
            }

            // ── Greeting ──
            item {
                CyberGreetingCard(greeting)
            }

            // ── Daily Almanac: 宜 / 忌 ──
            dayReading?.let { reading ->
                item {
                    DailyActivitiesSection(reading)
                }
            }

            // ── Feature Navigation Grid ──
            item {
                SectionHeader("占卜协议", "DIVINATION PROTOCOLS")
            }
            item {
                FeatureGrid(navController)
            }

            // ── Daily Energy + Advice ──
            dayReading?.let { reading ->
                item {
                    EnergyCard(reading)
                }
            }

            // ── Lucky Colors & Numbers ──
            dayReading?.let { reading ->
                item {
                    LuckySection(reading)
                }
            }

            // ── Recent Readings ──
            if (recentReadings.isNotEmpty()) {
                item {
                    SectionHeader("近期记录", "RECENT READINGS", "总计 $totalCount")
                }
                items(recentReadings, key = { it.id }) { reading ->
                    RecentReadingCard(reading, onClick = {
                        navController.navigate(reading.type.toRoute())
                    })
                }
            }

            // ── Solar Term / Warnings ──
            dayReading?.let { reading ->
                if (reading.warnings.isNotEmpty() || reading.currentSolarTerm != null) {
                    item {
                        WarningsSection(reading)
                    }
                }
            }

            // ── Footer ──
            item {
                FooterSection()
            }
        }
    }
}

// ── Section Components ───────────────────────────────────────────────────

@Composable
private fun HeaderSection(dateFormatted: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App title with neon glow effect
        Text(
            text = "赛博黄历",
            color = NeonCyan,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 6.sp
        )
        Text(
            text = "CYBER ALMANAC",
            color = NeonCyan.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 8.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Binary clock
        BinaryClock()

        Spacer(modifier = Modifier.height(8.dp))

        // Current date
        Text(
            text = dateFormatted,
            color = TextSecondary,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun GanzhiInfoCard(ganzhiDate: String, shichenName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "干支纪时",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ganzhiDate,
                    color = NeonCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(NeonCyan.copy(alpha = 0.3f))
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "当前时辰",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = shichenName,
                    color = NeonMagenta,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun CyberGreetingCard(greeting: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Animated border glow
                drawRoundRect(
                    color = NeonCyan.copy(alpha = borderAlpha * 0.4f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            },
        colors = CardDefaults.cardColors(containerColor = CyberDark.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = greeting,
            modifier = Modifier.padding(16.dp),
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DailyActivitiesSection(reading: AlmanacEngine.DayReading) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Auspicious activities
        ActivityCard(
            title = "今日宜",
            titleEn = "AUSPICIOUS",
            icon = "✅",
            activities = reading.auspiciousActivities.take(8),
            accentColor = AuspiciousGreen
        )

        // Inauspicious activities
        ActivityCard(
            title = "今日忌",
            titleEn = "INAUSPICIOUS",
            icon = "❌",
            activities = reading.inauspiciousActivities.take(6),
            accentColor = InauspiciousRed
        )
    }
}

@Composable
private fun ActivityCard(
    title: String,
    titleEn: String,
    icon: String,
    activities: List<AlmanacEngine.DailyActivity>,
    accentColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 16.sp)
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = titleEn,
                    color = accentColor.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity chips in a flow-like layout
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { activity ->
                    ActivityChip(activity, accentColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit,
) {
    // Simple flow implementation using multiple rows
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

@Composable
private fun ActivityChip(activity: AlmanacEngine.DailyActivity, accentColor: Color) {
    Surface(
        modifier = Modifier.animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.08f),
        contentColor = accentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = activity.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
            Text(
                text = activity.englishName,
                fontSize = 10.sp,
                color = accentColor.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FeatureGrid(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 2x2 grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureEntryCard(
                entry = FEATURE_ENTRIES[0],
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(FEATURE_ENTRIES[0].route) }
            )
            FeatureEntryCard(
                entry = FEATURE_ENTRIES[1],
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(FEATURE_ENTRIES[1].route) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureEntryCard(
                entry = FEATURE_ENTRIES[2],
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(FEATURE_ENTRIES[2].route) }
            )
            FeatureEntryCard(
                entry = FEATURE_ENTRIES[3],
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(FEATURE_ENTRIES[3].route) }
            )
        }
    }
}

@Composable
private fun FeatureEntryCard(
    entry: FeatureEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberDark.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Text(
                text = entry.icon,
                fontSize = 28.sp
            )

            // Labels
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.label,
                        color = entry.accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = entry.englishLabel,
                        color = entry.accentColor.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = entry.description,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EnergyCard(reading: AlmanacEngine.DayReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⚡", fontSize = 16.sp)
                Text(
                    text = "能量分析",
                    color = NeonYellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ENERGY ANALYSIS",
                    color = NeonYellow.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Energy level
            Text(
                text = reading.dailyEnergy,
                color = TextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Element advice
            Text(
                text = reading.elementAdvice,
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Overview
            Text(
                text = reading.overview,
                color = NeonCyan.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LuckySection(reading: AlmanacEngine.DayReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎯", fontSize = 16.sp)
                Text(
                    text = "今日吉祥",
                    color = FortuneGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "LUCKY SIGNALS",
                    color = FortuneGold.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lucky colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "吉祥色:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                reading.luckyColors.forEach { color ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberDark
                    ) {
                        Text(
                            text = color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = FortuneGold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lucky numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "吉祥数:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                reading.luckyNumbers.forEach { num ->
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$num",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentReadingCard(reading: DivinationReading, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberDark.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type icon
            Surface(
                shape = CircleShape,
                color = NeonCyan.copy(alpha = 0.1f)
            ) {
                Text(
                    text = reading.type.icon,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 20.sp
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reading.type.displayName,
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (reading.isFavorited) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = reading.question,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = formatTimestamp(reading.timestamp),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Arrow
            Text(
                text = "›",
                color = TextMuted,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun WarningsSection(reading: AlmanacEngine.DayReading) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        reading.currentSolarTerm?.let { term ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NeonPurple.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🌀", fontSize = 16.sp)
                    Column {
                        Text(
                            text = "节气: ${term.name}",
                            color = NeonPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${term.englishName} · ${term.element}气",
                            color = NeonPurple.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        reading.warnings.forEach { warning ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = InauspiciousRed.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "⚠️", fontSize = 14.sp)
                    Text(
                        text = warning,
                        color = InauspiciousRed.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(chineseTitle: String, englishTitle: String, badge: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Accent line
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NeonCyan)
        )

        Text(
            text = chineseTitle,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = englishTitle,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        badge?.let {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = it,
                color = NeonCyan.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp),
            color = NeonCyan.copy(alpha = 0.15f),
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            color = NeonCyan.copy(alpha = 0.1f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "赛博黄历 v0.1 · CyberAlmanac",
            color = TextMuted.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Text(
            text = "数据仅供参考 · For entertainment only",
            color = TextMuted.copy(alpha = 0.3f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun DivinationType.toRoute(): String = when (this) {
    DivinationType.LIUYAO -> Routes.LIUYAO
    DivinationType.TAROT -> Routes.TAROT
    DivinationType.VISION -> Routes.VISION
    DivinationType.MUYU -> Routes.MUYU
}
