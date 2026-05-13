package com.cyberdiviner.engine.agent

import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * AgentInterviewEngine -- The Interrogator.
 *
 * Manages a 5-round state machine that extracts the user's causal variables
 * through Socratic questioning. Outputs a Soul Hash (hex digest) that seeds
 * all subsequent divination algorithms.
 *
 * Flow: User input -> N rounds of AI probing -> Vectorize -> Soul Hash
 */

// ── Interview States ────────────────────────────────────────────────────────

enum class InterviewState {
    IDLE,           // Waiting for user's initial query
    PROBING,        // AI is asking follow-up questions (rounds 1-4)
    VECTORIZING,    // Converting responses to causal variables
    COMPLETE        // Soul Hash generated
}

// ── Causal Variables extracted from conversation ────────────────────────────

data class CausalVector(
    val desireForStability: Float,    // 0.0 (chaos-seeking) to 1.0 (stability-seeking)
    val fearOfUnknown: Float,         // 0.0 (embraces unknown) to 1.0 (dreads unknown)
    val temporalFocus: Float,         // 0.0 (past-oriented) to 1.0 (future-oriented)
    val socialDependency: Float,      // 0.0 (self-reliant) to 1.0 (externally validated)
    val emotionalValence: Float,      // -1.0 (distressed) to 1.0 (elevated)
    val complexityTolerance: Float,   // 0.0 (prefers simple) to 1.0 (thrives in chaos)
) {
    /** Normalize to a 32-byte hex string for hashing. */
    fun toBytes(): ByteArray {
        return byteArrayOf(
            (desireForStability * 255).toInt().toByte(),
            (fearOfUnknown * 255).toInt().toByte(),
            (temporalFocus * 255).toInt().toByte(),
            (socialDependency * 255).toInt().toByte(),
            ((emotionalValence + 1f) / 2f * 255).toInt().toByte(),
            (complexityTolerance * 255).toInt().toByte()
        )
    }
}

// ── Interview Record ────────────────────────────────────────────────────────

