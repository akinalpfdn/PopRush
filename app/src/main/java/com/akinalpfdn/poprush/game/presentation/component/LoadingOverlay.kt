package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily

/**
 * Loading overlay component shown during speed mode initialization.
 *
 * @param isVisible Whether the loading overlay should be visible
 * @param message Optional loading message to display
 * @param modifier Additional modifier for the overlay
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Initializing Speed Mode...",
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated loading indicator
                LoadingIndicator()

                // Loading message
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Animated loading indicator with rotating circles.
 */
@Composable
private fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingAnimation")

    // Rotating animation for outer circle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing animation for inner circle
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
                .background(
                    color = Color(0xFF60A5FA).copy(alpha = 0.3f), // blue-400
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF60A5FA).copy(alpha = 0.6f), // blue-400
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(
                            color = Color(0xFF3B82F6), // blue-600
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Speed mode specific loading overlay with custom styling.
 */
@Composable
fun SpeedModeLoadingOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    LoadingOverlay(
        isVisible = isVisible,
        message = "Starting Speed Mode...",
        modifier = modifier
    )
}