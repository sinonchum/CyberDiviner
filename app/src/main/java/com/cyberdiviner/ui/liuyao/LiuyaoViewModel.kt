package com.cyberdiviner.ui.liuyao

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LearningDao
import com.cyberdiviner.data.dao.LiuyaoDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import com.cyberdiviner.data.model.LiuyaoReading
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.LlmService
import com.cyberdiviner.data.remote.PromptManager
import com.cyberdiviner.engine.HexagramData.LineState
import com.cyberdiviner.engine.LiuyaoEngine
import com.cyberdiviner.engine.ShakeDetector
import com.cyberdiviner.engine.FortuneEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * LiuyaoViewModel — orchestrates the full liuyao divination flow:
 *
 * 1. Question input → user shakes phone (6 shakes = 6 lines)
 * 2. Hexagram computation (LiuyaoEngine)
 * 3. Save reading to database
 * 4. LLM interpretation (streaming)
 * 5. Navigate to result screen
 *
 * Physical shake detection replaces random number generation.
 * Each phone shake triggers one coin toss via the accelerometer.
 */

enum class LiuyaoPhase {
    INPUT,          // User entering question
    TOSSING,        // Waiting for shakes (6 rounds)
    COMPUTING,      // Engine computing hexagram
    INTERPRETING,   // LLM generating interpretation
    RESULT,         // Done, showing result
    ERROR           // Something went wrong
}

data class LiuyaoUiState(
    val phase: LiuyaoPhase = LiuyaoPhase.INPUT,
    val question: String = "",
    val currentTossIndex: Int = 0,
    val tossResults: List<LineState> = emptyList(),
    val shakeProgress: String = "摇一摇",
    val divinationResult: LiuyaoEngine.DivinationResult? = null,
    val llmInterpretation: String = "",
    val llmStreamChunks: String = "",
    val readingId: Long? = null,
    val errorMessage: String? = null,
    val progressMessage: String = "",
    val fourCharFortune: String = "",
    val fourCharMeaning: String = ""
)

