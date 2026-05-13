package com.cyberdiviner.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: ImageVector,
    val label: String,
    val englishLabel: String,
    val description: String,
    val accentColor: Color,
)

private val FEATURE_ENTRIES = listOf(
    FeatureEntry(
        Routes.LIUYAO, Icons.Default.Explore, "\u516D\u7238", "Liu Yao",
        "\u91CF\u5B50\u535C\u5366 \u00B7 Quantum I Ching",
        NeonCyan
    ),
    FeatureEntry(
        Routes.TAROT, Icons.Default.Style, "\u5854\u7F57", "Tarot",
        "\u8D5B\u535A\u5854\u7F57 \u00B7 Neon Card Reading",
        NeonMagenta
    ),
    FeatureEntry(
        Routes.VISION, Icons.Default.Visibility, "\u9762\u76F8", "Vision",
        "\u9762\u76F8\u626B\u63CF \u00B7 Neural Physiognomy",
        NeonGreen
    ),
    FeatureEntry(
        Routes.MUYU, Icons.Default.Vibration, "\u6728\u9C7C", "Muyu",
        "\u7535\u5B50\u6728\u9C7C \u00B7 Digital Zen",
        CyberTertiary
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Header ──
        item {
            HeaderSection(currentDateFormatted)
        }

        // ── Ganzhi + Shichen ──
        item {
            GanzhiInfoCard(ganzhiDate, shichenName)
        }

        // ── Greeting ──
        item {
            GreetingCard(greeting)
        }

        // ── Daily Almanac ──
        dayReading?.let { reading ->
            item {
                DailyAlmanacCard(reading)
            }
        }

        // ── Feature Navigation ──
        item {
            SectionHeader("\u5360\u535C\u534F\u8BAE", "DIVINATION PROTOCOLS")
        }
        item {
            FeatureGrid(navController)
        }

        // ── Energy + Lucky ──
        dayReading?.let { reading ->
            item {
                DailyInsightsCard(reading)
            }
        }

        // ── Recent Readings ──
        if (recentReadings.isNotEmpty()) {
            item {
                SectionHeader("\u8FD1\u671F\u8BB0\u5F55", "RECENT READINGS", "\u603B\u8BA1 $totalCount")
            }
            items(recentReadings, key = { it.id }) { reading ->
                RecentReadingCard(reading, onClick = {
                    navController.navigate(reading.type.toRoute())
                })
            }
        }

        // ── Warnings ──
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

// ── Section Components ───────────────────────────────────────────────────

@Composable
private fun HeaderSection(dateFormatted: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u8D5B\u535A\u9EC4\u5386",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "CYBER ALMANAC",
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 6.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        BinaryClock()
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = dateFormatted,
            color = TextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun GanzhiInfoCard(ganzhiDate: String, shichenName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("\u5E72\u652F\u7EAA\u65F6", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(ganzhiDate, color = NeonCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(TextMuted.copy(alpha = 0.3f)))
            Column(horizontalAlignment = Alignment.End) {
                Text("\u5F53\u524D\u65F6\u8FB0", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(shichenName, color = NeonMagenta, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun GreetingCard(greeting: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = greeting,
            modifier = Modifier.padding(14.dp),
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── Combined daily almanac card (auspicious + inauspicious) ──────────────

@Composable
private fun DailyAlmanacCard(reading: AlmanacEngine.DayReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Auspicious
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Check, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
                Text("\u4ECA\u65E5\u5B9C", color = AuspiciousGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("AUSPICIOUS", color = AuspiciousGreen.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reading.auspiciousActivities.take(8).forEach { activity ->
                    ActivityChip(activity, AuspiciousGreen)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inauspicious
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = InauspiciousRed, modifier = Modifier.size(16.dp))
                Text("\u4ECA\u65E5\u5FCC", color = InauspiciousRed, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("INAUSPICIOUS", color = InauspiciousRed.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reading.inauspiciousActivities.take(6).forEach { activity ->
                    ActivityChip(activity, InauspiciousRed)
                }
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit,
) {
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
        shape = RoundedCornerShape(6.dp),
        color = accentColor.copy(alpha = 0.08f),
        contentColor = accentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(activity.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace, color = accentColor)
            Text(activity.englishName, fontSize = 9.sp, color = accentColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
        }
    }
}

// ── Feature Grid ─────────────────────────────────────────────────────────

@Composable
private fun FeatureGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureEntryCard(FEATURE_ENTRIES[0], Modifier.weight(1f)) { navController.navigate(FEATURE_ENTRIES[0].route) }
            FeatureEntryCard(FEATURE_ENTRIES[1], Modifier.weight(1f)) { navController.navigate(FEATURE_ENTRIES[1].route) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureEntryCard(FEATURE_ENTRIES[2], Modifier.weight(1f)) { navController.navigate(FEATURE_ENTRIES[2].route) }
            FeatureEntryCard(FEATURE_ENTRIES[3], Modifier.weight(1f)) { navController.navigate(FEATURE_ENTRIES[3].route) }
        }
    }
}

@Composable
private fun FeatureEntryCard(entry: FeatureEntry, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = entry.label,
                tint = entry.accentColor,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(entry.label, color = entry.accentColor, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(entry.englishLabel, color = entry.accentColor.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 2.dp))
                }
                Text(entry.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ── Combined energy + lucky insights card ─────────────────────────────────

@Composable
private fun DailyInsightsCard(reading: AlmanacEngine.DayReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberGray),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Energy section
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(16.dp))
                Text("\u80FD\u91CF\u5206\u6790", color = NeonYellow, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("ENERGY", color = NeonYellow.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(reading.dailyEnergy, color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(reading.elementAdvice, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(reading.overview, color = NeonCyan.copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Lucky section
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FortuneGold, modifier = Modifier.size(16.dp))
                Text("\u4ECA\u65E5\u5409\u7965", color = FortuneGold, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("LUCKY", color = FortuneGold.copy(alpha = 0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Lucky colors
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("\u5409\u7965\u8272:", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                reading.luckyColors.forEach { color ->
                    Surface(shape = RoundedCornerShape(5.dp), color = CyberDark) {
                        Text(color, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), color = FortuneGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Lucky numbers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("\u5409\u7965\u6570:", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                reading.luckyNumbers.forEach { num ->
                    Surface(shape = CircleShape, color = NeonCyan.copy(alpha = 0.12f)) {
                        Text("$num", modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp), color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ── Recent Reading Card ──────────────────────────────────────────────────

@Composable
private fun RecentReadingCard(reading: DivinationReading, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = CircleShape, color = NeonCyan.copy(alpha = 0.08f)) {
                Icon(
                    imageVector = reading.type.toIcon(),
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.padding(8.dp).size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(reading.type.displayName, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    if (reading.isFavorited) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(10.dp))
                    }
                }
                Text(reading.question, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatTimestamp(reading.timestamp), color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Warnings ─────────────────────────────────────────────────────────────

@Composable
private fun WarningsSection(reading: AlmanacEngine.DayReading) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        reading.currentSolarTerm?.let { term ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSecondary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = CyberSecondary, modifier = Modifier.size(16.dp))
                    Column {
                        Text("\u8282\u6C14: ${term.name}", color = CyberSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("${term.englishName} \u00B7 ${term.element}\u6C14", color = CyberSecondary.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        reading.warnings.forEach { warning ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = InauspiciousRed.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = InauspiciousRed, modifier = Modifier.size(14.dp))
                    Text(warning, color = InauspiciousRed.copy(alpha = 0.9f), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// ── Section Header ───────────────────────────────────────────────────────

@Composable
private fun SectionHeader(chineseTitle: String, englishTitle: String, badge: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(14.dp).clip(RoundedCornerShape(2.dp)).background(NeonCyan))
        Text(chineseTitle, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(englishTitle, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
        badge?.let {
            Spacer(modifier = Modifier.weight(1f))
            Text(it, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// ── Footer ───────────────────────────────────────────────────────────────

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), color = TextMuted.copy(alpha = 0.15f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text("\u8D5B\u535A\u9EC4\u5386 v0.1 \u00B7 CyberAlmanac", color = TextMuted.copy(alpha = 0.35f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
        Text("\u6570\u636E\u4EC5\u4F9B\u53C2\u8003 \u00B7 For entertainment only", color = TextMuted.copy(alpha = 0.25f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
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

private fun DivinationType.toIcon(): ImageVector = when (this) {
    DivinationType.LIUYAO -> Icons.Default.Explore
    DivinationType.TAROT -> Icons.Default.Style
    DivinationType.VISION -> Icons.Default.Visibility
    DivinationType.MUYU -> Icons.Default.Vibration
}
