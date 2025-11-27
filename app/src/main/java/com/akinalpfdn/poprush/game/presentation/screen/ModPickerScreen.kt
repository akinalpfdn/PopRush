package com.akinalpfdn.poprush.game.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameMod

/**
 * Mod picker screen shown after selecting Single Player mode.
 * Allows users to choose between Classic and Speed game modes.
 *
 * @param onModSelected Callback when a game mod is selected
 * @param modifier Additional modifier for the screen
 */
@Composable
fun ModPickerScreen(
    onModSelected: (GameMod) -> Unit,
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = "Choose Game Mode",
                color = Color(0xFF44403C),
                fontSize = 28.sp,
                fontFamily = roundedFont,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mod cards
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Classic Mode Card
                ModCard(
                    mod = GameMod.CLASSIC,
                    title = "Classic Mode",
                    description = "Pop bubbles before time runs out!\nChoose your duration and test your reflexes.",
                    icon = Icons.Default.Timer,
                    onClick = { onModSelected(GameMod.CLASSIC) }
                )

                // Speed Mode Card
                ModCard(
                    mod = GameMod.SPEED,
                    title = "Speed Mode",
                    description = "Cells light up randomly!\nClick them before they all activate.\nSpeed increases over time!",
                    icon = Icons.Default.Speed,
                    onClick = { onModSelected(GameMod.SPEED) }
                )
            }
        }
    }
}

/**
 * Individual mod selection card with icon, title, and description.
 */
@Composable
private fun ModCard(
    mod: GameMod,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var elevation by remember { mutableStateOf(8.dp) }
    var borderColor by remember { mutableStateOf(Color(0xFFD1D5DB)) } // gray-300

    // Handle the press animation with LaunchedEffect
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
            elevation = 8.dp
            borderColor = Color(0xFFD1D5DB)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isPressed = true
                elevation = 12.dp
                borderColor = when (mod) {
                    GameMod.CLASSIC -> Color(0xFF86EFAC) // green-400
                    GameMod.SPEED -> Color(0xFFFCA5A5) // red-400
                }
                onClick()
            }
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                spotColor = when (mod) {
                    GameMod.CLASSIC -> Color(0xFF86EFAC) // green-400
                    GameMod.SPEED -> Color(0xFFFCA5A5) // red-400
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with mode-specific background
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = when (mod) {
                            GameMod.CLASSIC -> Color(0xFFDCFCE7) // green-100
                            GameMod.SPEED -> Color(0xFFFEE2E2) // red-100
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = when (mod) {
                        GameMod.CLASSIC -> Color(0xFF166534) // green-800
                        GameMod.SPEED -> Color(0xFF991B1B) // red-800
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF1C1917),
                    fontSize = 20.sp,
                    fontFamily = roundedFont,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    color = Color(0xFF6B7280), // gray-500
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start
                )
            }

            // Arrow indicator with animation
            androidx.compose.animation.AnimatedVisibility(
                visible = isPressed,
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut(animationSpec = tween(100))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Select",
                    tint = when (mod) {
                        GameMod.CLASSIC -> Color(0xFF86EFAC) // green-400
                        GameMod.SPEED -> Color(0xFFFCA5A5) // red-400
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            if (!isPressed) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Select",
                    tint = Color(0xFF9CA3AF), // gray-400
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}