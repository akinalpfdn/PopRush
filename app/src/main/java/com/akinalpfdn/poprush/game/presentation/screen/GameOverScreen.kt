package com.akinalpfdn.poprush.game.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.akinalpfdn.poprush.ui.theme.AppColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameState

/**
 * Game over overlay that appears on top of the game when the timer runs out.
 *
 * @param gameState Current game state containing score and other info
 * @param onPlayAgain Callback when the play again button is pressed
 * @param onBackToMenu Callback when the back to menu button is pressed
 * @param modifier Additional modifier for the overlay
 */
@Composable
fun GameOverScreen(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background overlay to darken the game and catch clicks
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.gameOverOverlay())
                .clickable(onClick = { /* Consume clicks, prevent interaction with game */ })
        )

        // Game over content centered on screen
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Background.Card
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Trophy icon with glow effect
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = AppColors.AmberMedium,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents, // Trophy icon
                            contentDescription = "Trophy",
                            tint = AppColors.Button.Text,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Game over title
                Text(
                    text = "Time's Up!",
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Final score
                Text(
                    text = gameState.score.toString(),
                    color = AppColors.RoseLight,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Score label
                Text(
                    text = "Final Score",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Try again button
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 160.dp)
                        .height(52.dp)
                        .background(
                            color = AppColors.Button.Primary,
                            shape = CircleShape
                        )
                        .clickable { onPlayAgain() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = AppColors.Button.Text,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "TRY AGAIN",
                            color = AppColors.Button.Text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back to menu button
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 160.dp)
                        .height(44.dp)
                        .background(
                            color = AppColors.Text.Label,
                            shape = CircleShape
                        )
                        .clickable { onBackToMenu() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Menu",
                            tint = AppColors.Button.Text,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "BACK TO MENU",
                            color = AppColors.Button.Text,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}