package com.cyberdiviner.data.dao

import androidx.room.*
import com.cyberdiviner.data.model.learning.LearningProgressEntity
import com.cyberdiviner.data.model.learning.LearningStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_progress WHERE pathId = :pathId ORDER BY lessonId")
    fun getProgressForPath(pathId: String): Flow<List<LearningProgressEntity>>

    @Query("SELECT * FROM learning_progress WHERE lessonId = :lessonId")
    suspend fun getProgress(lessonId: String): LearningProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LearningProgressEntity)

    @Query("SELECT * FROM learning_stats WHERE id = 'default'")
    fun getStats(): Flow<LearningStatsEntity?>

    @Update
    suspend fun updateStats(stats: LearningStatsEntity)

    @Query("SELECT COUNT(*) FROM learning_progress WHERE pathId = :pathId AND completed = 1")
    suspend fun getCompletedCountForPath(pathId: String): Int
}
