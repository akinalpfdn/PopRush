package com.akinalpfdn.poprush.game.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

/**
 * Game over overlay with bubbly design matching the rest of the app.
 * Features entrance animation, bubble-style card, gradient trophy, and bouncy buttons.
 */
@Composable
fun GameOverScreen(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val isHighScore = gameState.score > 0 &&
            gameState.score >= gameState.highScore

    Box(modifier = modifier.fillMaxSize()) {
        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.gameOverOverlay())
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        )

        // Animated card entrance
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Bubble-style card
            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = AppColors.Bubble.Grape.withAlpha(0.2f),
                        spotColor = AppColors.Bubble.Grape.withAlpha(0.2f)
                    )
            ) {
                // Gradient card background
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height

                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.Background.Primary,
                                AppColors.Background.Secondary,
                                AppColors.Background.Primary
                            ),
                            center = Offset(width * 0.5f, height * 0.3f),
                            radius = width * 1.2f
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx())
                    )

                    // Subtle glass highlight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.Bubble.SkyBlueGlow.withAlpha(0.08f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.2f, height * 0.15f),
                            radius = width * 0.6f
                        ),
                        radius = width * 0.5f,
                        center = Offset(width * 0.2f, height * 0.15f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy bubble
                    TrophyBubble(isHighScore = isHighScore)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Colored title
                    GameOverTitle(isHighScore = isHighScore)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Score display
                    Text(
                        text = gameState.score.toString(),
                        color = AppColors.Text.Primary,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = NunitoFontFamily,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "SCORE",
                        color = AppColors.Text.Label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        letterSpacing = 2.sp
                    )

                    if (isHighScore) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HighScoreBadge()
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Play again button
                    GameOverBubbleButton(
                        text = "PLAY AGAIN",
                        icon = Icons.Default.Refresh,
                        baseColor = AppColors.Bubble.Coral,
                        pressedColor = AppColors.Bubble.CoralPressed,
                        onClick = onPlayAgain
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Back to menu button
                    GameOverBubbleButton(
                        text = "MENU",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        baseColor = AppColors.Bubble.SkyBlue,
                        pressedColor = AppColors.Bubble.SkyBluePressed,
                        onClick = onBackToMenu,
                        isSmall = true
                    )
                }
            }
        }
    }
}

/**
 * Trophy icon inside a bubble-style circle with gradient and glass highlight.
 */
@Composable
private fun TrophyBubble(
    isHighScore: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = if (isHighScore) AppColors.Bubble.Lemon else AppColors.Bubble.Grape

    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyPulse"
    )

    Box(
        modifier = modifier
            .size(88.dp)
            .graphicsLayer {
                scaleX = trophyScale
                scaleY = trophyScale
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(50),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.35f, height * 0.3f),
                    radius = width * 0.7f
                )
            )

            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.45f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.3f, height * 0.28f),
                    radius = width * 0.35f
                ),
                radius = width * 0.25f,
                center = Offset(width * 0.3f, height * 0.28f)
            )
        }

        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = AppColors.Text.OnDark,
            modifier = Modifier.size(40.dp)
        )
    }
}

/**
 * "TIME'S UP!" or "NEW BEST!" with colored characters.
 */
@Composable
private fun GameOverTitle(
    isHighScore: Boolean,
    modifier: Modifier = Modifier
) {
    val text = if (isHighScore) "NEW BEST!" else "TIME'S UP!"
    val colors = listOf(
        AppColors.Bubble.Coral,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Mint,
        AppColors.Bubble.Grape,
        AppColors.Bubble.Lemon,
        AppColors.Bubble.Peach,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Coral,
        AppColors.Bubble.Mint
    )

    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            if (char != ' ') {
                Text(
                    text = char.toString(),
                    color = colors[index % colors.size],
                    fontSize = 30.sp,
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

/**
 * Small bubble badge for new high score.
 */
@Composable
private fun HighScoreBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AppColors.Bubble.Lemon.withAlpha(0.3f),
                spotColor = AppColors.Bubble.Lemon.withAlpha(0.3f)
            )
            .height(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val lighterColor = AppColors.Bubble.Lemon.withAlpha(0.85f).compositeOver(Color.White)
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, AppColors.Bubble.Lemon),
                    center = Offset(size.width * 0.3f, size.height * 0.3f),
                    radius = size.width * 0.8f
                ),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "NEW HIGH SCORE",
                color = AppColors.Text.OnDark,
                fontSize = 11.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Bubble-style button with gradient, glass highlight, and bounce animation.
 */
@Composable
private fun GameOverBubbleButton(
    text: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnScale"
    )

    val buttonHeight = if (isSmall) 46.dp else 52.dp
    val fontSize = if (isSmall) 14.sp else 16.sp
    val iconSize = if (isSmall) 18.dp else 22.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            )
            .defaultMinSize(minWidth = 180.dp)
            .height(buttonHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
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
                    center = Offset(width * 0.3f, height * 0.3f),
                    radius = width * 0.85f
                ),
                cornerRadius = CornerRadius(26.dp.toPx())
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
                fontSize = fontSize,
                letterSpacing = 1.sp
            )
        }
    }
}
