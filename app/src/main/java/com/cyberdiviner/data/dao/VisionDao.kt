package com.cyberdiviner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberdiviner.data.model.VisionReading
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Vision/Face (面相) readings.
 */
@Dao
interface VisionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: VisionReading): Long

    @Update
    suspend fun update(reading: VisionReading)

    @Delete
    suspend fun delete(reading: VisionReading)

    @Query("SELECT * FROM vision_readings WHERE id = :id")
    suspend fun getById(id: Long): VisionReading?

    @Query("SELECT * FROM vision_readings WHERE id = :id")
    fun observeById(id: Long): Flow<VisionReading?>

    @Query("SELECT * FROM vision_readings WHERE reading_id = :readingId")
    suspend fun getByReadingId(readingId: Long): VisionReading?

    @Query("SELECT * FROM vision_readings WHERE reading_id = :readingId")
    fun observeByReadingId(readingId: Long): Flow<VisionReading?>

    @Query("SELECT * FROM vision_readings ORDER BY id DESC")
    fun getAll(): Flow<List<VisionReading>>

    @Query("DELETE FROM vision_readings")
    suspend fun deleteAll()
}
