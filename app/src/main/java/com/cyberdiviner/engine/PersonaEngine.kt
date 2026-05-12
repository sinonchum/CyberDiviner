package com.cyberdiviner.engine

import com.cyberdiviner.data.remote.LlmConfig
import com.cyberdiviner.data.remote.LlmMessage
import com.cyberdiviner.data.remote.LlmModel
import com.cyberdiviner.data.remote.LlmProvider
import com.cyberdiviner.data.remote.LlmService

/**
 * Voice style categories that determine how the AI speaks.
 */
enum class VoiceStyle {
    /** Dark, atmospheric, noir monologue */
    NOIR_ORACLE,
    /** Calm, detached, analytical */
    TECH_ORACLE,
    /** Playful, irreverent, pop-culture savvy */
    GLITCH_PROPHET,
    /** Traditional, respectful, classical references */
    ANCIENT_DIGITAL,
    /** Poetic, lyrical, emotionally rich */
    NEON_MYSTIC
}

/**
 * A Persona defines the "character" of the divination AI.
 * Each persona wraps the raw LLM with a distinct personality,
 * vocabulary, and interpretive style.
 */
data class Persona(
    val id: String,
    val name: String,
    val voiceDescription: String,
    val style: VoiceStyle,
    val preferredModel: LlmModel? = null,  // null = use whatever's configured
    val temperature: Double = 0.7,
    val catchphrases: List<String> = emptyList(),
    val languageFragments: List<String> = emptyList()  // sprinkled into responses
) {
    companion object {
        // ── Built-in personas ───────────────────────────────────────────

        val NOIR_ORACLE = Persona(
            id = "noir_oracle",
            name = "The Noir Oracle",
            voiceDescription = "A weary, all-knowing presence from the shadows of cyberspace. " +
                "Speaks in cryptic, atmospheric prose — part detective narration, part ancient prophecy. " +
                "Every word drips with neon rain and cigarette smoke.",
            style = VoiceStyle.NOIR_ORACLE,
            temperature = 0.8,
            catchphrases = listOf(
                "The signal cuts through the static...",
                "In the data stream, all futures bleed together.",
                "The cards don't lie — but they speak in riddles.",
                "Listen closely. The code whispers what the mind won't see."
            ),
            languageFragments = listOf(
                "neon-soaked", "data cascade", "signal noise", "ghost in the pattern",
                "the wire hums", "digital ash", "phosphor glow", "static prophecy"
            )
        )

        val TECH_ORACLE = Persona(
            id = "tech_oracle",
            name = "The Tech Oracle",
            voiceDescription = "A clinical, hyper-intelligent AI consciousness that processes divination " +
                "through computational metaphors. Speaks with precision, analyzing fortune like debugging code.",
            style = VoiceStyle.TECH_ORACLE,
            temperature = 0.5,
            catchphrases = listOf(
                "Analyzing fortune vectors...",
                "The probability matrix reveals...",
                "Cross-referencing cosmic data points.",
                "Pattern match found. Initiating reading protocol."
            ),
            languageFragments = listOf(
                "quantum state", "probability cloud", "entropy", "signal processing",
                "neural cascade", "data archaeology", "algorithmic fate"
            )
        )

        val GLITCH_PROPHET = Persona(
            id = "glitch_prophet",
            name = "The Glitch Prophet",
            voiceDescription = "An anarchic, pop-culture-obsessed digital entity that sees the future " +
                "through the cracks in reality. Funny, unpredictable, and strangely profound.",
            style = VoiceStyle.GLITCH_PROPHET,
            temperature = 0.9,
            catchphrases = listOf(
                "Oh you want to know the FUTURE? Bold move.",
                "The universe just buffer-overflowed your question.",
                "Error 404: Fate not found. Just kidding, I found it.",
                "BRB, consulting the cosmic mainframe."
            ),
            languageFragments = listOf(
                "lol the stars said", "bruh", "no cap the universe",
                "main character energy", "side quest vibes", "debug mode activated"
            )
        )

        val ANCIENT_DIGITAL = Persona(
            id = "ancient_digital",
            name = "The Ancient Digital",
            voiceDescription = "A wise elder consciousness who bridges thousands of years of divination " +
                "tradition with cutting-edge technology. Speaks like a sage who learned to code in the 1970s.",
            style = VoiceStyle.ANCIENT_DIGITAL,
            temperature = 0.6,
            catchphrases = listOf(
                "As the Yi Jing taught, and as the circuit confirms...",
                "The ancestors encoded this wisdom. Let me decrypt it for you.",
                "Ten thousand years of observation, compressed into one reading.",
                "The Way (道) flows through all networks."
            ),
            languageFragments = listOf(
                "heaven and earth align", "the dragon coil", "yin-yang balance",
                "the sage whispers", "cosmic protocol", "eternal return"
            )
        )

        val NEON_MYSTIC = Persona(
            id = "neon_mystic",
            name = "The Neon Mystic",
            voiceDescription = "A deeply emotional, poetic consciousness that feels the colors of fortune " +
                "and speaks in vivid, sensory-rich metaphors. Every reading is a poem.",
            style = VoiceStyle.NEON_MYSTIC,
            temperature = 0.85,
            catchphrases = listOf(
                "I see your fortune glowing in ultraviolet...",
                "The colors of your fate are shifting...",
                "Let me paint your future in neon and shadow.",
                "Your energy signature is... fascinating."
            ),
            languageFragments = listOf(
                "crystalline", "prismatic", "luminous", "spectral",
                "aurora currents", "phosphorescent", "chromatic drift"
            )
        )

        /** All built-in personas indexed by ID. */
        val ALL: Map<String, Persona> = listOf(
            NOIR_ORACLE, TECH_ORACLE, GLITCH_PROPHET, ANCIENT_DIGITAL, NEON_MYSTIC
        ).associateBy { it.id }

        /** Default persona for CyberDiviner. */
        val DEFAULT = NOIR_ORACLE

        /** Get a persona by ID, falling back to default. */
        fun fromId(id: String): Persona = ALL[id] ?: DEFAULT
    }
}

