package com.akinalpfdn.poprush.game.domain

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.PerformanceRating
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Core game engine that orchestrates all game logic and coordinates between different use cases.
 * This is the central point for game state management and rule enforcement.
 */
@Singleton
class GameEngine @Inject constructor(
    private val initializeGameUseCase: InitializeGameUseCase,
    private val generateLevelUseCase: GenerateLevelUseCase,
    private val handleBubblePressUseCase: HandleBubblePressUseCase,
    private val timerUseCase: TimerUseCase
) {

    /**
     * Game configuration for current session.
     */
    data class GameConfig(
        val difficulty: GameDifficulty = GameDifficulty.NORMAL,
        val enableHapticFeedback: Boolean = true,
        val enableSoundEffects: Boolean = true,
        val enableBackgroundMusic: Boolean = true,
        val timeLimit: Duration = 60.seconds,
        val maxLevels: Int = Int.MAX_VALUE
    )

    /**
     * Current game session information.
     */
    data class GameSession(
        val startTime: Long = System.currentTimeMillis(),
        val endTime: Long? = null,
        val totalBubblePresses: Int = 0,
        val correctBubblePresses: Int = 0,
        val levelsCompleted: Int = 0,
        val totalGameTime: Duration = Duration.ZERO,
        val config: GameConfig = GameConfig()
    ) {
        val accuracy: Float
            get() = if (totalBubblePresses > 0) {
                (correctBubblePresses.toFloat() / totalBubblePresses) * 100f
            } else 0f

        val isCompleted: Boolean
            get() = endTime != null

        val duration: Duration
            get() = if (endTime != null) {
                (endTime - startTime).milliseconds
            } else {
                (System.currentTimeMillis() - startTime).milliseconds
            }
    }

    private var currentSession: GameSession? = null

    /**
     * Initializes a new game session.
     *
     * @param config Game configuration for the session
     * @return Initial GameState with bubble grid
     */
    suspend fun initializeGame(config: GameConfig = GameConfig()): GameState {
        try {
            // Create new session
            currentSession = GameSession(config = config)

            // Initialize bubble grid
            val bubbles = initializeGameUseCase.execute()

            // Validate bubble arrangement
            if (!initializeGameUseCase.validateBubbleArrangement(bubbles)) {
                throw IllegalStateException("Invalid bubble arrangement generated")
            }

            val initialState = GameState(
                bubbles = bubbles,
                timeRemaining = config.timeLimit
            )

            Timber.d("Game initialized with ${bubbles.size} bubbles")
            return initialState

        } catch (e: Exception) {
            Timber.e(e, "Error initializing game")
            throw e
        }
    }

    /**
     * Starts a new game with the given configuration.
     *
     * @param initialState Initial game state
     * @param config Game configuration
     * @return Updated GameState for the started game
     */
    suspend fun startGame(
        initialState: GameState,
        config: GameConfig = GameConfig()
    ): GameState {
        try {
            // Update session start time
            currentSession = currentSession?.copy(startTime = System.currentTimeMillis())

            // Start the timer
            timerUseCase.startTimer(config.timeLimit)

            // Generate first level
            val bubblesWithLevel = generateLevelUseCase.execute(
                currentBubbles = initialState.bubbles,
                difficulty = config.difficulty,
                currentLevel = 1
            )

            val startedState = initialState.copy(
                isPlaying = true,
                isGameOver = false,
                isPaused = false,
                score = 0,
                currentLevel = 1,
                timeRemaining = config.timeLimit,
                bubbles = bubblesWithLevel
            )

            Timber.d("Game started with difficulty: ${config.difficulty}")
            return startedState

        } catch (e: Exception) {
            Timber.e(e, "Error starting game")
            throw e
        }
    }

    /**
     * Ends the current game session and calculates results.
     *
     * @param currentState Current game state
     * @return GameResult with session statistics
     */
    suspend fun endGame(currentState: GameState): GameResult {
        try {
            // Stop the timer
            timerUseCase.stopTimer()

            val session = currentSession ?: throw IllegalStateException("No active game session")

            // Calculate game statistics
            val finalScore = currentState.score
            val levelsCompleted = currentState.currentLevel - 1
            val totalBubblesPressed = currentState.pressedBubbleCount
            val gameDuration = currentState.timeRemaining

            // Calculate accuracy based on successful bubble presses vs total attempts
            val accuracy = if (session.totalBubblePresses > 0) {
                (session.correctBubblePresses.toFloat() / session.totalBubblePresses) * 100f
            } else 100f // Perfect if no incorrect presses

            // Calculate average time per level
            val averageTimePerLevel = if (levelsCompleted > 0) {
                gameDuration / levelsCompleted
            } else gameDuration

            // Determine performance rating
            val performanceRating = determinePerformanceRating(finalScore, accuracy, levelsCompleted)

            // Update session
            currentSession = session.copy(
                endTime = System.currentTimeMillis(),
                levelsCompleted = levelsCompleted,
                totalGameTime = session.duration
            )

            val result = GameResult(
                finalScore = finalScore,
                levelsCompleted = levelsCompleted,
                totalBubblesPressed = totalBubblesPressed,
                accuracyPercentage = accuracy,
                averageTimePerLevel = averageTimePerLevel,
                gameDuration = session.duration,
                difficulty = session.config.difficulty,
                isHighScore = false, // Will be determined by repository
                timestamp = session.startTime
            )

            Timber.d("Game ended. Score: $finalScore, Accuracy: ${accuracy}%, Levels: $levelsCompleted")
            return result

        } catch (e: Exception) {
            Timber.e(e, "Error ending game")
            throw e
        }
    }

    /**
     * Processes a bubble press during gameplay.
     *
     * @param currentState Current game state
     * @param bubbleId ID of the pressed bubble
     * @return Updated GameState after processing the press
     */
    suspend fun processBubblePress(
        currentState: GameState,
        bubbleId: Int
    ): GameState {
        try {
            if (!currentState.isPlaying || currentState.isPaused) {
                return currentState
            }

            // Record press attempt
            currentSession = currentSession?.copy(
                totalBubblePresses = currentSession!!.totalBubblePresses + 1
            )

            // Process the press
            val pressResult = handleBubblePressUseCase.execute(
                bubbles = currentState.bubbles,
                bubbleId = bubbleId
            )

            // Update session if press was successful
            if (pressResult.wasValidPress) {
                currentSession = currentSession?.copy(
                    correctBubblePresses = currentSession!!.correctBubblePresses + 1
                )
            }

            // Check for level completion
            if (pressResult.isLevelComplete) {
                currentSession = currentSession?.copy(
                    levelsCompleted = currentSession!!.levelsCompleted + 1
                )

                // Generate next level after a brief delay
                return generateNextLevel(currentState.copy(bubbles = pressResult.updatedBubbles))
            }

            return currentState.copy(bubbles = pressResult.updatedBubbles)

        } catch (e: Exception) {
            Timber.e(e, "Error processing bubble press for bubble $bubbleId")
            return currentState
        }
    }

    /**
     * Generates the next level in the game.
     *
     * @param currentState Current game state
     * @return Updated GameState with new level
     */
    private suspend fun generateNextLevel(currentState: GameState): GameState {
        try {
            val session = currentSession ?: return currentState

            val newBubbles = generateLevelUseCase.execute(
                currentBubbles = currentState.bubbles,
                difficulty = session.config.difficulty,
                currentLevel = currentState.currentLevel + 1
            )

            return currentState.copy(
                bubbles = newBubbles,
                currentLevel = currentState.currentLevel + 1,
                score = currentState.score + 1
            )

        } catch (e: Exception) {
            Timber.e(e, "Error generating next level")
            return currentState
        }
    }

    /**
     * Pauses or resumes the game.
     *
     * @param currentState Current game state
     * @param pause Whether to pause (true) or resume (false)
     * @return Updated GameState
     */
    suspend fun togglePause(currentState: GameState, pause: Boolean): GameState {
        return try {
            if (pause && !currentState.isPaused) {
                timerUseCase.pauseTimer()
                currentState.copy(isPaused = true)
            } else if (!pause && currentState.isPaused) {
                timerUseCase.resumeTimer()
                currentState.copy(isPaused = false)
            } else {
                currentState
            }
        } catch (e: Exception) {
            Timber.e(e, "Error toggling pause")
            currentState
        }
    }

    /**
     * Updates the game timer.
     *
     * @param currentState Current game state
     * @param timeRemaining New remaining time
     * @return Updated GameState
     */
    fun updateTimer(currentState: GameState, timeRemaining: Duration): GameState {
        return currentState.copy(timeRemaining = timeRemaining)
    }

    /**
     * Gets the current game session information.
     */
    fun getCurrentSession(): GameSession? = currentSession

    /**
     * Checks if the current session is valid and active.
     */
    fun hasActiveSession(): Boolean = currentSession != null && !currentSession!!.isCompleted

    /**
     * Gets combined flow of timer and game state for reactive updates.
     */
    fun getGameFlow(): Flow<GameEngineState> {
        return combine(
            timerUseCase.getTimerFlow(),
            timerUseCase.getTimerStateFlow()
        ) { timeRemaining, timerState ->
            GameEngineState(
                timeRemaining = timeRemaining,
                timerState = timerState,
                session = currentSession
            )
        }
    }

    /**
     * Validates the current game state for consistency.
     *
     * @param gameState Current game state to validate
     * @return True if state is valid
     */
    fun validateGameState(gameState: GameState): Boolean {
        try {
            // Check bubble count
            if (gameState.bubbles.size != GameState.TOTAL_BUBBLES) {
                Timber.w("Invalid bubble count: ${gameState.bubbles.size}")
                return false
            }

            // Check bubble IDs are unique
            val duplicateIds = gameState.bubbles.groupBy { it.id }
                .filterValues { it.size > 1 }
                .keys

            if (duplicateIds.isNotEmpty()) {
                Timber.w("Duplicate bubble IDs found: $duplicateIds")
                return false
            }

            // Check for logical consistency
            if (gameState.isPlaying && gameState.isGameOver) {
                Timber.w("Game cannot be both playing and over")
                return false
            }

            return true

        } catch (e: Exception) {
            Timber.e(e, "Error validating game state")
            return false
        }
    }

    /**
     * Determines the performance rating based on game metrics.
     */
    private fun determinePerformanceRating(
        score: Int,
        accuracy: Float,
        levelsCompleted: Int
    ): String {
        return when {
            accuracy >= 95f && score >= 50 -> PerformanceRating.PERFECT.name
            accuracy >= 90f && score >= 40 -> PerformanceRating.EXCELLENT.name
            accuracy >= 80f && score >= 30 -> PerformanceRating.GREAT.name
            accuracy >= 70f && score >= 20 -> PerformanceRating.GOOD.name
            accuracy >= 60f && score >= 10 -> PerformanceRating.FAIR.name
            else -> PerformanceRating.NEEDS_PRACTICE.name
        }
    }

    /**
     * Cleans up game engine resources.
     */
    suspend fun cleanup() {
        try {
            timerUseCase.cleanup()
            currentSession = null
            Timber.d("GameEngine cleaned up")
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up game engine")
        }
    }
}

/**
 * Combined state from the game engine for reactive updates.
 */
data class GameEngineState(
    val timeRemaining: Duration,
    val timerState: com.akinalpfdn.poprush.game.domain.usecase.TimerState,
    val session: GameEngine.GameSession?
)