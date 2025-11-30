package com.akinalpfdn.poprush.core.data.repository

import com.akinalpfdn.poprush.core.data.local.GamePreferences
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore for persistence.
 * Handles user preferences and game settings.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val gamePreferences: GamePreferences
) : SettingsRepository {

    // Bubble Shape Settings
    override suspend fun saveBubbleShape(shape: BubbleShape) {
        gamePreferences.saveBubbleShape(shape)
    }

    override suspend fun getBubbleShape(): BubbleShape {
        return try {
            gamePreferences.getBubbleShape().first()
        } catch (e: Exception) {
            BubbleShape.CIRCLE
        }
    }

    override fun getBubbleShapeFlow(): Flow<BubbleShape> {
        return gamePreferences.getBubbleShape()
    }

    // Audio Settings
    override suspend fun toggleSoundEnabled() {
        val current = gamePreferences.getSoundEnabled().first()
        gamePreferences.saveSoundEnabled(!current)
    }

    override suspend fun isSoundEnabled(): Boolean {
        return try {
            gamePreferences.getSoundEnabled().first()
        } catch (e: Exception) {
            true
        }
    }

    override fun getSoundEnabledFlow(): Flow<Boolean> {
        return gamePreferences.getSoundEnabled()
    }

    override suspend fun toggleMusicEnabled() {
        val current = gamePreferences.getMusicEnabled().first()
        gamePreferences.saveMusicEnabled(!current)
    }

    override suspend fun isMusicEnabled(): Boolean {
        return try {
            gamePreferences.getMusicEnabled().first()
        } catch (e: Exception) {
            true
        }
    }

    override fun getMusicEnabledFlow(): Flow<Boolean> {
        return gamePreferences.getMusicEnabled()
    }

    override suspend fun setSoundVolume(volume: Float) {
        gamePreferences.saveSoundVolume(volume.coerceIn(0f, 1f))
    }

    override suspend fun getSoundVolume(): Float {
        return try {
            gamePreferences.getSoundVolume().first()
        } catch (e: Exception) {
            1.0f
        }
    }

    override fun getSoundVolumeFlow(): Flow<Float> {
        return gamePreferences.getSoundVolume()
    }

    override suspend fun setMusicVolume(volume: Float) {
        gamePreferences.saveMusicVolume(volume.coerceIn(0f, 1f))
    }

    override suspend fun getMusicVolume(): Float {
        return try {
            gamePreferences.getMusicVolume().first()
        } catch (e: Exception) {
            0.7f
        }
    }

    override fun getMusicVolumeFlow(): Flow<Float> {
        return gamePreferences.getMusicVolume()
    }

    // Game Settings
    override suspend fun setGameDifficulty(difficulty: GameDifficulty) {
        gamePreferences.saveGameDifficulty(difficulty)
    }

    override suspend fun getGameDifficulty(): GameDifficulty {
        return try {
            gamePreferences.getGameDifficulty().first()
        } catch (e: Exception) {
            GameDifficulty.NORMAL
        }
    }

    override fun getGameDifficultyFlow(): Flow<GameDifficulty> {
        return gamePreferences.getGameDifficulty()
    }

    // Visual Settings
    override suspend fun setZoomLevel(zoomLevel: Float) {
        gamePreferences.saveZoomLevel(zoomLevel.coerceIn(0.5f, 1.5f))
    }

    override suspend fun getZoomLevel(): Float {
        return try {
            gamePreferences.getZoomLevel().first()
        } catch (e: Exception) {
            1.0f
        }
    }

    override fun getZoomLevelFlow(): Flow<Float> {
        return gamePreferences.getZoomLevel()
    }

    override suspend fun toggleHapticFeedback() {
        val current = gamePreferences.getHapticFeedback().first()
        gamePreferences.saveHapticFeedback(!current)
    }

    override suspend fun isHapticFeedbackEnabled(): Boolean {
        return try {
            gamePreferences.getHapticFeedback().first()
        } catch (e: Exception) {
            true
        }
    }

    override fun getHapticFeedbackFlow(): Flow<Boolean> {
        return gamePreferences.getHapticFeedback()
    }

    // Utility Methods
    override suspend fun resetToDefaults() {
        gamePreferences.resetToDefaults()
    }

    override suspend fun exportSettings(): String {
        return try {
            val settingsMap = gamePreferences.exportPreferences()
            // Convert map to JSON string (simplified)
            settingsMap.entries.joinToString(",") { "${it.key}=${it.value}" }
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun importSettings(settingsJson: String): Boolean {
        return try {
            // Simplified import - parse key=value pairs
            val pairs = settingsJson.split(",")
            pairs.forEach { pair ->
                val parts = pair.split("=")
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()

                    when (key) {
                        "bubble_shape" -> {
                            try {
                                saveBubbleShape(BubbleShape.valueOf(value))
                            } catch (e: IllegalArgumentException) {
                                // Invalid shape, skip
                            }
                        }
                        "sound_enabled" -> gamePreferences.saveSoundEnabled(value.toBoolean())
                        "music_enabled" -> gamePreferences.saveMusicEnabled(value.toBoolean())
                        "sound_volume" -> gamePreferences.saveSoundVolume(value.toFloat())
                        "music_volume" -> gamePreferences.saveMusicVolume(value.toFloat())
                        "zoom_level" -> gamePreferences.saveZoomLevel(value.toFloat())
                        "haptic_feedback" -> gamePreferences.saveHapticFeedback(value.toBoolean())
                        "game_difficulty" -> {
                            try {
                                setGameDifficulty(GameDifficulty.valueOf(value))
                            } catch (e: IllegalArgumentException) {
                                // Invalid difficulty, skip
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isFirstLaunch(): Boolean {
        return try {
            gamePreferences.isFirstLaunch().first()
        } catch (e: Exception) {
            true
        }
    }

    override suspend fun markFirstLaunchCompleted() {
        gamePreferences.markFirstLaunchCompleted()
    }

    // User Profile Settings
    override suspend fun savePlayerName(playerName: String) {
        gamePreferences.savePlayerName(playerName)
    }

    override suspend fun getPlayerName(): String {
        return try {
            gamePreferences.getPlayerName().first()
        } catch (e: Exception) {
            "Player"
        }
    }

    override suspend fun savePlayerColor(playerColor: BubbleColor) {
        gamePreferences.savePlayerColor(playerColor)
    }

    override suspend fun getPlayerColor(): BubbleColor {
        return try {
            gamePreferences.getPlayerColor().first()
        } catch (e: Exception) {
            BubbleColor.ROSE
        }
    }
}