package com.cyberdiviner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks each wooden fish (木鱼) strike for mindfulness/meditation stats.
 * Used for the meditation counter feature.
 */
@Entity(tableName = "muyu_hits")
data class MuyuHit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    /** Duration of the hit sound in milliseconds */
    val durationMs: Int = 0,
    /** Session ID to group hits into a single meditation session */
    val sessionId: String = ""
)
