package com.cyberdiviner.data.model.learning

enum class QuizType {
    SINGLE_CHOICE,
    BINARY_CLASSIFY,
    MATCHING,
    ORDERING,
    CASE_JUDGE
}

/** Alias used by lesson catalogs */
typealias QuestionType = QuizType

/** A key-value pair for matching/ordering question items */
data class MatchItem(
    val key: String,
    val value: String
)

data class QuizQuestion(
    val id: String,
    val type: QuizType,
    val prompt: String,

    // ── Simple API (for SINGLE_CHOICE / CASE_JUDGE) ──────────────────
    val options: List<String> = emptyList(),
    val correctAnswerIds: List<String> = emptyList(),
    val explanationCorrect: String = "",
    val explanationWrong: String = "",

    // ── Rich API (for MATCHING / ORDERING / BINARY_CLASSIFY) ─────────
    val items: List<MatchItem> = emptyList(),
    val correctIndex: Int = -1,
    val explanation: String = ""
)
