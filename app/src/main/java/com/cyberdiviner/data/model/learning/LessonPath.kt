package com.cyberdiviner.data.model.learning

data class LessonPath(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val lessonIds: List<String>,
    val requiredPaths: List<String> = emptyList(),
    val iconLabel: String
)
