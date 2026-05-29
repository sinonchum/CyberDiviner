package com.cyberdiviner.ui.oracle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.LlmService
import com.cyberdiviner.data.dao.DivinationDao
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
    private val llmService: LlmService,
    private val configManager: LlmConfigManager,
    private val divinationDao: DivinationDao
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "OracleViewModel"
    }

    /**
     * 物理拦截 AI 的"老头行为"。
     * 剔除括号动作描写、Emoji、拟人化市井词汇。
     */
    private fun sanitizeOracleResponse(rawResponse: String): String {
        // 1. 物理剔除括号内的描述 (包含中文和英文括号)
        val noDescriptions = rawResponse.replace(Regex("(?s)[(（].*?[)）]"), "")

        // 2. 剔除 Emoji
        val noEmoji = noDescriptions.replace(Regex("[\\x{10000}-\\x{10FFFF}]"), "")

        // 3. 剔除拟人化市井词汇
        val cleaned = noEmoji
            .replace("老夫", "本系统")
            .replace("小伙子", "使用者")
            .replace("师傅", "先知")
            .trim()

        return cleaned
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
                val config = configManager.buildConfig(systemPrompt = com.cyberdiviner.data.remote.PromptManager().resolveSystem("oracle", com.cyberdiviner.engine.Persona.DEFAULT))
                if (config == null) {
                    addAgentMessage("[ 系统离线 ] 量子因果链未连接。请在设置中配置算命服务密钥。")
                    return@launch
                }

                // Build conversation history for API
                val apiMessages = _messages.value.map { msg ->
                    LlmMessage(
                        role = if (msg.isAgent) "assistant" else "user",
                        content = msg.text
                    )
                }

                val responseText = withContext(Dispatchers.IO) {
                    llmService.complete(config, apiMessages).text
                }

                val cleanedResponse = sanitizeOracleResponse(com.cyberdiviner.engine.Persona.stripActionDescriptions(responseText))
                addAgentMessage(cleanedResponse)

                // Save this exchange to archive in background
                saveExchangeToArchive(text, cleanedResponse)
            } catch (e: Exception) {
                Log.e(TAG, "LLM call failed", e)
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
     */
    private fun saveExchangeToArchive(userQuestion: String, aiResponse: String) {
        viewModelScope.launch {
            try {
                val summary = generateFourCharSummary(aiResponse)
                val resultJson = """{"question": "$userQuestion", "summary": "$summary", "response": "${aiResponse.take(300)}"}""".trimIndent()

                val reading = DivinationReading(
                    type = DivinationType.ORACLE,
                    question = summary,
                    resultJson = resultJson
                )
                divinationDao.insert(reading)
                Log.d(TAG, "Saved oracle exchange to archive: $summary")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save oracle exchange", e)
            }
        }
    }

    /**
     * Generate a 4-character philosophical summary from the AI response.
     * Extracts key themes and maps them to concise classical Chinese phrases.
     */
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
        val bestMatch = themeMap
            .map { (keywords, phrase) ->
                val score = keywords.count { response.contains(it) }
                phrase to score
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }

        if (bestMatch != null) return bestMatch.first

        // Fallback: pick based on response sentiment / length heuristics
        val fallbackPhrases = listOf(
            "玄机暗藏", "天机莫测", "因果相续", "缘起性空",
            "顺其自然", "厚积薄发", "守正出奇", "行稳致远"
        )
        val index = (response.length + response.hashCode()).let { if (it < 0) -it else it } % fallbackPhrases.size
        return fallbackPhrases[index]
    }
}
