package com.cyberdiviner.ui.oracle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.LlmService
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
    private val configManager: LlmConfigManager
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

                addAgentMessage(sanitizeOracleResponse(com.cyberdiviner.engine.Persona.stripActionDescriptions(responseText)))
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
}
