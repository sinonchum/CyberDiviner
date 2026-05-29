package com.cyberdiviner.ui.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.ui.shared.BackButton
import com.cyberdiviner.ui.shared.DesignTokens
import com.cyberdiviner.ui.theme.*

/**
 * Title screen — shows current title, XP progress, and path completion badges.
 */
@Composable
fun TitleScreen(
    onBack: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val paths by viewModel.paths.collectAsState()
    val pathProgress by viewModel.pathProgress.collectAsState()

    // Load progress for all paths
    LaunchedEffect(Unit) {
        paths.forEach { viewModel.loadPathProgress(it.id) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = DesignTokens.ScreenHorizontalPadding,
                    vertical = 32.dp
                )
        ) {
            BackButton(onBack = onBack)

            Spacer(modifier = Modifier.height(24.dp))

            // Current title
            val currentTitle = stats?.title ?: "初入卦门"
            val totalXp = stats?.totalXp ?: 0

            Text(
                text = "称号",
                color = AccentRed,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentTitle,
                color = CyberWhite,
                fontSize = 28.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // XP progress to next tier
            val tiers = listOf(
                0 to "初入卦门",
                50 to "卦象学徒",
                150 to "卦门弟子",
                300 to "初窥天机",
                500 to "六爻通达",
                800 to "卦象大师",
                1200 to "术数宗师",
                2000 to "天机阁主"
            )
            val currentTierIndex = tiers.indexOfLast { totalXp >= it.first }
            val nextTier = tiers.getOrNull(currentTierIndex + 1)
            val progressToNext = if (nextTier != null) {
                val currentBase = tiers[currentTierIndex].first
                val range = nextTier.first - currentBase
                ((totalXp - currentBase).toFloat() / range).coerceIn(0f, 1f)
            } else 1f

            Text(
                text = "经验",
                color = AccentRed,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$totalXp XP",
                color = CyberWhite,
                fontSize = 20.sp,
                fontFamily = MonoFontFamily
            )

            if (nextTier != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "距「${nextTier.second}」还需 ${nextTier.first - totalXp} XP",
                    color = GrayCaption,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(GraySurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressToNext)
                            .background(AccentRed)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已达最高称号",
                    color = AccentRed,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Path completion badges
            Text(
                text = "路径成就",
                color = AccentRed,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            val pathTitles = mapOf(
                "yijing_intro" to "识象者",
                "liuyao_intro" to "起爻者",
                "tarot_intro" to "读牌者",
                "practice" to "断语者"
            )

            paths.forEach { path ->
                val completed = viewModel.getCompletedCount(path.id)
                val total = viewModel.getTotalCount(path.id)
                val isComplete = completed >= total && total > 0
                val badge = pathTitles[path.id]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Completion indicator
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isComplete) AccentRed else Color.Transparent)
                            .border(1.dp, if (isComplete) AccentRed else GrayBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isComplete) {
                            Text(
                                text = "✓",
                                color = CyberWhite,
                                fontSize = 12.sp,
                                fontFamily = MonoFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = path.title,
                            color = CyberWhite,
                            fontSize = 15.sp,
                            fontFamily = HuiwenFontFamily
                        )
                        Text(
                            text = "$completed/$total 关" + if (badge != null && isComplete) " · $badge" else "",
                            color = if (isComplete) AccentRed else GrayCaption,
                            fontSize = 12.sp,
                            fontFamily = MonoFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title ladder reference
            Text(
                text = "称号阶梯",
                color = AccentRed,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            tiers.reversed().forEach { (xp, title) ->
                val isActive = totalXp >= xp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = if (isActive) CyberWhite else GrayMuted,
                        fontSize = 14.sp,
                        fontFamily = HuiwenFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${xp}XP",
                        color = if (isActive) AccentRed else GrayMuted,
                        fontSize = 12.sp,
                        fontFamily = MonoFontFamily
                    )
                }
            }
        }
    }
}
