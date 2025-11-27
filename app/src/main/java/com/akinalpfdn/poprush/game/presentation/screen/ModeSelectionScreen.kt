package com.akinalpfdn.poprush.game.presentation.screen

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameMode

/**
 * Mode selection screen shown at the very start of the game.
 * Allows users to choose between Single Player and Co-op modes.
 *
 * @param onModeSelected Callback when a game mode is selected
 * @param modifier Additional modifier for the screen
 */
@Composable
fun ModeSelectionScreen(
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.6f)), // Semi-transparent white overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game title
            Text(
                text = "POP RUSH",
                color = Color(0xFF44403C),
                fontSize = 36.sp,
                fontFamily = roundedFont,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Choose Your Game Mode",
                color = Color(0xFF6B7280),
                fontSize = 18.sp,
                fontFamily = roundedFont,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Mode buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Single Player Button
                ModeButton(
                    mode = GameMode.SINGLE,
                    icon = Icons.Default.Person,
                    onClick = { onModeSelected(GameMode.SINGLE) },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 140.dp)
                        .height(140.dp)
                )

                // Co-op Button
                ModeButton(
                    mode = GameMode.COOP,
                    icon = Icons.Default.Group,
                    onClick = { onModeSelected(GameMode.COOP) },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 140.dp)
                        .height(140.dp)
                )
            }
        }
    }
}

/**
 * Individual mode button with icon and text.
 */
@Composable
private fun ModeButton(
    mode: GameMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon button
        Box(
            modifier = modifier
                .background(
                    color = Color(0xFF1C1917), // stone-800
                    shape = CircleShape
                )
                .clickable { onClick() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = mode.displayName,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        // Mode label
        Text(
            text = mode.displayName,
            color = Color(0xFF1C1917),
            fontFamily = roundedFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}