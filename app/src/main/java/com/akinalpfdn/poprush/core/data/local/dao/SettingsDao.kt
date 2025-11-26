package com.akinalpfdn.poprush.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akinalpfdn.poprush.core.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for settings operations.
 * Provides methods to insert, update, delete, and query user settings.
 */
@Dao
interface SettingsDao {

    /**
     * Inserts or updates a setting. Replaces existing if conflict occurs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: SettingsEntity)

    /**
     * Updates an existing setting.
     */
    @Update
    suspend fun updateSetting(setting: SettingsEntity)

    /**
     * Deletes a specific setting.
     */
    @Delete
    suspend fun deleteSetting(setting: SettingsEntity)

    /**
     * Gets a specific setting by key.
     */
    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    /**
     * Gets a Flow of a specific setting for reactive updates.
     */
    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    /**
     * Gets all settings as a Flow.
     */
    @Query("SELECT * FROM settings ORDER BY key")
    fun getAllSettingsFlow(): Flow<List<SettingsEntity>>

    /**
     * Gets all settings as a list.
     */
    @Query("SELECT * FROM settings ORDER BY key")
    suspend fun getAllSettings(): List<SettingsEntity>

    /**
     * Gets settings matching a specific key pattern (using LIKE).
     */
    @Query("SELECT * FROM settings WHERE key LIKE :keyPattern ORDER BY key")
    suspend fun getSettingsByKeyPattern(keyPattern: String): List<SettingsEntity>

    /**
     * Deletes a setting by key.
     */
    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteSettingByKey(key: String)

    /**
     * Deletes all settings.
     */
    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()

    /**
     * Gets the total number of settings.
     */
    @Query("SELECT COUNT(*) FROM settings")
    suspend fun getSettingsCount(): Int

    /**
     * Checks if a specific setting exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM settings WHERE key = :key)")
    suspend fun settingExists(key: String): Boolean

    // Convenience methods for common settings

    /**
     * Gets bubble shape setting.
     */
    @Query("SELECT value FROM settings WHERE key = 'bubble_shape' LIMIT 1")
    suspend fun getBubbleShape(): String?

    /**
     * Gets sound enabled setting.
     */
    @Query("SELECT value FROM settings WHERE key = 'sound_enabled' LIMIT 1")
    suspend fun getSoundEnabled(): String?

    /**
     * Gets music enabled setting.
     */
    @Query("SELECT value FROM settings WHERE key = 'music_enabled' LIMIT 1")
    suspend fun getMusicEnabled(): String?

    /**
     * Gets zoom level setting.
     */
    @Query("SELECT value FROM settings WHERE key = 'zoom_level' LIMIT 1")
    suspend fun getZoomLevel(): String?
}