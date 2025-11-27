package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameState

/**
 * Game header showing score, timer, and high score.
 *
 * @param gameState Current game state
 * @param modifier Additional modifier for the header
 */
@Composable
fun GameHeader(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score section
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "SCORE",
                color = Color(0xFF78716C), // stone-400
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            )
            Text(
                text = gameState.score.toString(),
                color = Color(0xFF44403C), // stone-700
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Timer section
        TimerDisplay(
            timeRemaining = gameState.timeDisplay,
            isCritical = gameState.isTimerCritical,
            modifier = Modifier.weight(1f)
        )

        // High score section
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "BEST",
                color = Color(0xFF78716C), // stone-400
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            )
            Text(
                text = gameState.highScore.toString(),
                color = Color(0xFFFCD34D), // amber-300
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Timer display with critical state animation.
 *
 * @param timeRemaining Time remaining as string (e.g., "01:30")
 * @param isCritical Whether the timer is in critical state
 * @param modifier Additional modifier for the timer display
 */
@Composable
private fun TimerDisplay(
    timeRemaining: String,
    isCritical: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isCritical) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(pulseAnimation)
                .background(
                    color = Color(0xFFF5F5F4), // stone-100
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeRemaining.split(":")[1], // Only show seconds
                color = if (isCritical) {
                    Color(0xFFF87171) // rose-400
                } else {
                    Color(0xFF57534E) // stone-600
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}