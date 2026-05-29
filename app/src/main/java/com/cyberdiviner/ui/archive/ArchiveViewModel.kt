package com.cyberdiviner.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LearningDao
import com.cyberdiviner.data.dao.LiuyaoDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.dao.VisionDao
import com.cyberdiviner.data.model.DivinationReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cyberdiviner.engine.FortuneEngine

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val divinationDao: DivinationDao,
    private val learningDao: LearningDao,
    private val liuyaoDao: LiuyaoDao,
    private val tarotDao: TarotDao,
    private val visionDao: VisionDao
) : ViewModel() {

    val readings: StateFlow<List<DivinationReading>> = divinationDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Learning stats for review section
    val learningStats = learningDao.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Get hexagram name from liuyao_readings table */
    suspend fun getHexagramName(readingId: Long): String? {
        return try {
            liuyaoDao.getByReadingId(readingId)?.hexagramName
        } catch (e: Exception) { null }
    }

    /** Get 4-char tarot summary + one-line interpretation */
    suspend fun getTarotSummary(readingId: Long): TarotArchiveSummary? {
        return try {
            val reading = tarotDao.getByReadingId(readingId) ?: return null
            val cards = parseTarotCards(reading.cardsJson)
            if (cards.isEmpty()) return null

            val first = cards[0]
            val title = FortuneEngine.tarotFortune(first.nameZh, first.isReversed)
            val interp = generateTarotInterpretation(cards)

            TarotArchiveSummary(title = title, interpretation = interp)
        } catch (e: Exception) { null }
    }

    /** Get 4-char liuyao summary + one-line interpretation */
    suspend fun getLiuyaoSummary(readingId: Long): LiuyaoArchiveSummary? {
        return try {
            val hexName = liuyaoDao.getByReadingId(readingId)?.hexagramName
                ?: return null
            val title = FortuneEngine.liuyaoFortune(hexName)
            val interp = FortuneEngine.liuyaoMeaning(title)
            LiuyaoArchiveSummary(title = title, interpretation = interp)
        } catch (e: Exception) { null }
    }

    /** Get vision reading summary: Pair(title, interpretation) */
    suspend fun getVisionSummary(readingId: Long): Pair<String, String>? {
        return try {
            // Get interpretation from sub-reading table
            val interp = visionDao.getByReadingId(readingId)?.interpretation ?: ""

            // Generate 4-char fortune summary from interpretation text
            val title = FortuneEngine.visionFortune(interp)

            // One-sentence: first sentence of interpretation, or contextual fallback from title
            val oneSentence = if (interp.isNotBlank() && !isVisionTechnicalData(interp)) {
                val end = interp.indexOfFirst { it == '。' || it == '！' || it == '？' || it == '.' }
                val s = if (end > 0) interp.substring(0, end + 1) else interp.take(50)
                if (s.length > 50) s.take(47) + "..." else s
            } else {
                FortuneEngine.visionMeaning(title)
            }

            Pair(title, oneSentence)
        } catch (e: Exception) { null }
    }

    /** Detect if vision interpretation is raw technical/fallback data, not a meaningful reading */
    private fun isVisionTechnicalData(text: String): Boolean {
        val markers = listOf(
            "面相分析", "脸型", "对称性", "神经影像", "面部扫描",
            "━━━", "【脸型】", "【额头】", "【眼睛】", "【鼻子】",
            "信号提示", "赛博先知暂时离线"
        )
        return markers.any { text.contains(it) }
    }


    /** Get interpretation from the sub-reading table for any type */
    suspend fun getInterpretation(readingId: Long, type: com.cyberdiviner.data.model.DivinationType): String {
        return try {
            when (type) {
                com.cyberdiviner.data.model.DivinationType.LIUYAO ->
                    liuyaoDao.getByReadingId(readingId)?.interpretation ?: ""
                com.cyberdiviner.data.model.DivinationType.TAROT ->
                    tarotDao.getByReadingId(readingId)?.interpretation ?: ""
                com.cyberdiviner.data.model.DivinationType.VISION ->
                    visionDao.getByReadingId(readingId)?.interpretation ?: ""
                com.cyberdiviner.data.model.DivinationType.ORACLE -> {
                    // resultJson may be plain text (new) or JSON (old format)
                    val reading = divinationDao.getById(readingId) ?: return ""
                    val raw = reading.resultJson.trim()
                    if (raw.startsWith("{")) {
                        val m = Regex("\"response\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(raw)
                        m?.groupValues?.get(1)?.replace("\\\"", "\"")?.replace("\\n", "\n") ?: raw
                    } else {
                        raw
                    }
                }
                else -> ""
            }
        } catch (e: Exception) { "" }
    }

    fun deleteReading(reading: DivinationReading) {
        viewModelScope.launch {
            divinationDao.delete(reading)
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private data class ParsedCard(val nameZh: String, val isReversed: Boolean, val position: String)

    private fun parseTarotCards(json: String): List<ParsedCard> {
        val cards = mutableListOf<ParsedCard>()
        // Parse JSON array of card objects
        val entries = json.split(Regex("\\{")).drop(1)
        for (entry in entries) {
            val nameZh = Regex("\"card_zh\"\\s*:\\s*\"([^\"]+)\"").find(entry)?.groupValues?.get(1) ?: continue
            val reversed = Regex("\"isReversed\"\\s*:\\s*\"true\"").containsMatchIn(entry)
            val position = Regex("\"position\"\\s*:\\s*\"([^\"]+)\"").find(entry)?.groupValues?.get(1) ?: ""
            cards.add(ParsedCard(nameZh, reversed, position))
        }
        return cards
    }


    /** Generate one-line interpretation from tarot cards */
    private fun generateTarotInterpretation(cards: List<ParsedCard>): String {
        val first = cards[0]

        // Always return just the fortune meaning, never card names
        return FortuneEngine.tarotMeaning(first.nameZh, first.isReversed)
    }

    // ── Liuyao Fortune Mapping ────────────────────────────────

    // Fortune mapping logic now lives in FortuneEngine (shared with all divination screens)
}

data class TarotArchiveSummary(
    val title: String,
    val interpretation: String
)

data class LiuyaoArchiveSummary(
    val title: String,
    val interpretation: String
)
