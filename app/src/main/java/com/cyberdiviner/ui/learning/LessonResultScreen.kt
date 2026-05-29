package com.cyberdiviner.ui.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.data.model.learning.Lesson
import com.cyberdiviner.ui.shared.BackButton
import com.cyberdiviner.ui.shared.DesignTokens
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.theme.*

/**
 * Standalone lesson result screen — accessible via LEARN_RESULT route.
 * Shows score, XP, knowledge card, share button, and unlock notification.
 */
@Composable
fun LessonResultScreen(
    lessonId: String,
    onBack: () -> Unit,
    onReturnToLearn: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val lessonState by viewModel.lessonState.collectAsState()

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
        // Skip to RESULT phase
        viewModel.skipToResult()
    }

    val lesson = lessonState.lesson

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        if (lesson == null) {
            Text(
                text = ". . .",
                color = GrayCaption,
                fontFamily = MonoFontFamily,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
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

                ResultContent(
                    lesson = lesson,
                    lessonState = lessonState,
                    onComplete = {
                        val overallCorrect = lessonState.totalQuestions > 0 &&
                            lessonState.correctCount * 100 / lessonState.totalQuestions >= 70
                        viewModel.completeLesson(overallCorrect)
                        onReturnToLearn()
                    }
                )
            }
        }
    }
}

/**
 * Shared result content — used by both inline ResultPhase and standalone LessonResultScreen.
 */
@Composable
fun ResultContent(
    lesson: Lesson,
    lessonState: LessonUiState,
    onComplete: () -> Unit
) {
    val correctCount = lessonState.correctCount
    val total = lesson.questions.size
    val percentage = if (total > 0) (correctCount * 100 / total) else 0

    SectionHeader(title = "完成", subtitle = "COMPLETE")

    Spacer(modifier = Modifier.height(24.dp))

    // Score
    Text(
        text = "正确率: $correctCount/$total ($percentage%)",
        color = CyberWhite,
        fontSize = 18.sp,
        fontFamily = MonoFontFamily
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "XP +${if (percentage >= 70) 25 else 5}",
        color = AccentRed,
        fontSize = 24.sp,
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Unlock notification
    val unlockReward = lesson.unlockReward
    if (unlockReward != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentRed)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "解锁",
                    color = AccentRed,
                    fontSize = 11.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = unlockReward.description,
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Knowledge card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "今日学会",
                color = AccentRed,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lesson.explanation,
                color = GrayBody,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Share button
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (copied) AccentRed else GrayBorder)
            .clickable {
                val shareText = "【${lesson.title}】${lesson.subtitle}\n\n${lesson.explanation}\n\n— CyberDiviner 赛博算命"
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("knowledge_card", shareText))
                copied = true
            }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (copied) "已复制到剪贴板" else "分享知识卡",
            color = if (copied) AccentRed else CyberWhite,
            fontSize = 14.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    ContinueButton(text = "返回学习", onClick = onComplete)
}
