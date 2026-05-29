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
import androidx.compose.runtime.remember
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
import com.cyberdiviner.data.model.learning.LearningProgressEntity
import com.cyberdiviner.data.model.learning.Lesson
import com.cyberdiviner.ui.shared.BackButton
import com.cyberdiviner.ui.shared.DesignTokens
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.shared.StaggeredItem
import com.cyberdiviner.ui.theme.AccentRed
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBorder
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GrayMuted
import com.cyberdiviner.ui.theme.GraySurface
import com.cyberdiviner.ui.theme.HuiwenFontFamily
import com.cyberdiviner.ui.theme.MonoFontFamily

/**
 * Lesson Map Screen — displays N lessons for a given learning path.
 *
 * Visual states per card:
 * - Completed: white border, white text, checkmark
 * - Current (first incomplete): AccentRed border, clickable
 * - Locked: gray border, muted text, non-clickable
 *
 * @param pathId The learning path identifier
 * @param onNavigateToLesson Callback with lessonId when a lesson is tapped
 * @param onBack Navigate back
 * @param viewModel LearningViewModel (Hilt-injected)
 */
@Composable
fun LessonMapScreen(
    pathId: String,
    onNavigateToLesson: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val pathProgress by viewModel.pathProgress.collectAsState()
    val paths by viewModel.paths.collectAsState()
    val path = paths.find { it.id == pathId }
    val progressList = pathProgress[pathId] ?: emptyList()
    val completedIds = progressList.filter { it.completed }.map { it.lessonId }.toSet()

    // Resolve lesson objects for this path
    val lessons: List<Lesson?> = remember(pathId, paths) {
        (path?.lessonIds ?: emptyList()).map { viewModel.getLesson(it) }
    }

    // Load progress on entry
    LaunchedEffect(pathId) {
        viewModel.loadPathProgress(pathId)
    }

    // Determine the first incomplete lesson order
    val currentLessonOrder: Int = lessons.indexOfFirst { lesson ->
        lesson != null && lesson.id !in completedIds
    }.let { if (it == -1) lessons.size + 1 else it + 1 }

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
            // ── Back Button ────────────────────────────────────────────────
            BackButton(onBack = onBack)

            Spacer(modifier = Modifier.height(DesignTokens.HeaderBottomMargin))

            // ── Section Header ────────────────────────────────────────────
            SectionHeader(
                title = path?.title ?: "课程地图",
                subtitle = (path?.subtitle ?: "LESSON MAP").uppercase()
            )

            Spacer(modifier = Modifier.height(DesignTokens.SectionSpacing))

            // ── Lesson Cards ──────────────────────────────────────────────
            lessons.forEachIndexed { index, lesson ->
                val order = index + 1
                val isCompleted = lesson?.id in completedIds
                val isCurrent = order == currentLessonOrder
                val isLocked = order > currentLessonOrder

                StaggeredItem(index = index) {
                    LessonMapCard(
                        lesson = lesson,
                        order = order,
                        state = when {
                            isCompleted -> LessonCardState.COMPLETED
                            isCurrent -> LessonCardState.CURRENT
                            else -> LessonCardState.LOCKED
                        },
                        progress = lesson?.let { l ->
                            progressList.find { it.lessonId == l.id }
                        },
                        onClick = {
                            if (!isLocked && lesson != null) {
                                onNavigateToLesson(lesson.id)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(DesignTokens.ItemSpacing))
            }
        }
    }
}

// ── Internal types ──────────────────────────────────────────────────────────

private enum class LessonCardState { COMPLETED, CURRENT, LOCKED }

// ── Lesson card ─────────────────────────────────────────────────────────────

@Composable
private fun LessonMapCard(
    lesson: Lesson?,
    order: Int,
    state: LessonCardState,
    progress: LearningProgressEntity?,
    onClick: () -> Unit
) {
    val borderColor = when (state) {
        LessonCardState.COMPLETED -> CyberWhite
        LessonCardState.CURRENT -> AccentRed
        LessonCardState.LOCKED -> GrayBorder
    }
    val textColor = when (state) {
        LessonCardState.COMPLETED -> CyberWhite
        LessonCardState.CURRENT -> CyberWhite
        LessonCardState.LOCKED -> GrayMuted
    }
    val subtitleColor = when (state) {
        LessonCardState.COMPLETED -> GrayCaption
        LessonCardState.CURRENT -> AccentRed
        LessonCardState.LOCKED -> GrayBorder
    }
    val bgColor = when (state) {
        LessonCardState.LOCKED -> CyberBlack
        else -> GraySurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(bgColor)
            .clickable(enabled = state != LessonCardState.LOCKED, onClick = onClick)
            .padding(DesignTokens.CardPadding)
    ) {
        // Border
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = borderColor,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = if (state == LessonCardState.CURRENT) 2f else 1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Order number / checkmark
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state == LessonCardState.COMPLETED) {
                    Text(
                        text = "✓",
                        color = CyberWhite,
                        fontFamily = MonoFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = String.format("%02d", order),
                        color = if (state == LessonCardState.LOCKED) GrayBorder else GrayCaption,
                        fontFamily = MonoFontFamily,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson?.title ?: "—",
                    color = textColor,
                    fontFamily = HuiwenFontFamily,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (state) {
                        LessonCardState.COMPLETED -> "已完成 · 得分 ${progress?.score ?: 0}"
                        LessonCardState.CURRENT -> "开始学习 →"
                        LessonCardState.LOCKED -> "🔒 未解锁"
                    },
                    color = subtitleColor,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            // Mastery indicator for completed
            if (state == LessonCardState.COMPLETED && progress != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${progress.mastery}%",
                        color = GrayCaption,
                        fontFamily = MonoFontFamily,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                    ) {
                        drawRect(color = GrayBorder, size = Size(size.width, size.height))
                        drawRect(
                            color = CyberWhite,
                            size = Size(size.width * (progress.mastery / 100f), size.height)
                        )
                    }
                }
            }
        }
    }
}
