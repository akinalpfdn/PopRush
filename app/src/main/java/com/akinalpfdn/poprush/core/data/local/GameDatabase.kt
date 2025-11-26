package com.akinalpfdn.poprush.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akinalpfdn.poprush.core.data.local.entity.HighScoreEntity
import com.akinalpfdn.poprush.core.data.local.entity.SettingsEntity
import com.akinalpfdn.poprush.core.data.local.dao.HighScoreDao
import com.akinalpfdn.poprush.core.data.local.dao.SettingsDao

/**
 * Room database for storing PopRush game data.
 * Contains high scores, game statistics, and user settings.
 */
@Database(
    entities = [
        HighScoreEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    /**
     * Provides access to high score operations.
     */
    abstract fun highScoreDao(): HighScoreDao

    /**
     * Provides access to settings operations.
     */
    abstract fun settingsDao(): SettingsDao
}