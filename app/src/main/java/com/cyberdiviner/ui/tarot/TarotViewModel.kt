package com.cyberdiviner.ui.tarot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LearningDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import com.cyberdiviner.data.model.InferenceMode
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
                streamText = "",
                progressMessage = "正在召唤赛博先知"
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

            val result = inferenceRouter.completeStream(
                feature = "tarot",
                messages = messages,
                offlineUserPrompt = offlinePrompt
            ) { delta ->
                _uiState.value = _uiState.value.copy(
                    streamText = _uiState.value.streamText + delta
                )
            }
            val fullText = result.text

            val candidateText = if (result.isOffline) {
                com.cyberdiviner.engine.Persona.cleanOfflineOutput(fullText)
            } else {
                com.cyberdiviner.engine.Persona.stripActionDescriptions(fullText)
            }
            val fallbackText = buildFallbackInterpretation(cards, spread, question)
            val lowQuality = candidateText.isBlank() || isLowQualityTarotOutput(
                candidateText
                    .replace(Regex("\\n{3,}"), "\n\n")
                    .replace(Regex("(?i)\\b(Knight|Page|Queen|King|Ace|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten) of (Cups|Swords|Wands|Pentacles)\\b")) { match ->
                        englishCardNameToChinese(match.value)
                    }
                    .trim(),
                cards
            )
            if (result.isOffline && inferenceRouter.currentMode() == InferenceMode.OFFLINE && lowQuality) {
                _uiState.value = _uiState.value.copy(
                    interpretation = "",
                    streamText = "",
                    phase = TarotPhase.ERROR,
                    errorMessage = "离线先知输出异常。请重新占卡，或在配置页重新加载离线模型。"
                )
                return
            }
            val finalText = normalizeTarotInterpretation(candidateText, cards, fallbackText)
            _uiState.value = _uiState.value.copy(
                interpretation = finalText,
                phase = TarotPhase.RESULT,
                fourCharFortune = FortuneEngine.tarotFortune(cards[0].nameZh, cards[0].isReversed, question, finalText),
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
            if (inferenceRouter.currentMode() == InferenceMode.OFFLINE) {
                _uiState.value = _uiState.value.copy(
                    interpretation = "",
                    streamText = "",
                    phase = TarotPhase.ERROR,
                    errorMessage = "离线先知未能成文。请确认离线模型已下载并已启用，稍后再试。"
                )
                return
            }
            val fallback = buildFallbackInterpretation(cards, spread, question)
            _uiState.value = _uiState.value.copy(
                interpretation = fallback,
                phase = TarotPhase.RESULT,
                fourCharFortune = FortuneEngine.tarotFortune(cards[0].nameZh, cards[0].isReversed, question, fallback),
                fourCharMeaning = FortuneEngine.tarotMeaning(cards[0].nameZh, cards[0].isReversed)
            )
        }
    }

    private fun normalizeTarotInterpretation(
        candidate: String,
        cards: List<TarotCard>,
        fallback: String
    ): String {
        val cleaned = candidate
            .replace(Regex("\\n{3,}"), "\n\n")
            .replace(Regex("(?i)\\b(Knight|Page|Queen|King|Ace|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten) of (Cups|Swords|Wands|Pentacles)\\b")) { match ->
                englishCardNameToChinese(match.value)
            }
            .trim()

        if (cleaned.isBlank() || isLowQualityTarotOutput(cleaned, cards)) {
            return fallback
        }
        return cleaned
    }

    private fun isLowQualityTarotOutput(text: String, cards: List<TarotCard>): Boolean {
        val numericPseudoCards = Regex("""\b\d{1,2}(?:-\d{1,2}){1,3}\b""").findAll(text).count()
        val digitLoop = Regex("""\b(?:12|21|1212|2121){3,}\b""").containsMatchIn(text) ||
            Regex("""\d{8,}""").containsMatchIn(text)
        val repeatedUnits = repeatedUnitCount(text)
        val repeatedPhraseHits = listOf("你可能需要", "更强的自信心", "更强的执行力", "并有更强")
            .sumOf { phrase -> Regex(Regex.escape(phrase)).findAll(text).count() }
        val englishCardMentions = Regex("""\b(?:Knight|Page|Queen|King|Ace|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten) of (?:Cups|Swords|Wands|Pentacles)\b""", RegexOption.IGNORE_CASE)
            .findAll(text)
            .count()
        val promptEchoMarkers = listOf(
            "先解读每张牌", "这张牌的含义是", "牌面：", "请简要解读",
            "最终结果：", "每张牌的含义及其位置关系", "不要写数字代号"
        ).count { text.contains(it) }
        val realCardMentions = cards.count { card ->
            text.contains(card.nameZh) || text.contains(card.name)
        }
        val enoughCardContext = cards.size == 1 || realCardMentions >= minOf(2, cards.size)

        return numericPseudoCards >= 2 ||
            digitLoop ||
            repeatedUnits >= 2 ||
            repeatedPhraseHits >= 8 ||
            englishCardMentions >= 2 ||
            promptEchoMarkers >= 2 ||
            !enoughCardContext ||
            text.length > 1400 ||
            text.length < 120
    }

    private fun englishCardNameToChinese(name: String): String {
        val normalized = name.lowercase()
        val suit = when {
            normalized.contains("cups") -> "圣杯"
            normalized.contains("swords") -> "宝剑"
            normalized.contains("wands") -> "权杖"
            normalized.contains("pentacles") -> "星币"
            else -> ""
        }
        val rank = when {
            normalized.startsWith("ace") -> "一"
            normalized.startsWith("two") -> "二"
            normalized.startsWith("three") -> "三"
            normalized.startsWith("four") -> "四"
            normalized.startsWith("five") -> "五"
            normalized.startsWith("six") -> "六"
            normalized.startsWith("seven") -> "七"
            normalized.startsWith("eight") -> "八"
            normalized.startsWith("nine") -> "九"
            normalized.startsWith("ten") -> "十"
            normalized.startsWith("page") -> "侍从"
            normalized.startsWith("knight") -> "骑士"
            normalized.startsWith("queen") -> "王后"
            normalized.startsWith("king") -> "国王"
            else -> ""
        }
        return if (suit.isNotBlank() && rank.isNotBlank()) suit + rank else name
    }

    private fun repeatedUnitCount(text: String): Int {
        return text
            .split(Regex("[。！？!?\\n]+"))
            .map { it.trim() }
            .filter { it.length >= 12 }
            .groupingBy { it }
            .eachCount()
            .count { it.value >= 2 }
    }

    private fun buildFallbackInterpretation(
        cards: List<TarotCard>,
        spread: SpreadType,
        question: String
    ): String {
        val sb = StringBuilder()
        sb.appendLine("塔罗解读")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine()
        sb.appendLine("一、牌阵总论")
        sb.appendLine("此阵以${spread.displayName}显影，所问为「${question.ifBlank { "当下之势" }}」。牌面所示并非定命，而是提醒你看清当下力量的流向。")
        sb.appendLine()
        sb.appendLine("二、逐牌详析")
        cards.forEach { card ->
            val rev = if (card.isReversed) "逆位" else "正位"
            sb.appendLine("【${card.position}】${card.nameZh}（$rev）：${FortuneEngine.tarotMeaning(card.nameZh, card.isReversed)}。")
        }
        sb.appendLine()
        sb.appendLine("三、最终指引")
        sb.appendLine("此时宜先稳心，再择一处最可掌控之事落子。请知会本地专属先知：牌阵初断可作趋吉避凶之参照，不作定命之论。")
        return sb.toString()
    }
}
