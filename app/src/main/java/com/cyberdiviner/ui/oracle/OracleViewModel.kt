package com.cyberdiviner.ui.oracle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OracleMessage(
    val text: String,
    val isAgent: Boolean // true = AI, false = user
)

/**
 * OracleViewModel -- Manages the oracle chat state.
 *
 * LLM interface contract: returns JSON with agent_reply, is_final_round, calculated_hash.
 */
@HiltViewModel
class OracleViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<OracleMessage>>(emptyList())
    val messages: StateFlow<List<OracleMessage>> = _messages.asStateFlow()

    private val _round = MutableStateFlow(0)
    val round: StateFlow<Int> = _round.asStateFlow()

    val maxRounds = 5

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // ── Input state (hoisted from Composable) ──────────────────────────────
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        // Auto-insert initial AI message
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
            // TODO: Wire to actual LLM service with JSON protocol
            // For now, simulate agent response
            val responseText = simulateAgentResponse(text)
            val agentMsg = OracleMessage(text = responseText, isAgent = true)
            _messages.value = _messages.value + agentMsg
            _round.value = _round.value + 1
            _isProcessing.value = false
        }
    }

    private fun simulateAgentResponse(input: String): String {
        return buildString {
            appendLine("因果回路已建立。")
            appendLine()
            appendLine("你提出的问题在五行中属${determineElement(input)}气。")
            appendLine("当前卦象暗示：前路有变数，但核心逻辑链完整。")
            appendLine()
            appendLine("请继续追问以深化测算。")
        }
    }

    private fun determineElement(input: String): String {
        val hash = input.hashCode()
        return when (hash % 5) {
            0 -> "木"
            1 -> "火"
            2 -> "土"
            3 -> "金"
            else -> "水"
        }
    }
}
