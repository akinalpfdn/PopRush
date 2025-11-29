package com.akinalpfdn.poprush.game.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import com.akinalpfdn.poprush.core.ui.component.BubbleGrid
import com.akinalpfdn.poprush.game.presentation.component.GameHeader
import com.akinalpfdn.poprush.game.presentation.component.CoopComingSoonToast
import com.akinalpfdn.poprush.game.presentation.component.LoadingOverlay
import com.akinalpfdn.poprush.game.presentation.component.SpeedModeLoadingOverlay
import com.akinalpfdn.poprush.game.presentation.component.PauseButton
import com.akinalpfdn.poprush.game.presentation.component.SettingsOverlay
import com.akinalpfdn.poprush.game.presentation.component.BackConfirmationDialog
import com.akinalpfdn.poprush.game.presentation.GameViewModel
import com.akinalpfdn.poprush.coop.presentation.screen.CoopPlayerSetupScreen
import com.akinalpfdn.poprush.coop.presentation.screen.CoopConnectionScreen
import com.akinalpfdn.poprush.coop.presentation.screen.CoopGameplayScreen
import com.akinalpfdn.poprush.coop.presentation.component.CoopConnectionOverlay
import com.akinalpfdn.poprush.game.presentation.component.CoopConnectionSetupScreen
import kotlin.time.Duration

/**
 * Main game screen that orchestrates the entire PopRush game experience.
 * Handles game state, UI rendering, and user interactions.
 *
 * @param viewModel The game ViewModel for state management
 * @param modifier Additional modifier for the screen
 */
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
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

    // Handle back press for start screens (navigate back in flow)
    BackHandler(enabled = !gameState.isPlaying && !gameState.isGameOver) {
        viewModel.processIntent(GameIntent.NavigateBack)
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
            onDurationChange = { duration -> viewModel.processIntent(GameIntent.UpdateSelectedDuration(duration)) },
            onGameModeSelected = { mode -> viewModel.processIntent(GameIntent.SelectGameMode(mode)) },
            onGameModSelected = { mod -> viewModel.processIntent(GameIntent.SelectGameMod(mod)) },
            onDisconnectCoop = { viewModel.processIntent(GameIntent.DisconnectCoop) },
            onStartCoopConnection = { viewModel.processIntent(GameIntent.StartCoopConnection) },
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

        // TODO: Implement CoopComingSoonToast when needed
        // CoopComingSoonToast(
        //     isVisible = gameState.showCoopConnectionDialog,
        //     modifier = Modifier.fillMaxSize()
        // )

        // TODO: Implement SpeedModeLoadingOverlay when needed
        // SpeedModeLoadingOverlay(
        //     isVisible = false, // gameState.isLoadingSpeedMode when implemented
        //     modifier = Modifier.fillMaxSize()
        // )

        // Coop connection overlay
        CoopConnectionOverlay(
            isVisible = gameState.showCoopConnectionDialog,
            playerName = "", // Will be handled by coop state
            playerColor = com.akinalpfdn.poprush.core.domain.model.BubbleColor.ROSE, // Will be handled by coop state
            opponentColor = null, // Will be handled by coop state
            connectionState = com.akinalpfdn.poprush.coop.domain.model.ConnectionState.DISCONNECTED, // Will be handled by coop state
            discoveredEndpoints = emptyList(), // Will be handled by coop state
            errorMessage = gameState.coopErrorMessage,
            isHost = false, // Will be handled by coop state
            onPlayerNameChange = { viewModel.processIntent(GameIntent.UpdateCoopPlayerName(it)) },
            onColorSelected = { viewModel.processIntent(GameIntent.UpdateCoopPlayerColor(it)) },
            onPlayerSetupComplete = { viewModel.processIntent(GameIntent.StartCoopConnection) },
            onStartHosting = { viewModel.processIntent(GameIntent.StartHosting) },
            onStopHosting = { viewModel.processIntent(GameIntent.StopHosting) },
            onStartDiscovery = { viewModel.processIntent(GameIntent.StartDiscovery) },
            onStopDiscovery = { viewModel.processIntent(GameIntent.StopDiscovery) },
            onConnectToEndpoint = { viewModel.processIntent(GameIntent.ConnectToEndpoint(it)) },
            onDisconnect = { viewModel.processIntent(GameIntent.DisconnectCoop) },
            onClose = { viewModel.processIntent(GameIntent.CloseCoopConnection) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Main game content including header, bubble grid, and controls.
 */
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
private fun GameContent(
    gameState: GameState,
    onStartGame: () -> Unit,
    onBubblePress: (Int) -> Unit,
    onToggleSettings: () -> Unit,
    onSelectShape: (BubbleShape) -> Unit,
    onTogglePause: () -> Unit,
    onDurationChange: (Duration) -> Unit,
    onGameModeSelected: (com.akinalpfdn.poprush.core.domain.model.GameMode) -> Unit,
    onGameModSelected: (com.akinalpfdn.poprush.core.domain.model.GameMod) -> Unit,
    onDisconnectCoop: () -> Unit,
    onStartCoopConnection: () -> Unit,
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
            // Always show the appropriate main content with smooth transitions
            when {
                !gameState.isPlaying && !gameState.isGameOver -> {
                    AnimatedContent(
                        targetState = gameState.currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300, easing = LinearEasing)) with
                            fadeOut(animationSpec = tween(200, easing = LinearEasing))
                        },
                        contentKey = { it },
                        label = "startScreenTransition"
                    ) { currentScreen ->
                        when (currentScreen) {
                            StartScreenFlow.MODE_SELECTION -> {
                                ModeSelectionScreen(
                                    onModeSelected = onGameModeSelected,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            StartScreenFlow.MOD_PICKER -> {
                                ModPickerScreen(
                                    onModSelected = onGameModSelected,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            StartScreenFlow.GAME_SETUP -> {
                                StartScreen(
                                    gameState = gameState,
                                    onStartGame = onStartGame,
                                    onDurationChange = onDurationChange,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            StartScreenFlow.COOP_CONNECTION -> {
                                // Coop connection setup
                                CoopConnectionSetupScreen(
                                    onShowConnectionDialog = { onStartCoopConnection() },
                                    onBack = { onGameModeSelected(com.akinalpfdn.poprush.core.domain.model.GameMode.SINGLE) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                else -> {
                    if (gameState.isCoopMode && gameState.coopState != null) {
                        // Coop gameplay
                        CoopGameplayScreen(
                            coopGameState = gameState.coopState,
                            onBubbleClick = onBubblePress,
                            onPause = onTogglePause,
                            onDisconnect = onDisconnectCoop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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

