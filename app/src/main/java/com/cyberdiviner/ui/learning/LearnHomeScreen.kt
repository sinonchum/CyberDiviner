package com.cyberdiviner.ui.learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.cyberdiviner.data.model.learning.LearningProgressEntity
import com.cyberdiviner.ui.shared.BackButton
import com.cyberdiviner.ui.shared.DesignTokens
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.shared.StaggeredItem
import com.cyberdiviner.ui.theme.AccentRed
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBody
import com.cyberdiviner.ui.theme.GrayBorder
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GraySurface
import com.cyberdiviner.ui.theme.HuiwenFontFamily
import com.cyberdiviner.ui.theme.MonoFontFamily
import com.cyberdiviner.ui.theme.WenKaiFontFamily

/**
 * Learn Home Screen — entry point for the learning quest feature.
 *
 * Displays:
 * - SectionHeader ("修习之路" / LEARNING QUEST)
 * - Daily lesson card with quick-start CTA
 * - 4 path cards in 2×2 grid, each showing X/N progress
 * - XP / Streak / Title stats bar
 */
@Composable
fun LearnHomeScreen(
    onNavigateToPath: (String) -> Unit,
    onNavigateToLesson: (String) -> Unit,
    navController: NavHostController,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val paths by viewModel.paths.collectAsState()
    val pathProgress by viewModel.pathProgress.collectAsState()

    // Load progress for all paths on first composition
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
                    start = DesignTokens.ScreenHorizontalPadding,
                    end = DesignTokens.ScreenHorizontalPadding,
                    top = 32.dp,
                    bottom = 48.dp
                )
        ) {
            // ── Header ─────────────────────────────────────────────────────
            SectionHeader(title = "修习之路", subtitle = "LEARNING QUEST")

            Spacer(modifier = Modifier.height(20.dp))

            // ── Stats Bar ──────────────────────────────────────────────────
            StaggeredItem(index = 0) {
                StatsBar(
                    xp = stats?.totalXp ?: 0,
                    streak = stats?.currentStreak ?: 0,
                    title = stats?.title ?: "初入卦门",
                    onTitleClick = { navController.navigate("learn/title") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Daily Lesson Card ──────────────────────────────────────────
            StaggeredItem(index = 1) {
                DailyLessonCard(
                    paths = paths,
                    pathProgress = pathProgress,
                    onClick = { lessonId -> onNavigateToLesson(lessonId) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Section: 学习路径 ──────────────────────────────────────────
            StaggeredItem(index = 2) {
                SectionHeader(title = "学习路径", subtitle = "PATHS")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Path Grid (2×2) ───────────────────────────────────────────
            val chunkedPaths = paths.chunked(2)
            chunkedPaths.forEachIndexed { rowIndex, rowPaths ->
                StaggeredItem(index = rowIndex + 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.ItemSpacing)
                    ) {
                        rowPaths.forEach { path ->
                            val completedCount = pathProgress[path.id]
                                ?.count { it.completed } ?: 0
                            PathCard(
                                path = path,
                                completedCount = completedCount,
                                onClick = { onNavigateToPath(path.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number
                        if (rowPaths.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(DesignTokens.ItemSpacing))
            }
        }
    }
}

// ── Stats bar ───────────────────────────────────────────────────────────────

@Composable
private fun StatsBar(xp: Int, streak: Int, title: String, onTitleClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(GraySurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(label = "XP", value = xp.toString())
        StatItem(label = "连续", value = "${streak}日")
        Box(modifier = Modifier.clickable { onTitleClick() }) {
            StatItem(label = "称号", value = title)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = CyberWhite,
            fontFamily = HuiwenFontFamily,
            fontSize = 18.sp,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = GrayCaption,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )
    }
}

// ── Daily lesson card ───────────────────────────────────────────────────────

@Composable
private fun DailyLessonCard(
    paths: List<LearningPath>,
    pathProgress: Map<String, List<LearningProgressEntity>>,
    onClick: (String) -> Unit
) {
    // Find the first incomplete lesson across all paths
    val nextLessonId: String = paths.firstNotNullOfOrNull { path ->
        val completedIds = (pathProgress[path.id] ?: emptyList())
            .filter { it.completed }
            .map { it.lessonId }
            .toSet()
        path.lessonIds.firstOrNull { it !in completedIds }
    } ?: paths.firstOrNull()?.lessonIds?.firstOrNull() ?: return

    // Resolve the path for display
    val targetPath = paths.firstOrNull { it.lessonIds.contains(nextLessonId) }
    val order = targetPath?.lessonIds?.indexOf(nextLessonId)?.plus(1) ?: 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(GraySurface)
            .clickable { onClick(nextLessonId) }
            .padding(DesignTokens.CardPadding)
    ) {
        // Red accent line on left
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                color = AccentRed,
                topLeft = Offset.Zero,
                size = Size(2.dp.toPx(), size.height)
            )
        }

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "今日修习",
                color = GrayCaption,
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = targetPath?.title ?: "学习",
                color = CyberWhite,
                fontFamily = HuiwenFontFamily,
                fontSize = 18.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "第${order}课 · 点击开始",
                color = GrayBody,
                fontFamily = WenKaiFontFamily,
                fontSize = 13.sp
            )
        }
    }
}

// ── Path card ───────────────────────────────────────────────────────────────

@Composable
private fun PathCard(
    path: LearningPath,
    completedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = path.totalLessons.coerceAtLeast(1)
    val borderColor = if (completedCount >= total) AccentRed else GrayBorder

    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(2.dp))
            .background(GraySurface)
            .clickable(onClick = onClick)
            .padding(DesignTokens.CardPadding)
    ) {
        // Border
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = borderColor,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = 1f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = path.icon,
                color = CyberWhite,
                fontSize = 28.sp
            )
            Column {
                Text(
                    text = path.title,
                    color = CyberWhite,
                    fontFamily = HuiwenFontFamily,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedCount/$total",
                    color = if (completedCount >= total) AccentRed else GrayCaption,
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                // Progress bar
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                ) {
                    drawRect(color = GrayBorder, size = Size(size.width, size.height))
                    val progress = completedCount.toFloat() / total
                    drawRect(
                        color = if (completedCount >= total) AccentRed else CyberWhite,
                        size = Size(size.width * progress, size.height)
                    )
                }
            }
        }
    }
}
