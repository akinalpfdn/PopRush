package com.akinalpfdn.poprush.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages game preferences using Android DataStore.
 * Provides a type-safe API for storing and retrieving game settings.
 */
@Singleton
class GamePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // Preference keys
    companion object {
        private val BUBBLE_SHAPE_KEY = stringPreferencesKey("bubble_shape")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
        private val SOUND_VOLUME_KEY = floatPreferencesKey("sound_volume")
        private val MUSIC_VOLUME_KEY = floatPreferencesKey("music_volume")
        private val ZOOM_LEVEL_KEY = floatPreferencesKey("zoom_level")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        private val GAME_DIFFICULTY_KEY = stringPreferencesKey("game_difficulty")
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
        private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
        private val TOTAL_GAMES_PLAYED_KEY = intPreferencesKey("total_games_played")
        private val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        private val PLAYER_COLOR_KEY = stringPreferencesKey("player_color")
    }

    // Bubble Shape
    suspend fun saveBubbleShape(shape: BubbleShape) {
        dataStore.edit { preferences ->
            preferences[BUBBLE_SHAPE_KEY] = shape.name
        }
    }

    fun getBubbleShape(): Flow<BubbleShape> {
        return dataStore.data.map { preferences ->
            val shapeName = preferences[BUBBLE_SHAPE_KEY] ?: BubbleShape.CIRCLE.name
            try {
                BubbleShape.valueOf(shapeName)
            } catch (e: IllegalArgumentException) {
                BubbleShape.CIRCLE
            }
        }
    }

    // Audio Settings
    suspend fun saveSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    fun getSoundEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[SOUND_ENABLED_KEY] ?: true
        }
    }

    suspend fun saveMusicEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    fun getMusicEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[MUSIC_ENABLED_KEY] ?: true
        }
    }

    suspend fun saveSoundVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[SOUND_VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }

    fun getSoundVolume(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[SOUND_VOLUME_KEY] ?: 1.0f
        }
    }

    suspend fun saveMusicVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }

    fun getMusicVolume(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[MUSIC_VOLUME_KEY] ?: 0.7f
        }
    }

    // Visual Settings
    suspend fun saveZoomLevel(zoomLevel: Float) {
        dataStore.edit { preferences ->
            preferences[ZOOM_LEVEL_KEY] = zoomLevel.coerceIn(0.5f, 1.5f)
        }
    }

    fun getZoomLevel(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[ZOOM_LEVEL_KEY] ?: 1.0f
        }
    }

    suspend fun saveHapticFeedback(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }

    fun getHapticFeedback(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] ?: true
        }
    }

    // Game Settings
    suspend fun saveGameDifficulty(difficulty: GameDifficulty) {
        dataStore.edit { preferences ->
            preferences[GAME_DIFFICULTY_KEY] = difficulty.name
        }
    }

    fun getGameDifficulty(): Flow<GameDifficulty> {
        return dataStore.data.map { preferences ->
            val difficultyName = preferences[GAME_DIFFICULTY_KEY] ?: GameDifficulty.NORMAL.name
            try {
                GameDifficulty.valueOf(difficultyName)
            } catch (e: IllegalArgumentException) {
                GameDifficulty.NORMAL
            }
        }
    }

    // High Score
    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGH_SCORE_KEY] = score
        }
    }

    fun getHighScore(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[HIGH_SCORE_KEY] ?: 0
        }
    }

    // Game Statistics
    suspend fun incrementGamesPlayed() {
        dataStore.edit { preferences ->
            val currentCount = preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0
            preferences[TOTAL_GAMES_PLAYED_KEY] = currentCount + 1
        }
    }

    fun getTotalGamesPlayed(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0
        }
    }

    // First Launch
    suspend fun markFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    fun isFirstLaunch(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[FIRST_LAUNCH_KEY] ?: true
        }
    }

    // Utility Methods
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    // User Profile Settings
    suspend fun savePlayerName(playerName: String) {
        dataStore.edit { preferences ->
            preferences[PLAYER_NAME_KEY] = playerName
        }
    }

    fun getPlayerName(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PLAYER_NAME_KEY] ?: "Player"
        }
    }

    suspend fun savePlayerColor(playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor) {
        dataStore.edit { preferences ->
            preferences[PLAYER_COLOR_KEY] = playerColor.name
        }
    }

    fun getPlayerColor(): Flow<com.akinalpfdn.poprush.core.domain.model.BubbleColor> {
        return dataStore.data.map { preferences ->
            val colorName = preferences[PLAYER_COLOR_KEY] ?: "ROSE"
            try {
                com.akinalpfdn.poprush.core.domain.model.BubbleColor.valueOf(colorName)
            } catch (e: IllegalArgumentException) {
                com.akinalpfdn.poprush.core.domain.model.BubbleColor.ROSE
            }
        }
    }

    /**
     * Exports all preferences to a map for backup purposes.
     */
    suspend fun exportPreferences(): Map<String, Any> {
        val preferences = dataStore.data.first()
        return mapOf(
            "bubble_shape" to (preferences[BUBBLE_SHAPE_KEY] ?: BubbleShape.CIRCLE.name),
            "sound_enabled" to (preferences[SOUND_ENABLED_KEY] ?: true),
            "music_enabled" to (preferences[MUSIC_ENABLED_KEY] ?: true),
            "sound_volume" to (preferences[SOUND_VOLUME_KEY] ?: 1.0f),
            "music_volume" to (preferences[MUSIC_VOLUME_KEY] ?: 0.7f),
            "zoom_level" to (preferences[ZOOM_LEVEL_KEY] ?: 1.0f),
            "haptic_feedback" to (preferences[HAPTIC_FEEDBACK_KEY] ?: true),
            "game_difficulty" to (preferences[GAME_DIFFICULTY_KEY] ?: GameDifficulty.NORMAL.name),
            "high_score" to (preferences[HIGH_SCORE_KEY] ?: 0),
            "total_games_played" to (preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0),
            "player_name" to (preferences[PLAYER_NAME_KEY] ?: "Player"),
            "player_color" to (preferences[PLAYER_COLOR_KEY] ?: "ROSE")
        )
    }
}