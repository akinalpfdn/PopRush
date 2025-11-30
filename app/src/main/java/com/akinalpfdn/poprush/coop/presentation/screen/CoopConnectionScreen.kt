package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import timber.log.Timber
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.EndpointInfo
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.ui.theme.PastelColors

// Theme Colors
private val DarkGray = Color(0xFF1C1917)
private val LightGray = Color(0xFFF5F5F4)
private val SuccessGreen = Color(0xFF22C55E)
private val ErrorRed = Color(0xFFEF4444)

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
    // Debug: Log connection state and discovered endpoints
    LaunchedEffect(connectionState, discoveredEndpoints) {
        Timber.d("CoopConnectionScreen: connectionState = $connectionState, isHost = $isHost")
        Timber.d("CoopConnectionScreen: discoveredEndpoints.size = ${discoveredEndpoints.size}")
    }
    // Debug: Log when CoopConnectionScreen is rendered
    LaunchedEffect(Unit) {
        Timber.d("CoopConnectionScreen: isHost = $isHost, connectionState = $connectionState")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // -- Top Bar --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .background(LightGray, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkGray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // Small Player Badge
                PlayerBadge(playerName, playerColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -- Dynamic Content --
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (connectionState) {
                    ConnectionState.DISCONNECTED -> {
                        // FIX: Directly show loading.
                        // This prevents the "middle part" (buttons) from ever appearing.
                        // It also fixes the glitch because this view is neutral.
                        ConnectingView(message = "Initializing...")
                    }
                    ConnectionState.ADVERTISING -> {
                        AdvertisingView(
                            onStopHosting = onStopHosting
                        )
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

        // -- Error Toast --
        errorMessage?.let { message ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default,
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
        PulseAnimation(color = DarkGray)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Hosting...",
            color = DarkGray,
            fontSize = 24.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Waiting for players to join",
            color = Color.Gray,
            fontSize = 16.sp,
            fontFamily = FontFamily.Default,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStopHosting,
            colors = ButtonDefaults.buttonColors(
                containerColor = LightGray,
                contentColor = ErrorRed
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(50.dp)
        ) {
            Text("Cancel Hosting", fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DiscoveryView(
    endpoints: List<EndpointInfo>,
    onConnect: (String) -> Unit,
    onStopDiscovery: () -> Unit
) {
    // Debug: Log when DiscoveryView receives endpoints
    LaunchedEffect(endpoints) {
        Timber.d("DiscoveryView: endpoints.size = ${endpoints.size}, endpoints = $endpoints")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "NEARBY GAMES",
            color = DarkGray,
            fontSize = 24.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (endpoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(LightGray, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DarkGray, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scanning...",
                        color = Color.Gray,
                        fontFamily = FontFamily.Default
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

        Button(
            onClick = onStopDiscovery,
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Stop Searching", fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun ConnectingView(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = DarkGray,
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            color = DarkGray,
            fontSize = 20.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConnectedView(isHost: Boolean, onDisconnect: () -> Unit, onStartGame: () -> Unit) {
    // Debug: Log isHost value when ConnectedView is rendered
    LaunchedEffect(isHost) {
        Timber.tag("COOP_CONNECTION").d("🏠 CONNECTED_VIEW: isHost = $isHost")
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connected!",
            color = DarkGray,
            fontSize = 28.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Start Game Button (Host only)
        Timber.tag("COOP_CONNECTION").d("🏠 BUTTON_VISIBILITY: isHost = $isHost, shouldShowStartButton = ${isHost}")
        if (isHost) {
            Button(
                onClick = {
                    Timber.tag("COOP_CONNECTION").d("🎮 START_GAME_CLICKED: Host clicked Start Game button")
                    onStartGame()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Start Game", fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Timber.tag("COOP_CONNECTION").d("🏠 NO_START_BUTTON: Client device, isHost = $isHost")
        }

        // Disconnect Button (both players can disconnect)
        Button(
            onClick = onDisconnect,
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Disconnect", fontFamily = FontFamily.Default, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------
// UI Components
// -------------------------------------------------------------------------

@Composable
private fun PlayerBadge(name: String, color: BubbleColor) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(LightGray, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
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
    }
}

@Composable
private fun EndpointItem(endpoint: EndpointInfo, onClick: () -> Unit) {
    // Debug: Log when endpoint item is clicked
    LaunchedEffect(Unit) {
        Timber.d("EndpointItem created for: ${endpoint.name} (${endpoint.id})")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray, RoundedCornerShape(16.dp))
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
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                fontSize = 16.sp
            )
            endpoint.getPlayerColor()?.let { color ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(PastelColors.getColor(color), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Color", fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.Default)
                }
            }
        }
        Icon(
            imageVector = Icons.Default.NavigateNext,
            contentDescription = "Join",
            tint = DarkGray
        )
    }
}

@Composable
private fun PulseAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .background(color.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = alpha), CircleShape)
        )
        Icon(
            imageVector = Icons.Default.WifiTethering,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}