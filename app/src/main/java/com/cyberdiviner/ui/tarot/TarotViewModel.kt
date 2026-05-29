package com.cyberdiviner.ui.tarot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LearningDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import com.cyberdiviner.data.model.TarotReading
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.PromptManager
import com.cyberdiviner.engine.offline.InferenceRouter
import com.cyberdiviner.engine.offline.OfflinePromptBuilder
import com.cyberdiviner.engine.Persona
import com.cyberdiviner.engine.FortuneEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// ── Spread types ──────────────────────────────────────────────────────────

enum class SpreadType(val displayName: String, val cardCount: Int, val positions: List<String>) {
    SINGLE(
        "单牌", 1,
        listOf("核心")
    ),
    THREE_CARD(
        "三牌阵", 3,
        listOf("过去", "现在", "未来")
    ),
    CELTIC_CROSS(
        "凯尔特十字", 10,
        listOf("当前处境", "挑战", "过去基础", "近期过去", "可能结果", "近期未来", "自我态度", "环境影响", "内心期望", "最终结果")
    ),
    HORSESHOE(
        "马蹄阵", 7,
        listOf("过去", "现在", "隐藏影响", "近期未来", "外部影响", "自身态度", "最终结果")
    )
}

// ── Card data ─────────────────────────────────────────────────────────────

data class TarotCard(
    val name: String,
    val nameZh: String,
    val number: Int,
    val suit: String,       // "major", "wands", "cups", "swords", "pentacles"
    val position: String,
    val isReversed: Boolean = false
)

// Major Arcana card data
private val majorArcana = listOf(
    TarotCard("The Fool", "愚者", 0, "major", ""),
    TarotCard("The Magician", "魔术师", 1, "major", ""),
    TarotCard("The High Priestess", "女祭司", 2, "major", ""),
    TarotCard("The Empress", "女皇", 3, "major", ""),
    TarotCard("The Emperor", "皇帝", 4, "major", ""),
    TarotCard("The Hierophant", "教皇", 5, "major", ""),
    TarotCard("The Lovers", "恋人", 6, "major", ""),
    TarotCard("The Chariot", "战车", 7, "major", ""),
    TarotCard("Strength", "力量", 8, "major", ""),
    TarotCard("The Hermit", "隐者", 9, "major", ""),
    TarotCard("Wheel of Fortune", "命运之轮", 10, "major", ""),
    TarotCard("Justice", "正义", 11, "major", ""),
    TarotCard("The Hanged Man", "倒吊人", 12, "major", ""),
    TarotCard("Death", "死神", 13, "major", ""),
    TarotCard("Temperance", "节制", 14, "major", ""),
    TarotCard("The Devil", "恶魔", 15, "major", ""),
    TarotCard("The Tower", "塔", 16, "major", ""),
    TarotCard("The Star", "星星", 17, "major", ""),
    TarotCard("The Moon", "月亮", 18, "major", ""),
    TarotCard("The Sun", "太阳", 19, "major", ""),
    TarotCard("Judgement", "审判", 20, "major", ""),
    TarotCard("The World", "世界", 21, "major", "")
)

private val suitNames = listOf("wands", "cups", "swords", "pentacles")
private val suitNamesZh = mapOf(
    "wands" to "权杖", "cups" to "圣杯", "swords" to "宝剑", "pentacles" to "星币"
)
private val rankNames = listOf(
    "Ace", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
    "Page", "Knight", "Queen", "King"
)
private val rankNamesZh = listOf(
    "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
    "侍从", "骑士", "王后", "国王"
)

private val fullDeck: List<TarotCard> = majorArcana + suitNames.flatMapIndexed { si, suit ->
    List(14) { ri ->
        TarotCard(
            name = "${rankNames[ri]} of ${suit.replaceFirstChar { it.uppercase() }}",
            nameZh = "${suitNamesZh[suit]}${rankNamesZh[ri]}",
            number = si * 14 + ri,
            suit = suit,
            position = ""
        )
    }
}

// ── UI state ──────────────────────────────────────────────────────────────

enum class TarotPhase {
    SELECT_SPREAD,
    SHUFFLING,
    DRAWING,
    REVEALING,
    INTERPRETING,
    RESULT,
    ERROR
}