data class InterviewRound(
    val round: Int,
    val aiPrompt: String,
    val userResponse: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class InterviewResult(
    val soulHash: String,
    val causalVector: CausalVector,
    val rounds: List<InterviewRound>,
    val userQuery: String,
    val completedAt: LocalDateTime = LocalDateTime.now()
)

// ── The Engine ──────────────────────────────────────────────────────────────

class AgentInterviewEngine {

    private var state = InterviewState.IDLE
    private var currentRound = 0
    private val maxRounds = 5
    private val rounds = mutableListOf<InterviewRound>()
    private var userQuery = ""

    // ── Probing prompts (Socratic style) ────────────────────────────────────

    private val probingPrompts = listOf(
        // Round 1: Anchor the domain
        "\u4E8B\u4E1A\u4EC5\u662F\u56E0\u679C\u7684\u5206\u652F\u3002\u8BF7\u81EA\u8FF0\uFF1A\u4F60\u5F53\u524D\u5BF9\u7A33\u5B9A\u7684\u6E34\u671B\u7A0B\u5EA6\u662F\u5426\u8D85\u8FC7\u4E86\u5BF9\u672A\u77E5\u7684\u6050\u60E7\uFF1F",

        // Round 2: Temporal orientation
        "\u4F60\u7684\u56E0\u679C\u94FE\u63A5\u66F4\u504F\u5411\u8FC7\u53BB\u7684\u8BB0\u5FC6\uFF0C\u8FD8\u662F\u672A\u6765\u7684\u53EF\u80FD\u6027\uFF1F\u56DE\u7B54\u8BF7\u7528\u65F6\u95F4\u5355\u4F4D\u63CF\u8FF0\u3002",

        // Round 3: Social topology
        "\u5728\u4F60\u7684\u51B2\u7B97\u7B97\u6CD5\u4E2D\uFF0C\u5176\u4ED6\u8282\u70B9\u662F\u4F9D\u8D56\u9879\u8FD8\u662F\u5E72\u6270\u9879\uFF1F\u4E3E\u4E00\u4E2A\u5177\u4F53\u573A\u666F\u3002",

        // Round 4: Emotional calibration
        "\u7528\u4E00\u4E2A\u9891\u7387\u503C\u63CF\u8FF0\u4F60\u5F53\u524D\u7684\u5185\u90E8\u4FE1\u53F7\u5F3A\u5EA6\uFF1A\u4F4E\u9891\u9707\u8361\u3001\u4E2D\u9891\u7A33\u5B9A\u3001\u9AD8\u9891\u8FC7\u8F7D\u3002\u8BF7\u9644\u52A0\u539F\u56E0\u3002",

        // Round 5: Complexity check
        "\u5982\u679C\u4F60\u7684\u547D\u8FD0\u662F\u4E00\u4E2A\u7A0B\u5E8F\uFF0C\u4F60\u5E0C\u671B\u5B83\u662F\u7B80\u6D01\u7684\u811A\u672C\u8FD8\u662F\u590D\u6742\u7684\u5206\u5E03\u5F0F\u7CFB\u7EDF\uFF1F\u4E3A\u4EC0\u4E48\uFF1F"
    )

    // ── Public API ───────────────────────────────────────────────────────────

    fun getState(): InterviewState = state
    fun getCurrentRound(): Int = currentRound
    fun getMaxRounds(): Int = maxRounds

    /**
     * Start the interview with the user's initial query.
     * Returns the first probing question.
     */
    fun startInterview(query: String): String {
        userQuery = query
        state = InterviewState.PROBING
        currentRound = 1
        rounds.clear()
        return probingPrompts[0]
    }

    /**
     * Process a user response and return the next probing question,
     * or complete the interview if all rounds are done.
     */
    fun processResponse(response: String): Pair<String?, Boolean> {
        // Record this round
        rounds.add(InterviewRound(
            round = currentRound,
            aiPrompt = probingPrompts[currentRound - 1],
            userResponse = response
        ))

        currentRound++

        if (currentRound > maxRounds) {
            // Interview complete
            return Pair(null, true)
        }

        return Pair(probingPrompts[currentRound - 1], false)
    }

    /**
     * Complete the interview: vectorize responses and generate Soul Hash.
     */
    fun completeInterview(): InterviewResult {
        state = InterviewState.VECTORIZING

        val vector = vectorizeResponses()
        val hash = generateSoulHash(vector)

        state = InterviewState.COMPLETE

        return InterviewResult(
            soulHash = hash,
            causalVector = vector,
            rounds = rounds.toList(),
            userQuery = userQuery
        )
    }

    fun reset() {
        state = InterviewState.IDLE
        currentRound = 0
        rounds.clear()
        userQuery = ""
    }

    // ── Vectorization ────────────────────────────────────────────────────────

    /**
     * Convert the conversation into a CausalVector.
     * Simplified heuristic-based extraction.
     */
    private fun vectorizeResponses(): CausalVector {
        val allText = rounds.joinToString(" ") { it.userResponse }
        val len = allText.length.toFloat().coerceAtLeast(1f)

        // Heuristic extraction (in production, use LLM-based sentiment analysis)
        return CausalVector(
            desireForStability = computeStability(allText),
            fearOfUnknown = computeFear(allText),
            temporalFocus = computeTemporalFocus(allText),
            socialDependency = computeSocialDependency(allText),
            emotionalValence = computeEmotionalValence(allText),
            complexityTolerance = computeComplexity(allText)
        )
    }

    private fun computeStability(text: String): Float {
        val stabilityWords = listOf("\u7A33\u5B9A", "\u5B89\u5168", "\u786E\u5B9A", "\u5EFA\u8BAE", "\u5E0C\u671B", "\u7A33")
        val chaosWords = listOf("\u5192\u9669", "\u53D8\u5316", "\u521B\u65B0", "\u81EA\u7531", "\u7A81\u7834")
        val s = stabilityWords.count { text.contains(it) }.toFloat()
        val c = chaosWords.count { text.contains(it) }.toFloat()
        return (0.5f + (s - c) * 0.1f).coerceIn(0f, 1f)
    }

    private fun computeFear(text: String): Float {
        val fearWords = listOf("\u6050\u60E7", "\u62C5\u5FE7", "\u5BB3\u6015", "\u4E0D\u786E\u5B9A", "\u7126\u8651")
        val count = fearWords.count { text.contains(it) }.toFloat()
        return (0.3f + count * 0.12f).coerceIn(0f, 1f)
    }

    private fun computeTemporalFocus(text: String): Float {
        val pastWords = listOf("\u8FC7\u53BB", "\u4EE5\u524D", "\u66FE\u7ECF", "\u5F53\u5E74", "\u56DE\u5FC6")
        val futureWords = listOf("\u672A\u6765", "\u4EE5\u540E", "\u5E0C\u671B", "\u76EE\u6807", "\u8BA1\u5212")
        val p = pastWords.count { text.contains(it) }.toFloat()
        val f = futureWords.count { text.contains(it) }.toFloat()
        return (0.5f + (f - p) * 0.1f).coerceIn(0f, 1f)
    }

    private fun computeSocialDependency(text: String): Float {
        val socialWords = listOf("\u5BB6\u4EBA", "\u670B\u53CB", "\u540C\u4E8B", "\u5173\u7CFB", "\u4ED6\u4EBA", "\u670B\u53CB")
        val selfWords = listOf("\u81EA\u5DF1", "\u72EC\u7ACB", "\u81EA\u6211", "\u4E2A\u4EBA")
        val s = socialWords.count { text.contains(it) }.toFloat()
        val sl = selfWords.count { text.contains(it) }.toFloat()
        return (0.4f + (s - sl) * 0.1f).coerceIn(0f, 1f)
    }

    private fun computeEmotionalValence(text: String): Float {
        val positive = listOf("\u5F00\u5FC3", "\u5E0C\u671B", "\u6EE1\u8DB3", "\u611F\u8C22", "\u5145\u5B9E")
        val negative = listOf("\u5931\u843D", "\u7126\u8651", "\u75DB\u82E6", "\u56F0\u60D1", "\u607C\u6012")
        val p = positive.count { text.contains(it) }.toFloat()
        val n = negative.count { text.contains(it) }.toFloat()
        return ((p - n) * 0.15f).coerceIn(-1f, 1f)
    }

    private fun computeComplexity(text: String): Float {
        val complexWords = listOf("\u590D\u6742", "\u591A\u5C42", "\u7CFB\u7EDF", "\u7B97\u6CD5", "\u7ED3\u6784")
        val simpleWords = listOf("\u7B80\u5355", "\u76F4\u63A5", "\u660E\u786E", "\u6E05\u6670")
        val c = complexWords.count { text.contains(it) }.toFloat()
        val s = simpleWords.count { text.contains(it) }.toFloat()
        return (0.5f + (c - s) * 0.1f).coerceIn(0f, 1f)
    }

    // ── Soul Hash ────────────────────────────────────────────────────────────

    private fun generateSoulHash(vector: CausalVector): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
        val vectorBytes = vector.toBytes()
        val payload = "$timestamp-${vectorBytes.joinToString("") { "%02x".format(it) }}"

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(payload.toByteArray())

        return hashBytes.take(16).joinToString("") { "%02x".format(it) }
    }
}
