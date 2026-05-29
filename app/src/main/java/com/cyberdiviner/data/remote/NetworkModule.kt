package com.cyberdiviner.data.remote

import android.content.Context
import com.cyberdiviner.engine.PersonaEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.cyberdiviner.engine.offline.GemmaEngine
import com.cyberdiviner.engine.offline.InferenceRouter
import com.cyberdiviner.engine.offline.ModelManager
import com.cyberdiviner.engine.offline.OfflinePromptBuilder
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideLlmService(client: OkHttpClient): LlmService = LlmService(client)

    @Provides
    @Singleton
    fun providePromptManager(): PromptManager = PromptManager()

    @Provides
    @Singleton
    fun provideLlmConfigManager(
        @ApplicationContext context: Context
    ): LlmConfigManager = LlmConfigManager(context)

    @Provides
    @Singleton
    fun providePersonaEngine(
        llmService: LlmService
    ): PersonaEngine = PersonaEngine(llmService)

    @Provides
    @Singleton
    fun provideGemmaEngine(
        @ApplicationContext context: Context
    ): GemmaEngine = GemmaEngine(context)

    @Provides
    @Singleton
    fun provideModelManager(
        @ApplicationContext context: Context
    ): ModelManager = ModelManager(context)

    @Provides
    @Singleton
    fun provideOfflinePromptBuilder(): OfflinePromptBuilder = OfflinePromptBuilder()

    @Provides
    @Singleton
    fun provideInferenceRouter(
        @ApplicationContext context: Context,
        llmService: LlmService,
        gemmaEngine: GemmaEngine,
        configManager: LlmConfigManager,
        offlinePromptBuilder: OfflinePromptBuilder
    ): InferenceRouter = InferenceRouter(context, llmService, gemmaEngine, configManager, offlinePromptBuilder)
}
