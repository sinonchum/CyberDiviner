package com.cyberdiviner.data.di

import android.content.Context
import androidx.room.Room
import com.cyberdiviner.data.CyberDivinerDatabase
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.dao.LiuyaoDao
import com.cyberdiviner.data.dao.MuyuDao
import com.cyberdiviner.data.dao.TarotDao
import com.cyberdiviner.data.dao.VisionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CyberDivinerDatabase {
        return Room.databaseBuilder(
            context,
            CyberDivinerDatabase::class.java,
            CyberDivinerDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideDivinationDao(db: CyberDivinerDatabase): DivinationDao = db.divinationDao()

    @Provides
    fun provideLiuyaoDao(db: CyberDivinerDatabase): LiuyaoDao = db.liuyaoDao()

    @Provides
    fun provideTarotDao(db: CyberDivinerDatabase): TarotDao = db.tarotDao()

    @Provides
    fun provideVisionDao(db: CyberDivinerDatabase): VisionDao = db.visionDao()

    @Provides
    fun provideMuyuDao(db: CyberDivinerDatabase): MuyuDao = db.muyuDao()
}
