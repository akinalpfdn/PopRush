package com.akinalpfdn.poprush.game.presentation.strategy

/**
 * Interface for game modes that support pause/resume functionality.
 * Classic and Speed modes implement this; Coop does not (no pause in multiplayer).
 */
interface PausableGameMode {
    suspend fun pauseGame()
    suspend fun resumeGame()
}
