package com.cyberdiviner.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.LearningDao
import com.cyberdiviner.data.model.learning.LearningProgressEntity
import com.cyberdiviner.data.model.learning.LearningStatsEntity
import com.cyberdiviner.data.model.learning.Lesson
import com.cyberdiviner.data.model.learning.LessonPath
import com.cyberdiviner.engine.learning.LessonCatalogLiuyao
import com.cyberdiviner.engine.learning.LessonCatalogPractice
import com.cyberdiviner.engine.learning.LessonCatalogTarot
import com.cyberdiviner.engine.learning.LessonCatalogYijing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI-facing path definition — mirrors [LessonPath] with an [icon] and
 * [totalLessons] convenience field so existing composables work unchanged.
 */
data class LearningPath(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val totalLessons: Int,
    val lessonIds: List<String>   // real catalog lesson IDs
)

/**
 * LearningViewModel — drives the learning hub, path detail, and lesson screens.
 *
 * Injects [LearningDao] for persistence.  Catalogs are read from the static
 * [LessonCatalog*] objects and mapped into [LearningPath] definitions.
 */
/** Quiz data wrapper for the lesson screen UI */
sealed class QuizData {
    abstract val question: String
    data class SingleChoice(override val question: String, val options: List<String>, val correctIndex: Int, val explanation: String) : QuizData()
    data class BinaryClassify(override val question: String, val items: List<Pair<String, Boolean>>, val explanation: String) : QuizData()
    data class Matching(override val question: String, val left: List<String>, val right: List<String>, val explanation: String) : QuizData()
    data class Ordering(override val question: String, val items: List<String>, val explanation: String) : QuizData()
    data class CaseJudge(override val question: String, val scenario: String, val options: List<String>, val correctIndex: Int, val explanation: String) : QuizData()
}

enum class LessonPhase { CONCEPT, HOW_TO_READ, QUIZ, RESULT }

data class AnswerFeedback(
    val correct: Boolean,
    val explanation: String
)

