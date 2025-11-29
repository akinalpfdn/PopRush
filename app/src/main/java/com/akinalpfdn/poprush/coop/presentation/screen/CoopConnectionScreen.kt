package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.EndpointInfo
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.ui.theme.PastelColors

/**
 * Main coop connection screen for hosting and joining games
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoopConnectionScreen(
    playerName: String,
    playerColor: BubbleColor,
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Back button
        IconButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Player info card
            PlayerInfoCard(
                playerName = playerName,
                playerColor = playerColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection status
            ConnectionStatusCard(
                connectionState = connectionState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main content based on connection state
            when (connectionState) {
                ConnectionState.DISCONNECTED -> {
                    DisconnectedContent(
                        onStartHosting = onStartHosting,
                        onStartDiscovery = onStartDiscovery,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ConnectionState.ADVERTISING -> {
                    AdvertisingContent(
                        onStopHosting = onStopHosting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ConnectionState.DISCOVERING -> {
                    DiscoveringContent(
                        discoveredEndpoints = discoveredEndpoints,
                        onStopDiscovery = onStopDiscovery,
                        onConnectToEndpoint = onConnectToEndpoint,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ConnectionState.CONNECTING -> {
                    ConnectingContent(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ConnectionState.CONNECTED -> {
                    ConnectedContent(
                        onDisconnect = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error message
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerInfoCard(
    playerName: String,
    playerColor: BubbleColor,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = PastelColors.getColor(playerColor),
                        shape = CircleShape
                    )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = playerName.ifEmpty { "Player" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.DISCONNECTED -> "Ready to Connect" to MaterialTheme.colorScheme.outline
        ConnectionState.ADVERTISING -> "Hosting Game" to MaterialTheme.colorScheme.primary
        ConnectionState.DISCOVERING -> "Searching for Games" to MaterialTheme.colorScheme.primary
        ConnectionState.CONNECTING -> "Connecting..." to MaterialTheme.colorScheme.tertiary
        ConnectionState.CONNECTED -> "Connected!" to Color(0xFF4CAF50)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (connectionState) {
                ConnectionState.ADVERTISING, ConnectionState.DISCOVERING -> {
                    ConnectionPulseIndicator(
                        color = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                ConnectionState.CONNECTED -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Connected",
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = statusColor,
                                shape = CircleShape
                            )
                    )
                }
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = statusColor,
                fontFamily = FontFamily.Default
            )
        }
    }
}

@Composable
private fun ConnectionPulseIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = color.copy(alpha = alpha),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun DisconnectedContent(
    onStartHosting: () -> Unit,
    onStartDiscovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Choose Your Role",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        Text(
            text = "Host a game or join an existing one",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Host button
            Button(
                onClick = onStartHosting,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Host Game")
            }

            // Join button
            OutlinedButton(
                onClick = onStartDiscovery,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Join Game")
            }
        }
    }
}

@Composable
private fun AdvertisingContent(
    onStopHosting: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Waiting for Players",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        Text(
            text = "Other players can discover and join your game",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        Button(
            onClick = onStopHosting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Hosting")
        }
    }
}

@Composable
private fun DiscoveringContent(
    discoveredEndpoints: List<EndpointInfo>,
    onStopDiscovery: () -> Unit,
    onConnectToEndpoint: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Available Games",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        if (discoveredEndpoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No games found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(discoveredEndpoints) { endpoint ->
                    EndpointCard(
                        endpoint = endpoint,
                        onConnect = { onConnectToEndpoint(endpoint.id) }
                    )
                }
            }
        }

        Button(
            onClick = onStopDiscovery,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Searching")
        }
    }
}

@Composable
private fun EndpointCard(
    endpoint: EndpointInfo,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = endpoint.getPlayerName(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Default
                )

                endpoint.getPlayerColor()?.let { color ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Color:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Default
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = PastelColors.getColor(color),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            IconButton(
                onClick = onConnect,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Connect",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ConnectingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )

        Text(
            text = "Establishing Connection",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        Text(
            text = "Please wait while we connect to the game...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
private fun ConnectedContent(
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF4CAF50)
        )

        Text(
            text = "Connection Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF4CAF50),
            fontFamily = FontFamily.Default
        )

        Text(
            text = "Ready to start the game",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default
        )

        Button(
            onClick = onDisconnect,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disconnect")
        }
    }
}