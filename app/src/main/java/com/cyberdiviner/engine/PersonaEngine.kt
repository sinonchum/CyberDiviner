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
            voiceDescription = "A wise and knowing presence that processes divination " +
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

        // ── Pre-compiled regex patterns for stripActionDescriptions ────────
        private val REGEX_EMOJI = Regex("[\\x{10000}-\\x{10FFFF}]")
        private val REGEX_PAREN_ACTIONS = Regex("[（(][^）)]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑嘿嘿哈哈哼嗯啊哎呀叹]{1,20}[）)]")
        private val REGEX_BRACKET_ACTIONS = Regex("\\[[^\\]]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑]{1,20}]")
        private val REGEX_ASTERISK_ACTIONS = Regex("\\*[^*]*?[动作笑看点头沉思端详掐指皱眉叹气摇头微笑嘿嘿哈哈]{1,20}\\*")
        private val REGEX_SPEAKER_LINE = Regex("(?m)^.*?(?:算命师|先生|老人|大师|师傅|仙人|先知|老者).{0,10}(?:笑着|点点头|沉思|端详|掐指|皱眉|叹气|摇头|微笑|打量|看了看|注视|凝视|闭目|捋须|睁开|抬头|低头|轻声|低声).{0,30}(?:后|才|道|说|答|开口).{0,5}(?:说|道|答)?[：:,]")
        private val REGEX_STANDALONE_ACTION = Regex("(?m)^.*?(?:笑着|点点头|沉思片刻|端详|掐指一算|皱眉|叹气|摇头|微笑|打量|嘿嘿|哈哈).{0,20}[。.]\\s*$")
        private val REGEX_MULTI_BLANK_LINES = Regex("\\n{3,}")

        /**
         * Strip character action/expression descriptions from LLM output.
         * Removes patterns like "笑着打量你", "*点点头*", "(沉思片刻)", etc.
         * Also cleans garbled encoding artifacts from offline model byte-fallback tokenizer.
         */
        fun stripActionDescriptions(text: String): String {
            var result = REGEX_EMOJI.replace(text, "")
            result = REGEX_PAREN_ACTIONS.replace(result, "")
            result = REGEX_BRACKET_ACTIONS.replace(result, "")
            result = REGEX_ASTERISK_ACTIONS.replace(result, "")
            result = REGEX_SPEAKER_LINE.replace(result, "")
            result = REGEX_STANDALONE_ACTION.replace(result, "")
            result = REGEX_MULTI_BLANK_LINES.replace(result, "\n\n")
            result = cleanGarbledEncoding(result)
            result = cleanMarkdownArtifacts(result)
            return result.trim()
        }

        // ── Garbled encoding cleanup ────────────────────────────────────────
        // Qwen2.5 byte-fallback tokenizer sometimes produces non-CJK non-ASCII
        // chars before Chinese characters. Strips them when followed by CJK.
        private val REGEX_GARBLED_PREFIX = Regex(
            "[\\u0080-\\u00ff\\u0100-\\u024f\\u0250-\\u02af\\u2000-\\u206f\\u2070-\\u209f\\u20a0-\\u20cf\\u2100-\\u214f]{1,8}(?=[\\u4e00-\\u9fff\\u3400-\\u4dbf])"
        )

        // Markdown artifacts the small model likes to produce
        private val REGEX_MARKDOWN_HEADERS = Regex("#{1,6}\\s*")
        private val REGEX_MARKDOWN_BOLD = Regex("\\*\\*")
        private val REGEX_MARKDOWN_LIST_NUM = Regex("(?m)^\\d+[.．、]\\s+")
        private val REGEX_MARKDOWN_LIST_BULLET = Regex("(?m)^[-*]\\s+")
        // LLM self-generated section headers (NOT the bracket format [ 载入签文 ] etc.)
        private val REGEX_LLM_SECTION_HEADERS = Regex("(?m)^\\s*(?:诗意签文|白话解释|直接建议|签诗解读|签文解读)：?\\s*$")

        private fun cleanGarbledEncoding(text: String): String {
            return REGEX_GARBLED_PREFIX.replace(text, "")
        }

        /** Clean Markdown formatting and LLM section headers from offline model output */
        fun cleanMarkdownArtifacts(text: String): String {
            var result = text
            result = REGEX_MARKDOWN_HEADERS.replace(result, "")
            result = REGEX_MARKDOWN_BOLD.replace(result, "")
            result = REGEX_MARKDOWN_LIST_NUM.replace(result, "")
            result = REGEX_MARKDOWN_LIST_BULLET.replace(result, "")
            result = REGEX_LLM_SECTION_HEADERS.replace(result, "")
            result = REGEX_MULTI_BLANK_LINES.replace(result, "\n\n")
            return result.trim()
        }
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
    @Volatile private var activePersona: Persona = Persona.DEFAULT
    private val customPersonas: MutableMap<String, Persona> = java.util.concurrent.ConcurrentHashMap()
    @Volatile private var activeModel: LlmModel? = null
    @Volatile private var activeApiKey: String = ""

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
        var result = Persona.stripActionDescriptions(rawText)

        // Randomly prepend a catchphrase ~30% of the time for variety
        if (activePersona.catchphrases.isNotEmpty() && Math.random() < 0.3) {
            val catchphrase = activePersona.catchphrases.random()
            result = "$catchphrase\n\n$result"
        }

        return result
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
