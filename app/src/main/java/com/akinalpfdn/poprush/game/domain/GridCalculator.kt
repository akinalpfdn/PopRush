package com.akinalpfdn.poprush.game.domain

import com.akinalpfdn.poprush.core.domain.model.GameState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow

/**
 * Geometry and positioning calculations for the hexagonal bubble grid.
 * Handles bubble positioning, spacing, and grid layout calculations.
 */
@Singleton
class GridCalculator @Inject constructor() {

    companion object {
        // Bubble size and spacing constants
        private const val BUBBLE_SIZE_DP = 48f
        private const val BUBBLE_SPACING_DP = 12f
        private const val HORIZONTAL_SPACING_MULTIPLIER = 1.1f
        private const val VERTICAL_SPACING_MULTIPLIER = 0.9f

        // Hexagonal layout constants
        private const val HEXAGONAL_OFFSET_RATIO = 0.5f // Offset for alternating rows
        private const val ROW_CENTERING_OFFSET = 0.25f // Additional offset for centering
    }

    /**
     * Represents position and size information for a bubble.
     */
    data class BubblePosition(
        val x: Float,
        val y: Float,
        val size: Float,
        val row: Int,
        val col: Int,
        val isCentered: Boolean = false
    )

    /**
     * Represents overall grid dimensions and metrics.
     */
    data class GridDimensions(
        val width: Float,
        val height: Float,
        val centerX: Float,
        val centerY: Float,
        val maxBubbleSize: Float,
        val horizontalSpacing: Float,
        val verticalSpacing: Float
    )

    /**
     * Calculates positions for all bubbles in the hexagonal grid.
     *
     * @param bubbleSize Base size for bubbles in pixels
     * @param spacing Additional spacing between bubbles
     * @param screenWidth Available screen width
     * @param screenHeight Available screen height
     * @return List of bubble positions
     */
    fun calculateBubblePositions(
        bubbleSize: Float = BUBBLE_SIZE_DP,
        spacing: Float = BUBBLE_SPACING_DP,
        screenWidth: Float = 0f,
        screenHeight: Float = 0f
    ): List<BubblePosition> {
        val positions = mutableListOf<BubblePosition>()
        var bubbleId = 0

        // Calculate spacing
        val horizontalSpacing = bubbleSize * HORIZONTAL_SPACING_MULTIPLIER + spacing
        val verticalSpacing = bubbleSize * VERTICAL_SPACING_MULTIPLIER + spacing

        // Calculate grid dimensions for centering
        val gridDimensions = calculateGridDimensions(
            bubbleSize = bubbleSize,
            spacing = spacing
        )

        // Create hexagonal layout: 5-6-7-8-7-6-5
        GameState.ROW_SIZES.forEachIndexed { rowIndex, rowSize ->
            val rowPositions = calculateRowPositions(
                rowIndex = rowIndex,
                rowSize = rowSize,
                bubbleSize = bubbleSize,
                horizontalSpacing = horizontalSpacing,
                verticalSpacing = verticalSpacing,
                gridDimensions = gridDimensions,
                screenWidth = screenWidth
            )

            positions.addAll(rowPositions)
            bubbleId += rowSize
        }

        Timber.d("Calculated ${positions.size} bubble positions")
        return positions
    }

