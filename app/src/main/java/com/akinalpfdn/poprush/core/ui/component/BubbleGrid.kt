package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState

/**
 * Renders the exact 5-6-7-8-7-6-5 honeycomb grid layout.
 * Optimized to fill the screen width on phones by removing restrictive device checks.
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
    // 1. Full Grid Configuration
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

    // 3. Use BoxWithConstraints to calculate size based on the EXACT available width
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(), // Ensures the grid container takes all space
        contentAlignment = Alignment.Center
    ) {
        val gapWidth = 4.dp
        val maxRowBubbles = 8

        // Take the full available width from the parent (minus a small safety padding)
        val availableWidth = maxWidth * 0.98f

        // Calculate total gap space in the widest row (8 bubbles = 7 gaps)
        val totalGapWidth = gapWidth * (maxRowBubbles - 1)

        // Calculate the exact size needed per bubble to fill the width
        // Removed coerceIn upper limit to allow full expansion
        val baseBubbleSize = (availableWidth - totalGapWidth) / maxRowBubbles

        // Apply zoom level (if any)
        val finalBubbleSize = baseBubbleSize * zoomLevel

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            // We do not force a fixed width here; we rely on the bubble size to define the width
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
                            bubbleSize = finalBubbleSize,
                            onClick = onBubblePress,
                            enabled = enabled && bubble.canBePressed
                        )
                    }
                }
            }
        }
    }
}