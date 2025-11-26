package com.akinalpfdn.poprush.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a user setting in the database.
 * Stores key-value pairs for various game settings.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    /**
     * Unique key identifying the setting.
     */
    val key: String,

    /**
     * The value of the setting stored as a string.
     * Can be converted to appropriate type when needed.
     */
    val value: String,

    /**
     * When this setting was last updated.
     */
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        // Setting keys
        const val KEY_BUBBLE_SHAPE = "bubble_shape"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_MUSIC_ENABLED = "music_enabled"
        const val KEY_SOUND_VOLUME = "sound_volume"
        const val KEY_MUSIC_VOLUME = "music_volume"
        const val KEY_ZOOM_LEVEL = "zoom_level"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        const val KEY_GAME_DIFFICULTY = "game_difficulty"
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_TOTAL_GAMES_PLAYED = "total_games_played"

        // Default values
        const val DEFAULT_BUBBLE_SHAPE = "CIRCLE"
        const val DEFAULT_SOUND_ENABLED = "true"
        const val DEFAULT_MUSIC_ENABLED = "true"
        const val DEFAULT_SOUND_VOLUME = "1.0"
        const val DEFAULT_MUSIC_VOLUME = "0.7"
        const val DEFAULT_ZOOM_LEVEL = "1.0"
        const val DEFAULT_HAPTIC_FEEDBACK = "true"
        const val DEFAULT_GAME_DIFFICULTY = "NORMAL"
        const val DEFAULT_FIRST_LAUNCH = "true"
        const val DEFAULT_TOTAL_GAMES_PLAYED = "0"

        /**
         * Creates a SettingsEntity for a boolean value.
         */
        fun fromBoolean(key: String, value: Boolean): SettingsEntity {
            return SettingsEntity(key = key, value = value.toString())
        }

        /**
         * Creates a SettingsEntity for a float value.
         */
        fun fromFloat(key: String, value: Float): SettingsEntity {
            return SettingsEntity(key = key, value = value.toString())
        }

        /**
         * Creates a SettingsEntity for an int value.
         */
        fun fromInt(key: String, value: Int): SettingsEntity {
            return SettingsEntity(key = key, value = value.toString())
        }

        /**
         * Creates a SettingsEntity for a string value.
         */
        fun fromString(key: String, value: String): SettingsEntity {
            return SettingsEntity(key = key, value = value)
        }
    }

    /**
     * Gets the value as a boolean.
     */
    fun getAsBoolean(): Boolean {
        return value.toBooleanStrictOrNull() ?: false
    }

    /**
     * Gets the value as a float.
     */
    fun getAsFloat(): Float {
        return value.toFloatOrNull() ?: 0.0f
    }

    /**
     * Gets the value as an int.
     */
    fun getAsInt(): Int {
        return value.toIntOrNull() ?: 0
    }

    /**
     * Gets the value as a long.
     */
    fun getAsLong(): Long {
        return value.toLongOrNull() ?: 0L
    }
}