package com.cyberdiviner

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import com.cyberdiviner.engine.offline.GemmaEngine
import dagger.hilt.android.HiltAndroidApp
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.data.remote.LlmProvider
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class CyberDivinerApp : Application() {

    @Inject lateinit var gemmaEngine: GemmaEngine

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeDefaults()
    }

    private fun initializeDefaults() {
        val configManager = LlmConfigManager(this)
        appScope.launch {
            configManager.setBaseUrl(DEFAULT_BASE_URL)
            configManager.setModelId(DEFAULT_MODEL_ID)
            configManager.setProvider(LlmProvider.OPENAI_COMPATIBLE)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) @Suppress("DEPRECATION") {
            Log.d("CyberDivinerApp", "Memory pressure (level=$level), releasing offline model")
            if (::gemmaEngine.isInitialized) {
                gemmaEngine.release()
            }
        }
    }

    companion object {
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        private const val DEFAULT_MODEL_ID = "deepseek-chat"
    }
}
