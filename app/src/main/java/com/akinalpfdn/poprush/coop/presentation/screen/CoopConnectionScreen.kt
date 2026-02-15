package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import timber.log.Timber
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.EndpointInfo
import com.akinalpfdn.poprush.coop.presentation.component.BubbleIconButton
import com.akinalpfdn.poprush.coop.presentation.component.BubbleIconCircle
import com.akinalpfdn.poprush.coop.presentation.component.ColoredCoopTitle
import com.akinalpfdn.poprush.coop.presentation.component.CoopBubbleButton
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoopConnectionScreen(
    playerName: String,
    playerColor: BubbleColor,
    isHost: Boolean = false,
    connectionState: ConnectionState,
    discoveredEndpoints: List<EndpointInfo>,
    errorMessage: String?,
    onStartHosting: () -> Unit,
    onStopHosting: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectToEndpoint: (String) -> Unit,
    onDisconnect: () -> Unit,
    onBackToMenu: () -> Unit,
    onStartGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(connectionState, discoveredEndpoints) {
        Timber.d("CoopConnectionScreen: connectionState = $connectionState, isHost = $isHost")
        Timber.d("CoopConnectionScreen: discoveredEndpoints.size = ${discoveredEndpoints.size}")
    }
    LaunchedEffect(Unit) {
        Timber.d("CoopConnectionScreen: isHost = $isHost, connectionState = $connectionState")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BubbleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    baseColor = AppColors.Bubble.SkyBlue,
                    onClick = onBackToMenu
                )
                Spacer(modifier = Modifier.weight(1f))
                PlayerBadge(playerName, playerColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic content
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (connectionState) {
                    ConnectionState.DISCONNECTED -> {
                        ConnectingView(message = "Initializing...")
                    }
                    ConnectionState.ADVERTISING -> {
                        AdvertisingView(onStopHosting = onStopHosting)
                    }
                    ConnectionState.DISCOVERING -> {
                        DiscoveryView(
                            endpoints = discoveredEndpoints,
                            onConnect = onConnectToEndpoint,
                            onStopDiscovery = onStopDiscovery
                        )
                    }
                    ConnectionState.CONNECTING -> {
                        ConnectingView(message = "Connecting...")
                    }
                    ConnectionState.CONNECTED -> {
                        ConnectedView(
                            isHost = isHost,
                            onDisconnect = onDisconnect,
                            onStartGame = onStartGame
                        )
                    }
                }
            }
        }

        // Error toast
        errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = AppColors.Bubble.Coral.withAlpha(0.3f),
                        spotColor = AppColors.Bubble.Coral.withAlpha(0.3f)
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(AppColors.Bubble.Coral, AppColors.Bubble.CoralPressed),
                            center = Offset(size.width * 0.3f, size.height * 0.3f),
                            radius = size.width * 0.8f
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                Text(
                    text = message,
                    color = AppColors.Text.OnDark,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-Views
// -------------------------------------------------------------------------

@Composable
private fun AdvertisingView(onStopHosting: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BubblePulseAnimation(color = AppColors.Bubble.Grape)

        Spacer(modifier = Modifier.height(32.dp))

        ColoredCoopTitle(text = "HOSTING...", fontSize = 22)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Waiting for players to join",
            color = AppColors.Text.Label,
            fontSize = 14.sp,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(48.dp))

        CoopBubbleButton(
            text = "CANCEL",
            icon = Icons.Default.Close,
            baseColor = AppColors.Bubble.Peach,
            pressedColor = AppColors.Bubble.PeachPressed,
            onClick = onStopHosting,
            isSmall = true
        )
    }
}

