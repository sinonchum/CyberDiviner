package com.cyberdiviner.ui.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.cyberdiviner.ui.shared.*
import com.cyberdiviner.ui.theme.*

/**
 * Path detail screen — shows all lessons in a learning path with progress.
 */
@Composable
fun LearnPathScreen(
    pathId: String,
    onNavigateToLesson: (String) -> Unit,
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val paths by viewModel.paths.collectAsState()
    val pathProgress by viewModel.pathProgress.collectAsState()

    val path = paths.find { it.id == pathId }
    val progressList = pathProgress[pathId] ?: emptyList()
    val completedIds = progressList.filter { it.completed }.map { it.lessonId }.toSet()

    LaunchedEffect(pathId) {
        viewModel.loadPathProgress(pathId)
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
            // Back / title
            Text(
                text = "← 返回",
                color = GrayCaption,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.clickable(onClick = onBack)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = path?.title ?: pathId,
                subtitle = path?.subtitle ?: ""
            )

            Spacer(modifier = Modifier.height(DesignTokens.SectionSpacing))

            // Lesson list
            path?.lessonIds?.forEachIndexed { index, lessonId ->
                val completed = lessonId in completedIds
                LessonRow(
                    index = index + 1,
                    lessonId = lessonId,
                    completed = completed,
                    onClick = { onNavigateToLesson(lessonId) }
                )
                Spacer(modifier = Modifier.height(DesignTokens.ItemSpacing))
            }
        }
    }
}

@Composable
private fun LessonRow(
    index: Int,
    lessonId: String,
    completed: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (completed) AccentRed else GrayBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(GraySurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (completed) "✦" else "◇",
                color = if (completed) AccentRed else GrayCaption,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "第${index}课",
                    color = GrayCaption,
                    fontFamily = MonoFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = lessonId,
                    color = CyberWhite,
                    fontFamily = HuiwenFontFamily,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
            if (completed) {
                Text(
                    text = "已完成",
                    color = AccentRed,
                    fontFamily = MonoFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