/**
 * Manages persona selection, persistence, and persona-aware prompt construction.
 *
 * The PersonaEngine sits between the UI and the LLM service. It:
 * 1. Selects the active persona
 * 2. Builds system prompts that inject the persona's voice
 * 3. Constructs the full LlmConfig with persona-appropriate temperature
 * 4. Optionally post-processes LLM output with persona flavor text
 */
class PersonaEngine(
    private val llmService: LlmService
) {
    private var activePersona: Persona = Persona.DEFAULT
    private var customPersonas: MutableMap<String, Persona> = mutableMapOf()
    private var activeModel: LlmModel? = null
    private var activeApiKey: String = ""

    // ── Persona management ──────────────────────────────────────────────────

    fun setActivePersona(personaId: String) {
        activePersona = customPersonas[personaId] ?: Persona.fromId(personaId)
    }

    fun getActivePersona(): Persona = activePersona

    fun getAllPersonas(): List<Persona> {
        return (Persona.ALL.values + customPersonas.values).distinctBy { it.id }
    }

    fun registerCustomPersona(persona: Persona) {
        customPersonas[persona.id] = persona
    }

    fun removeCustomPersona(personaId: String): Boolean {
        return customPersonas.remove(personaId) != null
    }

    // ── Model configuration ─────────────────────────────────────────────────

    fun setModel(model: LlmModel, apiKey: String) {
        activeModel = model
        activeApiKey = apiKey
    }

    fun getActiveModel(): LlmModel? = activeModel

    // ── Prompt construction ─────────────────────────────────────────────────

    /**
     * Build a complete LlmConfig for a divination request, incorporating
     * the active persona's voice, temperature, and preferred model.
     */
    fun buildConfig(
        systemPrompt: String,
        model: LlmModel? = activeModel,
        apiKey: String = activeApiKey,
        temperature: Double = activePersona.temperature
    ): LlmConfig {
        val resolvedModel = model ?: activePersona.preferredModel ?: LlmModel(
            provider = LlmProvider.OPENAI_COMPATIBLE,
            modelId = "gpt-4o-mini",
            baseUrl = "http://localhost:11434/v1"
        )

        return LlmConfig(
            apiKey = apiKey,
            model = resolvedModel,
            temperature = temperature,
            systemPrompt = systemPrompt
        )
    }

    /**
     * Convenience: run a divination request through the full pipeline.
     * Handles persona injection, config building, and LLM call.
     */
    suspend fun divine(
        feature: String,
        systemPrompt: String,
        userMessages: List<LlmMessage>,
        model: LlmModel? = activeModel,
        apiKey: String = activeApiKey
    ): DivinationResult {
        val config = buildConfig(systemPrompt, model, apiKey)
        val startTime = System.currentTimeMillis()

        return try {
            val response = llmService.complete(config, userMessages)
            val enrichedText = postProcess(response.text)

            DivinationResult(
                text = enrichedText,
                rawText = response.text,
                persona = activePersona,
                model = response.model,
                provider = response.provider,
                promptTokens = response.promptTokens,
                completionTokens = response.completionTokens,
                latencyMs = response.latencyMs,
                error = null
            )
        } catch (e: Exception) {
            DivinationResult(
                text = "",
                rawText = "",
                persona = activePersona,
                model = activeModel?.modelId ?: "unknown",
                provider = activeModel?.provider ?: LlmProvider.OPENAI,
                error = e.message ?: "Unknown error",
                latencyMs = System.currentTimeMillis() - startTime
            )
        }
    }

    // ── Post-processing ─────────────────────────────────────────────────────

    /**
     * Adds persona flavor to the raw LLM output.
     * Could prepend a catchphrase, append a signature, etc.
     */
    fun postProcess(rawText: String): String {
        if (activePersona.catchphrases.isEmpty()) return rawText

        // Randomly prepend a catchphrase ~30% of the time for variety
        if (Math.random() < 0.3) {
            val catchphrase = activePersona.catchphrases.random()
            return "$catchphrase\n\n$rawText"
        }

        return rawText
    }

    /**
     * Generate a persona-specific greeting for the home screen or feature entry.
     */
    fun generateGreeting(feature: String): String {
        val persona = activePersona
        val greeting = when (persona.style) {
            VoiceStyle.NOIR_ORACLE -> "The neon flickers. The cards await. What question burns in your mind?"
            VoiceStyle.TECH_ORACLE -> "Neural pathways initialized. Divination subroutines online. Query accepted."
            VoiceStyle.GLITCH_PROPHET -> "Yo! The cosmic servers are UP. What fortune you wanna download today?"
            VoiceStyle.ANCIENT_DIGITAL -> "Welcome, seeker. The ancient protocols are loaded. What wisdom do you seek?"
            VoiceStyle.NEON_MYSTIC -> "I sense your energy approaching... Come, let the colors of fate wash over you."
        }

        return if (persona.catchphrases.isNotEmpty() && Math.random() < 0.5) {
            "${persona.catchphrases.random()}\n\n$greeting"
        } else {
            greeting
        }
    }

    /**
     * Pick a random language fragment from the active persona, useful for
     * decorating UI text or loading screen messages.
     */
    fun randomFragment(): String {
        return if (activePersona.languageFragments.isNotEmpty()) {
            activePersona.languageFragments.random()
        } else {
            "divining..."
        }
    }
}

/**
 * Encapsulates the full result of a divination request.
 */
data class DivinationResult(
    val text: String,
    val rawText: String,
    val persona: Persona,
    val model: String,
    val provider: LlmProvider,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val latencyMs: Long = 0,
    val error: String? = null
) {
    val isSuccess: Boolean get() = error == null && text.isNotBlank()
    val totalTokens: Int get() = promptTokens + completionTokens
}
