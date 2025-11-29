package com.akinalpfdn.poprush.coop.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.presentation.screen.CoopConnectionScreen
import com.akinalpfdn.poprush.coop.presentation.screen.CoopPlayerSetupScreen

/**
 * Overlay component that manages the entire coop connection flow
 * Handles player setup, connection establishment, and error states
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CoopConnectionOverlay(
    isVisible: Boolean,
    playerName: String,
    playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    opponentColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor?,
    connectionState: ConnectionState,
    discoveredEndpoints: List<com.akinalpfdn.poprush.coop.domain.model.EndpointInfo>,
    errorMessage: String?,
    isHost: Boolean,
    onPlayerNameChange: (String) -> Unit,
    onColorSelected: (com.akinalpfdn.poprush.core.domain.model.BubbleColor) -> Unit,
    onPlayerSetupComplete: () -> Unit,
    onStartHosting: () -> Unit,
    onStopHosting: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectToEndpoint: (String) -> Unit,
    onDisconnect: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { /* Prevent dismiss when connection is active */ },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = connectionState == ConnectionState.DISCONNECTED,
                dismissOnClickOutside = false
            )
        ) {
            CoopConnectionDialogContent(
                playerName = playerName,
                playerColor = playerColor,
                opponentColor = opponentColor,
                connectionState = connectionState,
                discoveredEndpoints = discoveredEndpoints,
                errorMessage = errorMessage,
                isHost = isHost,
                onPlayerNameChange = onPlayerNameChange,
                onColorSelected = onColorSelected,
                onPlayerSetupComplete = onPlayerSetupComplete,
                onStartHosting = onStartHosting,
                onStopHosting = onStopHosting,
                onStartDiscovery = onStartDiscovery,
                onStopDiscovery = onStopDiscovery,
                onConnectToEndpoint = onConnectToEndpoint,
                onDisconnect = onDisconnect,
                onClose = onClose,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CoopConnectionDialogContent(
    playerName: String,
    playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    opponentColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor?,
    connectionState: ConnectionState,
    discoveredEndpoints: List<com.akinalpfdn.poprush.coop.domain.model.EndpointInfo>,
    errorMessage: String?,
    isHost: Boolean,
    onPlayerNameChange: (String) -> Unit,
    onColorSelected: (com.akinalpfdn.poprush.core.domain.model.BubbleColor) -> Unit,
    onPlayerSetupComplete: () -> Unit,
    onStartHosting: () -> Unit,
    onStopHosting: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectToEndpoint: (String) -> Unit,
    onDisconnect: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(CoopConnectionStep.SETUP) }

    // Auto-transition from setup to connection when player setup is complete
    LaunchedEffect(connectionState) {
        if (connectionState != ConnectionState.DISCONNECTED && currentStep == CoopConnectionStep.SETUP) {
            currentStep = CoopConnectionStep.CONNECTION
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        @OptIn(ExperimentalAnimationApi::class)
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = EaseOutCubic)
                ) with slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300, easing = EaseInCubic)
                )
            },
            contentKey = { it },
            label = "connectionStepTransition"
        ) { step ->
            when (step) {
                CoopConnectionStep.SETUP -> {
                    PlayerSetupStep(
                        playerName = playerName,
                        playerColor = playerColor,
                        opponentColor = opponentColor,
                        isHost = isHost,
                        onPlayerNameChange = onPlayerNameChange,
                        onColorSelected = onColorSelected,
                        onContinue = onPlayerSetupComplete,
                        onBack = onClose
                    )
                }
                CoopConnectionStep.CONNECTION -> {
                    ConnectionStep(
                        playerName = playerName,
                        playerColor = playerColor,
                        connectionState = connectionState,
                        discoveredEndpoints = discoveredEndpoints,
                        errorMessage = errorMessage,
                        onStartHosting = onStartHosting,
                        onStopHosting = onStopHosting,
                        onStartDiscovery = onStartDiscovery,
                        onStopDiscovery = onStopDiscovery,
                        onConnectToEndpoint = onConnectToEndpoint,
                        onDisconnect = onDisconnect,
                        onBack = {
                            // Only allow going back if disconnected
                            if (connectionState == ConnectionState.DISCONNECTED) {
                                currentStep = CoopConnectionStep.SETUP
                            } else {
                                onClose()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSetupStep(
    playerName: String,
    playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    opponentColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor?,
    isHost: Boolean,
    onPlayerNameChange: (String) -> Unit,
    onColorSelected: (com.akinalpfdn.poprush.core.domain.model.BubbleColor) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        CoopPlayerSetupScreen(
            playerName = playerName,
            playerColor = playerColor,
            opponentColor = opponentColor,
            isHost = isHost,
            onPlayerNameChange = onPlayerNameChange,
            onColorSelected = onColorSelected,
            onContinue = onContinue,
            onBack = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConnectionStep(
    playerName: String,
    playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    connectionState: ConnectionState,
    discoveredEndpoints: List<com.akinalpfdn.poprush.coop.domain.model.EndpointInfo>,
    errorMessage: String?,
    onStartHosting: () -> Unit,
    onStopHosting: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectToEndpoint: (String) -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        CoopConnectionScreen(
            playerName = playerName,
            playerColor = playerColor,
            connectionState = connectionState,
            discoveredEndpoints = discoveredEndpoints,
            errorMessage = errorMessage,
            onStartHosting = onStartHosting,
            onStopHosting = onStopHosting,
            onStartDiscovery = onStartDiscovery,
            onStopDiscovery = onStopDiscovery,
            onConnectToEndpoint = onConnectToEndpoint,
            onDisconnect = onDisconnect,
            onBackToMenu = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Simple confirmation dialog for disconnection
 */
@Composable
fun CoopDisconnectDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Disconnect Game?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Default
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to disconnect from the game? This will end the current session.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Default
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Disconnect",
                        color = MaterialTheme.colorScheme.onError,
                        fontFamily = FontFamily.Default
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        fontFamily = FontFamily.Default
                    )
                }
            }
        )
    }
}

/**
 * Loading overlay for async connection operations
 */
@Composable
fun CoopLoadingOverlay(
    isVisible: Boolean,
    message: String = "Connecting...",
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

/**
 * Success notification for successful connection
 */
@Composable
fun CoopConnectionSuccessToast(
    isVisible: Boolean,
    opponentName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Connected to ${opponentName.ifEmpty { "Player" }}!",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

/**
 * Enum representing the connection flow steps
 */
private enum class CoopConnectionStep {
    SETUP,
    CONNECTION
}