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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

@Composable
fun ModeSelectionScreen(
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle breathing for title (matching StartScreen)
    val infiniteTransition = rememberInfiniteTransition(label = "modeScreen")
    
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
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.Center
            ) {
                // Game title with colored characters
                ColoredTitle(
                    modifier = Modifier.graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Mode",
                    color = AppColors.Text.Secondary,
                    fontSize = 18.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            // Buttons Section
            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BubbleGameModeCard(
                    title = "Single Player",
                    subtitle = "Challenge yourself",
                    icon = Icons.Default.Person,
                    baseColor = AppColors.Bubble.Coral,
                    pressedColor = AppColors.Bubble.CoralPressed,
                    onClick = { onModeSelected(GameMode.SINGLE) }
                )

                BubbleGameModeCard(
                    title = "Co-op",
                    subtitle = "Play with a friend",
                    icon = Icons.Default.Group,
                    baseColor = AppColors.Bubble.SkyBlue,
                    pressedColor = AppColors.Bubble.SkyBluePressed,
                    onClick = { onModeSelected(GameMode.COOP) }
                )
            }
        }
    }
}

/**
 * Title with each character in a different color.
 * Matches StartScreen.kt design.
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
 * Bubbly card for game modes.
 * Visual style matches Bubble.kt and StartScreen.kt BubbleButton but adapted for card layout.
 */
@Composable
private fun BubbleGameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bounce on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    // Subtle wobble for idle state (optional, keeps it alive)
    val infiniteTransition = rememberInfiniteTransition(label = "cardWobble")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = if (!isPressed) wobbleRotation else 0f
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bubble-style gradient background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = if (isPressed) {
                pressedColor
            } else {
                baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))
            }

            // Radial gradient from top-left
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.2f, height * 0.2f),
                    radius = width * 1.0f
                ),
                cornerRadius = CornerRadius(24.dp.toPx())
            )

            // Glass highlight spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.4f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.15f, height * 0.25f),
                    radius = height * 0.6f
                ),
                radius = height * 0.4f,
                center = Offset(width * 0.15f, height * 0.25f)
            )

            // Rim light on bottom-right
            if (!isPressed) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.withAlpha(0.15f),
                            Color.White.withAlpha(0f)
                        ),
                        center = Offset(width * 0.85f, height * 0.8f),
                        radius = height * 0.4f
                    ),
                    radius = height * 0.3f,
                    center = Offset(width * 0.85f, height * 0.8f)
                )
            }
        }

        // Card Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = AppColors.Text.OnDark,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = subtitle,
                    color = AppColors.Text.OnDark.withAlpha(0.8f),
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp
                )
            }

            // Icon with soft background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.withAlpha(0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
