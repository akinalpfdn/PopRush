package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents a single bubble in the game grid.
 *
 * @param id Unique identifier for the bubble (0-43)
 * @param position Position in the linear array (0-43)
 * @param row Row in the hexagonal grid (0-6)
 * @param col Column within the row (varies by row)
 * @param color The pastel color of the bubble
 * @param isActive Whether the bubble is currently lit/active for tapping (classic mode)
 * @param isPressed Whether the bubble has been pressed in the current level
 * @param transparency Transparency level for speed mode (0.0f = fully transparent, 1.0f = fully opaque)
 * @param isSpeedModeActive Whether the bubble is active in speed mode
 */
data class Bubble(
    val id: Int,
    val position: Int,
    val row: Int,
    val col: Int,
    val color: BubbleColor,
    val isActive: Boolean = false,
    val isPressed: Boolean = false,
    val transparency: Float = 1.0f,
    val isSpeedModeActive: Boolean = false
) {
    /**
     * Checks if this bubble should be visible and interactive.
     */
    val isVisible: Boolean
        get() = transparency > 0.0f // Only visible if not fully transparent

    /**
     * Checks if this bubble can be pressed (active and not already pressed).
     * Works for both classic mode and speed mode.
     */
    val canBePressed: Boolean
        get() = (isActive || isSpeedModeActive) && !isPressed

    /**
     * Checks if this bubble should be shown as active (for visual rendering).
     */
    val isVisuallyActive: Boolean
        get() = isActive || isSpeedModeActive

    /**
     * Gets the effective transparency for rendering.
     */
    val effectiveTransparency: Float
        get() = if (isSpeedModeActive) transparency else 1.0f
}

/**
 * Enum representing the different pastel colors available for bubbles.
 * Each color has corresponding pressed variants for visual feedback.
 */
enum class BubbleColor {
    ROSE,
    SKY,
    EMERALD,
    AMBER,
    VIOLET
}