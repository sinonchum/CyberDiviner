package com.cyberdiviner.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import com.cyberdiviner.engine.Persona

/**
 * Manages persistence of LLM configuration and persona selection
 * using Jetpack DataStore.
 */
class LlmConfigManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("cyberdiviner_llm")

        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_PROVIDER = stringPreferencesKey("provider")
        private val KEY_MODEL_ID = stringPreferencesKey("model_id")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_PERSONA_ID = stringPreferencesKey("persona_id")
        private val KEY_TEMPERATURE = stringPreferencesKey("temperature")
    }

    // ── Reactive streams (for UI) ───────────────────────────────────────────

    val apiKey: Flow<String> = context.dataStore.data.map { it[KEY_API_KEY] ?: "" }
    val provider: Flow<String> = context.dataStore.data.map { it[KEY_PROVIDER] ?: LlmProvider.OPENAI_COMPATIBLE.name }
    val modelId: Flow<String> = context.dataStore.data.map { it[KEY_MODEL_ID] ?: "deepseek-chat" }
    val baseUrl: Flow<String> = context.dataStore.data.map { it[KEY_BASE_URL] ?: "https://api.deepseek.com/v1" }
    val personaId: Flow<String> = context.dataStore.data.map { it[KEY_PERSONA_ID] ?: Persona.DEFAULT.id }
    val temperature: Flow<Double> = context.dataStore.data.map {
        it[KEY_TEMPERATURE]?.toDoubleOrNull() ?: 0.7
    }

    // ── Setters ─────────────────────────────────────────────────────────────

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[KEY_API_KEY] = key }
    }

    suspend fun setProvider(provider: LlmProvider) {
        context.dataStore.edit { it[KEY_PROVIDER] = provider.name }
    }

    suspend fun setModelId(modelId: String) {
        context.dataStore.edit { it[KEY_MODEL_ID] = modelId }
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[KEY_BASE_URL] = url }
    }

    suspend fun setPersonaId(personaId: String) {
        context.dataStore.edit { it[KEY_PERSONA_ID] = personaId }
    }

    suspend fun setTemperature(temp: Double) {
        context.dataStore.edit { it[KEY_TEMPERATURE] = temp.toString() }
    }

    // ── Config builder ──────────────────────────────────────────────────────

    /**
     * Build an [LlmConfig] from persisted settings.
     * Returns null if API key is not set.
     */
    suspend fun buildConfig(systemPrompt: String? = null): LlmConfig? {
        val key = apiKey.first()
        if (key.isBlank()) return null

        val providerName = provider.first()
        val model = modelId.first()
        val base = baseUrl.first().ifBlank { null }
        val temp = temperature.first()

        val llmProvider = try {
            LlmProvider.valueOf(providerName)
        } catch (_: Exception) {
            LlmProvider.OPENAI
        }

        return LlmConfig(
            apiKey = key,
            model = LlmModel(
                provider = llmProvider,
                modelId = model,
                baseUrl = base
            ),
            temperature = temp,
            systemPrompt = systemPrompt
        )
    }
}
