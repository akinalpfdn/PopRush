package com.akinalpfdn.poprush.game.domain.usecase

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for initializing the game board with bubbles.
 * Creates the initial set of 44 bubbles arranged in a hexagonal grid.
 */
@Singleton
class InitializeGameUseCase @Inject constructor() {

    /**
     * Creates the initial bubble arrangement for the game.
     *
     * @return List of 44 bubbles arranged in 5-6-7-8-7-6-5 hexagonal layout
     */
    suspend fun execute(): List<Bubble> {
        val bubbles = mutableListOf<Bubble>()
        var bubbleId = 0

        try {
            // Create hexagonal grid: 5-6-7-8-7-6-5
            GameState.ROW_SIZES.forEachIndexed { rowIndex, size ->
                for (colIndex in 0 until size) {
                    val bubble = Bubble(
                        id = bubbleId,
                        position = bubbleId,
                        row = rowIndex,
                        col = colIndex,
                        color = determineBubbleColor(bubbleId),
                        isActive = false,
                        isPressed = false
                    )
                    bubbles.add(bubble)
                    bubbleId++
                }
            }

            Timber.d("Initialized ${bubbles.size} bubbles in hexagonal layout")
            return bubbles

        } catch (e: Exception) {
            Timber.e(e, "Error initializing game bubbles")
            throw e
        }
    }

    /**
     * Determines the color of a bubble based on its ID.
     * Uses deterministic coloring to create a consistent pattern.
     *
     * @param bubbleId The unique identifier of the bubble
     * @return The BubbleColor for this bubble
     */
    private fun determineBubbleColor(bubbleId: Int): BubbleColor {
        val colors = BubbleColor.values()
        return colors[bubbleId % colors.size]
    }

    /**
     * Calculates the grid position for a bubble based on its row and column.
     * This can be used for positioning bubbles in the UI.
     *
     * @param row The row index (0-6)
     * @param col The column index within the row
     * @return Pair of x, y coordinates for the bubble center
     */
    fun calculateBubblePosition(row: Int, col: Int, bubbleSize: Float = 40f): Pair<Float, Float> {
        val horizontalSpacing = bubbleSize * 1.2f
        val verticalSpacing = bubbleSize * 1.1f

        // Center the grid horizontally by accounting for row width
        val rowWidth = GameState.ROW_SIZES[row]
        val horizontalOffset = (8 - rowWidth) * horizontalSpacing / 2f

        // Apply hexagonal offset for odd rows (except the middle row)
        val hexagonalOffset = if (row != 3 && row % 2 == 1) {
            horizontalSpacing / 2f
        } else {
            0f
        }

        val x = horizontalOffset + col * horizontalSpacing + hexagonalOffset
        val y = row * verticalSpacing

        return Pair(x, y)
    }

    /**
     * Validates that the bubble arrangement is correct.
     * Used for testing and debugging purposes.
     *
     * @param bubbles The list of bubbles to validate
     * @return True if the arrangement is valid
     */
    fun validateBubbleArrangement(bubbles: List<Bubble>): Boolean {
        // Check total count
        if (bubbles.size != GameState.TOTAL_BUBBLES) {
            Timber.w("Invalid bubble count: expected ${GameState.TOTAL_BUBBLES}, got ${bubbles.size}")
            return false
        }

        // Check each row has correct number of bubbles
        val bubblesByRow = bubbles.groupBy { it.row }
        GameState.ROW_SIZES.forEachIndexed { rowIndex, expectedSize ->
            val actualSize = bubblesByRow[rowIndex]?.size ?: 0
            if (actualSize != expectedSize) {
                Timber.w("Invalid bubble count in row $rowIndex: expected $expectedSize, got $actualSize")
                return false
            }
        }

        // Check bubble IDs are unique and sequential
        val sortedBubbles = bubbles.sortedBy { it.id }
        sortedBubbles.forEachIndexed { index, bubble ->
            if (bubble.id != index) {
                Timber.w("Invalid bubble ID at index $index: expected $index, got ${bubble.id}")
                return false
            }
        }

        // Check positions are within expected range
        bubbles.forEach { bubble ->
            if (bubble.position !in 0 until GameState.TOTAL_BUBBLES) {
                Timber.w("Invalid bubble position: ${bubble.position}")
                return false
            }
        }

        return true
    }
}