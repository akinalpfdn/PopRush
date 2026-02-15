package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

/**
 * Overlay animation shown when the player completes a wave faster than the target time.
 * Positioned above the game grid so it doesn't block gameplay.
 * Uses Coral palette for high contrast on the light background.
 */
@Composable
fun SpeedBonusOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(
                dampingRatio = 0.4f,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(100)),
        exit = scaleOut(
            targetScale = 1.5f,
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "speedBonus")

        val shimmer by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )

        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
        ) {
            // "FAST!" text with glow + shimmer gradient
            Box {
                // Glow layer
                Text(
                    text = "FAST!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = NunitoFontFamily,
                    color = AppColors.Bubble.Coral.withAlpha(0.4f),
                    letterSpacing = 4.sp,
                    modifier = Modifier
                        .blur(12.dp)
                        .graphicsLayer {
                            scaleX = 1.15f
                            scaleY = 1.15f
                        }
                )

                // Main text with shimmer
                Text(
                    text = "FAST!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = NunitoFontFamily,
                    letterSpacing = 4.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AppColors.Bubble.Coral,
                                AppColors.Bubble.Peach,
                                AppColors.Bubble.Coral
                            ),
                            start = Offset(shimmer * 400f - 80f, 0f),
                            end = Offset(shimmer * 400f + 80f, 0f)
                        ),
                        shadow = Shadow(
                            color = AppColors.Text.Primary.withAlpha(0.3f),
                            offset = Offset(2f, 3f),
                            blurRadius = 5f
                        )
                    )
                )
            }

        }
    }
}
