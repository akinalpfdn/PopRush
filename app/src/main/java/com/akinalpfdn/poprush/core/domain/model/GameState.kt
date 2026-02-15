package com.akinalpfdn.poprush.core.domain.model

import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
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
 * @param gameMode Currently selected game mode (single or coop)
 * @param selectedMod Currently selected game mod (classic or speed)
 * @param currentScreen Current screen flow state for start screen navigation
 * @param speedModeState State management for speed mode gameplay
 * @param isCoopMode Whether the game is currently in coop mode
* @param coopState Coop-specific game state when isCoopMode is true
 */
data class GameState(
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val score: Int = 0,
    val highScores: Map<String, Int> = emptyMap(),
    val timeRemaining: Duration = GAME_DURATION,
    val currentLevel: Int = 1,
    val bubbles: List<Bubble> = emptyList(),
    val selectedShape: BubbleShape = BubbleShape.CIRCLE,
    val zoomLevel: Float = 1.0f,
    val showSettings: Boolean = false,
    val showBackConfirmation: Boolean = false,
    val selectedDuration: Duration = 30.seconds, // Default 30 seconds
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 0.7f,
    val gameMode: GameMode = GameMode.SINGLE,
    val selectedMod: GameMod = GameMod.CLASSIC,
    val currentScreen: StartScreenFlow = StartScreenFlow.MODE_SELECTION,
    val speedModeState: SpeedModeState = SpeedModeState(),
    val isCoopMode: Boolean = false,
    val coopState: CoopGameState? = null,
    val showCoopConnectionDialog: Boolean = false,
    val coopErrorMessage: String? = null,
    val showSpeedBonus: Boolean = false,
    val speedBonusPoints: Int = 0
) {
    // --- Composite sub-state views (read-only, for gradual migration) ---

    /** Gameplay-related state grouped together. */
    val playState: PlayState
        get() = PlayState(isPlaying, isGameOver, isPaused, score, highScore, timeRemaining, currentLevel, bubbles)

    /** Audio and visual settings grouped together. */
    val settingsState: SettingsState
        get() = SettingsState(selectedShape, zoomLevel, soundEnabled, musicEnabled, soundVolume, musicVolume)

    /** UI navigation and dialog state grouped together. */
    val uiState: UiState
        get() = UiState(showSettings, showBackConfirmation, currentScreen, selectedDuration)

    /** Game mode and coop state grouped together. */
    val modeState: ModeState
        get() = ModeState(gameMode, selectedMod, speedModeState, isCoopMode, coopState, showCoopConnectionDialog, coopErrorMessage)

    /** High score for the currently selected game mod. */
    val highScore: Int
        get() = highScores[selectedMod.modKey] ?: 0

    // --- Computed gameplay properties ---

    val activeBubbleCount: Int
        get() = bubbles.count { it.canBePressed }

    val pressedBubbleCount: Int
        get() = bubbles.count { it.isPressed }

    val isLevelComplete: Boolean
        get() = bubbles.any { it.isActive } &&
                 bubbles.filter { it.isActive }.all { it.isPressed }

    val isTimerCritical: Boolean
        get() = timeRemaining <= CRITICAL_TIME_THRESHOLD

    val timeDisplay: String
        get() = String.format("%02d:%02d",
            timeRemaining.inWholeMinutes,
            timeRemaining.inWholeSeconds % 60)

    companion object {
        /**
         * Total game duration in seconds.
         */
        val GAME_DURATION: Duration = 60.seconds

        /**
         * Time threshold when timer becomes critical (10 seconds).
         */
        val CRITICAL_TIME_THRESHOLD: Duration = 10.seconds

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