    /**
     * Calculates positions for bubbles in a single row.
     */
    private fun calculateRowPositions(
        rowIndex: Int,
        rowSize: Int,
        bubbleSize: Float,
        horizontalSpacing: Float,
        verticalSpacing: Float,
        gridDimensions: GridDimensions,
        screenWidth: Float
    ): List<BubblePosition> {
        val positions = mutableListOf<BubblePosition>()
        val rowY = rowIndex * verticalSpacing

        // Calculate horizontal offset for centering the row
        val maxRowWidth = 8 * horizontalSpacing // Width of the widest row (8 bubbles)
        val currentRowWidth = rowSize * horizontalSpacing
        val horizontalOffset = (maxRowWidth - currentRowWidth) / 2f

        // Apply hexagonal offset for odd rows (except middle row 3)
        val hexagonalOffset = if (rowIndex != 3 && rowIndex % 2 == 1) {
            horizontalSpacing * HEXAGONAL_OFFSET_RATIO
        } else {
            0f
        }

        // Center within available screen width
        val screenCenterOffset = if (screenWidth > 0) {
            (screenWidth - maxRowWidth) / 2f
        } else {
            0f
        }

        for (colIndex in 0 until rowSize) {
            val x = screenCenterOffset + horizontalOffset + hexagonalOffset + colIndex * horizontalSpacing
            val y = rowY

            positions.add(
                BubblePosition(
                    x = x,
                    y = y,
                    size = bubbleSize,
                    row = rowIndex,
                    col = colIndex,
                    isCentered = rowIndex == 3 && colIndex == rowSize / 2 // Center bubble
                )
            )
        }

        return positions
    }

    /**
     * Calculates the overall dimensions of the bubble grid.
     */
    fun calculateGridDimensions(
        bubbleSize: Float = BUBBLE_SIZE_DP,
        spacing: Float = BUBBLE_SPACING_DP
    ): GridDimensions {
        val horizontalSpacing = bubbleSize * HORIZONTAL_SPACING_MULTIPLIER + spacing
        val verticalSpacing = bubbleSize * VERTICAL_SPACING_MULTIPLIER + spacing

        // Calculate width based on widest row (8 bubbles)
        val width = 8 * horizontalSpacing

        // Calculate height based on total rows (7 rows)
        val height = 7 * verticalSpacing

        return GridDimensions(
            width = width,
            height = height,
            centerX = width / 2f,
            centerY = height / 2f,
            maxBubbleSize = bubbleSize,
            horizontalSpacing = horizontalSpacing,
            verticalSpacing = verticalSpacing
        )
    }

    /**
     * Calculates the optimal bubble size for the given screen dimensions.
     *
     * @param screenWidth Available screen width
     * @param screenHeight Available screen height
     * @param padding Desired padding around the grid
     * @return Optimal bubble size in pixels
     */
    fun calculateOptimalBubbleSize(
        screenWidth: Float,
        screenHeight: Float,
        padding: Float = 48f
    ): Float {
        val availableWidth = screenWidth - (2 * padding)
        val availableHeight = screenHeight - (2 * padding)

        // Calculate bubble size based on width constraints
        val maxBubbleSizeByWidth = availableWidth / (8 * HORIZONTAL_SPACING_MULTIPLIER)

        // Calculate bubble size based on height constraints
        val maxBubbleSizeByHeight = availableHeight / (7 * VERTICAL_SPACING_MULTIPLIER)

        // Use the smaller of the two to ensure everything fits
        val optimalSize = minOf(maxBubbleSizeByWidth, maxBubbleSizeByHeight, BUBBLE_SIZE_DP * 1.5f)

        Timber.d("Optimal bubble size calculated: $optimalSize (screen: ${screenWidth}x${screenHeight})")
        return optimalSize.coerceAtLeast(20f) // Minimum bubble size
    }

    /**
     * Calculates zoom constraints for the grid.
     *
     * @param baseBubbleSize Original bubble size
     * @param minZoom Minimum zoom level
     * @param maxZoom Maximum zoom level
     * @return Zoom range information
     */
    fun calculateZoomConstraints(
        baseBubbleSize: Float,
        minZoom: Float = 0.5f,
        maxZoom: Float = 1.5f
    ): ZoomConstraints {
        return ZoomConstraints(
            minBubbleSize = baseBubbleSize * minZoom,
            maxBubbleSize = baseBubbleSize * maxZoom,
            minZoom = minZoom,
            maxZoom = maxZoom,
            currentZoom = 1.0f
        )
    }

    /**
     * Calculates the position of a specific bubble by its ID.
     *
     * @param bubbleId The ID of the bubble (0-43)
     * @param positions List of all bubble positions
     * @return Position of the specific bubble, or null if not found
     */
    fun findBubblePosition(bubbleId: Int, positions: List<BubblePosition>): BubblePosition? {
        return positions.getOrNull(bubbleId)
    }

