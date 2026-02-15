package com.akinalpfdn.poprush.core.domain.model

/**
 * Intents related to user settings: audio, visual, and game configuration.
 */
sealed interface SettingsIntent : GameIntent {
    data class SelectShape(val shape: BubbleShape) : SettingsIntent
    data class UpdateZoom(val zoomLevel: Float) : SettingsIntent
    data object ZoomIn : SettingsIntent
    data object ZoomOut : SettingsIntent
    data object ToggleSettings : SettingsIntent
    data object ToggleSound : SettingsIntent
    data object ToggleMusic : SettingsIntent
    data class UpdateSoundVolume(val volume: Float) : SettingsIntent
    data class UpdateMusicVolume(val volume: Float) : SettingsIntent
    data class ChangeDifficulty(val difficulty: GameDifficulty) : SettingsIntent
    data class UpdateSelectedDuration(val duration: kotlin.time.Duration) : SettingsIntent
}
