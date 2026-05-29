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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberdiviner.data.model.learning.Lesson
import com.cyberdiviner.data.model.learning.MatchItem
import com.cyberdiviner.data.model.learning.QuizType
import com.cyberdiviner.ui.shared.BackButton
import com.cyberdiviner.ui.shared.DesignTokens
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.theme.*

/**
 * Lesson Screen — 4-phase learning flow:
 * CONCEPT -> HOW_TO_READ -> QUIZ -> RESULT
 */
@Composable
fun LessonScreen(
    lessonId: String,
    onBack: () -> Unit,
    onLessonComplete: (String) -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val lessonState by viewModel.lessonState.collectAsState()

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }

    val lesson = lessonState.lesson

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        if (lesson == null) {
            // Loading
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

                // Phase indicator
                PhaseIndicator(currentPhase = lessonState.phase)

                Spacer(modifier = Modifier.height(24.dp))

                when (lessonState.phase) {
                    LessonPhase.CONCEPT -> ConceptPhase(
                        lesson = lesson,
                        onContinue = { viewModel.nextPhase() }
                    )
                    LessonPhase.HOW_TO_READ -> HowToReadPhase(
                        howToRead = lesson.howToRead,
                        onContinue = { viewModel.nextPhase() }
                    )
                    LessonPhase.QUIZ -> QuizPhase(
                        lesson = lesson,
                        lessonState = lessonState,
                        onSubmitAnswer = { idx -> viewModel.submitQuizAnswer(idx) },
                        onSubmitBoolAnswer = { correct -> viewModel.submitQuizAnswerFromBool(correct) },
                        onContinue = { viewModel.nextPhase() },
                        onClearFeedback = { viewModel.clearFeedback() }
                    )
                    LessonPhase.RESULT -> ResultPhase(
                        lesson = lesson,
                        lessonState = lessonState,
                        onComplete = {
                            viewModel.completeLesson(lessonState.answerFeedback?.correct ?: false)
                            onLessonComplete(lessonId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseIndicator(currentPhase: LessonPhase) {
    val phases = LessonPhase.entries
    val currentIndex = phases.indexOf(currentPhase)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        phases.forEachIndexed { index, phase ->
            val isActive = index <= currentIndex
            val color = if (isActive) AccentRed else GrayBorder

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            )

            if (index < phases.size - 1) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(1.dp)
                        .background(if (index < currentIndex) AccentRed else GrayBorder)
                )
            }
        }
    }
}

// ── CONCEPT phase ──────────────────────────────────────────────────

@Composable
private fun ConceptPhase(lesson: Lesson, onContinue: () -> Unit) {
    SectionHeader(title = lesson.title, subtitle = lesson.subtitle.uppercase())

    Spacer(modifier = Modifier.height(24.dp))

    // Concept card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = lesson.concept,
                color = CyberWhite,
                fontSize = 20.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = lesson.explanation,
                color = GrayBody,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    ContinueButton(text = "继续", onClick = onContinue)
}

// ── HOW_TO_READ phase ──────────────────────────────────────────────

@Composable
private fun HowToReadPhase(howToRead: List<String>, onContinue: () -> Unit) {
    SectionHeader(title = "怎么看", subtitle = "HOW TO READ")

    Spacer(modifier = Modifier.height(24.dp))

    howToRead.forEachIndexed { index, step ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "${index + 1}.",
                color = AccentRed,
                fontSize = 14.sp,
                fontFamily = MonoFontFamily,
                modifier = Modifier.width(24.dp)
            )
            Text(
                text = step,
                color = GrayBody,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 24.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    ContinueButton(text = "开始测验", onClick = onContinue)
}

// ── QUIZ phase ─────────────────────────────────────────────────────

@Composable
private fun QuizPhase(
    lesson: Lesson,
    lessonState: LessonUiState,
    onSubmitAnswer: (Int) -> Unit,
    onSubmitBoolAnswer: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onClearFeedback: () -> Unit
) {
    val questionIndex = lessonState.currentQuestionIndex
    val question = lesson.questions.getOrNull(questionIndex)

    if (question == null) {
        Text("No more questions", color = GrayCaption)
        return
    }

    SectionHeader(title = "测验 ${questionIndex + 1}/${lesson.questions.size}", subtitle = "QUIZ")

    Spacer(modifier = Modifier.height(24.dp))

    when (question.type) {
        QuizType.BINARY_CLASSIFY -> {
            BinaryClassifyQuiz(
                prompt = question.prompt,
                items = question.items.map { it.key to (it.value == "阳" || it.value == "正确" || it.value == "阳爻" || it.value == "true") },
                onAnswerSelected = { selections ->
                    val allCorrect = question.items.zip(selections).all { (item, selected) ->
                        val expectedTrue = item.value == "阳" || item.value == "正确" || item.value == "阳爻" || item.value == "true"
                        selected == expectedTrue
                    }
                    onSubmitBoolAnswer(allCorrect)
                }
            )
        }
        QuizType.MATCHING -> {
            // Matching: show items as key → value pairs with options
            MatchingQuiz(
                prompt = question.prompt,
                items = question.items,
                explanation = question.explanation,
                onAnswerSelected = { correct -> onSubmitBoolAnswer(correct) }
            )
        }
        QuizType.ORDERING -> {
            Text(
                text = question.prompt,
                color = CyberWhite,
                fontSize = 16.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            question.items.forEach { item ->
                Text(
                    text = "• ${item.key}",
                    color = GrayBody,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        else -> {
            // SINGLE_CHOICE / CASE_JUDGE
            Text(
                text = question.prompt,
                color = CyberWhite,
                fontSize = 16.sp,
                fontFamily = WenKaiFontFamily,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options
            var selectedIndex by remember { mutableIntStateOf(-1) }

            question.options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                val feedback = lessonState.answerFeedback
                val showResult = feedback != null
                val isCorrect = feedback?.correct == true

                val borderColor = when {
                    showResult && isSelected && isCorrect -> AccentRed
                    showResult && isSelected && !isCorrect -> GrayBorder
                    isSelected -> CyberWhite
                    else -> GrayBorder
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, borderColor)
                        .clickable {
                            if (!showResult) {
                                selectedIndex = index
                                onSubmitAnswer(index)
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) CyberWhite else GrayBody,
                        fontSize = 14.sp,
                        fontFamily = WenKaiFontFamily
                    )
                }
            }
        }
    }

    // Feedback
    val feedback = lessonState.answerFeedback
    if (feedback != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (feedback.correct) "判断成立。" else "此处不宜先断。 ",
            color = if (feedback.correct) AccentRed else GrayCaption,
            fontSize = 14.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feedback.explanation,
            color = GrayBody,
            fontSize = 13.sp,
            fontFamily = WenKaiFontFamily,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        val isLast = questionIndex >= lesson.questions.size - 1
        ContinueButton(
            text = if (isLast) "查看结果" else "下一题",
            onClick = {
                onClearFeedback()
                if (isLast) onContinue()
            }
        )
    }
}

// ── RESULT phase ───────────────────────────────────────────────────

@Composable
private fun ResultPhase(
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

    Spacer(modifier = Modifier.height(32.dp))

    ContinueButton(text = "返回学习", onClick = onComplete)
}

// ── Shared components ──────────────────────────────────────────────

@Composable
private fun ContinueButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberWhite)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = CyberWhite,
            fontSize = 14.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }
}
