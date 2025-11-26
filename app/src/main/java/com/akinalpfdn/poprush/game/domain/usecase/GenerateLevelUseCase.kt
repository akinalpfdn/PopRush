package com.akinalpfdn.poprush.game.domain.usecase

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import kotlin.random.Random
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of level generation operation.
 */
data class LevelGenerationResult(
    val bubbles: List<Bubble>,
    val activeBubbleCount: Int,
    val levelNumber: Int
)

/**
 * Use case for generating new game levels with random active bubbles.
 * Handles difficulty scaling and ensures fair level generation.
 */
@Singleton
class GenerateLevelUseCase @Inject constructor() {

    private val random = Random(System.currentTimeMillis())

    /**
     * Generates a new level by randomly selecting bubbles to activate.
     *
     * @param currentBubbles Current list of bubbles in the game
     * @param difficulty The difficulty level for level generation
     * @param currentLevel The current level number (for progression scaling)
     * @return LevelGenerationResult with updated bubbles
     */
    suspend fun execute(
        currentBubbles: List<Bubble>,
        difficulty: GameDifficulty,
        currentLevel: Int
    ): List<Bubble> {
        try {
            // Calculate number of bubbles to activate based on difficulty and level
            val numToActivate = calculateActiveBubbleCount(difficulty, currentLevel)

            // Select random bubbles to activate
            val activeBubbleIds = selectRandomBubbles(currentBubbles.size, numToActivate)

            // Update bubble states
            val newBubbles = currentBubbles.map { bubble ->
                val isActive = activeBubbleIds.contains(bubble.id)
                bubble.copy(
                    isActive = isActive,
                    isPressed = false // Reset pressed state for new level
                )
            }

            Timber.d("Generated level with $numToActivate active bubbles (difficulty: $difficulty, level: $currentLevel)")
            return newBubbles

        } catch (e: Exception) {
            Timber.e(e, "Error generating new level")
            // Return original bubbles on error
            return currentBubbles
        }
    }

    /**
     * Calculates how many bubbles should be active based on difficulty and level.
     *
     * @param difficulty The current game difficulty
     * @param levelNumber The current level number
     * @return Number of bubbles to activate (4-12 based on difficulty)
     */
    private fun calculateActiveBubbleCount(difficulty: GameDifficulty, levelNumber: Int): Int {
        val baseRange = when (difficulty) {
            GameDifficulty.EASY -> difficulty.minActiveBubbles..(difficulty.minActiveBubbles + 1)
            GameDifficulty.NORMAL -> difficulty.minActiveBubbles..difficulty.maxActiveBubbles
            GameDifficulty.HARD -> difficulty.minActiveBubbles..difficulty.maxActiveBubbles
            GameDifficulty.EXPERT -> difficulty.minActiveBubbles..difficulty.maxActiveBubbles
        }

        // Add level progression scaling (increase difficulty every 5 levels)
        val levelScaling = (levelNumber - 1) / 5
        val adjustedMax = (baseRange.last + levelScaling).coerceAtMost(15)

        val finalRange = baseRange.first..adjustedMax
        return random.nextInt(finalRange.first, finalRange.last + 1)
    }

    /**
     * Selects random bubble IDs to make active.
     * Ensures no duplicates and tries to create interesting patterns.
     *
     * @param totalBubbles Total number of bubbles available
     * @param count Number of bubbles to select
     * @return List of bubble IDs to activate
     */
    private fun selectRandomBubbles(totalBubbles: Int, count: Int): List<Int> {
        val availableIds = (0 until totalBubbles).toMutableList()
        val selectedIds = mutableListOf<Int>()

        repeat(count) {
            if (availableIds.isNotEmpty()) {
                val randomIndex = random.nextInt(availableIds.size)
                selectedIds.add(availableIds.removeAt(randomIndex))
            }
        }

        return selectedIds.sorted()
    }

    /**
     * Advanced level generation that creates patterns instead of pure random.
     * This could be used for special levels or future game modes.
     *
     * @param currentBubbles Current list of bubbles
     * @param patternType Type of pattern to generate
     * @return List of bubbles with pattern-based activation
     */
    suspend fun generatePatternLevel(
        currentBubbles: List<Bubble>,
        patternType: PatternType = PatternType.RANDOM
    ): List<Bubble> {
        val activeBubbleIds = when (patternType) {
            PatternType.RANDOM -> selectRandomBubbles(currentBubbles.size, 5)
            PatternType.HEART -> generateHeartPattern()
            PatternType.DIAGONAL -> generateDiagonalPattern()
            PatternType.CIRCLE -> generateCirclePattern()
            PatternType.CROSS -> generateCrossPattern()
            PatternType.SPIRAL -> generateSpiralPattern()
        }

        return currentBubbles.map { bubble ->
            bubble.copy(
                isActive = activeBubbleIds.contains(bubble.id),
                isPressed = false
            )
        }
    }

    // Pattern generation methods for special levels
    private fun generateHeartPattern(): List<Int> {
        // Returns bubble IDs that form a heart shape
        return listOf(13, 14, 20, 21, 22, 26, 27, 28, 29, 32, 33, 38)
    }

    private fun generateDiagonalPattern(): List<Int> {
        // Returns bubble IDs forming diagonal lines
        return listOf(0, 6, 12, 19, 26, 32, 38, 43, 4, 10, 17, 25, 31, 37)
    }

    private fun generateCirclePattern(): List<Int> {
        // Returns bubble IDs forming a circle
        return listOf(2, 8, 15, 23, 30, 36, 41, 1, 7, 16, 24, 29, 35, 40)
    }

    private fun generateCrossPattern(): List<Int> {
        // Returns bubble IDs forming a cross
        return listOf(2, 7, 12, 18, 25, 31, 36, 41, 19, 20, 21, 22, 23, 24)
    }

    private fun generateSpiralPattern(): List<Int> {
        // Returns bubble IDs forming a spiral
        return listOf(2, 8, 14, 20, 26, 33, 39, 42, 43, 37, 31, 25, 19, 13, 7, 1, 0, 6, 12, 18)
    }

    /**
     * Validates that a generated level is fair and playable.
     *
     * @param bubbles The generated bubbles
     * @param difficulty The difficulty level
     * @return True if the level is valid
     */
    fun validateLevel(bubbles: List<Bubble>, difficulty: GameDifficulty): Boolean {
        val activeBubbles = bubbles.filter { it.isActive }
        val activeCount = activeBubbles.size

        // Check bubble count is within reasonable range
        val expectedRange = difficulty.minActiveBubbles..difficulty.maxActiveBubbles
        if (activeCount !in expectedRange) {
            Timber.w("Invalid active bubble count: $activeCount, expected: $expectedRange")
            return false
        }

        // Check that active bubbles are spread across multiple rows
        val activeRows = activeBubbles.map { it.row }.distinct()
        if (activeRows.size < 2) {
            Timber.w("Active bubbles not spread across enough rows: ${activeRows.size}")
            return false
        }

        // Check that there are no isolated clusters (advanced validation)
        // This could be extended to ensure good playability

        return true
    }
}

/**
 * Types of patterns for special level generation.
 */
enum class PatternType {
    RANDOM,
    HEART,
    DIAGONAL,
    CIRCLE,
    CROSS,
    SPIRAL
}