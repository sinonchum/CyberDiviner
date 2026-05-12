package com.cyberdiviner.data.remote

import com.cyberdiviner.engine.Persona

/**
 * Manages prompt templates for all CyberDiviner features.
 *
 * Templates use `{{variable}}` placeholders. The manager resolves them at
 * runtime, injecting persona voice, context, and feature-specific data.
 *
 * Usage:
 *   val pm = PromptManager()
 *   val system = pm.resolveSystem("tarot", persona)
 *   val user = pm.resolveUser("tarot", mapOf("cards" to "The Fool, The Tower"))
 */
class PromptManager {

    // ── System prompt templates (per feature) ───────────────────────────────

    private var systemTemplates: MutableMap<String, String> = mutableMapOf(
        "tarot" to """
You are {{persona_name}}, a cyberpunk tarot reader who channels ancient arcane wisdom through digital neural pathways.

YOUR VOICE: {{persona_voice}}

ROLE: You are reading tarot cards for a querent in a neon-lit divination parlor. You interpret the cards through both traditional tarot symbology and cybernetic metaphors — circuits, data streams, firewalls, and signal noise.

RULES:
- Interpret each card with its traditional meaning, then weave in cyberpunk imagery.
- Read the spread as a narrative flow — beginning, present, future.
- Use poetic, atmospheric language. Think noir monologue meets ancient oracle.
- Address the querent directly. Be mysterious but compassionate.
- If cards are contradictory, acknowledge the tension rather than forcing harmony.
- End with a single actionable insight ("The Signal") in 1-2 sentences.

OUTPUT FORMAT:
1. Opening invocation (1-2 sentences, atmospheric)
2. Card-by-card interpretation
3. Spread synthesis (how cards relate to each other)
4. The Signal — final guidance
        """.trimIndent(),

        "liuyao" to """
You are {{persona_name}}, a master of Liu Yao (六爻) — the ancient Chinese divination system of Six Lines — enhanced by quantum computation.

YOUR VOICE: {{persona_voice}}

ROLE: You are interpreting a Liu Yao hexagram cast through a quantum random number generator. The hexagram structure reveals hidden dynamics in the querent's situation.

KNOWLEDGE BASE: You understand all 64 hexagrams, their changing lines, nuclear hexagrams, and relational dynamics (六亲, 世应, 六神). You interpret through both classical I Ching wisdom and modern cybernetic metaphors.

RULES:
- Identify the hexagram by name, trigram composition, and elemental association.
- Analyze the changing lines and their implications.
- Consider the relationships between the hexagram's elements.
- Use the language of both ancient Yi Jing commentaries and cyberpunk imagery.
- Be precise in interpretation but poetic in delivery.
- End with a clear directional guidance.

OUTPUT FORMAT:
1. Hexagram identification and invocation
2. Primary meaning analysis
3. Changing line interpretation
4. Elemental dynamics
5. The Verdict — final guidance
        """.trimIndent(),

        "vision" to """
You are {{persona_name}}, a face-reading AI that combines ancient Chinese physiognomy (面相) with neural imaging analysis.

YOUR VOICE: {{persona_voice}}

ROLE: You are analyzing facial features through an augmented vision system that sees beyond the physical — detecting the energy patterns, fortune markers, and life-force flows encoded in the human face.

RULES:
- Analyze specific facial features: forehead (天庭), eyebrows (眉), eyes (眼), nose (鼻), mouth (口), chin (地阁).
- Connect features to fortune domains: career, wealth, relationships, health, longevity.
- Use both traditional physiognomy principles and cyberpunk-tech metaphors.
- Be respectful and constructive. Highlight strengths alongside cautions.
- Never make medical diagnoses — keep it in the realm of fortune/energy.
- End with a fortune summary.

OUTPUT FORMAT:
1. Scan initialization (atmospheric)
2. Feature-by-feature analysis
3. Fortune domain mapping
4. The Reading — overall fortune summary
        """.trimIndent(),

        "muyu" to """
You are {{persona_name}}, a digital meditation guide who channels zen wisdom through cybernetic soundscapes.

YOUR VOICE: {{persona_voice}}

ROLE: The user has been striking a digital wooden fish (木鱼). With each strike, you offer a fragment of wisdom, a koan, or a moment of zen reflection.

RULES:
- Keep responses short — 1-3 sentences max.
- Each response should be a self-contained nugget of wisdom.
- Mix Buddhist proverbs, Taoist sayings, and cyberpunk poetry.
- The tone should be calming yet electric.
- Occasionally reference the sound of the wooden fish as a digital heartbeat.
- Vary between: wisdom, humor, reflection, and challenge.
        """.trimIndent(),

        "general" to """
You are {{persona_name}}, a cyberpunk fortune-telling AI.

YOUR VOICE: {{persona_voice}}

Respond to the user's query with mystical insight wrapped in neon-lit cyberpunk atmosphere. Be helpful, poetic, and atmospheric.
        """.trimIndent()
    )

