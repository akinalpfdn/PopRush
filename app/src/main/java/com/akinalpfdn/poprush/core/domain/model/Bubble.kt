package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents a single bubble in the game grid.
 *
 * @param id Unique identifier for the bubble (0-43)
 * @param position Position in the linear array (0-43)
 * @param row Row in the hexagonal grid (0-6)
 * @param col Column within the row (varies by row)
 * @param color The pastel color of the bubble
 * @param isActive Whether the bubble is currently lit/active for tapping
 * @param isPressed Whether the bubble has been pressed in the current level
 */
data class Bubble(
    val id: Int,
    val position: Int,
    val row: Int,
    val col: Int,
    val color: BubbleColor,
    val isActive: Boolean = false,
    val isPressed: Boolean = false
) {
    /**
     * Checks if this bubble should be visible and interactive.
     */
    val isVisible: Boolean
        get() = true // All bubbles are visible in the grid

    /**
     * Checks if this bubble can be pressed (active and not already pressed).
     */
    val canBePressed: Boolean
        get() = isActive && !isPressed
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