    /**
     * Checks if a point (touch) is within a bubble's bounds.
     *
     * @param touchX Touch X coordinate
     * @param touchY Touch Y coordinate
     * @param bubblePosition Bubble position and size
     * @param zoomLevel Current zoom level
     * @return True if touch is within bubble bounds
     */
    fun isTouchWithinBubble(
        touchX: Float,
        touchY: Float,
        bubblePosition: BubblePosition,
        zoomLevel: Float = 1.0f
    ): Boolean {
        val scaledSize = bubblePosition.size * zoomLevel
        val scaledRadius = scaledSize / 2f

        val centerX = bubblePosition.x + scaledRadius
        val centerY = bubblePosition.y + scaledRadius

        val distance = kotlin.math.sqrt(
            kotlin.math.pow((touchX - centerX).toDouble(), 2.0) +
            kotlin.math.pow((touchY - centerY).toDouble(), 2.0)
        )

        return distance <= scaledRadius
    }

    /**
     * Finds which bubble was touched based on coordinates.
     *
     * @param touchX Touch X coordinate
     * @param touchY Touch Y coordinate
     * @param positions List of bubble positions
     * @param zoomLevel Current zoom level
     * @return ID of the touched bubble, or null if none
     */
    fun findTouchedBubble(
        touchX: Float,
        touchY: Float,
        positions: List<BubblePosition>,
        zoomLevel: Float = 1.0f
    ): Int? {
        return positions.indexOfFirst { position ->
            isTouchWithinBubble(touchX, touchY, position, zoomLevel)
        }.takeIf { it != -1 }
    }

    /**
     * Calculates animation paths for bubble transitions.
     *
     * @param fromPosition Starting position
     * @param toPosition Ending position
     * @param duration Animation duration in milliseconds
     * @return Animation path data
     */
    fun calculateBubbleAnimationPath(
        fromPosition: BubblePosition,
        toPosition: BubblePosition,
        duration: Long = 300L
    ): BubbleAnimationPath {
        return BubbleAnimationPath(
            fromX = fromPosition.x,
            fromY = fromPosition.y,
            fromSize = fromPosition.size,
            toX = toPosition.x,
            toY = toPosition.y,
            toSize = toPosition.size,
            duration = duration,
            deltaX = toPosition.x - fromPosition.x,
            deltaY = toPosition.y - fromPosition.y,
            deltaSize = toPosition.size - fromPosition.size
        )
    }

    /**
     * Validates the grid layout for correctness.
     *
     * @param positions List of bubble positions
     * @return True if layout is valid
     */
    fun validateGridLayout(positions: List<BubblePosition>): Boolean {
        // Check total count
        if (positions.size != GameState.TOTAL_BUBBLES) {
            Timber.w("Invalid bubble count: expected ${GameState.TOTAL_BUBBLES}, got ${positions.size}")
            return false
        }

        // Check each row has correct number of bubbles
        val bubblesByRow = positions.groupBy { it.row }
        GameState.ROW_SIZES.forEachIndexed { rowIndex, expectedSize ->
            val actualSize = bubblesByRow[rowIndex]?.size ?: 0
            if (actualSize != expectedSize) {
                Timber.w("Invalid bubble count in row $rowIndex: expected $expectedSize, got $actualSize")
                return false
            }
        }

        return true
    }
}

/**
 * Constraints for zoom functionality.
 */
data class ZoomConstraints(
    val minBubbleSize: Float,
    val maxBubbleSize: Float,
    val minZoom: Float,
    val maxZoom: Float,
    val currentZoom: Float
)

/**
 * Animation path data for bubble transitions.
 */
data class BubbleAnimationPath(
    val fromX: Float,
    val fromY: Float,
    val fromSize: Float,
    val toX: Float,
    val toY: Float,
    val toSize: Float,
    val duration: Long,
    val deltaX: Float,
    val deltaY: Float,
    val deltaSize: Float
)