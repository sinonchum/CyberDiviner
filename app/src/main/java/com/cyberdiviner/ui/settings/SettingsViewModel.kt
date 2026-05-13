package com.cyberdiviner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmProvider
import com.cyberdiviner.engine.Persona
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val configManager = LlmConfigManager(application)

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _baseUrl = MutableStateFlow("")
    val baseUrl: StateFlow<String> = _baseUrl

    private val _provider = MutableStateFlow("OPENAI_COMPATIBLE")
    val provider: StateFlow<String> = _provider

    private val _modelId = MutableStateFlow("")
    val modelId: StateFlow<String> = _modelId

    private val _personaId = MutableStateFlow(Persona.DEFAULT.id)
    val personaId: StateFlow<String> = _personaId

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    init {
        // Load persisted values
        viewModelScope.launch {
            configManager.apiKey.first().let { if (it.isNotBlank()) _apiKey.value = it }
            configManager.baseUrl.first().let { if (it.isNotBlank()) _baseUrl.value = it }
            configManager.provider.first().let { if (it.isNotBlank()) _provider.value = it }
            configManager.modelId.first().let { if (it.isNotBlank()) _modelId.value = it }
            configManager.personaId.first().let { _personaId.value = it }
        }
    }

    fun setApiKey(key: String) { _apiKey.value = key }
    fun setBaseUrl(url: String) { _baseUrl.value = url }
    fun setProvider(provider: String) { _provider.value = provider }
    fun setModelId(modelId: String) { _modelId.value = modelId }
    fun setPersonaId(personaId: String) { _personaId.value = personaId }

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
            configManager.setPersonaId(_personaId.value)
            _saved.value = true
        }
    }
}
