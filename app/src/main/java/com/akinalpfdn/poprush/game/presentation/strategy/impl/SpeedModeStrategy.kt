package com.akinalpfdn.poprush.game.presentation.strategy.impl

import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerEvent
import com.akinalpfdn.poprush.game.presentation.strategy.BaseGameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeConfig
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import com.akinalpfdn.poprush.game.presentation.strategy.PausableGameMode
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
 */
class SpeedModeStrategy(
    dependencies: GameModeDependencies
) : BaseGameModeStrategy(dependencies), PausableGameMode {

    override val modeId: String = "speed"
    override val modeName: String = "Speed Mode"

    private val config = GameModeConfig.speed()
    private var speedModeCollectorJob: Job? = null

    override suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>) {
        super.initialize(scope, stateFlow)
    }

    override suspend fun startGame() {
        dependencies.speedModeTimerUseCase.stopTimer()
        speedModeCollectorJob?.cancel()
        speedModeCollectorJob = null

        kotlinx.coroutines.delay(500)

        dependencies.speedModeUseCase.initializeSpeedMode()

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
                timeRemaining = Duration.ZERO,
                score = 0
            )
        }

        dependencies.speedModeTimerUseCase.startTimer()

        speedModeCollectorJob = scope.launch {
            dependencies.speedModeTimerUseCase.timerEvents.collect { event ->
                Timber.d("SpeedMode: Received event $event")
                when (event) {
                    is SpeedModeTimerEvent.ActivateBubble -> {
                        if (event.bubbleId == -1) {
                            val currentBubbles = stateFlow.value.bubbles
                            val (bubbleId, _) = dependencies.speedModeUseCase.selectRandomBubble(currentBubbles)

                            bubbleId?.let { id ->
                                activateRandomBubble(id)
                            } ?: run {
                                handleSpeedModeGameOver()
                            }
                        } else {
                            activateRandomBubble(event.bubbleId)
                        }
                    }
                    is SpeedModeTimerEvent.GameOver -> {
                        handleSpeedModeGameOver()
                    }
                    is SpeedModeTimerEvent.Tick -> {
                        val totalElapsedTime = dependencies.speedModeTimerUseCase.getElapsedTime()

                        stateFlow.update { currentState ->
                            currentState.copy(
                                timeRemaining = totalElapsedTime,
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
            val updatedBubbles = dependencies.speedModeUseCase.deactivateBubble(bubbleId, currentState.bubbles)
            val hapticFeedback = dependencies.settingsRepository.getHapticFeedbackFlow().first()

            stateFlow.update {
                it.copy(
                    bubbles = updatedBubbles,
                    score = currentState.score + 1
                )
            }

            dependencies.audioRepository.playSoundWithHaptic(SoundType.BUBBLE_PRESS, hapticFeedback)
            Timber.d("Speed mode bubble $bubbleId deactivated")
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
        dependencies.speedModeTimerUseCase.stopTimer()
        speedModeCollectorJob?.cancel()
        speedModeCollectorJob = null

        val currentState = stateFlow.value

        val gameResult = GameResult(
            finalScore = (currentState.timeRemaining.inWholeSeconds).toInt(),
            levelsCompleted = 0,
            totalBubblesPressed = currentState.pressedBubbleCount,
            accuracyPercentage = 100f,
            averageTimePerLevel = Duration.ZERO,
            gameDuration = currentState.timeRemaining,
            isHighScore = (currentState.timeRemaining.inWholeSeconds).toInt() > currentState.highScore
        )

        saveAndAnnounceResult(gameResult)

        stateFlow.update { state ->
            state.copy(
                isGameOver = true,
                isPlaying = false,
                score = (state.timeRemaining.inWholeSeconds).toInt(),
                speedModeState = dependencies.speedModeUseCase.speedModeState.value.copy(isGameOver = true)
            )
        }
    }

    override suspend fun resetGame() {
        dependencies.speedModeTimerUseCase.stopTimer()
        speedModeCollectorJob?.cancel()
        speedModeCollectorJob = null
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
        dependencies.speedModeTimerUseCase.stopTimer()
        speedModeCollectorJob?.cancel()
        speedModeCollectorJob = null
    }

    override fun getConfig(): GameModeConfig = config
}
