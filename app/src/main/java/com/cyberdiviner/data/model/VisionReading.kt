package com.cyberdiviner.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a face/vision (面相) reading session.
 *
 * Stores the image URI (if available) and extracted facial features
 * as serialized JSON for AI-powered physiognomy analysis.
 *
 * featuresJson format:
 * {
 *   "forehead": { "shape": "broad", "lines": 2 },
 *   "eyes": { "shape": "phoenix", "size": "large" },
 *   "nose": { "shape": "dragon", "bridge": "high" },
 *   "mouth": { "shape": "cherry", "lips": "full" },
 *   "ears": { "shape": "lotus", "size": "medium" },
 *   "chin": { "shape": "rounded", "prominence": "strong" }
 * }
 */
@Entity(
    tableName = "vision_readings",
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
data class VisionReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "reading_id")
    val readingId: Long,

    /** URI of the analyzed photo, if user provided one */
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    /** JSON-serialized extracted facial features */
    @ColumnInfo(name = "features_json")
    val featuresJson: String = "{}",

    /** AI-generated interpretation of the face reading */
    val interpretation: String = ""
)
