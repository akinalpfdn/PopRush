package com.akinalpfdn.poprush.coop.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.presentation.screen.CoopConnectionScreen
import com.akinalpfdn.poprush.coop.presentation.screen.CoopPlayerSetupScreen
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.withAlpha

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
    onStartGame: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(CoopConnectionStep.HOST_JOIN_SELECTION) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            if (connectionState == ConnectionState.DISCONNECTED) {
                currentStep = CoopConnectionStep.HOST_JOIN_SELECTION
            } else {
                currentStep = CoopConnectionStep.CONNECTION
            }
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState != ConnectionState.DISCONNECTED && currentStep == CoopConnectionStep.HOST_JOIN_SELECTION) {
            currentStep = CoopConnectionStep.CONNECTION
        }
    }

    val handleBackNavigation = {
        when (currentStep) {
            CoopConnectionStep.PLAYER_SETUP -> {
                currentStep = CoopConnectionStep.HOST_JOIN_SELECTION
            }
            CoopConnectionStep.CONNECTION -> {
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
            onDismissRequest = { handleBackNavigation() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // Bubble-style card container
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = AppColors.Bubble.Grape.withAlpha(0.2f),
                            spotColor = AppColors.Bubble.Grape.withAlpha(0.2f)
                        )
                ) {
                    // Gradient card background
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColors.Background.Primary,
                                    AppColors.Background.Secondary,
                                    AppColors.Background.Primary
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.2f),
                                radius = size.width * 1.4f
                            ),
                            cornerRadius = CornerRadius(32.dp.toPx())
                        )

                        // Subtle glass highlight
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColors.Bubble.SkyBlueGlow.withAlpha(0.06f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.2f, size.height * 0.1f),
                                radius = size.width * 0.6f
                            ),
                            radius = size.width * 0.5f,
                            center = Offset(size.width * 0.2f, size.height * 0.1f)
                        )
                    }

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
                        onStartGame = onStartGame,
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
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
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
                            onStepChange(CoopConnectionStep.CONNECTION)
                            onStartHosting()
                        },
                        onJoinSelected = {
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
                        onPlayerNameChange = onPlayerNameChange,
                        onColorSelected = onColorSelected,
                        onContinue = { onStepChange(CoopConnectionStep.HOST_JOIN_SELECTION) },
                        onBack = { onStepChange(CoopConnectionStep.HOST_JOIN_SELECTION) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                CoopConnectionStep.CONNECTION -> {
                    CoopConnectionScreen(
                        playerName = playerName,
                        playerColor = playerColor,
                        isHost = isHost,
                        connectionState = connectionState,
                        discoveredEndpoints = discoveredEndpoints,
                        errorMessage = errorMessage,
                        onStartHosting = onStartHosting,
                        onStopHosting = { onBack() },
                        onStartDiscovery = onStartDiscovery,
                        onStopDiscovery = { onBack() },
                        onConnectToEndpoint = onConnectToEndpoint,
                        onDisconnect = onDisconnect,
                        onStartGame = onStartGame,
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
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            BubbleIconButton(
                icon = Icons.Default.Close,
                baseColor = AppColors.Bubble.SkyBlue,
                onClick = onBack
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Header icon in bubble circle
        BubbleIconCircle(
            icon = Icons.Default.Groups,
            baseColor = AppColors.Bubble.Grape,
            size = 72
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Colored title
        ColoredCoopTitle(text = "MULTIPLAYER")

        Spacer(modifier = Modifier.height(12.dp))

        // Profile pill
        ProfileSummaryPill(
            name = playerName,
            color = playerColor,
            onClick = onCustomizeProfile
        )

        Spacer(modifier = Modifier.weight(1f))

        // Host action card
        CoopActionCard(
            title = "Host Game",
            subtitle = "Create a room",
            icon = Icons.Default.WifiTethering,
            baseColor = AppColors.Bubble.Coral,
            pressedColor = AppColors.Bubble.CoralPressed,
            onClick = onHostSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Join action card
        CoopActionCard(
            title = "Join Game",
            subtitle = "Search nearby",
            icon = Icons.Default.Search,
            baseColor = AppColors.Bubble.SkyBlue,
            pressedColor = AppColors.Bubble.SkyBluePressed,
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
    val playerColor = PastelColors.getColor(color)

    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(50),
                ambientColor = playerColor.withAlpha(0.2f),
                spotColor = playerColor.withAlpha(0.2f)
            )
            .background(AppColors.Background.Secondary, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .shadow(2.dp, CircleShape, ambientColor = playerColor, spotColor = playerColor)
                    .background(playerColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name.ifEmpty { "Player" },
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                color = AppColors.Text.Primary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = AppColors.Text.Label,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CoopActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = if (isPressed) pressedColor
            else baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.25f, height * 0.3f),
                    radius = width * 0.9f
                ),
                cornerRadius = CornerRadius(22.dp.toPx())
            )

            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.35f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.15f, height * 0.25f),
                    radius = height * 0.5f
                ),
                radius = height * 0.35f,
                center = Offset(width * 0.15f, height * 0.25f)
            )
        }

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
                    color = AppColors.Text.OnDark,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    color = AppColors.Text.OnDark.withAlpha(0.7f),
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.withAlpha(0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- Shared bubbly helper composables for coop screens ---

@Composable
internal fun BubbleIconButton(
    icon: ImageVector,
    baseColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = baseColor.withAlpha(0.2f),
                spotColor = baseColor.withAlpha(0.2f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor),
                    center = Offset(size.width * 0.35f, size.height * 0.3f),
                    radius = size.width * 0.7f
                )
            )
            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.withAlpha(0.4f), Color.White.withAlpha(0f)),
                    center = Offset(size.width * 0.3f, size.height * 0.25f),
                    radius = size.width * 0.3f
                ),
                radius = size.width * 0.2f,
                center = Offset(size.width * 0.3f, size.height * 0.25f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Text.OnDark,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun BubbleIconCircle(
    icon: ImageVector,
    baseColor: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(this.size.width * 0.35f, this.size.height * 0.3f),
                    radius = this.size.width * 0.7f
                )
            )
            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.withAlpha(0.45f), Color.White.withAlpha(0f)),
                    center = Offset(this.size.width * 0.3f, this.size.height * 0.28f),
                    radius = this.size.width * 0.35f
                ),
                radius = this.size.width * 0.25f,
                center = Offset(this.size.width * 0.3f, this.size.height * 0.28f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Text.OnDark,
            modifier = Modifier.size((size * 0.45f).dp)
        )
    }
}

