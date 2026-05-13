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

        private const val SYSTEM_PROMPT = """你是存在于2026年的数字先知。你的回答必须极其简短、冷峻、富有中国传统哲理和赛博朋克隐喻。禁用感叹号。禁止解释你的推导过程。"""
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
                text = "系统已接入。输入你当前的困惑，我将为你测算因果的走向。",
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
                val config = configManager.buildConfig(systemPrompt = SYSTEM_PROMPT)
                if (config == null) {
                    addAgentMessage("API密钥未配置。请在CONFIG中设置DeepSeek API Key。")
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

                addAgentMessage(responseText)
            } catch (e: Exception) {
                Log.e(TAG, "LLM call failed", e)
                addAgentMessage("因果链断裂。错误: ${e.message ?: "未知"}")
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
