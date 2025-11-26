package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState

/**
 * Renders the exact 5-6-7-8-7-6-5 honeycomb grid layout.
 * Uses standard Rows/Columns for pixel-perfect flexbox-like alignment.
 */
@Composable
fun BubbleGrid(
    gameState: GameState,
    selectedShape: BubbleShape = BubbleShape.CIRCLE,
    onBubblePress: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 1. UPDATED: Full Grid Configuration (5-6-7-8-7-6-5)
    val rowConfigs = remember { listOf(5, 6, 7, 8, 7, 6, 5) }

    // 2. Slice the bubble list into rows
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

    // Zoom and Pan state
    var scale by remember { mutableFloatStateOf(0.85f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 3. Layout: Standard Column centering Rows of different widths
    Box(
        modifier = modifier
            .fillMaxSize()
            // Detect gestures on the full screen area
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    // Scale translation by zoom level to keep panning natural
                    offset += pan
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // To create the "Hexagonal" nesting effect, vertical spacing must be tighter than horizontal.
            // Math: (Size 48 + Gap 12) * sin(60) ≈ 52. Bubble height is 48, so gap should be ~4dp.
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                // IMPORTANT: Unbounded allows the grid to measure larger than the screen width
                // This creates the "Large Canvas" effect you requested.
                .wrapContentSize(unbounded = true)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            bubbleRows.forEach { rowBubbles ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // Matches React gap-3
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowBubbles.forEach { bubble ->
                        Bubble(
                            bubble = bubble,
                            shape = selectedShape,
                            bubbleSize = 48.dp, // Fixed size
                            onClick = onBubblePress,
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}