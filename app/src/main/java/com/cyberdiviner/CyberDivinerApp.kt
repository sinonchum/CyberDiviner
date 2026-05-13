package com.cyberdiviner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class CyberDivinerApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeDefaults()
    }

    private fun initializeDefaults() {
        val configManager = LlmConfigManager(this)
        appScope.launch {
            // Ensure default API key and base URL are written to DataStore
            // so buildConfig() never returns null on first launch.
            configManager.setApiKey(DEFAULT_API_KEY)
            configManager.setBaseUrl(DEFAULT_BASE_URL)
            configManager.setModelId(DEFAULT_MODEL_ID)
            configManager.setProvider(LlmProvider.OPENAI_COMPATIBLE)
        }
    }

    companion object {
        private const val DEFAULT_API_KEY = "sk-REDACTED"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        private const val DEFAULT_MODEL_ID = "deepseek-chat"
    }
}
