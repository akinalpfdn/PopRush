package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Strategy interface for game mode implementations.
 * Each game mode (Classic, Speed, Coop, etc.) implements this interface
 * to provide its own game logic while keeping the GameViewModel mode-agnostic.
 *
 * For modes that support pause/resume, also implement [PausableGameMode].
 */
interface GameModeStrategy {

    val modeId: String

    val modeName: String

    suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>)

    suspend fun startGame()

    suspend fun handleBubblePress(bubbleId: Int)

    suspend fun endGame()

    suspend fun resetGame()

    fun cleanup()

    fun getConfig(): GameModeConfig
}
