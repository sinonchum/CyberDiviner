package com.cyberdiviner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core entity representing a single divination reading session.
 * Each reading has a type, a user question, and serialized result data.
 * Specific sub-readings (liuyao, tarot, vision) are linked via foreign key.
 */
@Entity(tableName = "divination_readings")
data class DivinationReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: DivinationType,
    val question: String,
    val timestamp: Long = System.currentTimeMillis(),
    /** Serialized JSON of the type-specific result data */
    val resultJson: String,
    val isFavorited: Boolean = false,
    val notes: String = ""
)