@HiltViewModel
class LiuyaoViewModel @Inject constructor(
    application: Application,
    private val llmService: LlmService,
    private val promptManager: PromptManager,
    private val divinationDao: DivinationDao,
    private val learningDao: LearningDao,
    private val liuyaoDao: LiuyaoDao,
    private val configManager: LlmConfigManager
) : AndroidViewModel(application) {

    private val engine = LiuyaoEngine()
    private val _uiState = MutableStateFlow(LiuyaoUiState())

    // Learning annotations from completed lessons
    private val _learningAnnotations = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val learningAnnotations: StateFlow<List<Pair<String, String>>> = _learningAnnotations

    init {
        loadLearningAnnotations()
    }

    private fun loadLearningAnnotations() {
        viewModelScope.launch {
            learningDao.getProgressForPath("yijing_intro").collect { yj ->
                learningDao.getProgressForPath("liuyao_intro").collect { ly ->
                    val completed = (yj + ly).filter { it.completed }.map { it.lessonId }.toSet()
                    _learningAnnotations.value = com.cyberdiviner.ui.learning.LearningAnnotations.getForCompletedLessons(completed)
                }
            }
        }
    }
    val uiState: StateFlow<LiuyaoUiState> = _uiState.asStateFlow()

    // Shake detector — created on demand, started/stopped with phase
    private var shakeDetector: ShakeDetector? = null

    // Collect all toss results from shake events
    private val allTosses = mutableListOf<LineState>()

    // ── Public actions ────────────────────────────────────────────────────

    fun updateQuestion(question: String) {
        _uiState.value = _uiState.value.copy(question = question)
    }

    /**
     * Start the full divination flow: shake → compute → interpret.
     * Enters TOSSING phase and starts the accelerometer listener.
     */
    fun startDivination() {
        val question = _uiState.value.question.trim()
        if (question.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请输入您的问题"
            )
            return
        }

        allTosses.clear()

        _uiState.value = _uiState.value.copy(
            phase = LiuyaoPhase.TOSSING,
            question = question,
            tossResults = emptyList(),
            currentTossIndex = 0,
            divinationResult = null,
            llmInterpretation = "",
            llmStreamChunks = "",
            errorMessage = null,
            shakeProgress = "第 1 爻 · 摇一摇",
            progressMessage = "握住手机，用力摇动"
        )

        // Start accelerometer listener
        startShakeDetection()
    }

    /**
     * Called by ShakeDetector when a shake is detected.
     * Generates one coin toss, records the line, advances the count.
     */
    fun onShakeDetected() {
        val state = _uiState.value
        if (state.phase != LiuyaoPhase.TOSSING) return
        if (state.currentTossIndex >= 6) return

        // Generate one coin toss
        val toss = engine.throwCoins()
        allTosses.add(toss.lineState)

        val newIndex = state.currentTossIndex + 1
        val newState = when {
            newIndex >= 6 -> {
                // All 6 done — stop shaking, compute
                stopShakeDetection()
                LiuyaoPhase.COMPUTING
            }
            else -> LiuyaoPhase.TOSSING
        }

        val progressText = if (newIndex < 6) {
            "第 ${newIndex + 1} 爻 · 摇一摇"
        } else {
            "六爻已成"
        }

        _uiState.value = state.copy(
            currentTossIndex = newIndex,
            tossResults = allTosses.toList(),
            phase = newState,
            shakeProgress = progressText
        )

        // If all 6 done, proceed to computation
        if (newIndex >= 6) {
            viewModelScope.launch {
                delay(300L) // brief pause to show "六爻已成"
                computeHexagram()
            }
        }
    }

    /**
     * Dismiss error and return to input.
     */
    fun dismissError() {
        stopShakeDetection()
        _uiState.value = LiuyaoUiState()
    }

    /**
     * Navigate to result from history.
     */
    fun viewResult() {
        if (_uiState.value.divinationResult != null) {
            _uiState.value = _uiState.value.copy(phase = LiuyaoPhase.RESULT)
        }
    }

    /**
     * Start a new reading, resetting state.
     */
    fun newReading() {
        stopShakeDetection()
        allTosses.clear()
        _uiState.value = LiuyaoUiState()
    }

    /**
     * Force result phase.
     */
    fun showResult() {
        _uiState.value = _uiState.value.copy(phase = LiuyaoPhase.RESULT)
    }

    // ── Shake Detection ──────────────────────────────────────────────────

    private fun startShakeDetection() {
        if (shakeDetector == null) {
            shakeDetector = ShakeDetector(getApplication()) {
                // This runs on the main thread — safe to update state
                onShakeDetected()
            }
        }
        shakeDetector?.start()
    }

    private fun stopShakeDetection() {
        shakeDetector?.stop()
    }

    // ── Hexagram Computation ─────────────────────────────────────────────

    private suspend fun computeHexagram() {
        _uiState.value = _uiState.value.copy(
            phase = LiuyaoPhase.COMPUTING,
            progressMessage = "计算卦象中..."
        )
        delay(300L)

        try {
            // Build divination result from the 6 line states
            val result = engine.divineFromStates(
                question = _uiState.value.question,
                states = allTosses.toList()
            )
            _uiState.value = _uiState.value.copy(
                divinationResult = result,
                progressMessage = "卦象已成，解读中...",
                fourCharFortune = FortuneEngine.liuyaoFortune(result.primaryHexagram.chineseName),
                fourCharMeaning = FortuneEngine.liuyaoMeaning(FortuneEngine.liuyaoFortune(result.primaryHexagram.chineseName))
            )

            // Save to database
            val readingId = saveReading(result, _uiState.value.question)

            // Navigate to interpretation
            _uiState.value = _uiState.value.copy(
                phase = LiuyaoPhase.INTERPRETING,
                readingId = readingId,
                progressMessage = "正在召唤赛博先知..."
            )

            // Start LLM interpretation
            streamLlmInterpretation(result, _uiState.value.question)

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                phase = LiuyaoPhase.ERROR,
                errorMessage = "起卦失败: ${e.message}"
            )
        }
    }

    // ── Database ─────────────────────────────────────────────────────────

    private suspend fun saveReading(
        result: LiuyaoEngine.DivinationResult,
        question: String
    ): Long = withContext(Dispatchers.IO) {
        try {
            val reading = DivinationReading(
                type = DivinationType.LIUYAO,
                question = question,
                resultJson = result.summary()
            )
            val readingId = divinationDao.insert(reading)

            val liuyaoReading = LiuyaoReading(
                readingId = readingId,
                hexagramName = result.primaryHexagram.chineseName,
                hexagramNumber = result.primaryHexagram.number,
                upperTrigram = result.primaryHexagram.upperTrigram.chineseName,
                lowerTrigram = result.primaryHexagram.lowerTrigram.chineseName,
                hexagramLines = result.lines.joinToString(",") {
                    when (it.state) {
                        LineState.YOUNG_YANG -> "1"
                        LineState.YOUNG_YIN -> "0"
                        LineState.OLD_YANG -> "3"
                        LineState.OLD_YIN -> "2"
                    }
                },
                changingLines = result.tosses.mapIndexedNotNull { idx, toss ->
                    if (toss.lineState == LineState.OLD_YANG || toss.lineState == LineState.OLD_YIN) {
                        idx + 1
                    } else null
                }.joinToString(","),
                coinMethod = true
            )
            liuyaoDao.insert(liuyaoReading)

            readingId
        } catch (e: Exception) {
            -1L
        }
    }

    // ── LLM Interpretation ───────────────────────────────────────────────

    private suspend fun streamLlmInterpretation(
        result: LiuyaoEngine.DivinationResult,
        question: String
    ) {
        try {
            val systemPrompt = promptManager.resolveSystem(
                feature = "liuyao",
                persona = com.cyberdiviner.engine.Persona.DEFAULT
            )

            val changingLines = result.tosses.mapIndexedNotNull { idx, toss ->
                if (toss.lineState == LineState.OLD_YANG || toss.lineState == LineState.OLD_YIN) {
                    "${idx + 1}爻"
                } else null
            }.joinToString("、").ifEmpty { "无" }

            val userPrompt = promptManager.resolveUser(
                feature = "liuyao",
                variables = mapOf(
                    "hexagram_name" to "${result.primaryHexagram.chineseName} ${result.primaryHexagram.englishName}",
                    "hexagram_number" to result.primaryHexagram.number.toString(),
                    "upper_trigram" to "${result.primaryHexagram.upperTrigram.chineseName} (${result.primaryHexagram.upperTrigram.englishName})",
                    "lower_trigram" to "${result.primaryHexagram.lowerTrigram.chineseName} (${result.primaryHexagram.lowerTrigram.englishName})",
                    "changing_lines" to changingLines,
                    "day_gan_zhi" to java.time.LocalDate.now().toString(),
                    "question" to question
                )
            )

            val messages = listOf(LlmMessage(role = "user", content = userPrompt))
            val config = configManager.buildConfig(systemPrompt = systemPrompt)

            if (config == null) {
                _uiState.value = _uiState.value.copy(
                    llmInterpretation = result.summary(),
                    phase = LiuyaoPhase.RESULT
                )
                return
            }

            val fullText = llmService.completeStream(config, messages) { chunk ->
                if (chunk.delta.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        llmStreamChunks = _uiState.value.llmStreamChunks + chunk.delta
                    )
                }
            }

            val finalText = com.cyberdiviner.engine.Persona.stripActionDescriptions(fullText).ifBlank { result.summary() }
            _uiState.value = _uiState.value.copy(
                llmInterpretation = finalText,
                phase = LiuyaoPhase.RESULT
            )
            // Persist interpretation to database
            try {
                val rid = _uiState.value.readingId
                if (rid != null) {
                    val existing = liuyaoDao.getByReadingId(rid)
                    if (existing != null) {
                        liuyaoDao.update(existing.copy(interpretation = finalText))
                    }
                }
            } catch (_: Exception) {}

        } catch (e: Exception) {
            val fallback = result.summary()
            _uiState.value = _uiState.value.copy(
                llmInterpretation = fallback,
                phase = LiuyaoPhase.RESULT
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopShakeDetection()
    }
}
