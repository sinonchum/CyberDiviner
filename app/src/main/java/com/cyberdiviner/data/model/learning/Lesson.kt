package com.cyberdiviner.data.model.learning

data class Lesson(
    val id: String,
    val pathId: String,
    val order: Int,
    val title: String,
    val subtitle: String,
    val concept: String,
    val explanation: String,
    val howToRead: List<String>,
    val questions: List<QuizQuestion>,
    val unlockReward: UnlockReward?
)
