package com.akinalpfdn.poprush.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Simple circular wheel picker for selecting game duration (10-60 seconds).
 * Inspired by Samsung's alarm picker - just scroll the wheel.
 *
 * @param selectedDuration Currently selected duration
 * @param onDurationChange Callback when duration changes
 * @param modifier Additional modifier for the picker
 */
@Composable
fun DurationPicker(
    selectedDuration: Duration,
    onDurationChange: (Duration) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondsList = (10..60).toList()
    val itemHeight = 60.dp
    val visibleItemsCount = 3 // How many items fit in the box roughly

    // Calculate initial index based on duration
    val initialIndex = remember(selectedDuration) {
        secondsList.indexOf(selectedDuration.inWholeSeconds.toInt())
            .coerceIn(0, secondsList.size - 1)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Notify parent when scrolling stops and the item snaps
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            // Calculate which item is closest to the center
            val layoutInfo = listState.layoutInfo
            val centerOffset = layoutInfo.viewportEndOffset / 2

            // Find the item closest to the center pixel
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - centerOffset)
            }

            centeredItem?.let {
                val index = it.index.coerceIn(0, secondsList.size - 1)
                if (secondsList[index].seconds != selectedDuration) {
                    onDurationChange(secondsList[index].seconds)
                }
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Duration",
                tint = Color(0xFF78716C), // stone-400
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "GAME DURATION",
                color = Color(0xFF78716C), // stone-400
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Circular wheel picker
        Box(
            modifier = Modifier
                .height(itemHeight * visibleItemsCount + 10.dp) // Dynamic height based on items
                .width(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Selection indicator (center line)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .align(Alignment.Center)
            )

            // Scrollable wheel
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                // Padding ensures the first and last items can reach the exact center
                contentPadding = PaddingValues(vertical = (itemHeight * visibleItemsCount) / 2 - itemHeight / 2 + 10.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(secondsList) { index, second ->

                    // Dynamic calculation for Scale and Alpha based on distance from center
                    // Using derivedStateOf allows smooth animation during scroll
                    val scale by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val centerOffset = layoutInfo.viewportEndOffset / 2
                            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }

                            if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2
                                val distance = abs(centerOffset - itemCenter)
                                val maxDistance = itemHeight.value * 2

                                // Interpolate scale: 1.2 at center, dropping to 0.8 at edges
                                (1.2f - (distance / maxDistance) * 0.4f).coerceIn(0.8f, 1.2f)
                            } else {
                                0.8f
                            }
                        }
                    }

                    val alpha by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val centerOffset = layoutInfo.viewportEndOffset / 2
                            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }

                            if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2
                                val distance = abs(centerOffset - itemCenter)
                                val maxDistance = itemHeight.value * 2

                                // Interpolate alpha: 1.0 at center, dropping to 0.2 at edges
                                (1f - (distance / maxDistance) * 0.8f).coerceIn(0.2f, 1f)
                            } else {
                                0.2f
                            }
                        }
                    }

                    // Item Content
                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .scale(scale)
                            .alpha(alpha),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${second}s",
                            color = if (scale > 1.1f) Color(0xFF1C1917) else Color(0xFF78716C),
                            fontSize = 24.sp,
                            fontWeight = if (scale > 1.1f) FontWeight.Black else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Top gradient fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.9f), Color.Transparent)
                        )
                    )
                    .align(Alignment.TopCenter)
            )

            // Bottom gradient fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f))
                        )
                    )
                    .align(Alignment.BottomCenter)
            )
        }


    }
}