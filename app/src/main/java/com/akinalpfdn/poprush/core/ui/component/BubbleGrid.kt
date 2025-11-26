package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.game.domain.GridCalculator
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Composable that renders the hexagonal bubble grid layout.
 * Handles touch interactions and arranges bubbles in the 5-6-7-8-7-6-5 pattern.
 *
 * @param gameState Current game state containing bubble data
 * @param selectedShape Currently selected bubble shape
 * @param bubbleSize Size of individual bubbles
 * @param bubbleSpacing Spacing between bubbles
 * @param zoomLevel Current zoom level
 * @param onBubblePress Callback when a bubble is pressed
 * @param modifier Additional modifier for the grid
 * @param enabled Whether the grid is interactive
 */
@Composable
fun BubbleGrid(
    gameState: GameState,
    selectedShape: BubbleShape = BubbleShape.CIRCLE,
    bubbleSize: Float = 48f,
    bubbleSpacing: Float = 12f,
    zoomLevel: Float = 1f,
    onBubblePress: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val density = LocalDensity.current
    val gridCalculator = remember { GridCalculator() }

    // Calculate bubble positions
    val bubblePositions = remember(gameState.bubbles) {
        gridCalculator.calculateBubblePositions(
            bubbleSize = bubbleSize,
            spacing = bubbleSpacing
        )
    }

    // Zoom animation
    val zoomAnimation by animateFloatAsState(
        targetValue = zoomLevel,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zoomAnimation"
    )

    // Row configurations for hexagonal layout
    val rowConfigs = remember {
        listOf(
            RowConfig(0, 5),   // Row 0: 5 bubbles
            RowConfig(1, 6),   // Row 1: 6 bubbles
            RowConfig(2, 7),   // Row 2: 7 bubbles
            RowConfig(3, 8),   // Row 3: 8 bubbles (middle/longest row)
            RowConfig(4, 7),   // Row 4: 7 bubbles
            RowConfig(5, 6),   // Row 5: 6 bubbles
            RowConfig(6, 5)    // Row 6: 5 bubbles
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(zoomAnimation)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Grid container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Render each row
            rowConfigs.forEach { rowConfig ->
                BubbleRow(
                    rowConfig = rowConfig,
                    bubbles = gameState.bubbles,
                    bubblePositions = bubblePositions,
                    selectedShape = selectedShape,
                    bubbleSize = bubbleSize,
                    bubbleSpacing = bubbleSpacing,
                    onBubblePress = onBubblePress,
                    enabled = enabled && gameState.isPlaying && !gameState.isPaused,
                    modifier = Modifier.padding(vertical = bubbleSpacing.dp / 2)
                )
            }
        }
    }
}

/**
 * Renders a single row of bubbles.
 */
@Composable
private fun BubbleRow(
    rowConfig: RowConfig,
    bubbles: List<Bubble>,
    bubblePositions: List<GridCalculator.BubblePosition>,
    selectedShape: BubbleShape,
    bubbleSize: Float,
    bubbleSpacing: Float,
    onBubblePress: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Calculate row start and end indices
    val startIndex = when (rowConfig.rowIndex) {
        0 -> 0
        1 -> 5
        2 -> 11
        3 -> 18
        4 -> 26
        5 -> 33
        6 -> 39
        else -> 0
    }

    val endIndex = startIndex + rowConfig.bubbleCount

    // Get bubbles for this row
    val rowBubbles = bubbles.subList(startIndex, endIndex.coerceAtMost(bubbles.size))

    // Calculate row width for centering
    val totalRowWidth = rowBubbles.size * (bubbleSize + bubbleSpacing) - bubbleSpacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        rowBubbles.forEachIndexed { index, bubble ->
            val position = bubblePositions.getOrNull(bubble.id)

            Box(
                modifier = Modifier
                    .padding(horizontal = bubbleSpacing.dp / 2)
                    .size(bubbleSize.dp)
                    .zIndex(if (bubble.isActive) 1f else 0f)
            ) {
                Bubble(
                    bubble = bubble,
                    shape = selectedShape,
                    isActive = bubble.isActive,
                    isPressed = bubble.isPressed,
                    bubbleSize = bubbleSize.dp,
                    onClick = onBubblePress,
                    enabled = enabled
                )
            }
        }
    }
}

