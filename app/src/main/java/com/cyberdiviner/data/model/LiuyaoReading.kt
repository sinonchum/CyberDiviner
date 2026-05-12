package com.cyberdiviner.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a Liuyao (六爻) hexagram reading.
 *
 * A hexagram consists of 6 lines, each can be:
 * - Yin (0): broken line ⚋
 * - Yang (1): solid line ⚊
 * - Changing Yin (2): yin that transforms to yang
 * - Changing Yang (3): yang that transforms to yin
 *
 * The positions array maps to lines 1-6 (bottom to top).
 */
@Entity(
    tableName = "liuyao_readings",
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
data class LiuyaoReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "reading_id")
    val readingId: Long,

    /** Name of the hexagram (e.g., "乾", "坤", "屯") */
    @ColumnInfo(name = "hexagram_name")
    val hexagramName: String,

    /** Number / unicode index of the hexagram */
    @ColumnInfo(name = "hexagram_number")
    val hexagramNumber: Int,

    /** Upper trigram name (e.g., "乾", "兑", "离") */
    @ColumnInfo(name = "upper_trigram")
    val upperTrigram: String,

    /** Lower trigram name */
    @ColumnInfo(name = "lower_trigram")
    val lowerTrigram: String,

    /** Comma-separated line values: 0=yin, 1=yang, 2=changing-yin, 3=changing-yang (6 values, bottom→top) */
    @ColumnInfo(name = "hexagram_lines")
    val hexagramLines: String,

    /** Comma-separated positions of changing lines (1-indexed, bottom→top) */
    @ColumnInfo(name = "changing_lines")
    val changingLines: String = "",

    /** Whether this hexagram was obtained by coin toss (true) or yarrow stalks (false) */
    @ColumnInfo(name = "coin_method")
    val coinMethod: Boolean = true,

    /** AI-generated interpretation of the reading */
    val interpretation: String = ""
)
