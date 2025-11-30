package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
    modifier: Modifier = Modifier
) {
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

            // -- Role Indicator --
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isHost) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isHost) Icons.Default.Add else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isHost) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isHost) "Hosting Game" else "Joining Game",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isHost) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        fontFamily = FontFamily.Default
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -- Dynamic Content --
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (connectionState) {
                    ConnectionState.DISCONNECTED -> {
                        // Show choice view for users to host or join
                        DisconnectedView(
                            isHost = isHost,
                            onStartHosting = onStartHosting,
                            onStartDiscovery = onStartDiscovery
                        )
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
                        ConnectingView()
                    }
                    ConnectionState.CONNECTED -> {
                        ConnectedView(
                            onDisconnect = onDisconnect
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
private fun DisconnectedView(
    isHost: Boolean,
    onStartHosting: () -> Unit,
    onStartDiscovery: () -> Unit
) {
    Timber.d("DisconnectedView: isHost = $isHost")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isHost) "HOSTING GAME" else "JOINING GAME",
            color = DarkGray,
            fontSize = 32.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isHost) "Creating your game room..." else "Searching for nearby games...",
            color = Color.Gray,
            fontSize = 16.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Show appropriate action card based on role
        if (isHost) {
            CoopActionCard(
                title = "Start Hosting",
                subtitle = "Create a room for others to join",
                icon = Icons.Default.WifiTethering,
                onClick = onStartHosting
            )
        } else {
            CoopActionCard(
                title = "Search for Games",
                subtitle = "Find nearby game rooms",
                icon = Icons.Default.Search,
                onClick = onStartDiscovery
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show the other option as secondary
        if (isHost) {
            OutlinedButton(
                onClick = onStartDiscovery,
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch to Joining",
                    fontFamily = FontFamily.Default
                )
            }
        } else {
            OutlinedButton(
                onClick = onStartHosting,
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch to Hosting",
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

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
private fun ConnectingView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = DarkGray,
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Connecting...",
            color = DarkGray,
            fontSize = 20.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConnectedView(onDisconnect: () -> Unit) {
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
            .height(100.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGray),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
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
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun EndpointItem(endpoint: EndpointInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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