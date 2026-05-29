package com.cyberdiviner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.model.InferenceMode
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmProvider
import com.cyberdiviner.engine.Persona
import com.cyberdiviner.engine.offline.ModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val modelManager: ModelManager
) : AndroidViewModel(application) {

    private val configManager = LlmConfigManager(application)

    // ── Existing state ────────────────────────────────────────────────────────

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

    // ── New: Inference mode ───────────────────────────────────────────────────

    private val _inferenceMode = MutableStateFlow(InferenceMode.AUTO)
    val inferenceMode: StateFlow<InferenceMode> = _inferenceMode

    // ── New: Model state ──────────────────────────────────────────────────────

    val modelState: StateFlow<ModelManager.ModelState> = modelManager.state

    init {
        viewModelScope.launch {
            configManager.apiKey.first().let { if (it.isNotBlank()) _apiKey.value = it }
            configManager.baseUrl.first().let { if (it.isNotBlank()) _baseUrl.value = it }
            configManager.provider.first().let { if (it.isNotBlank()) _provider.value = it }
            configManager.modelId.first().let { if (it.isNotBlank()) _modelId.value = it }
            configManager.personaId.first().let { _personaId.value = it }
            configManager.inferenceMode.first().let {
                _inferenceMode.value = InferenceMode.fromName(it)
            }
        }
    }

    // ── Existing setters ──────────────────────────────────────────────────────

    fun setApiKey(key: String) { _apiKey.value = key }
    fun setBaseUrl(url: String) { _baseUrl.value = url }
    fun setProvider(provider: String) { _provider.value = provider }
    fun setModelId(modelId: String) { _modelId.value = modelId }
    fun setPersonaId(personaId: String) { _personaId.value = personaId }

    // ── New: Inference mode setter ────────────────────────────────────────────

    fun setInferenceMode(mode: InferenceMode) {
        _inferenceMode.value = mode
        viewModelScope.launch {
            configManager.setInferenceMode(mode.name)
        }
    }

    // ── New: Model management ─────────────────────────────────────────────────

    fun downloadModel() {
        viewModelScope.launch {
            modelManager.download()
        }
    }

    fun deleteModel() {
        viewModelScope.launch {
            modelManager.delete()
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

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
            configManager.setInferenceMode(_inferenceMode.value.name)
            _saved.value = true
        }
    }
}
