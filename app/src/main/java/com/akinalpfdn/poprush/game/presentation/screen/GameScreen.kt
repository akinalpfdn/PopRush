package com.akinalpfdn.poprush.game.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import com.akinalpfdn.poprush.core.ui.component.*
import com.akinalpfdn.poprush.game.presentation.component.*
import com.akinalpfdn.poprush.game.presentation.GameViewModel
import com.akinalpfdn.poprush.coop.presentation.screen.CoopGameplayScreen
import com.akinalpfdn.poprush.coop.presentation.component.CoopConnectionOverlay
import com.akinalpfdn.poprush.coop.presentation.component.CoopPermissionsDialog
import com.akinalpfdn.poprush.coop.presentation.permission.rememberCoopPermissionManager
import kotlin.time.Duration

/**
 * Enhanced main game screen with:
 * - Animated background
 * - Pop sound effects  
 * - Bubble pop animations with particles
 * - Entrance animations
 * - Smooth transitions
 * 
 * Note: Combo system (floating scores, combo counter, screen shake) 
 * is commented out - not useful for fast-paced gameplay
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val discoveredEndpoints by viewModel.discoveredEndpoints.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    
    val context = LocalContext.current
    val permissionManager = rememberCoopPermissionManager(context)
    
    // Sound manager for pop effects
    val soundManager = rememberPopSoundManager()
    
    // === COMBO SYSTEM DISABLED ===
    // Commented out - not useful for fast-paced gameplay
    // State for floating scores
    // var floatingScores by remember { mutableStateOf(listOf<FloatingScoreData>()) }
    
    // Combo tracking
    // var currentCombo by remember { mutableStateOf(0) }
    // var lastPopTime by remember { mutableStateOf(0L) }
    // var showComboBurst by remember { mutableStateOf(false) }
    
    // Screen shake trigger
    // var shouldShake by remember { mutableStateOf(false) }
    // === END COMBO SYSTEM ===
    
    // Permission states
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var hasCheckedPermissions by remember { mutableStateOf(false) }
    var pendingCoopSelection by remember { mutableStateOf(false) }
    
    // Debug logging
    LaunchedEffect(discoveredEndpoints) {
        Timber.d("GameScreen: discoveredEndpoints.size = ${discoveredEndpoints.size}")
    }
    
    LaunchedEffect(gameState.coopState?.isHost) {
        Timber.d("GameScreen: gameState.coopState?.isHost = ${gameState.coopState?.isHost}")
    }
    
    // Permission check on first load
    LaunchedEffect(hasCheckedPermissions) {
        if (!hasCheckedPermissions) {
            hasCheckedPermissions = true
            if (!permissionManager.hasPermissions) {
                showPermissionsDialog = true
            }
        }
    }
    
    // === COMBO SYSTEM DISABLED ===
    // Reset combo if game ends or restarts
    // LaunchedEffect(gameState.isPlaying) {
    //     if (!gameState.isPlaying) {
    //         currentCombo = 0
    //         floatingScores = emptyList()
    //     }
    // }
    
    // Combo timeout checker
    // LaunchedEffect(lastPopTime) {
    //     if (lastPopTime > 0) {
    //         delay(1500) // 1.5 second combo window
    //         if (System.currentTimeMillis() - lastPopTime >= 1500) {
    //             currentCombo = 0
    //         }
    //     }
    // }
    
    // Combo burst display timer
    // LaunchedEffect(showComboBurst) {
    //     if (showComboBurst) {
    //         delay(1000)
    //         showComboBurst = false
    //     }
    // }
    
    // Screen shake reset
    // LaunchedEffect(shouldShake) {
    //     if (shouldShake) {
    //         delay(300)
    //         shouldShake = false
    //     }
    // }
    // === END COMBO SYSTEM ===
    
    fun openAppSettings() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    // Simple bubble press handler - just play sound and send intent
    fun handleBubblePress(bubbleId: Int) {
        // Send intent to ViewModel
        if (gameState.isCoopMode) {
            viewModel.processIntent(GameIntent.CoopClaimBubble(bubbleId))
        } else {
            viewModel.processIntent(GameIntent.PressBubble(bubbleId))
        }
    }
    
    // === COMBO SYSTEM DISABLED ===
    // Remove completed floating scores
    // fun removeFloatingScore(id: String) {
    //     floatingScores = floatingScores.filter { it.id != id }
    // }
    // === END COMBO SYSTEM ===
    
    // Back handlers
    BackHandler(enabled = gameState.showSettings) {
        viewModel.processIntent(GameIntent.ToggleSettings)
    }
    
    BackHandler(enabled = gameState.isPlaying && !gameState.isGameOver) {
        viewModel.processIntent(GameIntent.ShowBackConfirmation)
    }
    
    BackHandler(enabled = !gameState.isPlaying && !gameState.isGameOver) {
        viewModel.processIntent(GameIntent.NavigateBack)
    }
    
    // Main screen with animated background
    Box(modifier = modifier.fillMaxSize()) {
        // Animated background layer
        AnimatedGameBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 25
        )
        
        // Main game content (no screen shake wrapper - disabled)
        GameContent(
            gameState = gameState,
            soundManager = soundManager,
            onStartGame = { viewModel.processIntent(GameIntent.StartGame) },
            onBubblePress = ::handleBubblePress,
            onToggleSettings = { viewModel.processIntent(GameIntent.ToggleSettings) },
            onSelectShape = { shape -> viewModel.processIntent(GameIntent.SelectShape(shape)) },
            onTogglePause = { viewModel.processIntent(GameIntent.TogglePause) },
            onDurationChange = { duration -> viewModel.processIntent(GameIntent.UpdateSelectedDuration(duration)) },
            onGameModeSelected = { mode -> viewModel.processIntent(GameIntent.SelectGameMode(mode)) },
            onGameModSelected = { mod -> viewModel.processIntent(GameIntent.SelectGameMod(mod)) },
            onDisconnectCoop = { viewModel.processIntent(GameIntent.DisconnectCoop) },
            onStartCoopConnection = { viewModel.processIntent(GameIntent.StartCoopConnection) },
            onStartMatch = { viewModel.processIntent(GameIntent.StartCoopMatch) },
            permissionManager = permissionManager,
            showPermissionsDialog = showPermissionsDialog,
            onShowPermissionsDialog = { showPermissionsDialog = true },
            onHidePermissionsDialog = { showPermissionsDialog = false },
            pendingCoopSelection = pendingCoopSelection,
            setPendingCoopSelection = { pendingCoopSelection = it },
            openAppSettings = { openAppSettings() },
            modifier = Modifier.fillMaxSize()
        )
        
        // === COMBO SYSTEM DISABLED ===
        // Floating scores layer
        // FloatingScoreManager(
        //     scores = floatingScores,
        //     onScoreComplete = ::removeFloatingScore,
        //     modifier = Modifier.fillMaxSize()
        // )
        
        // Combo indicator (top center when playing)
        // if (gameState.isPlaying && !gameState.isGameOver) {
        //     Box(
        //         modifier = Modifier
        //             .fillMaxWidth()
        //             .padding(top = 120.dp),
        //         contentAlignment = Alignment.TopCenter
        //     ) {
        //         ComboIndicator(
        //             combo = currentCombo
        //         )
        //     }
        //     
        //     // Combo burst text
        //     Box(
        //         modifier = Modifier.fillMaxSize(),
        //         contentAlignment = Alignment.Center
        //     ) {
        //         ComboBurstText(
        //             combo = currentCombo,
        //             isVisible = showComboBurst
        //         )
        //     }
        // }
        // === END COMBO SYSTEM ===
        
        // Settings button with subtle animation
        val infiniteTransition = rememberInfiniteTransition(label = "settingsBtn")
        val settingsPulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "settingsPulse"
        )
        
        IconButton(
            onClick = { viewModel.processIntent(GameIntent.ToggleSettings) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .graphicsLayer {
                    scaleX = settingsPulse
                    scaleY = settingsPulse
                }
                .background(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF57534E)
            )
        }
        
        // Bottom credit text
        Text(
            text = "MADE BY MOVI",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            color = Color(0xFFA8A29E),
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
        
        // Game over overlay
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
        
        // Coop connection overlay
        CoopConnectionOverlay(
            isVisible = gameState.showCoopConnectionDialog,
            playerName = gameState.coopState?.localPlayerName ?: "Player",
            playerColor = gameState.coopState?.localPlayerColor ?: com.akinalpfdn.poprush.core.domain.model.BubbleColor.ROSE,
            opponentColor = gameState.coopState?.opponentPlayerColor,
            connectionState = gameState.coopState?.let {
                when (it.connectionPhase) {
                    com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase.DISCONNECTED -> com.akinalpfdn.poprush.coop.domain.model.ConnectionState.DISCONNECTED
                    com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase.ADVERTISING -> com.akinalpfdn.poprush.coop.domain.model.ConnectionState.ADVERTISING
                    com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase.DISCOVERING -> com.akinalpfdn.poprush.coop.domain.model.ConnectionState.DISCOVERING
                    com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase.CONNECTING -> com.akinalpfdn.poprush.coop.domain.model.ConnectionState.CONNECTING
                    com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase.CONNECTED -> com.akinalpfdn.poprush.coop.domain.model.ConnectionState.CONNECTED
                    com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase.ERROR -> com.akinalpfdn.poprush.coop.domain.model.ConnectionState.DISCONNECTED
                }
            } ?: com.akinalpfdn.poprush.coop.domain.model.ConnectionState.DISCONNECTED,
            discoveredEndpoints = discoveredEndpoints,
            errorMessage = gameState.coopErrorMessage,
            isHost = gameState.coopState?.isHost ?: false,
            onPlayerNameChange = { viewModel.processIntent(GameIntent.UpdateCoopPlayerName(it)) },
            onColorSelected = { viewModel.processIntent(GameIntent.UpdateCoopPlayerColor(it)) },
            onPlayerSetupComplete = { viewModel.processIntent(GameIntent.StartCoopConnection) },
            onStartHosting = { viewModel.processIntent(GameIntent.StartHosting) },
            onStopHosting = { viewModel.processIntent(GameIntent.StopHosting) },
            onStartDiscovery = { viewModel.processIntent(GameIntent.StartDiscovery) },
            onStopDiscovery = { viewModel.processIntent(GameIntent.StopDiscovery) },
            onConnectToEndpoint = { viewModel.processIntent(GameIntent.ConnectToEndpoint(it)) },
            onDisconnect = { viewModel.processIntent(GameIntent.DisconnectCoop) },
            onStartGame = { viewModel.processIntent(GameIntent.StartCoopGame) },
            onClose = { viewModel.processIntent(GameIntent.CloseCoopConnection) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Main game content with enhanced visuals
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun GameContent(
    gameState: GameState,
    soundManager: PopSoundManager,
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
    onStartMatch: () -> Unit,
    permissionManager: com.akinalpfdn.poprush.coop.presentation.permission.CoopPermissionManager,
    showPermissionsDialog: Boolean,
    onShowPermissionsDialog: () -> Unit,
    onHidePermissionsDialog: () -> Unit,
    pendingCoopSelection: Boolean,
    setPendingCoopSelection: (Boolean) -> Unit,
    openAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (gameState.isCoopMode && gameState.coopState != null) {
            // Coop mode header handled separately
        } else {
            // Enhanced game header
            GameHeader(
                gameState = gameState,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Main content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                !gameState.isPlaying && !gameState.isGameOver && 
                (!gameState.isCoopMode || gameState.coopState == null || 
                 gameState.coopState.gamePhase == com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase.WAITING) -> {
                    
                    // Start screen flow with smooth transitions
                    AnimatedContent(
                        targetState = gameState.currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300, easing = LinearEasing)) togetherWith
                            fadeOut(animationSpec = tween(200, easing = LinearEasing))
                        },
                        contentKey = { it },
                        label = "startScreenTransition"
                    ) { currentScreen ->
                        when (currentScreen) {
                            StartScreenFlow.MODE_SELECTION -> {
                                ModeSelectionScreen(
                                    onModeSelected = { mode ->
                                        if (mode == com.akinalpfdn.poprush.core.domain.model.GameMode.COOP) {
                                            if (permissionManager.hasPermissions) {
                                                onGameModeSelected(mode)
                                            } else {
                                                setPendingCoopSelection(true)
                                                onShowPermissionsDialog()
                                            }
                                        } else {
                                            onGameModeSelected(mode)
                                        }
                                    },
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
                        CoopGameplayScreen(
                            coopGameState = gameState.coopState,
                            selectedDuration = gameState.selectedDuration,
                            onBubbleClick = onBubblePress,
                            onPause = onTogglePause,
                            onDisconnect = onDisconnectCoop,
                            onStartMatch = onStartMatch,
                            onDurationChange = onDurationChange,
                            onPlayAgain = {
                                Timber.tag("GAME_SCREEN").d("🔄 PLAY_AGAIN_CLICKED: Resetting coop game")
                                onDisconnectCoop()
                                onStartCoopConnection()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Enhanced bubble grid with sound
                        BubbleGrid(
                            gameState = gameState,
                            selectedShape = gameState.selectedShape,
                            zoomLevel = gameState.zoomLevel,
                            onBubblePress = onBubblePress,
                            enabled = gameState.isPlaying && !gameState.isPaused,
                            soundManager = soundManager,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        
        // Pause button with animation
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
        
        // Permissions dialog
        CoopPermissionsDialog(
            isVisible = showPermissionsDialog,
            missingPermissions = permissionManager.getMissingPermissionsDisplay(),
            onRequestPermissions = {
                onHidePermissionsDialog()
                openAppSettings()
            },
            onDismiss = {
                onHidePermissionsDialog()
                permissionManager.refreshPermissions()
                if (pendingCoopSelection && permissionManager.hasPermissions) {
                    setPendingCoopSelection(false)
                    onGameModeSelected(com.akinalpfdn.poprush.core.domain.model.GameMode.COOP)
                }
            },
            onNotNow = {
                onHidePermissionsDialog()
                setPendingCoopSelection(false)
            }
        )
    }
}