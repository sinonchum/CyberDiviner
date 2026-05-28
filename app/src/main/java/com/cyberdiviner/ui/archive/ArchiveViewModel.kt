package com.cyberdiviner.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
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

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val divinationDao: DivinationDao,
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
            val title = generateTarotFourCharSummary(first.nameZh, first.isReversed)
            val interp = generateTarotInterpretation(cards)

            TarotArchiveSummary(title = title, interpretation = interp)
        } catch (e: Exception) { null }
    }

    /** Get vision reading summary: Pair(title, interpretation) */
    suspend fun getVisionSummary(readingId: Long): Pair<String, String>? {
        return try {
            val reading = divinationDao.getById(readingId) ?: return null
            val json = reading.resultJson

            // Extract conclusion from featuresJson if available
            val conclusion = Regex("\"conclusion\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)

            // Generate 4-char title from features
            val title = when {
                json.contains("\"eyes\"") && json.contains("phoenix") -> "凤眼呈祥"
                json.contains("\"forehead\"") && json.contains("broad") -> "天庭饱满"
                json.contains("\"nose\"") && json.contains("dragon") -> "龙鼻主贵"
                json.contains("\"mouth\"") && json.contains("cherry") -> "樱桃小口"
                json.contains("\"chin\"") && json.contains("strong") -> "地阁方圆"
                json.contains("\"ears\"") && json.contains("lotus") -> "福耳垂珠"
                conclusion != null -> "面相玄机"
                else -> "面相玄机"
            }

            // Brief interpretation
            val interp = conclusion?.take(60) ?: ""

            Pair(title, interp)
        } catch (e: Exception) { null }
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

    /** Generate 4-char thematic summary from first tarot card */
    private fun generateTarotFourCharSummary(cardName: String, isReversed: Boolean): String {
        // Map card themes to 4-char classical Chinese phrases
        val themeMap = mapOf(
            // Major Arcana
            "愚者" to if (isReversed) "迷途知返" else "无畏启程",
            "魔术师" to if (isReversed) "力不从心" else "心想事成",
            "女祭司" to if (isReversed) "表里不一" else "静待花开",
            "女皇" to if (isReversed) "丰盛受阻" else "万物生长",
            "皇帝" to if (isReversed) "刚愎自用" else "掌控全局",
            "教皇" to if (isReversed) "离经叛道" else "正道指引",
            "恋人" to if (isReversed) "情路坎坷" else "天作之合",
            "战车" to if (isReversed) "方向迷失" else "势如破竹",
            "力量" to if (isReversed) "信心动摇" else "以柔克刚",
            "隐者" to if (isReversed) "闭门造车" else "明心见性",
            "命运之轮" to if (isReversed) "时运不济" else "否极泰来",
            "正义" to if (isReversed) "偏颇失衡" else "公正无私",
            "倒吊人" to if (isReversed) "无谓牺牲" else "柳暗花明",
            "死神" to if (isReversed) "故步自封" else "涅槃重生",
            "节制" to if (isReversed) "失衡失调" else "中正平和",
            "恶魔" to if (isReversed) "挣脱枷锁" else "执念深重",
            "塔" to if (isReversed) "危机将至" else "大厦将倾",
            "星星" to if (isReversed) "希望渺茫" else "曙光初现",
            "月亮" to if (isReversed) "拨云见日" else "迷雾重重",
            "太阳" to if (isReversed) "短暂阴霾" else "光明普照",
            "审判" to if (isReversed) "逃避反思" else "浴火重生",
            "世界" to if (isReversed) "功亏一篑" else "功德圆满",
            // Minor Arcana — suits
            "权杖" to if (isReversed) "热情消退" else "行动果决",
            "圣杯" to if (isReversed) "情感受挫" else "心灵丰盈",
            "宝剑" to if (isReversed) "思绪混乱" else "洞察真相",
            "星币" to if (isReversed) "财运不稳" else "稳扎稳打"
        )

        // Try exact match first
        themeMap[cardName]?.let { return it }

        // Try partial match (for minor arcana: "权杖一", "圣杯王后", etc.)
        for ((key, value) in themeMap) {
            if (cardName.startsWith(key)) return value
        }

        // Fallback: generate based on reversal
        return if (isReversed) "逆境待变" else "顺势而为"
    }

    /** Generate one-line interpretation from tarot cards */
    private fun generateTarotInterpretation(cards: List<ParsedCard>): String {
        val first = cards[0]
        val orient = if (first.isReversed) "逆位" else "正位"

        // Single card
        if (cards.size == 1) {
            return "${first.nameZh}${orient}：${cardBriefMeaning(first.nameZh, first.isReversed)}"
        }

        // Multi-card: summarize the spread
        val names = cards.joinToString("、") { it.nameZh }
        return "牌阵含${cards.size}张牌（$names），${cardBriefMeaning(first.nameZh, first.isReversed)}"
    }

    /** Brief one-line meaning for a card */
    private fun cardBriefMeaning(nameZh: String, isReversed: Boolean): String {
        val meanings = mapOf(
            "愚者" to if (isReversed) "冲动行事将导致失控，应回归理性" else "新的旅程即将开始，保持纯真与勇气",
            "魔术师" to if (isReversed) "才华被误用，需重新聚焦目标" else "你拥有实现目标的一切资源",
            "女祭司" to if (isReversed) "忽视直觉的警示，需倾听内心" else "静心聆听内心深处的智慧",
            "女皇" to if (isReversed) "创造力枯竭，需滋养身心" else "丰饶与创造力正在涌流",
            "皇帝" to if (isReversed) "控制欲过强，需学会放手" else "建立秩序与稳固的基础",
            "死神" to if (isReversed) "抗拒必要的改变，需勇敢放手" else "旧阶段结束，新生命萌芽",
            "塔" to if (isReversed) "勉强维持将导致更大崩塌" else "旧有结构崩塌后方能重建",
            "星星" to if (isReversed) "信心受挫，但黎明终将到来" else "希望之光正在指引方向",
            "月亮" to if (isReversed) "迷雾渐散，真相即将显现" else "表象之下暗藏玄机，需谨慎",
            "太阳" to if (isReversed) "暂时的困难遮不住光明" else "成功与喜悦正在降临",
            "命运之轮" to if (isReversed) "运势低迷，需蛰伏待机" else "命运转折已至，把握机遇",
            "正义" to if (isReversed) "偏见蒙蔽判断，需客观审视" else "公正的裁决即将到来",
            "审判" to if (isReversed) "逃避过去，需直面内心" else "觉醒之时，过往皆有答案",
            "世界" to if (isReversed) "尚有未竟之事，需善始善终" else "圆满达成，进入新境界"
        )
        // Try exact match
        meanings[nameZh]?.let { return it }
        // Try partial match for minor arcana
        for ((key, value) in meanings) {
            if (nameZh.startsWith(key)) return value
        }
        // Fallback
        return if (isReversed) "当前形势不利，宜守不宜进" else "天时地利，可以有所作为"
    }
}

data class TarotArchiveSummary(
    val title: String,
    val interpretation: String
)