@Composable
private fun DiscoveryView(
    endpoints: List<EndpointInfo>,
    onConnect: (String) -> Unit,
    onStopDiscovery: () -> Unit
) {
    LaunchedEffect(endpoints) {
        Timber.d("DiscoveryView: endpoints.size = ${endpoints.size}, endpoints = $endpoints")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ColoredCoopTitle(text = "NEARBY GAMES", fontSize = 22)

        Spacer(modifier = Modifier.height(16.dp))

        if (endpoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = AppColors.Bubble.SkyBlue.withAlpha(0.1f),
                        spotColor = AppColors.Bubble.SkyBlue.withAlpha(0.1f)
                    )
                    .background(AppColors.Background.Secondary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = AppColors.Bubble.SkyBlue,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scanning...",
                        color = AppColors.Text.Label,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(endpoints) { endpoint ->
                    EndpointItem(endpoint) { onConnect(endpoint.id) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CoopBubbleButton(
            text = "STOP SEARCHING",
            icon = Icons.Default.Close,
            baseColor = AppColors.Bubble.Coral,
            pressedColor = AppColors.Bubble.CoralPressed,
            onClick = onStopDiscovery
        )
    }
}

@Composable
private fun ConnectingView(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = AppColors.Bubble.Grape,
            strokeWidth = 5.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            color = AppColors.Text.Secondary,
            fontSize = 18.sp,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConnectedView(isHost: Boolean, onDisconnect: () -> Unit, onStartGame: () -> Unit) {
    LaunchedEffect(isHost) {
        Timber.tag("COOP_CONNECTION").d("CONNECTED_VIEW: isHost = $isHost")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        BubbleIconCircle(
            icon = Icons.Default.CheckCircle,
            baseColor = AppColors.Bubble.Mint,
            size = 88
        )

        Spacer(modifier = Modifier.height(20.dp))

        ColoredCoopTitle(text = "CONNECTED!", fontSize = 26)

        Spacer(modifier = Modifier.height(48.dp))

        // Start Game Button (Host only)
        Timber.tag("COOP_CONNECTION").d("BUTTON_VISIBILITY: isHost = $isHost, shouldShowStartButton = ${isHost}")
        if (isHost) {
            CoopBubbleButton(
                text = "START GAME",
                icon = Icons.Default.PlayArrow,
                baseColor = AppColors.Bubble.Mint,
                pressedColor = AppColors.Bubble.MintPressed,
                onClick = {
                    Timber.tag("COOP_CONNECTION").d("START_GAME_CLICKED: Host clicked Start Game button")
                    onStartGame()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))
        } else {
            Text(
                text = "Waiting for host to start...",
                color = AppColors.Text.Label,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Timber.tag("COOP_CONNECTION").d("NO_START_BUTTON: Client device, isHost = $isHost")
        }

        CoopBubbleButton(
            text = "DISCONNECT",
            icon = Icons.Default.LinkOff,
            baseColor = AppColors.Bubble.Coral,
            pressedColor = AppColors.Bubble.CoralPressed,
            onClick = onDisconnect,
            isSmall = true
        )
    }
}

// -------------------------------------------------------------------------
// UI Components
// -------------------------------------------------------------------------

@Composable
private fun PlayerBadge(name: String, color: BubbleColor) {
    val playerColor = PastelColors.getColor(color)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(50),
                ambientColor = playerColor.withAlpha(0.2f),
                spotColor = playerColor.withAlpha(0.2f)
            )
            .background(AppColors.Background.Secondary, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
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
    }
}

@Composable
private fun EndpointItem(endpoint: EndpointInfo, onClick: () -> Unit) {
    LaunchedEffect(Unit) {
        Timber.d("EndpointItem created for: ${endpoint.name} (${endpoint.id})")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = AppColors.Bubble.SkyBlue.withAlpha(0.15f),
                spotColor = AppColors.Bubble.SkyBlue.withAlpha(0.15f)
            )
            .background(AppColors.Background.Secondary, RoundedCornerShape(18.dp))
            .clickable(onClick = {
                Timber.d("Endpoint clicked: ${endpoint.name} (${endpoint.id})")
                onClick()
            })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = endpoint.getPlayerName(),
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                color = AppColors.Text.Primary,
                fontSize = 16.sp
            )
            endpoint.getPlayerColor()?.let { color ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val playerColor = PastelColors.getColor(color)
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .shadow(1.dp, CircleShape, ambientColor = playerColor, spotColor = playerColor)
                            .background(playerColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Color",
                        fontSize = 12.sp,
                        color = AppColors.Text.Label,
                        fontFamily = NunitoFontFamily
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.NavigateNext,
            contentDescription = "Join",
            tint = AppColors.Bubble.SkyBlue
        )
    }
}

@Composable
private fun BubblePulseAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulse ring
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.withAlpha(alpha * 0.5f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width / 2
                )
            )
        }

        // Inner bubble
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lighterColor = color.withAlpha(0.85f).compositeOver(Color.White)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(lighterColor, color),
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
                imageVector = Icons.Default.WifiTethering,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
