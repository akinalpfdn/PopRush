package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.presentation.component.BubbleIconCircle
import com.akinalpfdn.poprush.coop.presentation.component.ColoredCoopTitle
import com.akinalpfdn.poprush.coop.presentation.component.CoopBubbleButton
import com.akinalpfdn.poprush.coop.presentation.extensions.*
import com.akinalpfdn.poprush.core.ui.component.BubbleGrid
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

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
    androidx.activity.compose.BackHandler {
        if (coopGameState.currentPhase == CoopGamePhase.FINISHED) {
            onDisconnect()
        } else {
            onDisconnect()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Primary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        Box(modifier = Modifier.fillMaxSize()) {
                            PlayingPhaseContent(
                                coopGameState = coopGameState,
                                onBubbleClick = { },
                                onPause = { },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.3f)
                            )
                            PausedPhaseContent(
                                onResume = onPause,
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
        CompactGameHUD(
            localName = coopGameState.localPlayerName,
            localScore = coopGameState.localPlayerScore,
            localColor = coopGameState.localPlayerColor,
            remoteName = coopGameState.remotePlayerName,
            remoteScore = coopGameState.remotePlayerScore,
            remoteColor = coopGameState.remotePlayerColor,
            timeRemaining = coopGameState.timeRemaining,
            onPause = onPause
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                color = AppColors.Bubble.Grape,
                strokeWidth = 5.dp,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            ColoredCoopTitle(text = title, fontSize = 20)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = AppColors.Text.Label,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
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

            CoopBubbleButton(
                text = "CANCEL",
                icon = Icons.Default.Close,
                baseColor = AppColors.Bubble.Peach,
                pressedColor = AppColors.Bubble.PeachPressed,
                onClick = onDisconnect,
                isSmall = true
            )
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
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bubble-style card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
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
                        center = Offset(size.width * 0.5f, size.height * 0.3f),
                        radius = size.width * 1.2f
                    ),
                    cornerRadius = CornerRadius(28.dp.toPx())
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.Bubble.SkyBlueGlow.withAlpha(0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.15f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.2f, size.height * 0.15f)
                )
            }

            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BubbleIconCircle(
                    icon = Icons.Default.PauseCircle,
                    baseColor = AppColors.Bubble.Grape,
                    size = 72
                )

                ColoredCoopTitle(text = "PAUSED")

                Spacer(modifier = Modifier.height(8.dp))

                CoopBubbleButton(
                    text = "RESUME",
                    icon = Icons.Default.PlayArrow,
                    baseColor = AppColors.Bubble.Mint,
                    pressedColor = AppColors.Bubble.MintPressed,
                    onClick = onResume
                )

                CoopBubbleButton(
                    text = "QUIT GAME",
                    icon = Icons.Default.Close,
                    baseColor = AppColors.Bubble.Coral,
                    pressedColor = AppColors.Bubble.CoralPressed,
                    onClick = onDisconnect,
                    isSmall = true
                )
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

    val resultTitle = if (isDraw) "DRAW!" else if (isLocalWinner) "YOU WON!" else "YOU LOST!"
    val trophyColor = when {
        isDraw -> AppColors.Bubble.Lemon
        isLocalWinner -> AppColors.Bubble.Mint
        else -> AppColors.Bubble.Coral
    }
    val trophyIcon = if (isDraw) Icons.Default.Handshake else Icons.Default.EmojiEvents

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background.Primary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Trophy bubble
            BubbleIconCircle(
                icon = trophyIcon,
                baseColor = trophyColor,
                size = 88
            )

            // Colored result title
            ColoredCoopTitle(text = resultTitle, fontSize = 30)

            // Scores row
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Text.Label,
                    fontFamily = NunitoFontFamily
                )

                ScoreResultItem(
                    name = coopGameState.remotePlayerName,
                    score = coopGameState.remotePlayerScore,
                    color = coopGameState.remotePlayerColor,
                    isWinner = !isLocalWinner && !isDraw
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CoopBubbleButton(
                    text = "PLAY AGAIN",
                    icon = Icons.Default.Refresh,
                    baseColor = AppColors.Bubble.Mint,
                    pressedColor = AppColors.Bubble.MintPressed,
                    onClick = onPlayAgain
                )

                CoopBubbleButton(
                    text = "EXIT",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    baseColor = AppColors.Bubble.SkyBlue,
                    pressedColor = AppColors.Bubble.SkyBluePressed,
                    onClick = onExit,
                    isSmall = true
                )
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
    timeRemaining: Long,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AppColors.Bubble.Grape.withAlpha(0.1f),
                spotColor = AppColors.Bubble.Grape.withAlpha(0.1f)
            )
            .background(AppColors.Background.Secondary, RoundedCornerShape(24.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CompactScorePill(localName, localScore, localColor)

        // Timer bubble
        Box(
            modifier = Modifier
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .background(AppColors.Background.Primary, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${timeRemaining}s",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = if (timeRemaining <= 10) AppColors.Bubble.Coral else AppColors.Text.Primary,
                fontFamily = NunitoFontFamily
            )
        }

        CompactScorePill(remoteName, remoteScore, remoteColor)
    }
}

@Composable
private fun CompactScorePill(
    name: String,
    score: Int,
    color: BubbleColor
) {
    val playerColor = PastelColors.getColor(color)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(50),
                ambientColor = playerColor.withAlpha(0.2f),
                spotColor = playerColor.withAlpha(0.2f)
            )
            .background(AppColors.Background.Primary, RoundedCornerShape(50))
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .shadow(1.dp, CircleShape, ambientColor = playerColor, spotColor = playerColor)
                .background(playerColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = name.ifEmpty { "Player" }.take(8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Text.Label,
                lineHeight = 10.sp,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "$score",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.Text.Primary,
                lineHeight = 16.sp,
                fontFamily = NunitoFontFamily
            )
        }
    }
}

@Composable
private fun PlayerStatusPill(name: String, color: BubbleColor, isReady: Boolean) {
    val playerColor = PastelColors.getColor(color)
    val displayColor = if (isReady) playerColor else AppColors.StonePale

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isReady) 6.dp else 2.dp,
                    shape = CircleShape,
                    ambientColor = displayColor.withAlpha(0.3f),
                    spotColor = displayColor.withAlpha(0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lighterColor = displayColor.withAlpha(0.85f).compositeOver(Color.White)
                val darkerColor = displayColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(lighterColor, displayColor, darkerColor),
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
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(28.dp)
            )
            if (isReady) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
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
            color = AppColors.Text.Primary,
            fontFamily = NunitoFontFamily
        )
    }
}

@Composable
private fun ScoreResultItem(name: String, score: Int, color: BubbleColor, isWinner: Boolean) {
    val playerColor = PastelColors.getColor(color)
    val scale = if (isWinner) 1.15f else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        // Score in bubble circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = if (isWinner) 10.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = playerColor.withAlpha(0.3f),
                    spotColor = playerColor.withAlpha(0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lighterColor = playerColor.withAlpha(0.85f).compositeOver(Color.White)
                val darkerColor = playerColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(lighterColor, playerColor, darkerColor),
                        center = Offset(size.width * 0.35f, size.height * 0.3f),
                        radius = size.width * 0.7f
                    )
                )
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

            Text(
                text = "$score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.Text.OnDark,
                fontFamily = NunitoFontFamily
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Text.Primary,
            fontFamily = NunitoFontFamily
        )

        if (isWinner) {
            Text(
                text = "WINNER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = playerColor,
                fontFamily = NunitoFontFamily,
                letterSpacing = 1.sp
            )
        }
    }
}
