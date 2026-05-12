package com.cyberdiviner.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a Tarot card reading session.
 *
 * Cards are stored as a serialized JSON array for flexibility
 * across different spread types (single card, three-card, Celtic Cross, etc.).
 *
 * Each card entry in cardsJson has the format:
 * {
 *   "card": "The Fool",
 *   "number": 0,
 *   "suit": "major" | "wands" | "cups" | "swords" | "pentacles",
 *   "position": "past" | "present" | "future" | ...,
 *   "isReversed": false
 * }
 */
@Entity(
    tableName = "tarot_readings",
    foreignKeys = [
        ForeignKey(
            entity = DivinationReading::class,
            parentColumns = ["id"],
            childColumns = ["reading_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reading_id")]
)
data class TarotReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "reading_id")
    val readingId: Long,

    /** Spread type: "single", "three_card", "celtic_cross", "horseshoe" */
    @ColumnInfo(name = "spread_type")
    val spreadType: String,

    /** Number of cards drawn */
    @ColumnInfo(name = "card_count")
    val cardCount: Int,

    /** JSON-serialized array of drawn cards */
    @ColumnInfo(name = "cards_json")
    val cardsJson: String,

    /** AI-generated interpretation of the reading */
    val interpretation: String = ""
)
