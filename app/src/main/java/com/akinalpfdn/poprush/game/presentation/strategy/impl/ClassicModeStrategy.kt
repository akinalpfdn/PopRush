package com.akinalpfdn.poprush.game.presentation.strategy.impl

import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.game.presentation.strategy.BaseGameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeConfig
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import com.akinalpfdn.poprush.game.presentation.strategy.PausableGameMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Strategy implementation for Classic game mode.
 *
 * Scoring:
 * - Each wave awards points equal to the number of bubbles popped.
 * - Completing a wave faster than the target time (BASE_TIME_PER_BUBBLE_MS × bubbleCount)
 *   awards 1.5× points instead.
 */
class ClassicModeStrategy(
    dependencies: GameModeDependencies
) : BaseGameModeStrategy(dependencies), PausableGameMode {

    override val modeId: String = "classic"
    override val modeName: String = "Classic Mode"

    private val config = GameModeConfig.classic()

    private var waveStartTimeMs: Long = 0L
    private var speedBonusResetJob: Job? = null

    override suspend fun startGame() {
        dependencies.timerUseCase.stopTimer()

        val currentState = stateFlow.value
        val newBubbles = dependencies.initializeGameUseCase.execute()
        val difficulty = dependencies.settingsRepository.getGameDifficultyFlow().first()

        val activeBubbles = dependencies.generateLevelUseCase.execute(
            currentBubbles = newBubbles,
            difficulty = difficulty,
            currentLevel = 1
        )

        val selectedDuration = currentState.selectedDuration

        stateFlow.update {
            it.copy(
                isPlaying = true,
                isGameOver = false,
                isPaused = false,
                score = 0,
                timeRemaining = selectedDuration,
                currentLevel = 1,
                bubbles = activeBubbles,
                showSpeedBonus = false,
                speedBonusPoints = 0
            )
        }

        waveStartTimeMs = System.currentTimeMillis()

        dependencies.timerUseCase.startTimer(selectedDuration)
        collectTimerState()

        Timber.d("Classic mode game started with ${activeBubbles.count { it.isActive }} active bubbles")
        dependencies.audioRepository.playSound(SoundType.BUTTON_PRESS)
    }

    private fun collectTimerState() {
        scope.launch {
            scope.launch {
                dependencies.timerUseCase.timeRemaining.collect { timeRemaining ->
                    stateFlow.update { it.copy(timeRemaining = timeRemaining) }
                }
            }

            scope.launch {
                dependencies.timerUseCase.timerState.collect { timerState ->
                    if (timerState == com.akinalpfdn.poprush.game.domain.usecase.TimerState.FINISHED) {
                        endGame()
                    }
                }
            }
        }
    }

    override suspend fun handleBubblePress(bubbleId: Int) {
        val currentState = stateFlow.value
        val result = dependencies.handleBubblePressUseCase.execute(
            bubbles = currentState.bubbles,
            bubbleId = bubbleId
        )

        if (result.success) {
            stateFlow.update { it.copy(bubbles = result.updatedBubbles) }

            val hapticFeedback = dependencies.settingsRepository.getHapticFeedbackFlow().first()
            dependencies.audioRepository.playSoundWithHaptic(SoundType.BUBBLE_PRESS, hapticFeedback)

            if (result.isLevelComplete) {
                val activeBubbleCount = currentState.bubbles.count { it.isActive }
                val elapsedMs = System.currentTimeMillis() - waveStartTimeMs
                val targetMs = BASE_TIME_PER_BUBBLE_MS * activeBubbleCount
                val isFast = elapsedMs < targetMs

                val points = if (isFast) {
                    (activeBubbleCount * SPEED_BONUS_MULTIPLIER).toInt()
                } else {
                    activeBubbleCount
                }

                stateFlow.update { it.copy(score = it.score + points) }

                if (isFast) {
                    showSpeedBonusAnimation(points)
                }

                dependencies.audioRepository.playSound(SoundType.LEVEL_COMPLETE)

                delay(200)
                generateNewLevel()
            }
        }
    }

    private fun showSpeedBonusAnimation(points: Int) {
        speedBonusResetJob?.cancel()
        stateFlow.update {
            it.copy(showSpeedBonus = true, speedBonusPoints = points)
        }
        speedBonusResetJob = scope.launch {
            delay(SPEED_BONUS_DISPLAY_MS)
            stateFlow.update { it.copy(showSpeedBonus = false) }
        }
    }

    private fun generateNewLevel() {
        scope.launch {
            val currentState = stateFlow.value
            val difficulty = dependencies.settingsRepository.getGameDifficultyFlow().first()
            val nextLevel = currentState.currentLevel + 1

            val newBubbles = dependencies.generateLevelUseCase.execute(
                currentBubbles = currentState.bubbles,
                difficulty = difficulty,
                currentLevel = nextLevel
            )

            stateFlow.update {
                it.copy(
                    bubbles = newBubbles,
                    currentLevel = nextLevel
                )
            }

            waveStartTimeMs = System.currentTimeMillis()
            Timber.d("Generated new level $nextLevel with ${newBubbles.count { it.isActive }} active bubbles")
        }
    }

    override suspend fun pauseGame() {
        dependencies.timerUseCase.pauseTimer()
    }

    override suspend fun resumeGame() {
        dependencies.timerUseCase.resumeTimer()
    }

    override suspend fun endGame() {
        val currentState = stateFlow.value
        dependencies.timerUseCase.stopTimer()
        speedBonusResetJob?.cancel()

        val gameResult = createGameResult(currentState)
        saveAndAnnounceResult(gameResult)
        markGameOver()

        Timber.d("Classic game ended with score: ${currentState.score}")
    }

    override suspend fun resetGame() {
        dependencies.timerUseCase.stopTimer()
        speedBonusResetJob?.cancel()
        resetStateToDefaults()
        Timber.d("Classic game reset to initial state")
    }

    override fun cleanup() {
        if (isInitialized) {
            dependencies.timerUseCase.stopTimer()
            speedBonusResetJob?.cancel()
        }
    }

    override fun getConfig(): GameModeConfig = config

    private fun createGameResult(gameState: GameState) = GameResult(
        finalScore = gameState.score,
        levelsCompleted = gameState.currentLevel - 1,
        totalBubblesPressed = gameState.pressedBubbleCount,
        accuracyPercentage = if (gameState.pressedBubbleCount > 0) 100f else 0f,
        averageTimePerLevel = if (gameState.currentLevel > 1) {
            (GameState.GAME_DURATION - gameState.timeRemaining) / (gameState.currentLevel - 1)
        } else GameState.GAME_DURATION,
        gameDuration = GameState.GAME_DURATION - gameState.timeRemaining,
        isHighScore = gameState.score > gameState.highScore
    )

    companion object {
        const val BASE_TIME_PER_BUBBLE_MS = 400L
        const val SPEED_BONUS_MULTIPLIER = 1.5f
        const val SPEED_BONUS_DISPLAY_MS = 1500L
    }
}
