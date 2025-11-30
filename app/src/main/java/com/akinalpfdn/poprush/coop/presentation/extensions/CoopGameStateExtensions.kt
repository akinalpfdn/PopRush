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
 * Extension properties for easy access to coop game state from UI components
 */

// Use actual properties from CoopGameState
val CoopGameState.currentPhase: CoopGamePhase
    get() = gamePhase

val CoopGameState.localPlayerName: String
    get() = localPlayerName

val CoopGameState.localPlayerColor: BubbleColor
    get() = localPlayerColor

val CoopGameState.localPlayerScore: Int
    get() = localScore

val CoopGameState.remotePlayerName: String
    get() = opponentPlayerName

val CoopGameState.remotePlayerColor: BubbleColor
    get() = opponentPlayerColor

val CoopGameState.remotePlayerScore: Int
    get() = opponentScore

val CoopGameState.timeRemaining: Long
    get() {
        if (gameStartTime <= 0) return gameDuration // Return full duration if not started
        val elapsed = System.currentTimeMillis() - gameStartTime
        return (gameDuration - elapsed).coerceAtLeast(0L)
    }

/**
 * Converts CoopGameState to GameState for compatibility with existing UI components
 */
fun CoopGameState.toGameState(): GameState {
    val timeRemainingDuration = timeRemaining.milliseconds
    val convertedBubbles = bubbles.map { coopBubble ->
        com.akinalpfdn.poprush.core.domain.model.Bubble(
            id = coopBubble.id,
            position = coopBubble.position,
            row = coopBubble.row,
            col = coopBubble.col,
            color = coopBubble.owner?.let { ownerId ->
                // Convert owner ID back to color - for now, use player colors
                when (ownerId) {
                    localPlayerId -> localPlayerColor
                    opponentPlayerId -> opponentPlayerColor
                    else -> com.akinalpfdn.poprush.core.domain.model.BubbleColor.GRAY
                }
            } ?: com.akinalpfdn.poprush.core.domain.model.BubbleColor.GRAY,
            isActive = currentPhase == CoopGamePhase.PLAYING,
            isPressed = false, // In coop mode, bubbles are never "pressed" like in single player
            transparency = 1.0f, // Always opaque, unowned are gray
            isSpeedModeActive = currentPhase == CoopGamePhase.PLAYING
        )
    }

    return GameState(
        isPlaying = currentPhase == CoopGamePhase.PLAYING,
        isGameOver = currentPhase == CoopGamePhase.FINISHED,
        isPaused = currentPhase == CoopGamePhase.PAUSED,
        score = localScore, // Use local player's score as primary score
        highScore = maxOf(localScore, opponentScore),
        timeRemaining = timeRemainingDuration,
        currentLevel = 1,
        bubbles = convertedBubbles,
        selectedShape = com.akinalpfdn.poprush.core.domain.model.BubbleShape.CIRCLE,
        zoomLevel = 1.0f,
        showSettings = false,
        showBackConfirmation = false,
        selectedDuration = gameDuration.milliseconds,
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