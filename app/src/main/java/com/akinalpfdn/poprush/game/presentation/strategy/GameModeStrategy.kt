package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Strategy interface for game mode implementations.
 * Each game mode (Classic, Speed, Coop, etc.) implements this interface
 * to provide its own game logic while keeping the GameViewModel mode-agnostic.
 */
interface GameModeStrategy {

    /**
     * Unique identifier for this game mode.
     */
    val modeId: String

    /**
     * Human-readable name for this mode.
     */
    val modeName: String

    /**
     * Initialize the strategy with coroutine scope and game state.
     * Called when the strategy is first created or switched to.
     */
    suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>)

    /**
     * Start a new game session in this mode.
     */
    suspend fun startGame()

    /**
     * Handle a bubble press event.
     * @param bubbleId The ID of the pressed bubble
     */
    suspend fun handleBubblePress(bubbleId: Int)

    /**
     * Pause the current game session.
     */
    suspend fun pauseGame()

    /**
     * Resume the paused game session.
     */
    suspend fun resumeGame()

    /**
     * End the current game session and save results.
     */
    suspend fun endGame()

    /**
     * Reset the mode to its initial state.
     */
    suspend fun resetGame()

    /**
     * Clean up resources when switching away from this mode.
     */
    fun cleanup()

    /**
     * Get mode-specific configuration data.
     */
    fun getConfig(): GameModeConfig
}
