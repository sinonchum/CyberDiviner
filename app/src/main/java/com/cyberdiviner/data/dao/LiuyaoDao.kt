package com.cyberdiviner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberdiviner.data.model.LiuyaoReading
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Liuyao (六爻) hexagram readings.
 */
@Dao
interface LiuyaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: LiuyaoReading): Long

    @Update
    suspend fun update(reading: LiuyaoReading)

    @Delete
    suspend fun delete(reading: LiuyaoReading)

    @Query("SELECT * FROM liuyao_readings WHERE id = :id")
    suspend fun getById(id: Long): LiuyaoReading?

    @Query("SELECT * FROM liuyao_readings WHERE id = :id")
    fun observeById(id: Long): Flow<LiuyaoReading?>

    @Query("SELECT * FROM liuyao_readings WHERE reading_id = :readingId")
    suspend fun getByReadingId(readingId: Long): LiuyaoReading?

    @Query("SELECT * FROM liuyao_readings WHERE reading_id = :readingId")
    fun observeByReadingId(readingId: Long): Flow<LiuyaoReading?>

    @Query("SELECT * FROM liuyao_readings ORDER BY id DESC")
    fun getAll(): Flow<List<LiuyaoReading>>

    @Query("SELECT * FROM liuyao_readings WHERE hexagram_name = :name ORDER BY id DESC")
    fun getByHexagramName(name: String): Flow<List<LiuyaoReading>>

    @Query("DELETE FROM liuyao_readings")
    suspend fun deleteAll()
}
