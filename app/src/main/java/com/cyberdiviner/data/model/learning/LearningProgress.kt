package com.cyberdiviner.data.model.learning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_progress")
data class LearningProgressEntity(
    @PrimaryKey val lessonId: String,
    val pathId: String,
    val completed: Boolean = false,
    val score: Int = 0,
    val attempts: Int = 0,
    val lastCompletedAt: Long? = null,
    val mastery: Int = 0
)

@Entity(tableName = "learning_stats")
data class LearningStatsEntity(
    @PrimaryKey val id: String = "default",
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastStudyDate: String? = null,
    val title: String = "初入卦门"
)
