package com.akinalpfdn.poprush.game.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.ui.component.BubbleGrid
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import timber.log.Timber

/**
 * Main game screen that orchestrates the entire PopRush game experience.
 * Handles game state, UI rendering, and user interactions.
 *
 * @param viewModel The game ViewModel for state management
 * @param modifier Additional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main game content
        GameContent(
            gameState = gameState,
            onStartGame = { viewModel.processIntent(GameIntent.StartGame) },
            onBubblePress = { bubbleId -> viewModel.processIntent(GameIntent.PressBubble(bubbleId)) },
            onZoomIn = { viewModel.processIntent(GameIntent.ZoomIn) },
            onZoomOut = { viewModel.processIntent(GameIntent.ZoomOut) },
            onToggleSettings = { viewModel.processIntent(GameIntent.ToggleSettings) },
            onSelectShape = { shape -> viewModel.processIntent(GameIntent.SelectShape(shape)) },
            modifier = Modifier.fillMaxSize()
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
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleSettings: () -> Unit,
    onSelectShape: (BubbleShape) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Game header with score, timer, and high score
        GameHeader(
            gameState = gameState,
            modifier = Modifier.fillMaxWidth()
        )

        // Bubble grid or start/game over screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                !gameState.isPlaying && !gameState.isGameOver -> {
                    StartScreen(
                        onStartGame = onStartGame,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                gameState.isGameOver -> {
                    GameOverScreen(
                        gameState = gameState,
                        onPlayAgain = onStartGame,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    BubbleGrid(
                        gameState = gameState,
                        selectedShape = gameState.selectedShape,
                        bubbleSize = 48f,
                        bubbleSpacing = 12f,
                        zoomLevel = gameState.zoomLevel,
                        onBubblePress = onBubblePress,
                        enabled = gameState.isPlaying && !gameState.isPaused,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Bottom controls
        GameControls(
            gameState = gameState,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            onToggleSettings = onToggleSettings,
            modifier = Modifier.fillMaxWidth()
        )
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
        HeaderItem(
            title = "Score",
            value = gameState.score.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Timer section
        TimerDisplay(
            timeRemaining = gameState.timeDisplay,
            isCritical = gameState.isTimerCritical,
            modifier = Modifier.weight(1f)
        )

        // High score section
        HeaderItem(
            title = "Best",
            value = gameState.highScore.toString(),
            color = PastelColors.getPressedColor(com.akinalpfdn.poprush.core.domain.model.BubbleColor.AMBER),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual header item showing title and value.
 */
@Composable
private fun HeaderItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (title == "Score") Alignment.Start else if (title == "Best") Alignment.End else Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp
        )
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
        Card(
            modifier = Modifier
                .size(64.dp)
                .scale(pulseAnimation),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isCritical) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeRemaining,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isCritical) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
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
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Game title
        Text(
            text = "POP RUSH",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            fontSize = 48.sp,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Speed Challenge",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Play button
        PlayButton(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp)
        )
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
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "PLAY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Trophy icon
        Card(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = PastelColors.getPressedColor(com.akinalpfdn.poprush.core.domain.model.BubbleColor.AMBER)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
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
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Final score
        Text(
            text = gameState.score.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Play again button
        Card(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "TRY AGAIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Game controls including zoom and settings buttons.
 */
@Composable
private fun GameControls(
    gameState: GameState,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings button
        IconButton(
            onClick = onToggleSettings,
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (gameState.showSettings) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    }
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = if (gameState.showSettings) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Zoom out button
        IconButton(
            onClick = onZoomOut,
            enabled = gameState.zoomLevel > 0.5f,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Zoom Out",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Zoom in button
        IconButton(
            onClick = onZoomIn,
            enabled = gameState.zoomLevel < 1.5f,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}