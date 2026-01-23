package com.akinalpfdn.poprush.game.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
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
import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

@Composable
fun ModPickerScreen(
    onModSelected: (GameMod) -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle breathing for title
    val infiniteTransition = rememberInfiniteTransition(label = "modPicker")
    
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
            .background(AppColors.Background.Primary)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // "GAME MODS" Colored Title
                    ColoredTitle(
                        text = "GAME MODS",
                        modifier = Modifier.graphicsLayer {
                            scaleX = titleScale
                            scaleY = titleScale
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select your challenge",
                        color = AppColors.Text.Secondary,
                        fontSize = 18.sp,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            item {
                BubbleModCard(
                    title = "Classic Mode",
                    description = "Pop bubbles against the clock. Test your reflexes in this timeless challenge.",
                    icon = Icons.Default.Timer,
                    baseColor = AppColors.Bubble.Mint,
                    pressedColor = AppColors.Bubble.MintPressed,
                    onClick = { onModSelected(GameMod.CLASSIC) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                BubbleModCard(
                    title = "Speed Mode",
                    description = "Cells light up randomly! The pace quickens with every second.",
                    icon = Icons.Default.Speed,
                    baseColor = AppColors.Bubble.Coral,
                    pressedColor = AppColors.Bubble.CoralPressed,
                    onClick = { onModSelected(GameMod.SPEED) }
                )
            }
        }
    }
}

/**
 * Title with each character in a different color.
 */
@Composable
private fun ColoredTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    // Colors Pattern
    val colors = listOf(
        AppColors.Bubble.Coral,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Mint,
        AppColors.Bubble.Grape,
        AppColors.Bubble.Lemon,
        AppColors.Bubble.Peach,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Mint
    )

    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            if (char != ' ') {
                Text(
                    text = char.toString(),
                    color = colors.getOrElse(index % colors.size) { AppColors.Text.Primary },
                    fontSize = 36.sp,
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
 * Bubbly card for game mods.
 * Visual style matches Bubble.kt and ModeSelectionScreen.kt
 */
@Composable
private fun BubbleModCard(
    title: String,
    description: String,
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

    // Subtle wobble for idle state
    val infiniteTransition = rememberInfiniteTransition(label = "cardWobble")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
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
            )
    ) {
        // Bubble-style gradient background (Variable height based on content)
        Canvas(modifier = Modifier.matchParentSize()) {
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
                    center = Offset(width * 0.2f, height * 0.3f),
                    radius = width * 1.0f
                ),
                cornerRadius = CornerRadius(24.dp.toPx())
            )

            // Glass highlight spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.15f), // Significantly reduced opacity
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.15f, height * 0.2f),
                    radius = width * 0.5f // Increased radius for softer diffusion
                ),
                radius = width * 0.4f,
                center = Offset(width * 0.15f, height * 0.2f)
            )
        }

        // Card Content
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.withAlpha(0.25f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = AppColors.Text.OnDark,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = AppColors.Text.OnDark.withAlpha(0.9f),
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.Text.OnDark.withAlpha(0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
