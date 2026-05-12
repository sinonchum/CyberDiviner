package com.cyberdiviner.ui.liuyao

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
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
 * 1. Question input → coin toss animation (6 rounds)
 * 2. Hexagram computation (LiuyaoEngine)
 * 3. Save reading to database
 * 4. LLM interpretation (streaming)
 * 5. Navigate to result screen
 */

enum class LiuyaoPhase {
    INPUT,          // User entering question
    TOSSING,        // Coin animation running
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
    val currentCoins: List<CoinState> = emptyList(),
    val isCoinAnimating: Boolean = false,
    val divinationResult: LiuyaoEngine.DivinationResult? = null,
    val llmInterpretation: String = "",
    val llmStreamChunks: String = "",
    val readingId: Long? = null,
    val errorMessage: String? = null,
    val progressMessage: String = ""
)

@HiltViewModel
class LiuyaoViewModel @Inject constructor(
    application: Application,
    private val llmService: LlmService,
    private val promptManager: PromptManager,
    private val divinationDao: DivinationDao,
    private val liuyaoDao: LiuyaoDao,
    private val configManager: LlmConfigManager
) : AndroidViewModel(application) {

    private val engine = LiuyaoEngine()
    private val _uiState = MutableStateFlow(LiuyaoUiState())
    val uiState: StateFlow<LiuyaoUiState> = _uiState.asStateFlow()

    // ── Public actions ────────────────────────────────────────────────────

    fun updateQuestion(question: String) {
        _uiState.value = _uiState.value.copy(question = question)
    }

    /**
     * Start the full divination flow: toss coins → compute → interpret.
     */
    fun startDivination() {
        val question = _uiState.value.question.trim()
        if (question.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请输入您的问题"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                phase = LiuyaoPhase.TOSSING,
                question = question,
                tossResults = emptyList(),
                currentCoins = emptyList(),
                currentTossIndex = 0,
                divinationResult = null,
                llmInterpretation = "",
                llmStreamChunks = "",
                errorMessage = null,
                progressMessage = "准备抛掷铜钱..."
            )

            // Animate 6 coin tosses
            val allTosses = mutableListOf<LineState>()
            for (i in 0 until 6) {
                // Generate coin states for this toss
                val coins = generateCoinStates()
                _uiState.value = _uiState.value.copy(
                    currentTossIndex = i,
                    currentCoins = coins.map { it.copy(isRevealed = false) },
                    isCoinAnimating = true,
                    tossResults = allTosses.toList(),
                    progressMessage = "第 ${i + 1} 爻抛掷中..."
                )

                // Spin animation delay
                delay(800L)

                // Reveal coins one by one
                for (j in coins.indices) {
                    val revealed = coins.mapIndexed { idx, coin ->
                        if (idx <= j) coin.copy(isRevealed = true) else coin
                    }
                    _uiState.value = _uiState.value.copy(currentCoins = revealed)
                    delay(200L)
                }

                // Final state — calculate line
                val lineState = coins.toLineState()
                allTosses.add(lineState)

                _uiState.value = _uiState.value.copy(
                    currentCoins = coins.map { it.copy(isRevealed = true) },
                    tossResults = allTosses.toList(),
                    isCoinAnimating = false
                )
                delay(400L)
            }

            // All 6 tosses done — compute hexagram
            _uiState.value = _uiState.value.copy(
                phase = LiuyaoPhase.COMPUTING,
                progressMessage = "计算卦象中..."
            )
            delay(300L)

            try {
                val result = engine.divine(question)
                _uiState.value = _uiState.value.copy(
                    divinationResult = result,
                    progressMessage = "卦象已成，解读中..."
                )

                // Save to database
                val readingId = saveReading(result, question)

                // Navigate to interpretation
                _uiState.value = _uiState.value.copy(
                    phase = LiuyaoPhase.INTERPRETING,
                    readingId = readingId,
                    progressMessage = "正在召唤赛博先知..."
                )

                // Start LLM interpretation
                streamLlmInterpretation(result, question)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = LiuyaoPhase.ERROR,
                    errorMessage = "起卦失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Dismiss error and return to input.
     */
    fun dismissError() {
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
        _uiState.value = LiuyaoUiState()
    }

    /**
     * Force result phase (e.g. after animation completes and interpretation is done).
     */
    fun showResult() {
        _uiState.value = _uiState.value.copy(phase = LiuyaoPhase.RESULT)
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private fun generateCoinStates(): List<CoinState> {
        return List(3) {
            CoinState(
                index = it,
                isHeads = kotlin.random.Random.nextBoolean(),
                isRevealed = false
            )
        }
    }

    private fun List<CoinState>.toLineState(): LineState {
        val sum = fold(0) { acc, coin -> acc + if (coin.isHeads) 2 else 1 }
        return when (sum) {
            6 -> LineState.OLD_YIN
            5 -> LineState.YOUNG_YANG
            4 -> LineState.YOUNG_YIN
            3 -> LineState.OLD_YANG
            else -> LineState.YOUNG_YANG
        }
    }

    private suspend fun saveReading(
        result: LiuyaoEngine.DivinationResult,
        question: String
    ): Long = withContext(Dispatchers.IO) {
        try {
            // Create the parent divination reading
            val reading = DivinationReading(
                type = DivinationType.LIUYAO,
                question = question,
                resultJson = result.summary()
            )
            val readingId = divinationDao.insert(reading)

            // Create the liuyao-specific sub-reading
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
            // Non-fatal — reading is still viewable from memory
            -1L
        }
    }

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

            // Build config from persisted settings
            val config = configManager.buildConfig(systemPrompt = systemPrompt)

            if (config == null) {
                // No API key — fall back to engine's built-in summary
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

            _uiState.value = _uiState.value.copy(
                llmInterpretation = fullText.ifBlank { result.summary() },
                phase = LiuyaoPhase.RESULT
            )

        } catch (e: Exception) {
            // LLM failure is non-fatal — fall back to engine summary
            _uiState.value = _uiState.value.copy(
                llmInterpretation = result.summary(),
                phase = LiuyaoPhase.RESULT
            )
        }
    }
}
