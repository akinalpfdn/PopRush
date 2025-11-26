package com.akinalpfdn.poprush.game.domain.usecase

import com.akinalpfdn.poprush.core.domain.model.Bubble
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of bubble press operation.
 */
data class BubblePressResult(
    val success: Boolean,
    val updatedBubbles: List<Bubble>,
    val isLevelComplete: Boolean,
    val pressedBubbleId: Int,
    val wasValidPress: Boolean,
    val scoreIncrement: Int = 0
)

/**
 * Use case for handling bubble press events.
 * Manages bubble state updates and validates user input.
 */
@Singleton
class HandleBubblePressUseCase @Inject constructor() {

    /**
     * Processes a bubble press event and updates bubble states accordingly.
     *
     * @param bubbles Current list of bubbles in the game
     * @param bubbleId ID of the pressed bubble
     * @return BubblePressResult with the outcome of the press
     */
    suspend fun execute(
        bubbles: List<Bubble>,
        bubbleId: Int
    ): BubblePressResult {
        try {
            // Find the pressed bubble
            val pressedBubble = bubbles.find { it.id == bubbleId }
                ?: return createFailureResult(bubbles, bubbleId, "Bubble not found")

            // Check if the bubble can be pressed
            if (!pressedBubble.canBePressed) {
                Timber.d("Invalid bubble press: bubble $bubbleId is not active or already pressed")
                return createFailureResult(bubbles, bubbleId, "Bubble cannot be pressed")
            }

            // Update bubble state
            val updatedBubbles = bubbles.map { bubble ->
                if (bubble.id == bubbleId) {
                    bubble.copy(isPressed = true)
                } else {
                    bubble
                }
            }

            // Check if level is complete
            val isLevelComplete = checkLevelCompletion(updatedBubbles)

            // Calculate score increment (could be extended with combos, multipliers, etc.)
            val scoreIncrement = calculateScoreIncrement(pressedBubble, isLevelComplete)

            Timber.d("Successfully pressed bubble $bubbleId. Level complete: $isLevelComplete")

            return BubblePressResult(
                success = true,
                updatedBubbles = updatedBubbles,
                isLevelComplete = isLevelComplete,
                pressedBubbleId = bubbleId,
                wasValidPress = true,
                scoreIncrement = scoreIncrement
            )

        } catch (e: Exception) {
            Timber.e(e, "Error handling bubble press for bubble $bubbleId")
            return createFailureResult(bubbles, bubbleId, "Exception occurred: ${e.message}")
        }
    }

    /**
     * Processes multiple simultaneous bubble presses (for multi-touch support).
     *
     * @param bubbles Current list of bubbles
     * @param bubbleIds List of bubble IDs that were pressed simultaneously
     * @return BubblePressResult for the combined press
     */
    suspend fun executeMultiPress(
        bubbles: List<Bubble>,
        bubbleIds: List<Int>
    ): BubblePressResult {
        try {
            if (bubbleIds.isEmpty()) {
                return createFailureResult(bubbles, -1, "No bubble IDs provided")
            }

            // Filter valid bubble presses
            val validBubbles = bubbleIds.mapNotNull { bubbleId ->
                val bubble = bubbles.find { it.id == bubbleId }
                if (bubble?.canBePressed == true) bubble else null
            }

            if (validBubbles.isEmpty()) {
                return createFailureResult(bubbles, bubbleIds.firstOrNull() ?: -1, "No valid bubble presses")
            }

            // Update all valid bubbles
            val pressedBubbleIds = validBubbles.map { it.id }.toSet()
            val updatedBubbles = bubbles.map { bubble ->
                if (pressedBubbleIds.contains(bubble.id)) {
                    bubble.copy(isPressed = true)
                } else {
                    bubble
                }
            }

            // Check if level is complete
            val isLevelComplete = checkLevelCompletion(updatedBubbles)

            // Calculate score with multi-touch bonus
            val baseScore = validBubbles.size
            val multiTouchBonus = if (validBubbles.size > 1) 1 else 0
            val totalScore = baseScore + multiTouchBonus

            Timber.d("Successfully pressed ${validBubbles.size} bubbles. Level complete: $isLevelComplete")

            return BubblePressResult(
                success = true,
                updatedBubbles = updatedBubbles,
                isLevelComplete = isLevelComplete,
                pressedBubbleId = validBubbles.first().id,
                wasValidPress = true,
                scoreIncrement = totalScore
            )

        } catch (e: Exception) {
            Timber.e(e, "Error handling multi-press for bubbles $bubbleIds")
            return createFailureResult(bubbles, bubbleIds.firstOrNull() ?: -1, "Exception occurred: ${e.message}")
        }
    }

