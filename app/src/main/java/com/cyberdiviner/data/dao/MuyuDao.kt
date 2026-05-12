package com.cyberdiviner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberdiviner.data.model.MuyuHit
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Muyu (木鱼) meditation hit tracking.
 */
@Dao
interface MuyuDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hit: MuyuHit): Long

    @Delete
    suspend fun delete(hit: MuyuHit)

    @Query("SELECT * FROM muyu_hits WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getBySession(sessionId: String): Flow<List<MuyuHit>>

    @Query("SELECT * FROM muyu_hits ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MuyuHit>>

    @Query("SELECT COUNT(*) FROM muyu_hits")
    fun getTotalHits(): Flow<Int>

    @Query("SELECT COUNT(*) FROM muyu_hits WHERE sessionId = :sessionId")
    fun getHitCountForSession(sessionId: String): Flow<Int>

    @Query("DELETE FROM muyu_hits WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM muyu_hits")
    suspend fun deleteAll()
}
