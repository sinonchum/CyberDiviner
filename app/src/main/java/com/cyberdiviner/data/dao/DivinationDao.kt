package com.cyberdiviner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.DivinationType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for core divination readings.
 */
@Dao
interface DivinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: DivinationReading): Long

    @Update
    suspend fun update(reading: DivinationReading)

    @Delete
    suspend fun delete(reading: DivinationReading)

    @Query("SELECT * FROM divination_readings WHERE id = :id")
    suspend fun getById(id: Long): DivinationReading?

    @Query("SELECT * FROM divination_readings WHERE id = :id")
    fun observeById(id: Long): Flow<DivinationReading?>

    @Query("SELECT * FROM divination_readings ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DivinationReading>>

    @Query("SELECT * FROM divination_readings WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: DivinationType): Flow<List<DivinationReading>>

    @Query("SELECT * FROM divination_readings WHERE isFavorited = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<DivinationReading>>

    @Query("UPDATE divination_readings SET isFavorited = :favorited WHERE id = :id")
    suspend fun setFavorited(id: Long, favorited: Boolean)

    @Query("SELECT * FROM divination_readings WHERE question LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<DivinationReading>>

    @Query("SELECT * FROM divination_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<DivinationReading>>

    @Query("SELECT COUNT(*) FROM divination_readings")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM divination_readings WHERE type = :type")
    fun getCountByType(type: DivinationType): Flow<Int>

    @Query("DELETE FROM divination_readings")
    suspend fun deleteAll()
}
