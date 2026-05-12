package com.cyberdiviner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberdiviner.data.model.TarotReading
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Tarot card readings.
 */
@Dao
interface TarotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: TarotReading): Long

    @Update
    suspend fun update(reading: TarotReading)

    @Delete
    suspend fun delete(reading: TarotReading)

    @Query("SELECT * FROM tarot_readings WHERE id = :id")
    suspend fun getById(id: Long): TarotReading?

    @Query("SELECT * FROM tarot_readings WHERE id = :id")
    fun observeById(id: Long): Flow<TarotReading?>

    @Query("SELECT * FROM tarot_readings WHERE reading_id = :readingId")
    suspend fun getByReadingId(readingId: Long): TarotReading?

    @Query("SELECT * FROM tarot_readings WHERE reading_id = :readingId")
    fun observeByReadingId(readingId: Long): Flow<TarotReading?>

    @Query("SELECT * FROM tarot_readings ORDER BY id DESC")
    fun getAll(): Flow<List<TarotReading>>

    @Query("SELECT * FROM tarot_readings WHERE spread_type = :spreadType ORDER BY id DESC")
    fun getBySpreadType(spreadType: String): Flow<List<TarotReading>>

    @Query("DELETE FROM tarot_readings")
    suspend fun deleteAll()
}