@Composable
internal fun ColoredCoopTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 26
) {
    val colors = listOf(
        AppColors.Bubble.Coral,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Mint,
        AppColors.Bubble.Grape,
        AppColors.Bubble.Lemon,
        AppColors.Bubble.Peach,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Coral,
        AppColors.Bubble.Mint,
        AppColors.Bubble.Grape,
        AppColors.Bubble.Lemon
    )

    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            if (char != ' ') {
                Text(
                    text = char.toString(),
                    color = colors[index % colors.size],
                    fontSize = fontSize.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
internal fun CoopBubbleButton(
    text: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit,
    isSmall: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "coopBtnScale"
    )

    val buttonHeight = if (isSmall) 46.dp else 54.dp
    val textSize = if (isSmall) 14.sp else 16.sp
    val iconSize = if (isSmall) 18.dp else 22.dp
    val actualBaseColor = if (enabled) baseColor else AppColors.StonePale
    val actualPressedColor = if (enabled) pressedColor else AppColors.StonePale

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (!enabled) 2.dp else if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = actualBaseColor.withAlpha(0.3f),
                spotColor = actualBaseColor.withAlpha(0.3f)
            )
            .fillMaxWidth()
            .height(buttonHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighterColor = actualBaseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = if (isPressed) actualPressedColor
            else actualBaseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, actualBaseColor, darkerColor),
                    center = Offset(width * 0.3f, height * 0.3f),
                    radius = width * 0.85f
                ),
                cornerRadius = CornerRadius(24.dp.toPx())
            )

            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.4f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.2f, height * 0.3f),
                    radius = height * 0.45f
                ),
                radius = height * 0.3f,
                center = Offset(width * 0.2f, height * 0.3f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = AppColors.Text.OnDark,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = textSize,
                letterSpacing = 1.sp
            )
        }
    }
}
