package com.akinalpfdn.poprush.coop.presentation.screen

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.presentation.extensions.*
import com.akinalpfdn.poprush.core.ui.component.BubbleGrid
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily

// Theme Colors
private val DarkGray = Color(0xFF1C1917)
private val LightGray = Color(0xFFF5F5F4)
private val SoftWhite = Color(0xFFFAFAFA)

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
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle back press logic
    androidx.activity.compose.BackHandler {
        if (coopGameState.currentPhase == CoopGamePhase.FINISHED) {
            onDisconnect()
        } else {
            // In other phases, back might act as disconnect or pause
            onDisconnect()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                                title = "SETUP IN PROGRESS",
                                message = "Host is configuring the match...",
                                onDisconnect = onDisconnect,
                                coopGameState = coopGameState
                            )
                        }
                    }
                    CoopGamePhase.WAITING -> {
                        WaitingPhaseContent(
                            title = "WAITING FOR PLAYERS",
                            message = "Game will start automatically...",
                            onDisconnect = onDisconnect,
                            coopGameState = coopGameState
                        )
                    }
                    CoopGamePhase.PLAYING -> {
                        PlayingPhaseContent(
                            coopGameState = coopGameState,
                            onBubbleClick = onBubbleClick,
                            onPause = onPause,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CoopGamePhase.PAUSED -> {
                        // Show gameplay in background, overlaid by Pause menu
                        Box(modifier = Modifier.fillMaxSize()) {
                            PlayingPhaseContent(
                                coopGameState = coopGameState,
                                onBubbleClick = { }, // Disable clicks
                                onPause = { },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.3f) // Dimmed
                            )
                            PausedPhaseContent(
                                onResume = onPause, // Re-using pause toggle as resume
                                onDisconnect = onDisconnect
                            )
                        }
                    }
                    CoopGamePhase.FINISHED -> {
                        FinishedPhaseContent(
                            coopGameState = coopGameState,
                            onPlayAgain = onPlayAgain,
                            onExit = onDisconnect
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Phase Contents
// -------------------------------------------------------------------------

@Composable
private fun PlayingPhaseContent(
    coopGameState: CoopGameState,
    onBubbleClick: (Int) -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Compact HUD (Heads Up Display)
        CompactGameHUD(
            localName = coopGameState.localPlayerName,
            localScore = coopGameState.localPlayerScore,
            localColor = coopGameState.localPlayerColor,
            remoteName = coopGameState.remotePlayerName,
            remoteScore = coopGameState.remotePlayerScore,
            remoteColor = coopGameState.remotePlayerColor,
            // Convert Duration to Long (seconds) here
            timeRemaining = coopGameState.timeRemaining,
            onPause = onPause
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Game Grid Container - Removed Card Background
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BubbleGrid(
                gameState = coopGameState.toGameState(),
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
private fun WaitingPhaseContent(
    title: String,
    message: String,
    coopGameState: CoopGameState,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = DarkGray,
                strokeWidth = 6.dp,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = DarkGray,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = NunitoFontFamily
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Player Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerStatusPill(
                    name = coopGameState.localPlayerName,
                    color = coopGameState.localPlayerColor,
                    isReady = true
                )
                PlayerStatusPill(
                    name = coopGameState.remotePlayerName,
                    color = coopGameState.remotePlayerColor,
                    isReady = coopGameState.remotePlayerName.isNotEmpty()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            TextButton(onClick = onDisconnect) {
                Text("Cancel", color = Color.Gray, fontFamily = NunitoFontFamily)
            }
        }
    }
}

@Composable
private fun PausedPhaseContent(
    onResume: () -> Unit,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PauseCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = DarkGray
                )

                Text(
                    text = "PAUSED",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkGray,
                    fontFamily = NunitoFontFamily
                )

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Resume",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                }

                TextButton(onClick = onDisconnect) {
                    Text(
                        "Quit Game",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontFamily = NunitoFontFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun FinishedPhaseContent(
    coopGameState: CoopGameState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val isLocalWinner = coopGameState.localPlayerScore > coopGameState.remotePlayerScore
    val isDraw = coopGameState.localPlayerScore == coopGameState.remotePlayerScore

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Result Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isDraw) Icons.Default.Handshake else Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = if (isDraw) DarkGray else PastelColors.getColor(if (isLocalWinner) coopGameState.localPlayerColor else coopGameState.remotePlayerColor)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isDraw) "DRAW!" else if (isLocalWinner) "YOU WON!" else "YOU LOST!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkGray,
                    fontFamily = NunitoFontFamily,
                    letterSpacing = 2.sp
                )
            }

            // Scores Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreResultItem(
                    name = coopGameState.localPlayerName,
                    score = coopGameState.localPlayerScore,
                    color = coopGameState.localPlayerColor,
                    isWinner = isLocalWinner && !isDraw
                )

                Text(
                    "VS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = NunitoFontFamily
                )

                ScoreResultItem(
                    name = coopGameState.remotePlayerName,
                    score = coopGameState.remotePlayerScore,
                    color = coopGameState.remotePlayerColor,
                    isWinner = !isLocalWinner && !isDraw
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGray),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Play Again",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGray),
                    border = androidx.compose.foundation.BorderStroke(2.dp, LightGray),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Exit to Menu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// UI Components
// -------------------------------------------------------------------------

@Composable
private fun CompactGameHUD(
    localName: String,
    localScore: Int,
    localColor: BubbleColor,
    remoteName: String,
    remoteScore: Int,
    remoteColor: BubbleColor,
    timeRemaining: Long, // Changed to Long as requested
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray, RoundedCornerShape(24.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Local Player
        CompactScorePill(localName, localScore, localColor)

        // Timer (Center)
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${timeRemaining}s", // Displaying raw long as seconds
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = DarkGray,
                fontFamily = NunitoFontFamily
            )
        }

        // Remote Player (or Pause)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompactScorePill(remoteName, remoteScore, remoteColor)

        }
    }
}

@Composable
private fun CompactScorePill(
    name: String,
    score: Int,
    color: BubbleColor
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(50))
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(PastelColors.getColor(color), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = name.ifEmpty { "Player" }.take(8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                lineHeight = 10.sp,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "$score",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkGray,
                lineHeight = 16.sp,
                fontFamily = NunitoFontFamily
            )
        }
    }
}

@Composable
private fun PlayerStatusPill(name: String, color: BubbleColor, isReady: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (isReady) PastelColors.getColor(color) else LightGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            if (isReady) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .offset(x = 4.dp, y = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isReady) name else "Waiting...",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            fontFamily = NunitoFontFamily
        )
    }
}

@Composable
private fun ScoreResultItem(name: String, score: Int, color: BubbleColor, isWinner: Boolean) {
    val scale = if (isWinner) 1.2f else 1f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(PastelColors.getColor(color), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = NunitoFontFamily
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            fontFamily = NunitoFontFamily
        )
        if (isWinner) {
            Text(
                text = "WINNER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = PastelColors.getColor(color),
                fontFamily = NunitoFontFamily
            )
        }
    }
}