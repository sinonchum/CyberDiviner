package com.cyberdiviner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val configManager = LlmConfigManager(application)

    private val _apiKey = MutableStateFlow("sk-REDACTEDorCUXBFpD7CBN0KSppvfEfF6xpXHq3fBEZ0rXBIeaRJL6")
    val apiKey: StateFlow<String> = _apiKey

    private val _baseUrl = MutableStateFlow("https://api.shqbb.com/v1")
    val baseUrl: StateFlow<String> = _baseUrl

    private val _provider = MutableStateFlow("OPENAI_COMPATIBLE")
    val provider: StateFlow<String> = _provider

    private val _modelId = MutableStateFlow("gpt-5.4-mini")
    val modelId: StateFlow<String> = _modelId

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    init {
        // Load persisted values
        viewModelScope.launch {
            configManager.apiKey.first().let { if (it.isNotBlank()) _apiKey.value = it }
            configManager.baseUrl.first().let { if (it.isNotBlank()) _baseUrl.value = it }
            configManager.provider.first().let { if (it.isNotBlank()) _provider.value = it }
            configManager.modelId.first().let { if (it.isNotBlank()) _modelId.value = it }
        }
    }

    fun setApiKey(key: String) { _apiKey.value = key }
    fun setBaseUrl(url: String) { _baseUrl.value = url }
    fun setProvider(provider: String) { _provider.value = provider }
    fun setModelId(modelId: String) { _modelId.value = modelId }

    fun save() {
        viewModelScope.launch {
            configManager.setApiKey(_apiKey.value)
            configManager.setBaseUrl(_baseUrl.value)
            try {
                configManager.setProvider(LlmProvider.valueOf(_provider.value))
            } catch (_: Exception) {
                configManager.setProvider(LlmProvider.OPENAI_COMPATIBLE)
            }
            configManager.setModelId(_modelId.value)
            _saved.value = true
        }
    }
}
