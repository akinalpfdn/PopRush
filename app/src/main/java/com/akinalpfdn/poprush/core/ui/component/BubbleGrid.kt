package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState
import kotlinx.coroutines.delay

/**
 * Enhanced BubbleGrid with:
 * - Staggered entrance animations (bubbles drop in with bounce)
 * - Wave effect on game start
 * - Smooth transitions between states
 * - Optimized for full screen width on phones
 */
@Composable
fun BubbleGrid(
    gameState: GameState,
    selectedShape: BubbleShape = BubbleShape.CIRCLE,
    zoomLevel: Float = 1f,
    onBubblePress: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    soundManager: PopSoundManager? = null
) {
    // Grid configuration: 5-6-7-8-7-6-5 honeycomb pattern
    val rowConfigs = remember { listOf(5, 6, 7, 8, 7, 6, 5) }

    // Track if entrance animation should play
    var hasAnimatedEntrance by remember { mutableStateOf(false) }
    var triggerEntrance by remember { mutableStateOf(false) }

    // Reset entrance animation when game starts/restarts
    LaunchedEffect(gameState.isPlaying, gameState.bubbles) {
        if (gameState.isPlaying && !hasAnimatedEntrance) {
            delay(100) // Small delay before animation starts
            triggerEntrance = true
            hasAnimatedEntrance = true
        } else if (!gameState.isPlaying) {
            // Reset for next game
            hasAnimatedEntrance = false
            triggerEntrance = false
        }
    }

    // Slice bubbles into rows
    val bubbleRows = remember(gameState.bubbles) {
        val rows = mutableListOf<List<Bubble>>()
        var currentIndex = 0
        rowConfigs.forEach { count ->
            if (currentIndex < gameState.bubbles.size) {
                val end = (currentIndex + count).coerceAtMost(gameState.bubbles.size)
                rows.add(gameState.bubbles.subList(currentIndex, end))
                currentIndex = end
            }
        }
        rows
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val gapWidth = 4.dp
        val maxRowBubbles = 8

        // Take full available width (minus small safety margin)
        val availableWidth = maxWidth * 0.98f

        // Calculate gap space for widest row
        val totalGapWidth = gapWidth * (maxRowBubbles - 1)

        // Calculate bubble size to fill width
        val baseBubbleSize = (availableWidth - totalGapWidth) / maxRowBubbles
        val finalBubbleSize = baseBubbleSize * zoomLevel

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            bubbleRows.forEachIndexed { rowIndex, rowBubbles ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gapWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowBubbles.forEachIndexed { colIndex, bubble ->
                        // Calculate staggered delay based on position
                        // Creates a wave effect from top-left to bottom-right
                        val entranceDelay = calculateEntranceDelay(rowIndex, colIndex, rowBubbles.size)

                        AnimatedBubbleWrapper(
                            shouldAnimate = triggerEntrance,
                            delayMs = entranceDelay,
                            rowIndex = rowIndex
                        ) {
                            Bubble(
                                bubble = bubble,
                                shape = selectedShape,
                                bubbleSize = finalBubbleSize,
                                onClick = onBubblePress,
                                enabled = enabled && bubble.canBePressed,
                                soundManager = soundManager
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculate entrance delay for wave effect
 * Creates diagonal wave from top to bottom
 */
private fun calculateEntranceDelay(rowIndex: Int, colIndex: Int, rowSize: Int): Int {
    // Base delay increases with row
    val rowDelay = rowIndex * 40

    // Column delay creates horizontal wave
    // Center columns animate slightly before edges for a "bulge" effect
    val centerOffset = kotlin.math.abs(colIndex - rowSize / 2)
    val colDelay = centerOffset * 15

    return rowDelay + colDelay
}

/**
 * Wrapper that animates bubble entrance with drop + bounce effect
 */
@Composable
fun AnimatedBubbleWrapper(
    shouldAnimate: Boolean,
    delayMs: Int,
    rowIndex: Int,
    content: @Composable () -> Unit
) {
    var hasStarted by remember { mutableStateOf(false) }
    var animationComplete by remember { mutableStateOf(false) }

    // Trigger animation after delay
    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate && !hasStarted) {
            delay(delayMs.toLong())
            hasStarted = true
        } else if (!shouldAnimate) {
            hasStarted = false
            animationComplete = false
        }
    }

    // Scale animation (0 -> 1 with bounce)
    val scale by animateFloatAsState(
        targetValue = if (hasStarted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { animationComplete = true },
        label = "entranceScale"
    )

    // Vertical drop animation
    val offsetY by animateFloatAsState(
        targetValue = if (hasStarted) 0f else -60f - (rowIndex * 10f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "entranceOffset"
    )

    // Rotation for playful entrance
    val rotation by animateFloatAsState(
        targetValue = if (hasStarted) 0f else -15f + (rowIndex % 2) * 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entranceRotation"
    )

    // Alpha for fade in
    val alpha by animateFloatAsState(
        targetValue = if (hasStarted) 1f else 0f,
        animationSpec = tween(200),
        label = "entranceAlpha"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationY = offsetY
            rotationZ = rotation
            this.alpha = alpha
        }
    ) {
        content()
    }
}

/**
 * Alternative grid with "ripple" entrance effect
 * Bubbles animate from center outward
 */
@Composable
fun BubbleGridRippleEntrance(
    gameState: GameState,
    selectedShape: BubbleShape = BubbleShape.CIRCLE,
    zoomLevel: Float = 1f,
    onBubblePress: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    soundManager: PopSoundManager? = null
) {
    val rowConfigs = remember { listOf(5, 6, 7, 8, 7, 6, 5) }

    var triggerEntrance by remember { mutableStateOf(false) }

    LaunchedEffect(gameState.isPlaying) {
        if (gameState.isPlaying) {
            delay(100)
            triggerEntrance = true
        } else {
            triggerEntrance = false
        }
    }

    val bubbleRows = remember(gameState.bubbles) {
        val rows = mutableListOf<List<Bubble>>()
        var currentIndex = 0
        rowConfigs.forEach { count ->
            if (currentIndex < gameState.bubbles.size) {
                val end = (currentIndex + count).coerceAtMost(gameState.bubbles.size)
                rows.add(gameState.bubbles.subList(currentIndex, end))
                currentIndex = end
            }
        }
        rows
    }

    // Calculate center of grid for ripple effect
    val centerRow = rowConfigs.size / 2
    val centerCol = rowConfigs[centerRow] / 2

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val gapWidth = 4.dp
        val maxRowBubbles = 8
        val availableWidth = maxWidth * 0.98f
        val totalGapWidth = gapWidth * (maxRowBubbles - 1)
        val baseBubbleSize = (availableWidth - totalGapWidth) / maxRowBubbles
        val finalBubbleSize = baseBubbleSize * zoomLevel

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            bubbleRows.forEachIndexed { rowIndex, rowBubbles ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gapWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowBubbles.forEachIndexed { colIndex, bubble ->
                        // Calculate distance from center for ripple
                        val distanceFromCenter = kotlin.math.sqrt(
                            ((rowIndex - centerRow) * (rowIndex - centerRow) +
                                    (colIndex - rowBubbles.size / 2) * (colIndex - rowBubbles.size / 2)).toFloat()
                        )
                        val entranceDelay = (distanceFromCenter * 60).toInt()

                        RippleBubbleWrapper(
                            shouldAnimate = triggerEntrance,
                            delayMs = entranceDelay
                        ) {
                            Bubble(
                                bubble = bubble,
                                shape = selectedShape,
                                bubbleSize = finalBubbleSize,
                                onClick = onBubblePress,
                                enabled = enabled && bubble.canBePressed,
                                soundManager = soundManager
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ripple animation wrapper - expands from center
 */
@Composable
fun RippleBubbleWrapper(
    shouldAnimate: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    var hasStarted by remember { mutableStateOf(false) }

    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            delay(delayMs.toLong())
            hasStarted = true
        } else {
            hasStarted = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (hasStarted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rippleScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (hasStarted) 1f else 0f,
        animationSpec = tween(150),
        label = "rippleAlpha"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    ) {
        content()
    }
}