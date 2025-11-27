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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = Color(0xFFD1D5DB), // gray-300
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = Color(0xFFF3F4F6), // gray-100
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF1C1917), // stone-800
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

            // Arrow indicator
            Icon(
                imageVector = Icons.Default.PlayArrow, // Reuse existing icon
                contentDescription = "Select",
                tint = Color(0xFF9CA3AF), // gray-400
                modifier = Modifier.size(24.dp)
            )
        }
    }
}