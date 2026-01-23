package com.akinalpfdn.poprush.game.presentation.screen

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.akinalpfdn.poprush.ui.theme.AppColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.game.presentation.component.DurationPicker
import kotlin.time.Duration

/**
 * Start screen shown when the game hasn't started yet.
 * Displays the game title, duration picker, and play button.
 *
 * @param gameState Current game state containing selected duration
 * @param onStartGame Callback when the play button is pressed
 * @param onDurationChange Callback when duration selection changes
 * @param modifier Additional modifier for the screen
 */
// This grabs the built-in Android "sans-serif-rounded" font
val NunitoFontFamily = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        Typeface.create("sans-serif-rounded", Typeface.BOLD)
    )
)
@Composable
fun StartScreen(
    gameState: GameState,
    onStartGame: () -> Unit,
    onDurationChange: (Duration) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Overlay), // Semi-transparent white overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game title
            Text(
                text = "POP RUSH",
                color = AppColors.StoneGray,
                fontSize = 36.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode title
            Text(
                text = gameState.selectedMod.displayName,
                color = AppColors.GrayMedium,
                fontSize = 20.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Duration picker - only show for Classic mode
            if (gameState.selectedMod.durationRequired) {
                DurationPicker(
                    selectedDuration = gameState.selectedDuration,
                    onDurationChange = onDurationChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Play button
            PlayButton(
                onClick = onStartGame,
                modifier = Modifier
                    .defaultMinSize(minWidth = 160.dp)
                    .height(56.dp)
            )
        }
    }
}

/**
 * Play button with icon and text.
 */
@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = AppColors.Button.Primary,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 40.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = AppColors.Button.Text,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "PLAY",
                color = AppColors.Button.Text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}