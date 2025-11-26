package com.akinalpfdn.poprush.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the complete state of the PopRush game.
 * This is an immutable data class that represents a single point in time of the game.
 *
 * @param isPlaying Whether the game is currently active and running
 * @param isGameOver Whether the game has ended (time ran out)
 * @param isPaused Whether the game is currently paused
 * @param score Current score in the active game
 * @param highScore Highest score achieved across all games
 * @param timeRemaining Time left in the current game session
 * @param currentLevel Current level number (starts at 1)
 * @param bubbles List of all bubbles in the grid with their current states
 * @param selectedShape Currently selected bubble shape for visual rendering
 * @param zoomLevel Current zoom level for the game board (0.5f to 1.5f)
 * @param showSettings Whether the settings dropdown is currently visible
 * @param soundEnabled Whether sound effects are enabled
 * @param musicEnabled Whether background music is enabled
 * @param soundVolume Current sound effects volume (0.0f to 1.0f)
 * @param musicVolume Current music volume (0.0f to 1.0f)
 */
data class GameState(
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val score: Int = 0,
    val highScore: Int = 0,
    val timeRemaining: Duration = GAME_DURATION,
    val currentLevel: Int = 1,
    val bubbles: List<Bubble> = emptyList(),
    val selectedShape: BubbleShape = BubbleShape.CIRCLE,
    val zoomLevel: Float = 1.0f,
    val showSettings: Boolean = false,
    val showBackConfirmation: Boolean = false,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 0.7f
) {
    /**
     * Returns the number of currently active bubbles that can be pressed.
     */
    val activeBubbleCount: Int
        get() = bubbles.count { it.canBePressed }

    /**
     * Returns the total number of bubbles that have been pressed in the current level.
     */
    val pressedBubbleCount: Int
        get() = bubbles.count { it.isPressed }

    /**
     * Checks if the current level is complete (all active bubbles have been pressed).
     */
    val isLevelComplete: Boolean
        get() = bubbles.any { it.isActive } &&
                 bubbles.filter { it.isActive }.all { it.isPressed }

    /**
     * Returns whether the timer is in critical state (less than 10 seconds).
     */
    val isTimerCritical: Boolean
        get() = timeRemaining <= CRITICAL_TIME_THRESHOLD

    /**
     * Returns the time formatted as MM:SS string for display.
     */
    val timeDisplay: String
        get() = String.format("%02d:%02d",
            timeRemaining.inWholeMinutes,
            timeRemaining.inWholeSeconds % 60)

    companion object {
        /**
         * Total game duration in seconds.
         */
        val GAME_DURATION: Duration = 5.seconds

        /**
         * Time threshold when timer becomes critical (2 seconds).
         */
        val CRITICAL_TIME_THRESHOLD: Duration = 2.seconds

        /**
         * Minimum zoom level allowed.
         */
        const val MIN_ZOOM_LEVEL: Float = 0.5f

        /**
         * Maximum zoom level allowed.
         */
        const val MAX_ZOOM_LEVEL: Float = 1.5f

        /**
         * Number of bubbles in each row of the hexagonal grid.
         */
        val ROW_SIZES: List<Int> = listOf(5, 6, 7, 8, 7, 6, 5)

        /**
         * Total number of bubbles in the grid.
         */
        val TOTAL_BUBBLES: Int = ROW_SIZES.sum()
    }
}