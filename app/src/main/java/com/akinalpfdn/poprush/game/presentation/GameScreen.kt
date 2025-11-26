package com.akinalpfdn.poprush.game.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.ui.component.BubbleGrid

/**
 * Main game screen that orchestrates the entire PopRush game experience.
 * Handles game state, UI rendering, and user interactions.
 *
 * @param viewModel The game ViewModel for state management
 * @param modifier Additional modifier for the screen
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    // Handle back press for settings
    BackHandler(enabled = gameState.showSettings) {
        viewModel.processIntent(GameIntent.ToggleSettings)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main game content
        GameContent(
            gameState = gameState,
            onStartGame = { viewModel.processIntent(GameIntent.StartGame) },
            onBubblePress = { bubbleId -> viewModel.processIntent(GameIntent.PressBubble(bubbleId)) },
            onToggleSettings = { viewModel.processIntent(GameIntent.ToggleSettings) },
            onSelectShape = { shape -> viewModel.processIntent(GameIntent.SelectShape(shape)) },
            modifier = Modifier.fillMaxSize()
        )

        // Settings button only
        IconButton(
            onClick = { viewModel.processIntent(GameIntent.ToggleSettings) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF57534E) // stone-600
            )
        }

        // Bottom instruction text
        Text(
            text = "TAP THE LIT BUBBLES",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            color = Color(0xFFA8A29E), // stone-400
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        // Settings overlay
        AnimatedVisibility(
            visible = gameState.showSettings,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = EaseInCubic)
            )
        ) {
            SettingsOverlay(
                gameState = gameState,
                onSelectShape = { shape ->
                    viewModel.processIntent(GameIntent.SelectShape(shape))
                    viewModel.processIntent(GameIntent.ToggleSettings)
                },
                onClose = { viewModel.processIntent(GameIntent.ToggleSettings) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Main game content including header, bubble grid, and controls.
 */
@Composable
private fun GameContent(
    gameState: GameState,
    onStartGame: () -> Unit,
    onBubblePress: (Int) -> Unit,
    onToggleSettings: () -> Unit,
    onSelectShape: (BubbleShape) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 32.dp), // Much less horizontal padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game header with score, timer, and high score
        GameHeader(
            gameState = gameState,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bubble grid or start/game over screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Always show the appropriate main content
            when {
                !gameState.isPlaying && !gameState.isGameOver -> {
                    StartScreen(
                        onStartGame = onStartGame,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    BubbleGrid(
                        gameState = gameState,
                        selectedShape = gameState.selectedShape,
                        zoomLevel = gameState.zoomLevel,
                        onBubblePress = onBubblePress,
                        enabled = gameState.isPlaying && !gameState.isPaused,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Show game over overlay on top when game is over
            if (gameState.isGameOver) {
                GameOverScreen(
                    gameState = gameState,
                    onPlayAgain = onStartGame,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Game header showing score, timer, and high score.
 */
@Composable
private fun GameHeader(
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

/**
 * Start screen displayed before game begins.
 */
@Composable
private fun StartScreen(
    onStartGame: () -> Unit,
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
                color = Color(0xFF44403C), // stone-700
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Speed Challenge",
                color = Color(0xFFA8A29E), // stone-400
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "PLAY",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Game over screen showing final score and restart option.
 */
@Composable
private fun GameOverScreen(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background overlay to darken the game
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        // Game over content centered on screen
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp)
                .width(300.dp)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFCECBCB) // stone-900
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
                            color = Color(0xFFFBBF24), // amber-400
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents, // Trophy icon
                        contentDescription = "Trophy",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

                // Game over title
                Text(
                    text = "Time's Up!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Final score
                Text(
                    text = gameState.score.toString(),
                    color = Color(0xFFFCA5A5), // rose-300 (lighter)
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Score label
                Text(
                    text = "Final Score",
                    color = Color(0xFFFFFFFF), // stone-400
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
                            color = Color.White,
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
                            tint = Color(0xFF1C1917), // stone-800
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "TRY AGAIN",
                            color = Color(0xFF1C1917), // stone-800
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}