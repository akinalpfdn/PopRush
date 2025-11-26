package com.akinalpfdn.poprush.di

import android.content.Context
import androidx.room.Room
import com.akinalpfdn.poprush.core.data.local.GameDatabase
import com.akinalpfdn.poprush.core.data.local.dao.HighScoreDao
import com.akinalpfdn.poprush.core.data.local.dao.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for database-related dependencies.
 * Provides Room database instance and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideGameDatabase(
        @ApplicationContext context: Context
    ): GameDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = GameDatabase::class.java,
            name = "poprush_database"
        )
            .fallbackToDestructiveMigration() // For development - consider migrations in production
            .build()
    }

    /**
     * Provides the High Score DAO for database operations.
     */
    @Provides
    @Singleton
    fun provideHighScoreDao(database: GameDatabase): HighScoreDao {
        return database.highScoreDao()
    }

    /**
     * Provides the Settings DAO for database operations.
     */
    @Provides
    @Singleton
    fun provideSettingsDao(database: GameDatabase): SettingsDao {
        return database.settingsDao()
    }
}