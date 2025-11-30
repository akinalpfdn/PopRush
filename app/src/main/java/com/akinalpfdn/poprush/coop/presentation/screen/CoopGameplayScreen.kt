package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.model.CoopBubble
import com.akinalpfdn.poprush.coop.presentation.extensions.toGameState
import com.akinalpfdn.poprush.coop.presentation.extensions.currentPhase
import com.akinalpfdn.poprush.coop.presentation.extensions.localPlayerName
import com.akinalpfdn.poprush.coop.presentation.extensions.localPlayerColor
import com.akinalpfdn.poprush.coop.presentation.extensions.localPlayerScore
import com.akinalpfdn.poprush.coop.presentation.extensions.remotePlayerName
import com.akinalpfdn.poprush.coop.presentation.extensions.remotePlayerColor
import com.akinalpfdn.poprush.coop.presentation.extensions.remotePlayerScore
import com.akinalpfdn.poprush.coop.presentation.extensions.timeRemaining
import com.akinalpfdn.poprush.core.ui.component.BubbleGrid
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.ui.theme.PastelColors

/**
 * Main coop gameplay screen showing the bubble grid and player scores
 */
@Composable
fun CoopGameplayScreen(
    coopGameState: CoopGameState,
    selectedDuration: kotlin.time.Duration,
    onBubbleClick: (Int) -> Unit,
    onPause: () -> Unit,
    onDisconnect: () -> Unit,
    onStartMatch: () -> Unit,
    onDurationChange: (kotlin.time.Duration) -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle back press
    androidx.activity.compose.BackHandler {
        if (coopGameState.currentPhase == CoopGamePhase.PLAYING || 
            coopGameState.currentPhase == CoopGamePhase.PAUSED ||
            coopGameState.currentPhase == CoopGamePhase.WAITING ||
            coopGameState.currentPhase == CoopGamePhase.SETUP) {
            onDisconnect()
        } else if (coopGameState.currentPhase == CoopGamePhase.FINISHED) {
            onDisconnect() // Or navigate back to menu
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Game content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section with scores and controls
            // Top section with scores and controls
            // CoopGameHeader removed as per user request

            Spacer(modifier = Modifier.height(16.dp))

            // Main game area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (coopGameState.currentPhase) {
                    CoopGamePhase.SETUP -> {
                        if (coopGameState.isHost) {
                            CoopSetupScreen(
                                coopGameState = coopGameState,
                                selectedDuration = selectedDuration,
                                onStartMatch = onStartMatch,
                                onDurationChange = onDurationChange,
                                onDisconnect = onDisconnect,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            WaitingPhaseContent(
                                coopGameState = coopGameState,
                                message = "Host is setting up the game...",
                                onDisconnect = onDisconnect,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    CoopGamePhase.WAITING -> {
                        WaitingPhaseContent(
                            coopGameState = coopGameState,
                            onDisconnect = onDisconnect,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CoopGamePhase.PLAYING -> {
                        PlayingPhaseContent(
                            coopGameState = coopGameState,
                            onBubbleClick = onBubbleClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CoopGamePhase.PAUSED -> {
                        PausedPhaseContent(
                            coopGameState = coopGameState,
                            onResume = { /* TODO: Implement resume functionality */ },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CoopGamePhase.FINISHED -> {
                        FinishedPhaseContent(
                            coopGameState = coopGameState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}







@Composable
private fun WaitingPhaseContent(
    coopGameState: CoopGameState,
    message: String = "Waiting for Players",
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default
            )

            Text(
                text = "The game will start automatically when both players are ready",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default
            )

            // Connected players status
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerStatusIndicator(
                    playerName = coopGameState.localPlayerName,
                    playerColor = coopGameState.localPlayerColor,
                    isConnected = true
                )

                PlayerStatusIndicator(
                    playerName = coopGameState.remotePlayerName,
                    playerColor = coopGameState.remotePlayerColor,
                    isConnected = coopGameState.remotePlayerName.isNotEmpty()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Disconnect button
            TextButton(onClick = onDisconnect) {
                Text(
                    text = "Disconnect",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PlayingPhaseContent(
    coopGameState: CoopGameState,
    onBubbleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BubbleGrid(
                gameState = coopGameState.toGameState(), // Convert to regular GameState for the grid
                selectedShape = BubbleShape.CIRCLE,
                zoomLevel = 1.0f,
                onBubblePress = onBubbleClick,
                enabled = coopGameState.currentPhase == CoopGamePhase.PLAYING,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PausedPhaseContent(
    coopGameState: CoopGameState,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Game Paused",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default
                )

                Text(
                    text = "Tap Resume to continue playing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default
                )

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume")
                }
            }
        }
    }
}

@Composable
private fun FinishedPhaseContent(
    coopGameState: CoopGameState,
    modifier: Modifier = Modifier
) {
    val isLocalWinner = coopGameState.localPlayerScore > coopGameState.remotePlayerScore
    val isDraw = coopGameState.localPlayerScore == coopGameState.remotePlayerScore

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Winner announcement
                if (isDraw) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "It's a Draw!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Default
                    )
                } else {
                    val winnerColor = if (isLocalWinner) coopGameState.localPlayerColor else coopGameState.remotePlayerColor
                    val winnerName = if (isLocalWinner) coopGameState.localPlayerName else coopGameState.remotePlayerName

                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = PastelColors.getColor(winnerColor)
                    )
                    Text(
                        text = "${winnerName.ifEmpty { "Player" }} Wins!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PastelColors.getColor(winnerColor),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Default
                    )
                }

                // Final scores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FinalScoreCard(
                        playerName = coopGameState.localPlayerName,
                        playerColor = coopGameState.localPlayerColor,
                        score = coopGameState.localPlayerScore,
                        isWinner = isLocalWinner && !isDraw
                    )

                    FinalScoreCard(
                        playerName = coopGameState.remotePlayerName,
                        playerColor = coopGameState.remotePlayerColor,
                        score = coopGameState.remotePlayerScore,
                        isWinner = !isLocalWinner && !isDraw
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerStatusIndicator(
    playerName: String,
    playerColor: BubbleColor,
    isConnected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isConnected)
                        PastelColors.getColor(playerColor) else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isConnected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Connected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.QuestionMark,
                    contentDescription = "Waiting",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = playerName.ifEmpty { "Player" },
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
private fun FinalScoreCard(
    playerName: String,
    playerColor: BubbleColor,
    score: Int,
    isWinner: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isWinner) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner)
                PastelColors.getColor(playerColor).copy(alpha = 0.2f) else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.scale(scale)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isWinner) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Winner",
                    tint = PastelColors.getColor(playerColor),
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = PastelColors.getColor(playerColor),
                        shape = CircleShape
                    )
            )

            Text(
                text = playerName.ifEmpty { "Player" },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default
            )

            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PastelColors.getColor(playerColor),
                fontFamily = FontFamily.Default
            )
        }
    }
}

