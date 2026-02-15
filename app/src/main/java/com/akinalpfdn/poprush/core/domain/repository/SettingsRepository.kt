package com.akinalpfdn.poprush.core.domain.repository

/**
 * Repository interface for managing user settings and preferences.
 * Composes all settings sub-interfaces for backward compatibility.
 *
 * New consumers should depend on the specific sub-interface they need:
 * - [AudioSettingsRepository] for sound/music settings
 * - [VisualSettingsRepository] for visual preferences
 * - [GameSettingsRepository] for game configuration
 * - [PlayerProfileRepository] for player identity
 */
interface SettingsRepository : AudioSettingsRepository, VisualSettingsRepository,
    GameSettingsRepository, PlayerProfileRepository {

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
}
