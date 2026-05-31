package com.cyberdiviner.ui.oracle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.engine.offline.InferenceRouter
import com.cyberdiviner.engine.offline.OfflinePromptBuilder
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class OracleMessage(
    val text: String,
    val isAgent: Boolean
)

/**
 * OracleViewModel -- Manages oracle chat with real DeepSeek API.
 *
 * System prompt enforces: digital prophet, terse, cold, Chinese philosophy + cyberpunk metaphor.
 */
@HiltViewModel
class OracleViewModel @Inject constructor(
    application: Application,
    private val inferenceRouter: InferenceRouter,
    private val offlinePromptBuilder: OfflinePromptBuilder,
    private val divinationDao: DivinationDao
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "OracleViewModel"
    }

    private data class OracleSections(
        val poem: String,
        val analysis: String,
        val verdict: String
    )

    /**
     * 物理拦截 AI 的"老头行为"。
     * 剔除括号动作描写、Emoji、拟人化市井词汇。
     * 保留 [ 载入签文 ] [ 逻辑解析 ] [ 最终断语 ] 等方括号节标题。
     */
    private fun sanitizeOracleResponse(rawResponse: String): String {
        // 1. 只剔除括号内的动作/表情描写，不破坏正文
        //    匹配短小的动作描述（≤30字符），如（点点头）、(沉思片刻)
        //    不匹配跨行或超长内容（那些是正文，不是动作描写）
        val actionPattern = Regex("[（(][^）)\\n]{0,30}[）)]")
        val noDescriptions = actionPattern.replace(rawResponse, "")

        // 2. 剔除 Emoji
        val noEmoji = noDescriptions.replace(Regex("[\\x{10000}-\\x{10FFFF}]"), "")

        // 3. 剔除拟人化市井词汇
        val cleaned = noEmoji
            .replace("老夫", "本系统")
            .replace("小伙子", "使用者")
            .replace("师傅", "先知")
            .trim()

        // 4. 剔除末尾的"问题"回显（LLM有时会把用户问题复述一遍）
        val questionEcho = Regex("\\n+问题[：:].+$", RegexOption.DOT_MATCHES_ALL)
        return questionEcho.replace(cleaned, "").trim()
    }

    /**
     * Treat model output as untrusted text and render it through one canonical
     * Oracle shape. This keeps UI format stable across online/offline/future models.
     */
    private fun normalizeOracleResponse(rawResponse: String, question: String): String {
        val sanitized = sanitizeOracleResponse(rawResponse)
        val fallback = parseOracleSections(generateOfflineFallback(question))!!
        val parsed = parseOracleSections(sanitized)

        val poem = normalizePoem(parsed?.poem).ifBlank { fallback.poem }
        val analysis = normalizeParagraph(parsed?.analysis, maxSentences = 5, maxChars = 180)
            .ifBlank { fallback.analysis }
        val verdict = normalizeParagraph(parsed?.verdict, maxSentences = 2, maxChars = 80)
            .ifBlank { fallback.verdict }

        return renderOracleSections(OracleSections(poem, analysis, verdict))
    }

    private fun parseOracleSections(text: String): OracleSections? {
        val poemHeader = Regex("""(?m)^\s*\[\s*载入签文\s*]\s*$""").find(text) ?: return null
        val analysisHeader = Regex("""(?m)^\s*\[\s*逻辑解析\s*]\s*$""").find(text, poemHeader.range.last + 1) ?: return null
        val verdictHeader = Regex("""(?m)^\s*\[\s*最终断语\s*]\s*$""").find(text, analysisHeader.range.last + 1) ?: return null

        val poem = text.substring(poemHeader.range.last + 1, analysisHeader.range.first).trim()
        val analysis = text.substring(analysisHeader.range.last + 1, verdictHeader.range.first).trim()
        val verdict = text.substring(verdictHeader.range.last + 1).trim()
        return OracleSections(poem, analysis, verdict)
    }

    private fun renderOracleSections(sections: OracleSections): String =
        "[ 载入签文 ]\n${sections.poem}\n\n" +
            "[ 逻辑解析 ]\n${sections.analysis}\n\n" +
            "[ 最终断语 ]\n${sections.verdict}"

    private fun normalizePoem(rawPoem: String?): String {
        if (rawPoem.isNullOrBlank()) return ""

        val cleaned = rawPoem
            .replace(Regex("[「」『』“”\"']"), "")
            .replace(Regex("(?m)^\\s*[一二三四1234][.．、]\\s*"), "")
            .trim()

        val sentences = cleaned.split(Regex("(?<=[。！？!?])"))
            .map { it.trim().trim('。', '！', '？', '!', '?') }
            .filter { it.isNotBlank() }
            .filterNot { isOracleBlessingOrPlainWish(it) }
            .take(4)

        if (sentences.size < 2) return ""

        return sentences.joinToString("\n") { sentence ->
            val normalized = sentence.replace(Regex("\\s+"), "")
            if (normalized.endsWith("。")) normalized else "$normalized。"
        }
    }

    private fun isOracleBlessingOrPlainWish(sentence: String): Boolean {
        val normalized = sentence.replace(Regex("\\s+"), "")
        val markers = listOf(
            "希望你", "祝你", "祝愿", "一切顺利", "早日找到",
            "找到好工作", "找到工作", "加油", "好运"
        )
        return markers.any { normalized.contains(it) } ||
            normalized.length > 18 && listOf("工作", "事业", "感情", "财运", "健康").any { normalized.contains(it) }
    }

    private fun normalizeParagraph(raw: String?, maxSentences: Int, maxChars: Int): String {
        if (raw.isNullOrBlank()) return ""

        val cleaned = raw
            .replace(Regex("(?m)^\\s*[一二三四五六七八九十\\d]+[.．、]\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val sentences = cleaned.split(Regex("(?<=[。！？!?])"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(maxSentences)

        val paragraph = if (sentences.isNotEmpty()) sentences.joinToString("")
        else cleaned

        return paragraph.take(maxChars).trim().trimEnd('，', ',', '；', ';')
    }

    private val _messages = MutableStateFlow<List<OracleMessage>>(emptyList())
    val messages: StateFlow<List<OracleMessage>> = _messages.asStateFlow()

    private val _round = MutableStateFlow(0)
    val round: StateFlow<Int> = _round.asStateFlow()

    val maxRounds = 5

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        _messages.value = listOf(
            OracleMessage(
                text = "[ 系统载入 ] 赛博算命系统已上线。因果链就绪。\n\n输入你的困惑。事业、感情、财运、健康——系统将为你演算签文。",
                isAgent = true
            )
        )
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun clearInput() {
        _inputText.value = ""
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isProcessing.value) return

        val userMsg = OracleMessage(text = text, isAgent = false)
        _messages.value = _messages.value + userMsg
        _isProcessing.value = true

        viewModelScope.launch {
            try {
                // Build conversation history for API
                val apiMessages = _messages.value.map { msg ->
                    LlmMessage(
                        role = if (msg.isAgent) "assistant" else "user",
                        content = msg.text
                    )
                }

                val offlinePrompt = offlinePromptBuilder.buildOraclePrompt(text)

                val result = withContext(Dispatchers.IO) {
                    inferenceRouter.complete(
                        feature = "oracle",
                        messages = apiMessages,
                        offlineUserPrompt = offlinePrompt
                    )
                }

                val rawResponse = if (result.isOffline) {
                    com.cyberdiviner.engine.Persona.cleanOfflineOutput(result.text)
                } else {
                    com.cyberdiviner.engine.Persona.stripActionDescriptions(result.text)
                }
                val formattedResponse = normalizeOracleResponse(rawResponse, text)

                // Prefix with mode indicator when offline
                val displayResponse = if (result.isOffline) {
                    "[ 本地签筒 ]\n$formattedResponse"
                } else formattedResponse

                addAgentMessage(displayResponse)
                saveExchangeToArchive(text, formattedResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Inference failed", e)
                addAgentMessage("[ 系统异常 ] 量子因果链中断。错误码: ${e.message ?: "未知"}。请稍后重试。")
            } finally {
                _round.value = _round.value + 1
                _isProcessing.value = false
            }
        }
    }

    private fun addAgentMessage(text: String) {
        _messages.value = _messages.value + OracleMessage(text = text, isAgent = true)
    }

    /**
     * Save a single oracle exchange to the archive as a background task.
     * Each exchange becomes an independent entry in the 因果命簿.
     * Stores response directly in resultJson (no JSON wrapping) for reliable extraction.
     */
    private fun saveExchangeToArchive(userQuestion: String, aiResponse: String) {
        viewModelScope.launch {
            try {
                val summary = generateFourCharSummary(aiResponse)
                // Store response as plain text — avoids JSON escaping issues
                val responseExcerpt = aiResponse.replace("\n", " ").replace("\r", "").take(300)

                val reading = DivinationReading(
                    type = DivinationType.ORACLE,
                    question = summary,
                    resultJson = responseExcerpt
                )
                divinationDao.insert(reading)
                Log.d(TAG, "Saved oracle exchange to archive: $summary")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save oracle exchange", e)
            }
        }
    }

    /**
     * Generate a fallback response when the offline model produces empty/near-empty output.
     * Uses template-based generation with randomized poetic content.
     */
    private fun generateOfflineFallback(question: String): String {
        val poems = listOf(
            "云开月明终有日，守得初心见真章。\n春来草木自青青，莫问前程且自行。",
            "山重水复疑无路，柳暗花明又一村。\n静水深流藏真意，守正待时自有期。",
            "风起青萍末，事成细微中。\n天道酬勤终不负，行稳致远自亨通。",
            "否极泰来运将转，守得云开见月明。\n蓄势待发正当时，厚积薄发展宏图。"
        )
        val analyses = listOf(
            "此签主先难后易。眼前虽有困顿，但因果链已开始转动。关键在于保持定力，不被短期波动干扰。",
            "签文显示局势正在酝酿变化。当前的停滞并非坏事，而是系统在重新校准方向。宜静观其变。",
            "此签暗示贵人将至。你所求之事并非不可为，只是时机未到。保持开放心态，机遇自会显现。"
        )
        val advices = listOf(
            "近期宜守不宜攻，等待时机成熟再行动。",
            "多关注身边细节，答案往往藏在被忽略之处。",
            "放下执念，顺其自然，该来的终会来。"
        )

        val idx = (question.hashCode().toLong().let { if (it < 0) -it else it }).toInt()
        val poem = poems[idx % poems.size]
        val analysis = analyses[(idx shr 4) % analyses.size]
        val advice = advices[(idx shr 8) % advices.size]

        return "[ 载入签文 ]\n$poem\n\n[ 逻辑解析 ]\n$analysis\n\n[ 最终断语 ]\n$advice"
    }

    private fun generateFourCharSummary(response: String): String {
        if (response.isBlank()) return "玄机未显"

        // Map thematic keywords to 4-character philosophical phrases
        val themeMap = listOf(
            listOf("事业", "工作", "职业", "升职", "创业") to "鹏程万里",
            listOf("感情", "爱情", "恋爱", "婚姻", "桃花") to "情缘天定",
            listOf("财运", "金钱", "财富", "投资", "发财") to "财源广进",
            listOf("健康", "身体", "疾病", "养生") to "身心康泰",
            listOf("学业", "考试", "学习", "智慧") to "金榜题名",
            listOf("家庭", "亲人", "父母", "子女") to "家宅安宁",
            listOf("贵人", "人缘", "人际", "社交") to "贵人相助",
            listOf("小人", "是非", "口舌", "争端") to "明哲保身",
            listOf("变动", "迁移", "出行", "旅行") to "逢凶化吉",
            listOf("等待", "耐心", "时机", "蓄势") to "静待花开",
            listOf("果断", "决定", "选择", "抉择") to "当机立断",
            listOf("危机", "困难", "阻碍", "逆境") to "否极泰来",
            listOf("顺利", "亨通", "吉祥", "好运") to "万事亨通",
            listOf("转机", "变化", "革新", "突破") to "革故鼎新",
            listOf("修行", "修心", "内省", "觉悟") to "明心见性",
        )

        // Find best matching theme based on keyword frequency
        val scored = themeMap
            .map { (keywords, phrase) -> phrase to keywords.count { response.contains(it) } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        if (scored.isNotEmpty()) {
            val topScore = scored.first().second
            val topGroup = scored.filter { it.second == topScore }
            val idx = (response.hashCode().toLong().let { if (it < 0) -it else it }).toInt() % topGroup.size
            return topGroup[idx].first
        }

        // Fallback: pick based on response sentiment / length heuristics
        val fallbackPhrases = listOf(
            "玄机暗藏", "天机莫测", "因果相续", "缘起性空",
            "顺其自然", "厚积薄发", "守正出奇", "行稳致远"
        )
        val index = (response.length + response.hashCode()).let { if (it < 0) -it else it } % fallbackPhrases.size
        return fallbackPhrases[index]
    }
}
