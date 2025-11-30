package com.akinalpfdn.poprush.coop.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.presentation.screen.CoopConnectionScreen
import com.akinalpfdn.poprush.coop.presentation.screen.CoopPlayerSetupScreen
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import timber.log.Timber

// Theme Colors
private val DarkGray = Color(0xFF1C1917)
private val LightGray = Color(0xFFF5F5F4)

private enum class CoopConnectionStep {
    HOST_JOIN_SELECTION,
    PLAYER_SETUP,
    CONNECTION
}

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
    // 1. Hoist state to Overlay level to handle Back Gesture
    var currentStep by remember { mutableStateOf(CoopConnectionStep.HOST_JOIN_SELECTION) }

    // Reset to start when overlay re-opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Only reset if completely disconnected, otherwise we might want to return to active state
            if (connectionState == ConnectionState.DISCONNECTED) {
                currentStep = CoopConnectionStep.HOST_JOIN_SELECTION
            } else {
                currentStep = CoopConnectionStep.CONNECTION
            }
        }
    }

    // Auto-forward to Connection screen if state changes externally (e.g. connected)
    LaunchedEffect(connectionState) {
        if (connectionState != ConnectionState.DISCONNECTED && currentStep == CoopConnectionStep.HOST_JOIN_SELECTION) {
            currentStep = CoopConnectionStep.CONNECTION
        }
    }

    // 2. Define Back Navigation Logic
    val handleBackNavigation = {
        when (currentStep) {
            CoopConnectionStep.PLAYER_SETUP -> {
                currentStep = CoopConnectionStep.HOST_JOIN_SELECTION
            }
            CoopConnectionStep.CONNECTION -> {
                // If active, stop operations before going back
                when (connectionState) {
                    ConnectionState.ADVERTISING -> onStopHosting()
                    ConnectionState.DISCOVERING -> onStopDiscovery()
                    ConnectionState.CONNECTED -> onDisconnect()
                    else -> {}
                }
                currentStep = CoopConnectionStep.HOST_JOIN_SELECTION
            }
            CoopConnectionStep.HOST_JOIN_SELECTION -> {
                onClose()
            }
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = {
                // 3. Intercept System Back Gesture
                handleBackNavigation()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            // Dimmed Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // Compact Card Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f) // Compact width
                        .fillMaxHeight(0.85f), // Compact height
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    CoopConnectionDialogContent(
                        currentStep = currentStep,
                        onStepChange = { currentStep = it },
                        onBack = handleBackNavigation,
                        playerName = playerName,
                        playerColor = playerColor,
                        opponentColor = opponentColor,
                        connectionState = connectionState,
                        discoveredEndpoints = discoveredEndpoints,
                        errorMessage = errorMessage,
                        isHost = isHost,
                        onPlayerNameChange = onPlayerNameChange,
                        onColorSelected = onColorSelected,
                        onStartHosting = onStartHosting,
                        onStartDiscovery = onStartDiscovery,
                        onConnectToEndpoint = onConnectToEndpoint,
                        onDisconnect = onDisconnect,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CoopConnectionDialogContent(
    currentStep: CoopConnectionStep,
    onStepChange: (CoopConnectionStep) -> Unit,
    onBack: () -> Unit,
    playerName: String,
    playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    opponentColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor?,
    connectionState: ConnectionState,
    discoveredEndpoints: List<com.akinalpfdn.poprush.coop.domain.model.EndpointInfo>,
    errorMessage: String?,
    isHost: Boolean,
    onPlayerNameChange: (String) -> Unit,
    onColorSelected: (com.akinalpfdn.poprush.core.domain.model.BubbleColor) -> Unit,
    onStartHosting: () -> Unit,
    onStartDiscovery: () -> Unit,
    onConnectToEndpoint: (String) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                // Fixed: Removed sliding, using simple fade to avoid glitches
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(200))
            },
            label = "connectionStepTransition"
        ) { step ->
            when (step) {
                CoopConnectionStep.HOST_JOIN_SELECTION -> {
                    HostJoinSelectionStep(
                        playerName = playerName,
                        playerColor = playerColor,
                        onHostSelected = {
                            // Auto-start Hosting logic: Switch step AND trigger action
                            onStepChange(CoopConnectionStep.CONNECTION)
                            onStartHosting()
                        },
                        onJoinSelected = {
                            // Auto-start Join logic
                            onStepChange(CoopConnectionStep.CONNECTION)
                            onStartDiscovery()
                        },
                        onCustomizeProfile = { onStepChange(CoopConnectionStep.PLAYER_SETUP) },
                        onBack = onBack
                    )
                }
                CoopConnectionStep.PLAYER_SETUP -> {
                    CoopPlayerSetupScreen(
                        playerName = playerName,
                        playerColor = playerColor,
                        opponentColor = opponentColor,
                        isHost = isHost,
                        onPlayerNameChange = onPlayerNameChange,
                        onColorSelected = onColorSelected,
                        onContinue = { onStepChange(CoopConnectionStep.HOST_JOIN_SELECTION) },
                        onBack = onBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                CoopConnectionStep.CONNECTION -> {
                    CoopConnectionScreen(
                        playerName = playerName,
                        playerColor = playerColor,
                        connectionState = connectionState,
                        discoveredEndpoints = discoveredEndpoints,
                        errorMessage = errorMessage,
                        onStartHosting = onStartHosting,
                        onStopHosting = {
                            onBack()
                        },
                        onStartDiscovery = onStartDiscovery,
                        onStopDiscovery = { onBack() },
                        onConnectToEndpoint = onConnectToEndpoint,
                        onDisconnect = onDisconnect,
                        onBackToMenu = onBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun HostJoinSelectionStep(
    playerName: String,
    playerColor: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    onHostSelected: () -> Unit,
    onJoinSelected: () -> Unit,
    onCustomizeProfile: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // -- Close Button --
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(LightGray, CircleShape)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Close, "Close", tint = DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -- Header --
        Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DarkGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MULTIPLAYER",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkGray,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // -- Profile Pill --
        ProfileSummaryPill(
            name = playerName,
            color = playerColor,
            onClick = onCustomizeProfile
        )

        Spacer(modifier = Modifier.weight(1f))

        // -- Actions --
        CoopActionCard(
            title = "Host Game",
            subtitle = "Create a room",
            icon = Icons.Default.WifiTethering,
            onClick = onHostSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        CoopActionCard(
            title = "Join Game",
            subtitle = "Search nearby",
            icon = Icons.Default.Search,
            onClick = onJoinSelected
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileSummaryPill(
    name: String,
    color: com.akinalpfdn.poprush.core.domain.model.BubbleColor,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(PastelColors.getColor(color), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name.ifEmpty { "Player" },
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CoopActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGray),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(6.dp)
            )
        }
    }
}