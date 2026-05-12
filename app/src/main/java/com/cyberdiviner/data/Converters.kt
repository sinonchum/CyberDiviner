package com.cyberdiviner.data

import androidx.room.TypeConverter
import com.cyberdiviner.data.model.DivinationType

/**
 * Room type converters for non-primitive types.
 */
class Converters {

    @TypeConverter
    fun fromDivinationType(type: DivinationType): String = type.name

    @TypeConverter
    fun toDivinationType(value: String): DivinationType = DivinationType.valueOf(value)
}