    /**
     * Validates that a bubble press is valid within game rules.
     *
     * @param bubble The bubble to validate
     * @param allBubbles All bubbles in the current game state
     * @return True if the press is valid
     */
    fun validateBubblePress(bubble: Bubble, allBubbles: List<Bubble>): Boolean {
        // Check if bubble exists and is in valid state
        if (!allBubbles.contains(bubble)) {
            Timber.w("Bubble ${bubble.id} not found in game state")
            return false
        }

        // Check if bubble can be pressed
        if (!bubble.canBePressed) {
            Timber.d("Bubble ${bubble.id} cannot be pressed: active=${bubble.isActive}, pressed=${bubble.isPressed}")
            return false
        }

        return true
    }

    /**
     * Checks if the current level is complete (all active bubbles pressed).
     *
     * @param bubbles Current list of bubbles
     * @return True if all active bubbles have been pressed
     */
    private fun checkLevelCompletion(bubbles: List<Bubble>): Boolean {
        val activeBubbles = bubbles.filter { it.isActive }
        return activeBubbles.isNotEmpty() && activeBubbles.all { it.isPressed }
    }

    /**
     * Calculates the score increment for a bubble press.
     * Can be extended with combo systems, multipliers, and bonuses.
     *
     * @param bubble The pressed bubble
     * @param isLevelComplete Whether this press completed the level
     * @return Score increment value
     */
    private fun calculateScoreIncrement(bubble: Bubble, isLevelComplete: Boolean): Int {
        var score = 1 // Base score for any valid press

        // Add bonus for level completion
        if (isLevelComplete) {
            score += 1
        }

        // Future: Add color-specific bonuses, timing bonuses, combo multipliers, etc.

        return score
    }

    /**
     * Creates a failure result for invalid bubble presses.
     */
    private fun createFailureResult(
        bubbles: List<Bubble>,
        bubbleId: Int,
        reason: String
    ): BubblePressResult {
        return BubblePressResult(
            success = false,
            updatedBubbles = bubbles,
            isLevelComplete = false,
            pressedBubbleId = bubbleId,
            wasValidPress = false,
            scoreIncrement = 0
        )
    }

    /**
     * Gets statistics about bubble presses for analytics.
     *
     * @param allBubbles All bubbles in the current state
     * @return BubblePressStatistics with current state info
     */
    fun getBubblePressStatistics(allBubbles: List<Bubble>): BubblePressStatistics {
        val activeBubbles = allBubbles.filter { it.isActive }
        val pressedBubbles = allBubbles.filter { it.isPressed }
        val activeAndPressedBubbles = allBubbles.filter { it.isActive && it.isPressed }

        return BubblePressStatistics(
            totalBubbles = allBubbles.size,
            activeBubbles = activeBubbles.size,
            pressedBubbles = pressedBubbles.size,
            remainingActiveBubbles = activeBubbles.size - activeAndPressedBubbles.size,
            levelProgress = if (activeBubbles.isNotEmpty()) {
                activeAndPressedBubbles.size.toFloat() / activeBubbles.size
            } else 1f
        )
    }
}

/**
 * Statistics about the current bubble press state.
 */
data class BubblePressStatistics(
    val totalBubbles: Int,
    val activeBubbles: Int,
    val pressedBubbles: Int,
    val remainingActiveBubbles: Int,
    val levelProgress: Float // 0.0 to 1.0
) {
    /**
     * Checks if the level is complete.
     */
    val isLevelComplete: Boolean
        get() = remainingActiveBubbles == 0 && activeBubbles > 0

    /**
     * Gets the level progress as a percentage.
     */
    val progressPercentage: Int
        get() = (levelProgress * 100).toInt()
}