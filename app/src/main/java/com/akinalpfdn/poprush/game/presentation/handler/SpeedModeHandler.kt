package com.akinalpfdn.poprush.game.presentation.handler

import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerEvent
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.flow.first

class SpeedModeHandler @Inject constructor(
    private val speedModeUseCase: SpeedModeUseCase,
    private val speedModeTimerUseCase: SpeedModeTimerUseCase,
    private val audioRepository: AudioRepository,
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository,
    private val initializeGameUseCase: com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
) {
    private lateinit var scope: CoroutineScope
    private lateinit var gameStateFlow: MutableStateFlow<GameState>
    private var speedModeCollectorJob: Job? = null

    fun init(scope: CoroutineScope, gameStateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.gameStateFlow = gameStateFlow
    }

    fun handleActivateRandomBubble(bubbleId: Int) {
        scope.launch {
            try {
                val currentState = gameStateFlow.value
                val updatedBubbles = speedModeUseCase.activateBubble(bubbleId, currentState.bubbles)

                gameStateFlow.update { it.copy(bubbles = updatedBubbles) }

                // Check if speed mode is game over
                if (speedModeUseCase.isGameOver(updatedBubbles)) {
                    handleSpeedModeGameOver()
                }

            } catch (e: Exception) {
                Timber.e(e, "Error activating bubble in speed mode")
            }
        }
    }

    fun handleUpdateSpeedModeInterval() {
        scope.launch {
            try {
                // This would typically be called by a timer tick
                // For now, just log the current interval
                val currentInterval = speedModeUseCase.speedModeState.value.currentInterval
            } catch (e: Exception) {
                Timber.e(e, "Error updating speed mode interval")
            }
        }
    }

    fun handleStartSpeedModeTimer() {
        // FIX: Check if job is active to prevent multiple listeners (The Freeze Fix)
        if (speedModeCollectorJob?.isActive == true) {
            return
        }

        scope.launch {
            try {
                // Wait briefly for UI transition
                kotlinx.coroutines.delay(500)

                speedModeUseCase.initializeSpeedMode()

                // Initialize bubbles invisible for speed mode
                val initialBubbles = initializeGameUseCase.execute().map { bubble ->
                    bubble.copy(
                        transparency = 0.0f,
                        isSpeedModeActive = false,
                        isActive = false,
                        isPressed = false
                    )
                }
                
                Timber.d("SpeedMode: Initialized ${initialBubbles.size} bubbles. Active: ${initialBubbles.count { it.isSpeedModeActive }}")

                gameStateFlow.update {
                    it.copy(
                        bubbles = initialBubbles,
                        isPlaying = true,
                        isGameOver = false,
                        isPaused = false
                    )
                }

                speedModeTimerUseCase.startTimer()

                // FIX: Assign job to property so we can track it
                speedModeCollectorJob = launch {
                    speedModeTimerUseCase.timerEvents.collect { event ->
                        Timber.d("SpeedMode: Received event $event")
                        when (event) {
                            is SpeedModeTimerEvent.ActivateBubble -> {
                                if (event.bubbleId == -1) {
                                    // Random selection needed
                                    val currentBubbles = gameStateFlow.value.bubbles
                                    Timber.d("SpeedMode: Selecting random bubble from ${currentBubbles.size} bubbles")
                                    
                                    val (bubbleId, _) = speedModeUseCase.selectRandomBubble(currentBubbles)

                                    bubbleId?.let { id ->
                                        Timber.d("SpeedMode: Selected random bubble $id")
                                        handleActivateRandomBubble(id)
                                    } ?: run {
                                        Timber.d("SpeedMode: No bubble selected (Game Over)")
                                        handleSpeedModeGameOver()
                                    }
                                } else {
                                    handleActivateRandomBubble(event.bubbleId)
                                }
                            }
                            is SpeedModeTimerEvent.GameOver -> {
                                Timber.d("SpeedMode: Timer sent GameOver event")
                                handleSpeedModeGameOver()
                            }
                            is SpeedModeTimerEvent.Tick -> {
                                // FIX: Only read total time. DO NOT call updateSpeedMode() here.
                                val totalElapsedTime = speedModeTimerUseCase.getElapsedTime()

                                gameStateFlow.update { currentState ->
                                    currentState.copy(
                                        timeRemaining = totalElapsedTime,
                                        score = (totalElapsedTime.inWholeSeconds).toInt(),
                                        speedModeState = speedModeUseCase.speedModeState.value
                                    )
                                }
                            }
                            else -> { /* Ignore others */ }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error starting speed mode timer")
            }
        }
    }

    fun handleResetSpeedModeState() {
        scope.launch {
            try {
                stopTimer()

                // Reset speed mode use case
                speedModeUseCase.resetSpeedMode()

                // Update game state
                gameStateFlow.update { currentState ->
                    currentState.copy(
                        speedModeState = speedModeUseCase.speedModeState.value,
                        bubbles = currentState.bubbles.map { bubble ->
                            bubble.copy(
                                transparency = 1.0f,
                                isSpeedModeActive = false,
                                isActive = false,
                                isPressed = false
                            )
                        }
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "Error resetting speed mode state")
            }
        }
    }

    fun handleSpeedModeGameOver() {
        scope.launch {
            try {
                stopTimer()

                val currentState = gameStateFlow.value
                
                // Create game result
                val gameResult = GameResult(
                    finalScore = (currentState.timeRemaining.inWholeSeconds).toInt(),
                    levelsCompleted = 0, // Speed mode doesn't have levels
                    totalBubblesPressed = currentState.pressedBubbleCount,
                    accuracyPercentage = 100f, // Simplified
                    averageTimePerLevel = Duration.ZERO,
                    gameDuration = currentState.timeRemaining, // Time survived
                    isHighScore = (currentState.timeRemaining.inWholeSeconds).toInt() > currentState.highScore
                )
                
                gameRepository.saveGameResult(gameResult)
                
                if (gameResult.isHighScore) {
                    gameRepository.updateHighScore(gameResult.finalScore)
                    audioRepository.playSound(SoundType.HIGH_SCORE)
                } else {
                    audioRepository.playSound(SoundType.GAME_OVER)
                }

                // Final score and mark game as over
                gameStateFlow.update { state ->
                    state.copy(
                        isGameOver = true,
                        isPlaying = false,
                        score = (state.timeRemaining.inWholeSeconds).toInt(), // Final score = seconds survived
                        speedModeState = speedModeUseCase.speedModeState.value.copy(isGameOver = true)
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "Error handling speed mode game over")
            }
        }
    }
    
    fun handleBubblePress(bubbleId: Int) {
        scope.launch {
            try {
                val currentState = gameStateFlow.value
                val bubble = currentState.bubbles.find { it.id == bubbleId }

                if (bubble != null && bubble.isSpeedModeActive) {
                    // Deactivate the bubble (make it transparent again)
                    val updatedBubbles = speedModeUseCase.deactivateBubble(bubbleId, currentState.bubbles)
                    
                    // Get haptic feedback setting
                    val hapticFeedback = settingsRepository.getHapticFeedbackFlow().first()

                    gameStateFlow.update {
                        it.copy(
                            bubbles = updatedBubbles,
                            score = currentState.score + 1 // Increment score for speed mode
                        )
                    }

                    // Play sound with haptic feedback
                    audioRepository.playSoundWithHaptic(
                        SoundType.BUBBLE_PRESS,
                        hapticFeedback
                    )

                    Timber.d("Speed mode bubble $bubbleId deactivated")
                } else {
                    Timber.d("Speed mode bubble $bubbleId not active for deactivation")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling bubble press for bubble $bubbleId")
            }
        }
    }

    fun stopTimer() {
        speedModeTimerUseCase.stopTimer()
        speedModeCollectorJob?.cancel()
        speedModeCollectorJob = null
    }

    fun pauseTimer() {
        speedModeTimerUseCase.pauseTimer()
    }

    fun resumeTimer() {
        speedModeTimerUseCase.resumeTimer()
    }
    
    fun cleanup() {
        stopTimer()
        speedModeTimerUseCase.cleanup()
        speedModeUseCase.resetSpeedMode()
    }
}
