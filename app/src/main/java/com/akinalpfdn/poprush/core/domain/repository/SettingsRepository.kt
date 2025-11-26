package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user settings and preferences.
 * Handles audio settings, visual preferences, and game configurations.
 */
interface SettingsRepository {

    // Bubble Shape Settings
    /**
     * Saves the selected bubble shape.
     *
     * @param shape The bubble shape to save
     */
    suspend fun saveBubbleShape(shape: BubbleShape)

    /**
     * Gets the currently selected bubble shape.
     *
     * @return The saved bubble shape, defaults to CIRCLE
     */
    suspend fun getBubbleShape(): BubbleShape

    /**
     * Gets a stream of bubble shape changes.
     *
     * @return Flow that emits bubble shape updates
     */
    fun getBubbleShapeFlow(): Flow<BubbleShape>

    // Audio Settings
    /**
     * Toggles sound effects on/off.
     */
    suspend fun toggleSoundEnabled()

    /**
     * Gets whether sound effects are enabled.
     *
     * @return True if sound effects are enabled, false otherwise
     */
    suspend fun isSoundEnabled(): Boolean

    /**
     * Gets a stream of sound enabled state changes.
     *
     * @return Flow that emits sound enabled state
     */
    fun getSoundEnabledFlow(): Flow<Boolean>

    /**
     * Toggles background music on/off.
     */
    suspend fun toggleMusicEnabled()

    /**
     * Gets whether background music is enabled.
     *
     * @return True if music is enabled, false otherwise
     */
    suspend fun isMusicEnabled(): Boolean

    /**
     * Gets a stream of music enabled state changes.
     *
     * @return Flow that emits music enabled state
     */
    fun getMusicEnabledFlow(): Flow<Boolean>

    /**
     * Sets the sound effects volume.
     *
     * @param volume Volume level between 0.0f and 1.0f
     */
    suspend fun setSoundVolume(volume: Float)

    /**
     * Gets the current sound effects volume.
     *
     * @return Current volume level between 0.0f and 1.0f
     */
    suspend fun getSoundVolume(): Float

    /**
     * Gets a stream of sound volume changes.
     *
     * @return Flow that emits volume updates
     */
    fun getSoundVolumeFlow(): Flow<Float>

    /**
     * Sets the background music volume.
     *
     * @param volume Volume level between 0.0f and 1.0f
     */
    suspend fun setMusicVolume(volume: Float)

    /**
     * Gets the current background music volume.
     *
     * @return Current volume level between 0.0f and 1.0f
     */
    suspend fun getMusicVolume(): Float

    /**
     * Gets a stream of music volume changes.
     *
     * @return Flow that emits volume updates
     */
    fun getMusicVolumeFlow(): Flow<Float>

    // Game Settings
    /**
     * Sets the game difficulty level.
     *
     * @param difficulty The difficulty level to save
     */
    suspend fun setGameDifficulty(difficulty: GameDifficulty)

    /**
     * Gets the current game difficulty level.
     *
     * @return The saved difficulty level, defaults to NORMAL
     */
    suspend fun getGameDifficulty(): GameDifficulty

    /**
     * Gets a stream of difficulty level changes.
     *
     * @return Flow that emits difficulty updates
     */
    fun getGameDifficultyFlow(): Flow<GameDifficulty>

    // Visual Settings
    /**
     * Sets the zoom level for the game board.
     *
     * @param zoomLevel Zoom level between 0.5f and 1.5f
     */
    suspend fun setZoomLevel(zoomLevel: Float)

    /**
     * Gets the current zoom level.
     *
     * @return Current zoom level between 0.5f and 1.5f
     */
    suspend fun getZoomLevel(): Float

    /**
     * Gets a stream of zoom level changes.
     *
     * @return Flow that emits zoom level updates
     */
    fun getZoomLevelFlow(): Flow<Float>

    /**
     * Toggles haptic feedback on/off.
     */
    suspend fun toggleHapticFeedback()

    /**
     * Gets whether haptic feedback is enabled.
     *
     * @return True if haptic feedback is enabled, false otherwise
     */
    suspend fun isHapticFeedbackEnabled(): Boolean

    /**
     * Gets a stream of haptic feedback state changes.
     *
     * @return Flow that emits haptic feedback state
     */
    fun getHapticFeedbackFlow(): Flow<Boolean>

    // Utility Methods
    /**
     * Resets all settings to their default values.
     */
    suspend fun resetToDefaults()

    /**
     * Exports all settings to a JSON string (for backup).
     *
     * @return JSON representation of all settings
     */
    suspend fun exportSettings(): String

    /**
     * Imports settings from a JSON string.
     *
     * @param settingsJson JSON string containing settings
     * @return True if import was successful, false otherwise
     */
    suspend fun importSettings(settingsJson: String): Boolean

    /**
     * Gets whether this is the first time the app is launched.
     *
     * @return True if first launch, false otherwise
     */
    suspend fun isFirstLaunch(): Boolean

    /**
     * Marks that the first launch has been completed.
     */
    suspend fun markFirstLaunchCompleted()
}