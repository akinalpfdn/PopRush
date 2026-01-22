package com.akinalpfdn.poprush.game.presentation.strategy.impl

import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeConfig
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Strategy implementation for Classic game mode.
 * Wraps the existing GameLogicHandler functionality.
 */
class ClassicModeStrategy(
    private val dependencies: GameModeDependencies
) : GameModeStrategy {

    override val modeId: String = "classic"
    override val modeName: String = "Classic Mode"

    private lateinit var scope: CoroutineScope
    private lateinit var stateFlow: MutableStateFlow<GameState>

    private val config = GameModeConfig.classic()

    override suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.stateFlow = stateFlow
        Timber.d("ClassicModeStrategy initialized")
    }

    override suspend fun startGame() {
        val currentState = stateFlow.value
        val newBubbles = dependencies.initializeGameUseCase.execute()

        // Get difficulty from settings
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
                bubbles = activeBubbles
            )
        }

        // Start the timer with selected duration
        dependencies.timerUseCase.startTimer(selectedDuration)

        // Start collecting timer updates
        collectTimerState()

        Timber.d("Classic mode game started with ${activeBubbles.count { it.isActive }} active bubbles")

        // Play start sound
        dependencies.audioRepository.playSound(com.akinalpfdn.poprush.core.domain.model.SoundType.BUTTON_PRESS)
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
            // Update bubbles
            stateFlow.update { it.copy(bubbles = result.updatedBubbles) }

            // Play sound with haptic feedback
            val hapticFeedback = dependencies.settingsRepository.getHapticFeedbackFlow().first()
            dependencies.audioRepository.playSoundWithHaptic(
                com.akinalpfdn.poprush.core.domain.model.SoundType.BUBBLE_PRESS,
                hapticFeedback
            )

            // Check if level is complete
            if (result.isLevelComplete) {
                stateFlow.update { it.copy(score = it.score + 1) }
                dependencies.audioRepository.playSound(com.akinalpfdn.poprush.core.domain.model.SoundType.LEVEL_COMPLETE)

                // Generate new level after a short delay
                kotlinx.coroutines.delay(200)
                generateNewLevel()
            }
        }
    }

    private fun generateNewLevel() {
        scope.launch {
            val currentState = stateFlow.value
            val difficulty = dependencies.settingsRepository.getGameDifficultyFlow().first()

            val newBubbles = dependencies.generateLevelUseCase.execute(
                currentBubbles = currentState.bubbles,
                difficulty = difficulty,
                currentLevel = currentState.currentLevel
            )

            stateFlow.update { it.copy(
                bubbles = newBubbles,
                currentLevel = it.currentLevel + 1
            )}

            Timber.d("Generated new level ${currentState.currentLevel + 1}")
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

        // Stop timer
        dependencies.timerUseCase.stopTimer()

        // Create game result for statistics
        val gameResult = createGameResult(currentState)

        // Save game result
        dependencies.gameRepository.saveGameResult(gameResult)

        // Update high score if needed
        if (currentState.score > currentState.highScore) {
            dependencies.gameRepository.updateHighScore(currentState.score)
            dependencies.audioRepository.playSound(com.akinalpfdn.poprush.core.domain.model.SoundType.HIGH_SCORE)
        } else {
            dependencies.audioRepository.playSound(com.akinalpfdn.poprush.core.domain.model.SoundType.GAME_OVER)
        }

        stateFlow.update { it.copy(
            isPlaying = false,
            isGameOver = true,
            isPaused = false
        )}

        Timber.d("Classic game ended with score: ${currentState.score}")
    }

    override suspend fun resetGame() {
        dependencies.timerUseCase.stopTimer()

        stateFlow.update { currentState ->
            currentState.copy(
                isPlaying = false,
                isGameOver = false,
                isPaused = false,
                score = 0,
                currentLevel = 1,
                bubbles = emptyList(),
                timeRemaining = GameState.GAME_DURATION
            )
        }

        Timber.d("Classic game reset to initial state")
    }

    override fun cleanup() {
        // Launch in a scope if available, otherwise create a new one
        if (this::scope.isInitialized) {
            scope.launch { dependencies.timerUseCase.stopTimer() }
        }
    }

    override fun getConfig(): GameModeConfig = config

    private fun createGameResult(gameState: GameState) = com.akinalpfdn.poprush.core.domain.model.GameResult(
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
}