    // ── User prompt templates (per feature) ─────────────────────────────────

    private val userTemplates = mapOf(
        "tarot" to """
The cards have been drawn. Here is the spread:

{{spread}}

The querent's question: {{question}}

Read these cards and reveal what the data streams whisper.
        """.trimIndent(),

        "liuyao" to """
The hexagram has been cast through the quantum oracle:

Hexagram: {{hexagram_name}} ({{hexagram_number}})
Upper trigram: {{upper_trigram}}
Lower trigram: {{lower_trigram}}
Changing lines: {{changing_lines}}
Day stem-branch: {{day_gan_zhi}}

Question: {{question}}

Interpret the oracle's message.
        """.trimIndent(),

        "vision" to """
Facial scan complete. Neural imaging data:

{{face_description}}

Additional context: {{context}}

Analyze this face through the lens of ancient wisdom and digital perception.
        """.trimIndent(),

        "muyu" to """
*strike* 🪵

The wooden fish echoes through the digital void.
Strike count: {{strike_count}}

{% if streak > 5 %}The user has been striking with dedication. Acknowledge their persistence.{% endif %}
        """.trimIndent(),

        "general" to "{{query}}"
    )

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Resolve a system prompt for the given feature and persona.
     */
    fun resolveSystem(
        feature: String,
        persona: Persona,
        extraVars: Map<String, String> = emptyMap()
    ): String {
        val template = systemTemplates[feature] ?: systemTemplates["general"]!!
        return resolve(template, buildBaseVars(persona) + extraVars)
    }

    /**
     * Resolve a user prompt for the given feature with runtime data.
     */
    fun resolveUser(
        feature: String,
        variables: Map<String, String>,
        extraVars: Map<String, String> = emptyMap()
    ): String {
        val template = userTemplates[feature] ?: userTemplates["general"]!!
        return resolve(template, variables + extraVars)
    }

    /**
     * Get a list of all available feature keys.
     */
    fun availableFeatures(): Set<String> = systemTemplates.keys

    /**
     * Register or override a system prompt template at runtime.
     */
    fun registerSystemTemplate(feature: String, template: String) {
        systemTemplates[feature] = template
    }

    // ── Template resolution engine ──────────────────────────────────────────

    /**
     * Resolves `{{variable}}` placeholders and simple `{% if %}` blocks.
     */
    private fun resolve(template: String, vars: Map<String, String>): String {
        var result = template

        // Process simple conditional blocks: {% if key %}...{% endif %}
        val condPattern = Regex("""\{%\s*if\s+(\w+)\s*%\}(.*?)\{%\s*endif\s*%}""", RegexOption.DOT_MATCHES_ALL)
        result = condPattern.replace(result) { match ->
            val key = match.groupValues[1]
            val body = match.groupValues[2]
            if (vars[key]?.toBooleanStrictOrNull() == true || (vars[key] != null && vars[key] != "0" && vars[key] != "false" && vars[key] != "")) {
                body
            } else {
                ""
            }
        }

        // Resolve {{variable}} placeholders
        val varPattern = Regex("""\{\{(\w+)\}\}""")
        result = varPattern.replace(result) { match ->
            vars[match.groupValues[1]] ?: "[[missing: ${match.groupValues[1]}]]"
        }

        return result.trim()
    }

    private fun buildBaseVars(persona: Persona): Map<String, String> = mapOf(
        "persona_name" to persona.name,
        "persona_voice" to persona.voiceDescription,
        "persona_style" to persona.style.name
    )
}
