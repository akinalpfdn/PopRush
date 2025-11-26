package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState

/**
 * Renders the exact 5-6-7-8-7-6-5 honeycomb grid layout with dynamic bubble sizing.
 * Calculates bubble size based on available screen width for perfect hexagonal hive shape.
 */
@Composable
fun BubbleGrid(
    gameState: GameState,
    selectedShape: BubbleShape = BubbleShape.CIRCLE,
    zoomLevel: Float = 1f,
    onBubblePress: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 1. Full Grid Configuration (5-6-7-8-7-6-5)
    val rowConfigs = remember { listOf(5, 6, 7, 8,7, 6, 5) }

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

    // 3. Calculate dynamic bubble size based on screen width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // The widest row has 8 bubbles, each needs width + gap
    // Total gap for 8 bubbles = 7 gaps * gapWidth
    val gapWidth = 4.dp // Tight gap for hexagonal look
    val maxRowBubbles = 8
    val totalGapWidth = gapWidth * (maxRowBubbles - 1)
    val availableWidthForBubbles = screenWidth * 1.9f // Use 90% of screen width
    val calculatedBubbleSize = (availableWidthForBubbles - totalGapWidth) / maxRowBubbles

    // Ensure minimum size for usability
    val dynamicBubbleSize = calculatedBubbleSize.coerceIn(24.dp, 60.dp)

    // 4. Layout: Fixed canvas with dynamic bubble sizing
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // To create the "Hexagonal" nesting effect, vertical spacing must be tighter than horizontal.
            verticalArrangement = Arrangement.spacedBy(2.dp), // Very tight for hive look
            modifier = Modifier.fillMaxWidth()
        ) {
            bubbleRows.forEach { rowBubbles ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gapWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowBubbles.forEach { bubble ->
                        Bubble(
                            bubble = bubble,
                            shape = selectedShape,
                            bubbleSize = dynamicBubbleSize * zoomLevel,
                            onClick = onBubblePress,
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}