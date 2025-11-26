package com.akinalpfdn.poprush.game.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Handle back press for game (show confirmation when game is playing)
    BackHandler(enabled = gameState.isPlaying && !gameState.isGameOver) {
        viewModel.processIntent(GameIntent.ShowBackConfirmation)
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
            onTogglePause = { viewModel.processIntent(GameIntent.TogglePause) },
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
            text = "MADE BY MOVI",
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

        // Game over overlay on top of everything
        if (gameState.isGameOver) {
            GameOverScreen(
                gameState = gameState,
                onPlayAgain = { viewModel.processIntent(GameIntent.StartGame) },
                onBackToMenu = { viewModel.processIntent(GameIntent.BackToMenu) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Back confirmation dialog
        BackConfirmationDialog(
            onConfirm = { viewModel.processIntent(GameIntent.BackToMenu) },
            onDismiss = { viewModel.processIntent(GameIntent.HideBackConfirmation) },
            isVisible = gameState.showBackConfirmation
        )
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
    onTogglePause: () -> Unit,
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
        }

        // Pause button (only show when game is playing and not game over)
        if (gameState.isPlaying && !gameState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                PauseButton(
                    isPaused = gameState.isPaused,
                    onPauseToggle = onTogglePause
                )
            }
        }
    }
}

