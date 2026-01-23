package com.akinalpfdn.poprush.coop.presentation.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.game.presentation.component.DurationPicker
import kotlin.time.Duration

/**
 * Setup screen for Coop Mode, shown to the host before starting the match.
 * Allows selecting game duration.
 */
val NunitoFontFamily = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        Typeface.create("sans-serif-rounded", Typeface.BOLD)
    )
)

@Composable
fun CoopSetupScreen(
    coopGameState: CoopGameState,
    selectedDuration: Duration,
    onStartMatch: () -> Unit,
    onDurationChange: (Duration) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "COOP SETUP",
                color = Color(0xFF44403C),
                fontSize = 36.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle (Players)
            Text(
                text = "${coopGameState.localPlayerName} vs ${coopGameState.opponentPlayerName.ifEmpty { "Opponent" }}",
                color = Color(0xFF6B7280),
                fontSize = 20.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Duration picker
            DurationPicker(
                selectedDuration = selectedDuration,
                onDurationChange = onDurationChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Start Match button
            PlayButton(
                onClick = onStartMatch,
                modifier = Modifier
                    .defaultMinSize(minWidth = 160.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Disconnect button
            TextButton(onClick = onDisconnect) {
                Text(
                    text = "Disconnect",
                    color = Color(0xFFEF4444), // red-500
                    fontSize = 16.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFF1C1917), // stone-800
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
                contentDescription = "Start Match",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "START MATCH",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
