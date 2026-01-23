package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameState
import timber.log.Timber

/**
 * Game header showing score, timer, and high score in bubble-style pills.
 */
@Composable
fun GameHeader(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    // Debug logging for timer in coop mode
    LaunchedEffect(gameState.timeDisplay, gameState.isCoopMode) {
        if (gameState.isCoopMode) {
            Timber.tag("GAME_HEADER").v("Timer display: timeDisplay=${gameState.timeDisplay}, timeRemaining=${gameState.timeRemaining}, isCoopMode=${gameState.isCoopMode}")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp), // Added status bar padding + breathing room
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score Pill
        HeaderPill(
            icon = Icons.Default.Star,
            value = gameState.score.toString(),
            label = "SCORE",
            baseColor = AppColors.Bubble.SkyBlue,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Timer Pill (Center)
        TimerPill(
            timeRemaining = gameState.timeDisplay,
            isCritical = gameState.isTimerCritical,
            modifier = Modifier.weight(1.2f) // Slightly wider
        )

        Spacer(modifier = Modifier.width(8.dp))

        // High Score Pill
        HeaderPill(
            icon = Icons.Default.EmojiEvents,
            value = gameState.highScore.toString(),
            label = "BEST",
            baseColor = AppColors.Bubble.LemonPressed, // Using Amber for gold feel
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Generic header pill for stats.
 */
@Composable
private fun HeaderPill(
    icon: ImageVector,
    value: String,
    label: String,
    baseColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp) // Compact height
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = baseColor.withAlpha(0.2f),
                spotColor = baseColor.withAlpha(0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bubble Gradient Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.2f, height * 0.2f),
                    radius = width * 1.5f
                ),
                cornerRadius = CornerRadius(16.dp.toPx())
            )

            // Subtle Glass Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.15f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.1f, height * 0.2f),
                    radius = height * 0.6f
                ),
                radius = height * 0.5f,
                center = Offset(width * 0.1f, height * 0.2f)
            )
        }

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Label (Tiny)
            Text(
                text = label,
                color = AppColors.Text.OnDark.withAlpha(0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                letterSpacing = 0.5.sp
            )
            
            // Value Row
            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    tint = AppColors.Text.OnDark,
//                    modifier = Modifier.size(14.dp)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = value,
                    color = AppColors.Text.OnDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = NunitoFontFamily
                )
            }
        }
    }
}

/**
 * Specialized Timer Pill with pulse animation.
 */
@Composable
private fun TimerPill(
    timeRemaining: String,
    isCritical: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timer")
    
    // Scale pulse for critical state
    val scale by animateFloatAsState(
        targetValue = if (isCritical) 1.05f else 1f,
        animationSpec = if (isCritical) {
            infiniteRepeatable(tween(500, easing = EaseInOutSine), RepeatMode.Reverse)
        } else {
            snap()
        },
        label = "scale"
    )

    // Color transition
    val baseColor by animateColorAsState(
        targetValue = if (isCritical) AppColors.Bubble.Coral else AppColors.Bubble.Mint,
        animationSpec = tween(300),
        label = "color"
    )

    Box(
        modifier = modifier
            .height(64.dp) // Just slightly taller to verify hierarchy
            .scale(scale)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bubble Gradient Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.5f, height * 0.2f), // Center light for timer
                    radius = width * 1.2f
                ),
                cornerRadius = CornerRadius(20.dp.toPx())
            )
            
            // Glass Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.15f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.5f, height * 0.1f),
                    radius = width * 0.6f
                ),
                radius = width * 0.5f,
                center = Offset(width * 0.5f, height * 0.1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeRemaining,
                color = AppColors.Text.OnDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = NunitoFontFamily,
                letterSpacing = 1.sp
            )
        }
    }
}