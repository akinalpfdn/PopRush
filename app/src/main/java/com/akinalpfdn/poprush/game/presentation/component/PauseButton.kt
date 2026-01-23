package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.akinalpfdn.poprush.ui.theme.AppColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pause/Play button that allows users to pause and resume the game.
 *
 * @param isPaused Whether the game is currently paused
 * @param onPauseToggle Callback when the pause/play button is pressed
 * @param modifier Additional modifier for the button
 */
@Composable
fun PauseButton(
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(min = 120.dp, max = 200.dp) // Constrain width
            .height(44.dp)
            .clickable { onPauseToggle() },
        shape = CircleShape,
        color = if (isPaused) {
            AppColors.Button.Success
        } else {
            AppColors.Button.Warning
        },
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) {
                        Icons.Default.PlayArrow
                    } else {
                        Icons.Default.Pause
                    },
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = AppColors.Button.Text,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isPaused) "RESUME" else "PAUSE",
                    color = AppColors.Button.Text,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}