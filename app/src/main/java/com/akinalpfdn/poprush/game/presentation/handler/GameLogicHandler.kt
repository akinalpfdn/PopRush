package com.akinalpfdn.poprush.game.presentation.handler

import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration

class GameLogicHandler @Inject constructor(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    private val initializeGameUseCase: InitializeGameUseCase,
    private val generateLevelUseCase: GenerateLevelUseCase,
    private val handleBubblePressUseCase: HandleBubblePressUseCase,
    private val timerUseCase: TimerUseCase
) {
    private lateinit var scope: CoroutineScope
    private lateinit var gameStateFlow: MutableStateFlow<GameState>

    fun init(scope: CoroutineScope, gameStateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.gameStateFlow = gameStateFlow
    }

    fun startClassicGame() {
        scope.launch {
            try {
                val currentState = gameStateFlow.value
                val newBubbles = initializeGameUseCase.execute()

                // Get difficulty from settings
                val difficulty = settingsRepository.getGameDifficultyFlow().first()

                val activeBubbles = generateLevelUseCase.execute(
                    currentBubbles = newBubbles,
                    difficulty = difficulty,
                    currentLevel = 1
                )

                val selectedDuration = currentState.selectedDuration

                gameStateFlow.update {
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
                timerUseCase.startTimer(selectedDuration)
                
                // Start collecting timer updates
                collectTimerState()

                Timber.d("Classic mode game started with ${activeBubbles.count { it.isActive }} active bubbles")
                
                // Play start sound
                audioRepository.playSound(SoundType.BUTTON_PRESS)
                
            } catch (e: Exception) {
                Timber.e(e, "Error starting classic game")
            }
        }
    }

    private fun collectTimerState() {
        scope.launch {
            launch {
                timerUseCase.timeRemaining.collect { timeRemaining ->
                    gameStateFlow.update { it.copy(timeRemaining = timeRemaining) }
                }
            }
            
            launch {
                timerUseCase.timerState.collect { timerState ->
                    if (timerState == com.akinalpfdn.poprush.game.domain.usecase.TimerState.FINISHED) {
                        handleEndGame()
                    }
                }
            }
        }
    }

    fun handleBubblePress(bubbleId: Int) {
        scope.launch {
            try {
                val currentState = gameStateFlow.value
                val result = handleBubblePressUseCase.execute(
                    bubbles = currentState.bubbles,
                    bubbleId = bubbleId
                )

                if (result.success) {
                    // Update bubbles
                    gameStateFlow.update { it.copy(bubbles = result.updatedBubbles) }

                    // Play sound with haptic feedback
                    val hapticFeedback = settingsRepository.getHapticFeedbackFlow().first()
                    audioRepository.playSoundWithHaptic(
                        SoundType.BUBBLE_PRESS,
                        hapticFeedback
                    )

                    // Check if level is complete
                    if (result.isLevelComplete) {
                        gameStateFlow.update { it.copy(score = it.score + 1) }
                        audioRepository.playSound(SoundType.LEVEL_COMPLETE)

                        // Generate new level after a short delay
                        delay(200)
                        handleGenerateNewLevel()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling bubble press for bubble $bubbleId")
            }
        }
    }

    fun handleGenerateNewLevel() {
        scope.launch {
            try {
                val currentState = gameStateFlow.value
                val difficulty = settingsRepository.getGameDifficultyFlow().first()

                val newBubbles = generateLevelUseCase.execute(
                    currentBubbles = currentState.bubbles,
                    difficulty = difficulty,
                    currentLevel = currentState.currentLevel
                )

                gameStateFlow.update { it.copy(
                    bubbles = newBubbles,
                    currentLevel = it.currentLevel + 1
                )}

                Timber.d("Generated new level ${currentState.currentLevel + 1}")
            } catch (e: Exception) {
                Timber.e(e, "Error generating new level")
            }
        }
    }

    fun handleEndGame() {
        scope.launch {
            try {
                val currentState = gameStateFlow.value

                // Stop timer
                timerUseCase.stopTimer()

                // Create game result for statistics
                val gameResult = createGameResult(currentState)

                // Save game result
                gameRepository.saveGameResult(gameResult)

                // Update high score if needed
                if (currentState.score > currentState.highScore) {
                    gameRepository.updateHighScore(currentState.score)
                    audioRepository.playSound(SoundType.HIGH_SCORE)
                } else {
                    audioRepository.playSound(SoundType.GAME_OVER)
                }

                gameStateFlow.update { it.copy(
                    isPlaying = false,
                    isGameOver = true,
                    isPaused = false
                )}

                Timber.d("Game ended with score: ${currentState.score}")
            } catch (e: Exception) {
                Timber.e(e, "Error ending game")
            }
        }
    }
    
    fun handleResetGame() {
        scope.launch {
            try {
                timerUseCase.stopTimer()

                gameStateFlow.update { currentState ->
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

                Timber.d("Game reset to initial state")
            } catch (e: Exception) {
                Timber.e(e, "Error resetting game")
            }
        }
    }
    
    fun handleLoadGameData() {
        scope.launch {
            try {
                val highScore = gameRepository.getHighScore()
                gameStateFlow.update { it.copy(highScore = highScore) }
                Timber.d("Game data loaded. High score: $highScore")
            } catch (e: Exception) {
                Timber.e(e, "Error loading game data")
            }
        }
    }
    
    fun handleSaveGameData() {
        // Most data is already saved automatically via repositories
    }
    
    fun handleUpdateHighScore(newHighScore: Int) {
        scope.launch {
            gameRepository.updateHighScore(newHighScore)
        }
    }

    suspend fun stopTimer() {
        timerUseCase.stopTimer()
    }
    
    suspend fun pauseTimer() {
        timerUseCase.pauseTimer()
    }
    
    suspend fun resumeTimer() {
        timerUseCase.resumeTimer()
    }
    
    fun getTimerFlow() = timerUseCase.getTimerFlow()

    private fun createGameResult(gameState: GameState) = GameResult(
        finalScore = gameState.score,
        levelsCompleted = gameState.currentLevel - 1,
        totalBubblesPressed = gameState.pressedBubbleCount,
        accuracyPercentage = if (gameState.pressedBubbleCount > 0) 100f else 0f, // Simplified
        averageTimePerLevel = if (gameState.currentLevel > 1) {
            (GameState.GAME_DURATION - gameState.timeRemaining) / (gameState.currentLevel - 1)
        } else GameState.GAME_DURATION,
        gameDuration = GameState.GAME_DURATION - gameState.timeRemaining,
        isHighScore = gameState.score > gameState.highScore
    )
}