data class LessonUiState(
    val lesson: Lesson? = null,
    val phase: LessonPhase = LessonPhase.CONCEPT,
    val isLoading: Boolean = true,
    val currentQuestionIndex: Int = 0,
    val selectedAnswerId: String? = null,
    val answerFeedback: AnswerFeedback? = null,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Int = 0,
    val isComplete: Boolean = false,
    val quiz: QuizData? = null,
    val quizCorrect: Boolean? = null
)

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningDao: LearningDao
) : ViewModel() {

    // ── Stats ──────────────────────────────────────────────────────────

    val stats: StateFlow<LearningStatsEntity?> = learningDao
        .getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Paths ──────────────────────────────────────────────────────────

    private val _paths = MutableStateFlow(buildPaths())
    val paths: StateFlow<List<LearningPath>> = _paths.asStateFlow()

    // ── Path progress map ──────────────────────────────────────────────

    private val _pathProgress = MutableStateFlow<Map<String, List<LearningProgressEntity>>>(emptyMap())
    val pathProgress: StateFlow<Map<String, List<LearningProgressEntity>>> = _pathProgress.asStateFlow()

    // ── Current lesson state ───────────────────────────────────────────

    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()

    private val _lessonProgress = MutableStateFlow<LearningProgressEntity?>(null)
    val lessonProgress: StateFlow<LearningProgressEntity?> = _lessonProgress.asStateFlow()

    // ── Feedback after answer ──────────────────────────────────────────

    private val _answerFeedback = MutableStateFlow<AnswerFeedback?>(null)
    val answerFeedback: StateFlow<AnswerFeedback?> = _answerFeedback.asStateFlow()

    private val _lessonState = MutableStateFlow(LessonUiState())
    val lessonState: StateFlow<LessonUiState> = _lessonState.asStateFlow()

    // ── Load / query ───────────────────────────────────────────────────

    /**
     * Observe progress for every lesson in [pathId].
     * Results are collected into [pathProgress].
     */
    fun loadPathProgress(pathId: String) {
        viewModelScope.launch {
            learningDao.getProgressForPath(pathId).collect { list ->
                _pathProgress.value = _pathProgress.value.toMutableMap().apply {
                    put(pathId, list)
                }
            }
        }
    }

    /**
     * Get a lesson from the catalogs by its id.
     */
    fun getLesson(lessonId: String): Lesson? = allLessons().find { it.id == lessonId }

    /**
     * Load a lesson into [currentLesson] and its persisted progress into
     * [lessonProgress].
     */
    fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            val lesson = getLesson(lessonId)
            _currentLesson.value = lesson
            _lessonProgress.value = learningDao.getProgress(lessonId)
            _answerFeedback.value = null
            _lessonState.value = LessonUiState(
                lesson = lesson,
                phase = LessonPhase.CONCEPT,
                isLoading = lesson == null,
                totalQuestions = lesson?.questions?.size ?: 0
            )
        }
    }

    /**
     * Returns persisted progress list for a path.
     */
    suspend fun getPathProgress(pathId: String): List<LearningProgressEntity> =
        learningDao.getProgressForPath(pathId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .value

    /**
     * Returns completed count for a given path.
     */
    fun getCompletedCount(pathId: String): Int =
        _pathProgress.value[pathId]?.count { it.completed } ?: 0

    /**
     * Returns total lesson count for a given path.
     */
    fun getTotalCount(pathId: String): Int =
        allLessons().count { it.pathId == pathId }

    // ── Submit answer ──────────────────────────────────────────────────

    /**
     * Evaluate the user's answer for a quiz question.
     *
     * @param questionIndex zero-based index into the current lesson's questions list
     * @param selectedAnswerIds the answer ids the user selected
     */
    fun submitAnswer(questionIndex: Int, selectedAnswerIds: List<String>) {
        val lesson = _currentLesson.value ?: return
        val question = lesson.questions.getOrNull(questionIndex) ?: return

        val correct = selectedAnswerIds.toSet() == question.correctAnswerIds.toSet()
        val explanation = if (correct) question.explanationCorrect else question.explanationWrong

        _answerFeedback.value = AnswerFeedback(
            correct = correct,
            explanation = explanation
        )
    }

    // ── Complete lesson ────────────────────────────────────────────────

    /**
     * Mark a lesson as completed, update XP / streak / title.
     *
     * @param correct whether the user answered correctly
     */
    fun completeLesson(correct: Boolean) {
        val lesson = _currentLesson.value ?: return
        viewModelScope.launch {
            val existing = learningDao.getProgress(lesson.id)
            val xpGain = if (correct) 25 else 5
            val scoreNow = if (correct) 100 else 0

            learningDao.upsertProgress(
                LearningProgressEntity(
                    lessonId = lesson.id,
                    pathId = lesson.pathId,
                    completed = true,
                    score = maxOf(scoreNow, existing?.score ?: 0),
                    attempts = (existing?.attempts ?: 0) + 1,
                    lastCompletedAt = System.currentTimeMillis(),
                    mastery = if (correct) 100 else 50
                )
            )

            // ── Update global stats ──
            val cur = stats.value ?: LearningStatsEntity()
            val today = LocalDate.now().toString()
            val yesterday = LocalDate.now().minusDays(1).toString()

            val newStreak = when (cur.lastStudyDate) {
                today -> cur.currentStreak          // already studied today
                yesterday -> cur.currentStreak + 1  // streak continues
                else -> 1                           // streak reset
            }

            val newXp = cur.totalXp + xpGain
            val newTitle = titleForXp(newXp)

            learningDao.updateStats(
                cur.copy(
                    totalXp = newXp,
                    currentStreak = newStreak,
                    bestStreak = maxOf(cur.bestStreak, newStreak),
                    lastStudyDate = today,
                    title = newTitle
                )
            )

            // Refresh lesson progress snapshot
            _lessonProgress.value = learningDao.getProgress(lesson.id)
        }
    }

    /**
     * Clear answer feedback and advance to the next question.
     */
    fun clearFeedback() {
        val state = _lessonState.value
        val nextIndex = state.currentQuestionIndex + 1
        _lessonState.value = state.copy(
            answerFeedback = null,
            currentQuestionIndex = nextIndex
        )
    }

    /** Advance to the next phase in the lesson flow */
    fun nextPhase() {
        val current = _lessonState.value
        val next = when (current.phase) {
            LessonPhase.CONCEPT -> LessonPhase.HOW_TO_READ
            LessonPhase.HOW_TO_READ -> LessonPhase.QUIZ
            LessonPhase.QUIZ -> LessonPhase.RESULT
            LessonPhase.RESULT -> LessonPhase.RESULT
        }
        _lessonState.value = current.copy(phase = next)
    }

    /** Submit a quiz answer and update state */
    fun submitQuizAnswer(selectedIndex: Int) {
        val state = _lessonState.value
        val lesson = state.lesson ?: return
        val question = lesson.questions.getOrNull(state.currentQuestionIndex) ?: return

        val correct = question.correctAnswerIds.contains(selectedIndex.toString())
        _lessonState.value = state.copy(
            answerFeedback = AnswerFeedback(
                correct = correct,
                explanation = if (correct) question.explanationCorrect else question.explanationWrong
            ),
            correctCount = state.correctCount + if (correct) 1 else 0,
            quizCorrect = correct
        )
    }

    /** Submit a quiz answer from a boolean result (for binary/matching quizzes) */
    fun submitQuizAnswerFromBool(correct: Boolean) {
        val state = _lessonState.value
        val lesson = state.lesson ?: return
        val question = lesson.questions.getOrNull(state.currentQuestionIndex) ?: return

        _lessonState.value = state.copy(
            answerFeedback = AnswerFeedback(
                correct = correct,
                explanation = question.explanation.ifEmpty {
                    if (correct) question.explanationCorrect else question.explanationWrong
                }
            ),
            correctCount = state.correctCount + if (correct) 1 else 0,
            quizCorrect = correct
        )
    }

    // ── Title ladder ───────────────────────────────────────────────────

    private fun titleForXp(xp: Int): String = when {
        xp >= 2000 -> "天机阁主"
        xp >= 1200 -> "术数宗师"
        xp >= 800  -> "卦象大师"
        xp >= 500  -> "六爻通达"
        xp >= 300  -> "初窥天机"
        xp >= 150  -> "卦门弟子"
        xp >= 50   -> "卦象学徒"
        else       -> "初入卦门"
    }

    // ── Path definitions ───────────────────────────────────────────────

    private fun buildPaths(): List<LearningPath> = listOf(
        LearningPath(
            id = "yijing_intro",
            title = "易经入门",
            subtitle = "I Ching Foundations",
            icon = "☰",
            totalLessons = LessonCatalogYijing.lessons.size,
            lessonIds = LessonCatalogYijing.lessons.map { it.id }
        ),
        LearningPath(
            id = "liuyao_intro",
            title = "六爻预测",
            subtitle = "Liuyao Prediction",
            icon = "⚊",
            totalLessons = LessonCatalogLiuyao.lessons.size,
            lessonIds = LessonCatalogLiuyao.lessons.map { it.id }
        ),
        LearningPath(
            id = "tarot_intro",
            title = "塔罗牌",
            subtitle = "Tarot Reading",
            icon = "☆",
            totalLessons = LessonCatalogTarot.lessons.size,
            lessonIds = LessonCatalogTarot.lessons.map { it.id }
        ),
        LearningPath(
            id = "practice",
            title = "实战断卦",
            subtitle = "Practice & Review",
            icon = "☰",
            totalLessons = LessonCatalogPractice.lessons.size,
            lessonIds = LessonCatalogPractice.lessons.map { it.id }
        )
    )

    /** Unified list of all catalog lessons. */
    private fun allLessons(): List<Lesson> =
        LessonCatalogYijing.lessons +
        LessonCatalogLiuyao.lessons +
        LessonCatalogTarot.lessons +
        LessonCatalogPractice.lessons
}