/**
 * Alternative implementation using Canvas for precise positioning.
 * This provides better control over exact bubble placement and performance.
 */
@Composable
fun BubbleGridCanvas(
    gameState: GameState,
    selectedShape: BubbleShape = BubbleShape.CIRCLE,
    bubbleSize: Float = 48f,
    bubbleSpacing: Float = 12f,
    zoomLevel: Float = 1f,
    onBubblePress: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gridCalculator = remember { GridCalculator() }

    // Calculate bubble positions
    val bubblePositions = remember(gameState.bubbles) {
        gridCalculator.calculateBubblePositions(
            bubbleSize = bubbleSize,
            spacing = bubbleSpacing
        )
    }

    // Zoom animation
    val zoomAnimation by animateFloatAsState(
        targetValue = zoomLevel,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zoomAnimation"
    )

    // Touch handling
    var touchedBubbleId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(zoomAnimation)
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(gameState.bubbles, bubblePositions, enabled) {
                detectBubbleTouch(
                    bubbles = gameState.bubbles,
                    positions = bubblePositions,
                    enabled = enabled && gameState.isPlaying && !gameState.isPaused,
                    zoomLevel = zoomAnimation,
                    onBubbleTouched = { bubbleId ->
                        touchedBubbleId = bubbleId
                        if (bubbleId != null) {
                            onBubblePress(bubbleId)
                        }
                    }
                )
            }
    ) {
        // Render bubbles on canvas
        gameState.bubbles.forEach { bubble ->
            val position = bubblePositions.getOrNull(bubble.id) ?: return@forEach

            BubbleCanvas(
                bubble = bubble,
                position = position,
                shape = selectedShape,
                bubbleSize = bubbleSize,
                isTouched = touchedBubbleId == bubble.id,
                modifier = Modifier.offset(
                    x = position.x.dp,
                    y = position.y.dp
                )
            )
        }
    }
}

/**
 * Canvas-based bubble rendering for precise positioning.
 */
@Composable
private fun BubbleCanvas(
    bubble: Bubble,
    position: GridCalculator.BubblePosition,
    shape: BubbleShape,
    bubbleSize: Float,
    isTouched: Boolean,
    modifier: Modifier = Modifier
) {
    val pressAnimation by animateFloatAsState(
        targetValue = if (isTouched) 0.85f else 1f,
        animationSpec = tween(150),
        label = "pressAnimation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (bubble.isActive && !bubble.isPressed) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )

    Bubble(
        bubble = bubble,
        shape = shape,
        isActive = bubble.isActive,
        isPressed = bubble.isPressed,
        bubbleSize = bubbleSize.dp,
        modifier = modifier
            .scale(scaleAnimation * pressAnimation)
    )
}

/**
 * Configuration for a bubble row.
 */
private data class RowConfig(
    val rowIndex: Int,
    val bubbleCount: Int
)

/**
 * Advanced touch handling for bubble grid with multi-touch support.
 */
private fun detectBubbleTouch(
    bubbles: List<Bubble>,
    positions: List<GridCalculator.BubblePosition>,
    enabled: Boolean,
    zoomLevel: Float,
    onBubbleTouched: (Int?) -> Unit
) {
    // This would be implemented with proper pointer input handling
    // For now, we'll use a simplified approach

    // In a full implementation, this would:
    // 1. Track all touch points
    // 2. Calculate which bubbles are touched
    // 3. Handle multi-touch scenarios
    // 4. Provide haptic feedback
    // 5. Handle touch animations

    onBubbleTouched(null)
}

/**
 * Utility function to get the bubble ID from touch coordinates.
 */
private fun findTouchedBubbleId(
    touchX: Float,
    touchY: Float,
    positions: List<GridCalculator.BubblePosition>,
    zoomLevel: Float
): Int? {
    return positions.indexOfFirst { position ->
        val scaledSize = position.size * zoomLevel
        val centerX = position.x + scaledSize / 2
        val centerY = position.y + scaledSize / 2
        val radius = scaledSize / 2

        val dx = touchX - centerX
        val dy = touchY - centerY
        val distance = kotlin.math.sqrt(dx.pow(2) + dy.pow(2))

        distance <= radius
    }.takeIf { it != -1 }
}