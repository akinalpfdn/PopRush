package com.akinalpfdn.poprush.game.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.game.presentation.component.DurationPicker
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha
import kotlin.time.Duration

/**
 * Title with each character in a different color from our candy palette.
 * "POP RUSH" - 8 characters (including space)
 */
@Composable
private fun ColoredTitle(
    modifier: Modifier = Modifier
) {
    // Colors for each character: P-O-P- -R-U-S-H
    val colors = listOf(
        AppColors.Bubble.Coral,     // P
        AppColors.Bubble.SkyBlue,   // O
        AppColors.Bubble.Mint,      // P
        Color.Transparent,          // (space)
        AppColors.Bubble.Grape,     // R
        AppColors.Bubble.Lemon,     // U
        AppColors.Bubble.Peach,     // S
        AppColors.Bubble.SkyBlue    // H
    )

    val text = "POP RUSH"

    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            if (char != ' ') {
                Text(
                    text = char.toString(),
                    color = colors.getOrElse(index) { AppColors.Text.Primary },
                    fontSize = 42.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

/**
 * Start screen with bubbly, playful design.
 * Visual style matches Bubble.kt - gradients, highlights, colored shadows, animations.
 */
@Composable
fun StartScreen(
    gameState: GameState,
    onStartGame: () -> Unit,
    onDurationChange: (Duration) -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle breathing for title
    val infiniteTransition = rememberInfiniteTransition(label = "startScreen")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleBreath"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game title with colored characters
            ColoredTitle(
                modifier = Modifier.graphicsLayer {
                    scaleX = titleScale
                    scaleY = titleScale
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode badge - bubbly pill style
            BubblePill(
                text = gameState.selectedMod.displayName,
                baseColor = AppColors.Bubble.Mint,
                glowColor = AppColors.Bubble.MintGlow
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Duration picker - only show for Classic mode
            if (gameState.selectedMod.durationRequired) {
                DurationPicker(
                    selectedDuration = gameState.selectedDuration,
                    onDurationChange = onDurationChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))
            }

            // Bubbly play button
            BubbleButton(
                text = "PLAY",
                icon = Icons.Default.PlayArrow,
                baseColor = AppColors.Bubble.Coral,
                pressedColor = AppColors.Bubble.CoralPressed,
                onClick = onStartGame
            )
        }
    }
}

/**
 * Bubbly pill badge - same visual treatment as bubbles.
 * Radial gradient with glass highlight.
 */
@Composable
private fun BubblePill(
    text: String,
    baseColor: Color,
    glowColor: Color,
    modifier: Modifier = Modifier
) {
    val lighterColor = baseColor.withAlpha(0.7f).compositeOver(Color.White)

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            )
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // Bubble-style gradient
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, baseColor),
                    center = Offset(width * 0.3f, height * 0.25f),
                    radius = width * 0.8f
                ),
                cornerRadius = CornerRadius(20.dp.toPx())
            )

            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.4f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.2f, height * 0.3f),
                    radius = height * 0.5f
                ),
                radius = height * 0.35f,
                center = Offset(width * 0.2f, height * 0.3f)
            )
        }

        Text(
            text = text,
            color = AppColors.Text.OnDark,
            fontSize = 14.sp,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

/**
 * Bubbly button with same visual treatment as Bubble.kt:
 * - Radial gradient (light top-left to darker)
 * - Glass highlight spots
 * - Colored shadow
 * - Bounce on press
 * - Subtle wobble idle animation
 */
@Composable
private fun BubbleButton(
    text: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bounce on press (like Bubble.kt)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // Subtle wobble (like Bubble.kt idle animation)
    val infiniteTransition = rememberInfiniteTransition(label = "wobble")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val buttonWidth = 160.dp
    val buttonHeight = 56.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = if (!isPressed) wobbleRotation else 0f
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = baseColor.withAlpha(0.4f),
                spotColor = baseColor.withAlpha(0.4f)
            )
            .width(buttonWidth)
            .height(buttonHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bubble-style background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Create gradient like Bubble.kt
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = if (isPressed) {
                pressedColor
            } else {
                baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))
            }

            // Radial gradient from top-left (like bubble light source)
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.3f, height * 0.3f),
                    radius = width * 0.85f
                ),
                cornerRadius = CornerRadius(28.dp.toPx())
            )

            // Glass highlight spot (like Bubble.kt)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.5f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.22f, height * 0.32f),
                    radius = height * 0.45f
                ),
                radius = height * 0.3f,
                center = Offset(width * 0.22f, height * 0.32f)
            )

            // Secondary small highlight (like Bubble.kt)
            drawCircle(
                color = Color.White.withAlpha(0.35f),
                radius = height * 0.08f,
                center = Offset(width * 0.32f, height * 0.25f)
            )

            // Rim light on bottom-right (like Bubble.kt)
            if (!isPressed) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.withAlpha(0.12f),
                            Color.White.withAlpha(0f)
                        ),
                        center = Offset(width * 0.75f, height * 0.7f),
                        radius = height * 0.3f
                    ),
                    radius = height * 0.2f,
                    center = Offset(width * 0.75f, height * 0.7f)
                )
            }
        }

        // Button content
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                color = AppColors.Text.OnDark,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
        }
    }
}