data class TarotUiState(
    val phase: TarotPhase = TarotPhase.SELECT_SPREAD,
    val question: String = "",
    val selectedSpread: SpreadType = SpreadType.THREE_CARD,
    val recommendedSpread: SpreadType? = null,
    val drawnCards: List<TarotCard> = emptyList(),
    val revealedCount: Int = 0,
    val interpretation: String = "",
    val streamText: String = "",
    val readingId: Long? = null,
    val errorMessage: String? = null,
    val progressMessage: String = "",
    val fourCharFortune: String = "",
    val fourCharMeaning: String = ""
)

// ── ViewModel ─────────────────────────────────────────────────────────────

@HiltViewModel
class TarotViewModel @Inject constructor(
    application: Application,
    private val inferenceRouter: InferenceRouter,
    private val offlinePromptBuilder: OfflinePromptBuilder,
    private val divinationDao: DivinationDao,
    private val tarotDao: TarotDao,
    private val learningDao: LearningDao,
    private val promptManager: PromptManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TarotUiState())
    val uiState: StateFlow<TarotUiState> = _uiState.asStateFlow()

    // Learning annotations from completed lessons
    private val _learningAnnotations = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val learningAnnotations: StateFlow<List<Pair<String, String>>> = _learningAnnotations

    init {
        loadLearningAnnotations()
    }

    private fun loadLearningAnnotations() {
        viewModelScope.launch {
            learningDao.getProgressForPath("tarot_intro").collect { progress ->
                val completed = progress.filter { it.completed }.map { it.lessonId }.toSet()
                _learningAnnotations.value = com.cyberdiviner.ui.learning.LearningAnnotations.getForCompletedLessons(completed)
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun updateQuestion(question: String) {
        _uiState.value = _uiState.value.copy(question = question)
        // Auto-recommend spread based on question complexity
        _uiState.value = _uiState.value.copy(
            recommendedSpread = recommendSpread(question)
        )
    }

    fun selectSpread(spread: SpreadType) {
        _uiState.value = _uiState.value.copy(selectedSpread = spread)
    }

    fun startReading() {
        val state = _uiState.value
        if (state.question.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入你的问题")
            return
        }

        viewModelScope.launch {
            val spread = state.selectedSpread
            _uiState.value = _uiState.value.copy(
                phase = TarotPhase.SHUFFLING,
                selectedSpread = spread,
                progressMessage = ""
            )
        }
    }

    fun shuffleAndDraw() {
        viewModelScope.launch {
            val spread = _uiState.value.selectedSpread
            _uiState.value = _uiState.value.copy(
                phase = TarotPhase.DRAWING,
                progressMessage = "正在抽取 ${spread.displayName}..."
            )

            // Draw cards
            val cards = drawCards(spread)
            _uiState.value = _uiState.value.copy(
                drawnCards = cards,
                phase = TarotPhase.REVEALING,
                revealedCount = 0,
                progressMessage = "翻牌中..."
            )

            // Reveal cards one by one
            for (i in cards.indices) {
                delay(600L)
                _uiState.value = _uiState.value.copy(revealedCount = i + 1)
            }

            // Save reading
            val readingId = saveReading(cards, spread, _uiState.value.question)

            // Interpret
            _uiState.value = _uiState.value.copy(
                phase = TarotPhase.INTERPRETING,
                readingId = readingId,
                progressMessage = "赛博先知正在解读..."
            )

            streamInterpretation(cards, spread, _uiState.value.question)
        }
    }

    fun dismissError() {
        _uiState.value = TarotUiState()
    }

    fun newReading() {
        _uiState.value = TarotUiState()
    }

    // ── Internal ────────────────────────────────────────────────────────

    private fun recommendSpread(question: String): SpreadType {
        val len = question.length
        val hasComplexKeywords = listOf("为什么", "如何", "应该", "关系", "事业", "选择", "将来", "未来")
            .any { question.contains(it) }
        return when {
            len > 30 || hasComplexKeywords -> SpreadType.CELTIC_CROSS
            len > 15 -> SpreadType.THREE_CARD
            else -> SpreadType.SINGLE
        }
    }

    private fun drawCards(spread: SpreadType): List<TarotCard> {
        val deck = fullDeck.shuffled()
        return deck.take(spread.cardCount).mapIndexed { i, card ->
            card.copy(
                position = spread.positions[i],
                isReversed = kotlin.random.Random.nextBoolean()
            )
        }
    }

    private suspend fun saveReading(
        cards: List<TarotCard>,
        spread: SpreadType,
        question: String
    ): Long = withContext(Dispatchers.IO) {
        try {
            val cardJson = json.encodeToString(cards.map { c ->
                mapOf(
                    "card" to c.name,
                    "card_zh" to c.nameZh,
                    "number" to c.number.toString(),
                    "suit" to c.suit,
                    "position" to c.position,
                    "isReversed" to c.isReversed.toString()
                )
            })

            // Parent reading
            val reading = DivinationReading(
                type = DivinationType.TAROT,
                question = question,
                resultJson = cardJson
            )
            val readingId = divinationDao.insert(reading)

            // Tarot sub-reading
            val tarotReading = TarotReading(
                readingId = readingId,
                spreadType = spread.name.lowercase(),
                cardCount = spread.cardCount,
                cardsJson = cardJson
            )
            tarotDao.insert(tarotReading)

            readingId
        } catch (e: Exception) {
            -1L
        }
    }

    private suspend fun streamInterpretation(
        cards: List<TarotCard>,
        spread: SpreadType,
        question: String
    ) {
        try {
            val systemPrompt = promptManager.resolveSystem(
                feature = "tarot",
                persona = Persona.DEFAULT
            )

            val spreadText = cards.joinToString("\n") { c ->
                val rev = if (c.isReversed) "（逆位）" else "（正位）"
                "${c.position}: ${c.nameZh} ${c.name} $rev"
            }

            val userPrompt = promptManager.resolveUser(
                feature = "tarot",
                variables = mapOf(
                    "spread" to spreadText,
                    "question" to question
                )
            )

            val messages = listOf(LlmMessage(role = "user", content = userPrompt))
            val offlinePrompt = offlinePromptBuilder.buildTarotPrompt(
                cards = spreadText,
                question = question
            )

            val fullText = inferenceRouter.completeStream(
                feature = "tarot",
                messages = messages,
                offlineUserPrompt = offlinePrompt
            ) { delta ->
                _uiState.value = _uiState.value.copy(
                    streamText = _uiState.value.streamText + delta
                )
            }.text

            val finalText = com.cyberdiviner.engine.Persona.stripActionDescriptions(fullText).ifBlank { buildFallbackInterpretation(cards, spread, question) }
            _uiState.value = _uiState.value.copy(
                interpretation = finalText,
                phase = TarotPhase.RESULT,
                fourCharFortune = FortuneEngine.tarotFortune(cards[0].nameZh, cards[0].isReversed),
                fourCharMeaning = FortuneEngine.tarotMeaning(cards[0].nameZh, cards[0].isReversed)
            )
            // Persist interpretation to database
            try {
                val rid = _uiState.value.readingId
                if (rid != null) {
                    val existing = tarotDao.getByReadingId(rid)
                    if (existing != null) {
                        tarotDao.update(existing.copy(interpretation = finalText))
                    }
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            val fallback = buildFallbackInterpretation(cards, spread, question)
            _uiState.value = _uiState.value.copy(
                interpretation = fallback,
                phase = TarotPhase.RESULT,
                fourCharFortune = FortuneEngine.tarotFortune(cards[0].nameZh, cards[0].isReversed),
                fourCharMeaning = FortuneEngine.tarotMeaning(cards[0].nameZh, cards[0].isReversed)
            )
        }
    }

    private fun buildFallbackInterpretation(
        cards: List<TarotCard>,
        spread: SpreadType,
        question: String
    ): String {
        val sb = StringBuilder()
        sb.appendLine("${spread.displayName}解读")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine()
        cards.forEach { card ->
            val rev = if (card.isReversed) "逆位" else "正位"
            sb.appendLine("【${card.position}】${card.nameZh} — $rev")
        }
        sb.appendLine()
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("你的问题：$question")
        sb.appendLine()
        sb.appendLine("信号提示：牌阵已展开，但赛博先知暂时离线。")
        sb.appendLine("请在设置中配置 API 密钥以获取完整的解读。")
        return sb.toString()
    }
}
