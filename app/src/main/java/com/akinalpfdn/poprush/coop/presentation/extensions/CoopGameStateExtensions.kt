package com.akinalpfdn.poprush.coop.presentation.extensions

import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopBubble
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Extension functions for CoopGameState
 */

/**
 * Placeholder extension properties for CoopGameState
 * These will be replaced with actual properties when CoopGameState is fully implemented in Phase 4
 */

// Placeholder properties - will be implemented in Phase 4
val CoopGameState.currentPhase: CoopGamePhase
    get() = CoopGamePhase.WAITING // TODO: Implement actual phase tracking

val CoopGameState.localPlayerName: String
    get() = "Player" // TODO: Implement actual player name tracking

val CoopGameState.localPlayerColor: BubbleColor
    get() = BubbleColor.ROSE // TODO: Implement actual player color tracking

val CoopGameState.localPlayerScore: Int
    get() = 0 // TODO: Implement actual score tracking

val CoopGameState.remotePlayerName: String
    get() = "" // TODO: Implement actual remote player name tracking

val CoopGameState.remotePlayerColor: BubbleColor
    get() = BubbleColor.SKY // TODO: Implement actual remote player color tracking

val CoopGameState.remotePlayerScore: Int
    get() = 0 // TODO: Implement actual remote score tracking

val CoopGameState.timeRemaining: Long
    get() = 60_000L // TODO: Implement actual time remaining tracking (in milliseconds)

val CoopGameState.bubbles: List<CoopBubble>
    get() = emptyList() // TODO: Implement actual bubble tracking

/**
 * Converts CoopGameState to GameState for compatibility with existing UI components
 * Note: This is a placeholder implementation for Phase 3. Phase 4 will implement the actual CoopGameState.
 */
fun CoopGameState.toGameState(): GameState {
    // Since CoopGameState is not fully implemented yet, return a placeholder GameState
    // This will be replaced with proper implementation in Phase 4
    return GameState(
        isPlaying = false, // TODO: Use currentPhase when implemented
        isGameOver = false, // TODO: Use currentPhase when implemented
        isPaused = false,
        score = 0, // TODO: Use localPlayerScore when implemented
        highScore = 0, // TODO: Use maxOf(localPlayerScore, remotePlayerScore) when implemented
        timeRemaining = 60_000.milliseconds, // TODO: Use timeRemaining when implemented
        currentLevel = 1,
        bubbles = emptyList(), // TODO: Convert coopBubbles when implemented
        selectedShape = BubbleShape.CIRCLE,
        zoomLevel = 1.0f,
        showSettings = false,
        showBackConfirmation = false,
        selectedDuration = 30_000.milliseconds,
        soundEnabled = true,
        musicEnabled = true,
        soundVolume = 1.0f,
        musicVolume = 0.7f,
        gameMode = GameMode.COOP,
        selectedMod = com.akinalpfdn.poprush.core.domain.model.GameMod.CLASSIC,
        currentScreen = StartScreenFlow.GAME_SETUP,
        speedModeState = com.akinalpfdn.poprush.core.domain.model.SpeedModeState(),
        isCoopMode = true,
        coopState = this,
        showCoopConnectionDialog = false,
        coopErrorMessage = null
    )
}

/**
 * Helper function to convert bubble position to grid row
 */
private fun getRowForPosition(position: Int): Int {
    val rowSizes = listOf(5, 6, 7, 8, 7, 6, 5)
    var currentPos = 0
    for ((rowIndex, size) in rowSizes.withIndex()) {
        if (currentPos + size > position) {
            return rowIndex
        }
        currentPos += size
    }
    return 0
}

/**
 * Helper function to convert bubble position to grid column
 */
private fun getColForPosition(position: Int): Int {
    val rowSizes = listOf(5, 6, 7, 8, 7, 6, 5)
    var currentPos = 0
    for (size in rowSizes) {
        if (currentPos + size > position) {
            return position - currentPos
        }
        currentPos += size
    }
    return 0
}