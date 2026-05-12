package com.cyberdiviner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LiuyaoDao
import com.cyberdiviner.data.dao.MuyuDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.dao.VisionDao
import com.cyberdiviner.data.model.DivinationReading
import com.cyberdiviner.data.model.LiuyaoReading
import com.cyberdiviner.data.model.MuyuHit
import com.cyberdiviner.data.model.TarotReading
import com.cyberdiviner.data.model.VisionReading

@Database(
    entities = [
        DivinationReading::class,
        LiuyaoReading::class,
        TarotReading::class,
        VisionReading::class,
        MuyuHit::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CyberDivinerDatabase : RoomDatabase() {

    abstract fun divinationDao(): DivinationDao
    abstract fun liuyaoDao(): LiuyaoDao
    abstract fun tarotDao(): TarotDao
    abstract fun visionDao(): VisionDao
    abstract fun muyuDao(): MuyuDao

    companion object {
        const val DATABASE_NAME = "cyberdiviner.db"
    }
}
