package com.akinalpfdn.poprush.game.presentation.strategy.impl

import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerEvent
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeConfig
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration

/**
 * Strategy implementation for Speed game mode.
 * Bubbles light up randomly and the player must tap them quickly.
 * Score is based on survival time and bubbles pressed.
 */
class SpeedModeStrategy(
    private val dependencies: GameModeDependencies
) : GameModeStrategy {

    override val modeId: String = "speed"
    override val modeName: String = "Speed Mode"

    private lateinit var scope: CoroutineScope
    private lateinit var stateFlow: MutableStateFlow<GameState>

    private val config = GameModeConfig.speed()
    private var speedModeCollectorJob: Job? = null

    override suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.stateFlow = stateFlow
        Timber.d("SpeedModeStrategy initialized")
    }

    override suspend fun startGame() {
        // Check if job is active to prevent multiple listeners
        if (speedModeCollectorJob?.isActive == true) {
            return
        }

        // Wait briefly for UI transition
        kotlinx.coroutines.delay(500)

        dependencies.speedModeUseCase.initializeSpeedMode()

        // Initialize bubbles invisible for speed mode
        val initialBubbles = dependencies.initializeGameUseCase.execute().map { bubble ->
            bubble.copy(
                transparency = 0.0f,
                isSpeedModeActive = false,
                isActive = false,
                isPressed = false
            )
        }

        Timber.d("SpeedMode: Initialized ${initialBubbles.size} bubbles")

        stateFlow.update {
            it.copy(
                bubbles = initialBubbles,
                isPlaying = true,
                isGameOver = false,
                isPaused = false,
                timeRemaining = Duration.ZERO,  // Speed mode uses elapsed time, not remaining
                score = 0
            )
        }

        dependencies.speedModeTimerUseCase.startTimer()

        // Assign job to property so we can track it
        speedModeCollectorJob = scope.launch {
            dependencies.speedModeTimerUseCase.timerEvents.collect { event ->
                Timber.d("SpeedMode: Received event $event")
                when (event) {
                    is SpeedModeTimerEvent.ActivateBubble -> {
                        if (event.bubbleId == -1) {
                            // Random selection needed
                            val currentBubbles = stateFlow.value.bubbles
                            Timber.d("SpeedMode: Selecting random bubble from ${currentBubbles.size} bubbles")

                            val (bubbleId, _) = dependencies.speedModeUseCase.selectRandomBubble(currentBubbles)

                            bubbleId?.let { id ->
                                Timber.d("SpeedMode: Selected random bubble $id")
                                activateRandomBubble(id)
                            } ?: run {
                                Timber.d("SpeedMode: No bubble selected (Game Over)")
                                handleSpeedModeGameOver()
                            }
                        } else {
                            activateRandomBubble(event.bubbleId)
                        }
                    }
                    is SpeedModeTimerEvent.GameOver -> {
                        Timber.d("SpeedMode: Timer sent GameOver event")
                        handleSpeedModeGameOver()
                    }
                    is SpeedModeTimerEvent.Tick -> {
                        // Only read total time
                        val totalElapsedTime = dependencies.speedModeTimerUseCase.getElapsedTime()

                        stateFlow.update { currentState ->
                            currentState.copy(
                                timeRemaining = totalElapsedTime,  // Using as elapsed time
                                score = (totalElapsedTime.inWholeSeconds).toInt(),
                                speedModeState = dependencies.speedModeUseCase.speedModeState.value
                            )
                        }
                    }
                    else -> { /* Ignore others */ }
                }
            }
        }
    }

    private fun activateRandomBubble(bubbleId: Int) {
        scope.launch {
            try {
                val currentState = stateFlow.value
                val updatedBubbles = dependencies.speedModeUseCase.activateBubble(bubbleId, currentState.bubbles)

                stateFlow.update { it.copy(bubbles = updatedBubbles) }

                // Check if speed mode is game over
                if (dependencies.speedModeUseCase.isGameOver(updatedBubbles)) {
                    handleSpeedModeGameOver()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error activating bubble in speed mode")
            }
        }
    }

    override suspend fun handleBubblePress(bubbleId: Int) {
        val currentState = stateFlow.value
        val bubble = currentState.bubbles.find { it.id == bubbleId }

        if (bubble != null && bubble.isSpeedModeActive) {
            // Deactivate the bubble (make it transparent again)
            val updatedBubbles = dependencies.speedModeUseCase.deactivateBubble(bubbleId, currentState.bubbles)

            // Get haptic feedback setting
            val hapticFeedback = dependencies.settingsRepository.getHapticFeedbackFlow().first()

            stateFlow.update {
                it.copy(
                    bubbles = updatedBubbles,
                    score = currentState.score + 1 // Increment score for speed mode
                )
            }

            // Play sound with haptic feedback
            dependencies.audioRepository.playSoundWithHaptic(
                SoundType.BUBBLE_PRESS,
                hapticFeedback
            )

            Timber.d("Speed mode bubble $bubbleId deactivated")
        } else {
            Timber.d("Speed mode bubble $bubbleId not active for deactivation")
        }
    }

    override suspend fun pauseGame() {
        dependencies.speedModeTimerUseCase.pauseTimer()
    }

    override suspend fun resumeGame() {
        dependencies.speedModeTimerUseCase.resumeTimer()
    }

    override suspend fun endGame() {
        handleSpeedModeGameOver()
    }

    private suspend fun handleSpeedModeGameOver() {
        stopTimerInternal()

        val currentState = stateFlow.value

        // Create game result
        val gameResult = com.akinalpfdn.poprush.core.domain.model.GameResult(
            finalScore = (currentState.timeRemaining.inWholeSeconds).toInt(),
            levelsCompleted = 0, // Speed mode doesn't have levels
            totalBubblesPressed = currentState.pressedBubbleCount,
            accuracyPercentage = 100f, // Simplified
            averageTimePerLevel = Duration.ZERO,
            gameDuration = currentState.timeRemaining, // Time survived
            isHighScore = (currentState.timeRemaining.inWholeSeconds).toInt() > currentState.highScore
        )

        dependencies.gameRepository.saveGameResult(gameResult)

        if (gameResult.isHighScore) {
            dependencies.gameRepository.updateHighScore(gameResult.finalScore)
            dependencies.audioRepository.playSound(SoundType.HIGH_SCORE)
        } else {
            dependencies.audioRepository.playSound(SoundType.GAME_OVER)
        }

        // Final score and mark game as over
        stateFlow.update { state ->
            state.copy(
                isGameOver = true,
                isPlaying = false,
                score = (state.timeRemaining.inWholeSeconds).toInt(), // Final score = seconds survived
                speedModeState = dependencies.speedModeUseCase.speedModeState.value.copy(isGameOver = true)
            )
        }
    }

    override suspend fun resetGame() {
        stopTimerInternal()
        dependencies.speedModeUseCase.resetSpeedMode()

        stateFlow.update { currentState ->
            currentState.copy(
                speedModeState = dependencies.speedModeUseCase.speedModeState.value,
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
    }

    override fun cleanup() {
        if (this::scope.isInitialized) {
            scope.launch {
                stopTimerInternal()
            }
        }
        dependencies.speedModeTimerUseCase.cleanup()
        dependencies.speedModeUseCase.resetSpeedMode()
    }

    override fun getConfig(): GameModeConfig = config

    private fun stopTimer() {
        if (this::scope.isInitialized) {
            scope.launch { stopTimerInternal() }
        }
    }

    private suspend fun stopTimerInternal() {
        dependencies.speedModeTimerUseCase.stopTimer()
        speedModeCollectorJob?.cancel()
        speedModeCollectorJob = null
    }
}
