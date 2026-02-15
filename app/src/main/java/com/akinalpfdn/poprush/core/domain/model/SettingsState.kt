package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents the user's audio and visual settings.
 */
data class SettingsState(
    val selectedShape: BubbleShape = BubbleShape.CIRCLE,
    val zoomLevel: Float = 1.0f,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 0.7f
)
