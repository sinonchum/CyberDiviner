package com.cyberdiviner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LearningDao
import com.cyberdiviner.data.dao.LiuyaoDao
import com.cyberdiviner.data.dao.MuyuDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.dao.VisionDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.LiuyaoReading
import com.cyberdiviner.data.model.MuyuHit
import com.cyberdiviner.data.model.TarotReading
import com.cyberdiviner.data.model.VisionReading
import com.cyberdiviner.data.model.learning.LearningProgressEntity
import com.cyberdiviner.data.model.learning.LearningStatsEntity

@Database(
    entities = [
        DivinationReading::class,
        LiuyaoReading::class,
        TarotReading::class,
        VisionReading::class,
        MuyuHit::class,
        LearningProgressEntity::class,
        LearningStatsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CyberDivinerDatabase : RoomDatabase() {

    abstract fun divinationDao(): DivinationDao
    abstract fun liuyaoDao(): LiuyaoDao
    abstract fun tarotDao(): TarotDao
    abstract fun visionDao(): VisionDao
    abstract fun muyuDao(): MuyuDao
    abstract fun learningDao(): LearningDao

    companion object {
        const val DATABASE_NAME = "cyberdiviner.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `learning_progress` (
                        `lessonId` TEXT NOT NULL,
                        `pathId` TEXT NOT NULL,
                        `completed` INTEGER NOT NULL DEFAULT 0,
                        `score` INTEGER NOT NULL DEFAULT 0,
                        `attempts` INTEGER NOT NULL DEFAULT 0,
                        `lastCompletedAt` INTEGER,
                        `mastery` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`lessonId`)
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `learning_stats` (
                        `id` TEXT NOT NULL DEFAULT 'default',
                        `totalXp` INTEGER NOT NULL DEFAULT 0,
                        `currentStreak` INTEGER NOT NULL DEFAULT 0,
                        `bestStreak` INTEGER NOT NULL DEFAULT 0,
                        `lastStudyDate` TEXT,
                        `title` TEXT NOT NULL DEFAULT '初入卦门',
                        PRIMARY KEY(`id`)
                    )"""
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `learning_stats` (`id`) VALUES ('default')"
                )
            }
        }
    }
}
