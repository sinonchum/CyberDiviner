package com.cyberdiviner.data.model.learning

enum class UnlockType {
    TERM_ANNOTATION,
    HINT_DISPLAY,
    POSTER_STYLE,
    TITLE
}

data class UnlockReward(
    val type: UnlockType,
    val target: String,
    val description: String
)
