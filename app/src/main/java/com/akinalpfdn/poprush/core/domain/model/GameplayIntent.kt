package com.akinalpfdn.poprush.core.domain.model

/**
 * Intents related to core gameplay: starting, stopping, scoring, and bubble interactions.
 */
sealed interface GameplayIntent : GameIntent {
    data object StartGame : GameplayIntent
    data object EndGame : GameplayIntent
    data object TogglePause : GameplayIntent
    data object RestartGame : GameplayIntent
    data object ResetGame : GameplayIntent
    data object LoadGameData : GameplayIntent
    data object SaveGameData : GameplayIntent
    data class PressBubble(val bubbleId: Int) : GameplayIntent
    data class UpdateHighScore(val newHighScore: Int) : GameplayIntent
    data class UpdateTimer(val timeRemaining: kotlin.time.Duration) : GameplayIntent
    data object GenerateNewLevel : GameplayIntent

    // Speed mode specific
    data class ActivateRandomBubble(val bubbleId: Int) : GameplayIntent
    data object UpdateSpeedModeInterval : GameplayIntent
    data object StartSpeedModeTimer : GameplayIntent
    data object ResetSpeedModeState : GameplayIntent
}
