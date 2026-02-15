package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.presentation.component.ColoredCoopTitle
import com.akinalpfdn.poprush.coop.presentation.component.CoopBubbleButton
import com.akinalpfdn.poprush.game.presentation.component.DurationPicker
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import kotlin.time.Duration

/**
 * Setup screen for Coop Mode, shown to the host before starting the match.
 * Allows selecting game duration.
 */
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
            .background(AppColors.Background.Overlay),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Colored title
            ColoredCoopTitle(text = "COOP SETUP", fontSize = 30)

            Spacer(modifier = Modifier.height(16.dp))

            // Player names
            Text(
                text = "${coopGameState.localPlayerName} vs ${coopGameState.opponentPlayerName.ifEmpty { "Opponent" }}",
                color = AppColors.Text.Label,
                fontSize = 16.sp,
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

            // Start match button
            CoopBubbleButton(
                text = "START MATCH",
                icon = Icons.Default.PlayArrow,
                baseColor = AppColors.Bubble.Mint,
                pressedColor = AppColors.Bubble.MintPressed,
                onClick = onStartMatch
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Disconnect button
            CoopBubbleButton(
                text = "DISCONNECT",
                icon = Icons.Default.LinkOff,
                baseColor = AppColors.Bubble.Coral,
                pressedColor = AppColors.Bubble.CoralPressed,
                onClick = onDisconnect,
                isSmall = true
            )
        }
    }
}
