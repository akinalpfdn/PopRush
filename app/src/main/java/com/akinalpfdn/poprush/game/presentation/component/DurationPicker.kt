package com.akinalpfdn.poprush.game.presentation.component

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
import androidx.compose.ui.text.font.FontWeight
import com.akinalpfdn.poprush.ui.theme.AppColors
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.ui.theme.withAlpha
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Compact circular wheel picker for selecting game duration (10-60 seconds).
 */
@Composable
fun DurationPicker(
    selectedDuration: Duration,
    onDurationChange: (Duration) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondsList = (10..60).toList()
    // Reduced height for compactness
    val itemHeight = 40.dp
    val visibleItemsCount = 3

    val initialIndex = remember(selectedDuration) {
        secondsList.indexOf(selectedDuration.inWholeSeconds.toInt())
            .coerceIn(0, secondsList.size - 1)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val centerOffset = layoutInfo.viewportEndOffset / 2
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
        // Compact Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp) // Reduced padding
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Duration",
                tint = AppColors.Text.Label,
                modifier = Modifier.size(16.dp) // Smaller icon
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "GAME DURATION",
                color = AppColors.Text.Label,
                fontSize = 12.sp, // Smaller font
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Circular wheel picker
        Box(
            modifier = Modifier
                .height(itemHeight * visibleItemsCount)
                .width(100.dp), // Slightly narrower
            contentAlignment = Alignment.Center
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        color = AppColors.BluePrimary.withAlpha(0.6f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .align(Alignment.Center)
            )

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                // Calculation adjusts padding exactly for the new height
                contentPadding = PaddingValues(vertical = (itemHeight * visibleItemsCount) / 2 - itemHeight / 2),
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(secondsList) { index, second ->

                    val scale by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val centerOffset = layoutInfo.viewportEndOffset / 2
                            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }

                            if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2
                                val distance = abs(centerOffset - itemCenter)
                                val maxDistance = itemHeight.value * 2
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
                                (1f - (distance / maxDistance) * 0.8f).coerceIn(0.2f, 1f)
                            } else {
                                0.2f
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .scale(scale)
                            .alpha(alpha),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${second}s",
                            color = if (scale > 1.1f) AppColors.DarkGray else AppColors.Text.Label,
                            fontSize = 20.sp,
                            fontWeight = if (scale > 1.1f) FontWeight.Black else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Gradients adjusted to new height
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