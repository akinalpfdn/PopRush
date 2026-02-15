package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

/**
 * Bubbly-themed back confirmation dialog matching the app's visual style.
 * Features entrance animation, gradient card, colored title, and bubble-style buttons.
 */
@Composable
fun BackConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean
) {
    if (!isVisible) return

    var showCard by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showCard = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Dark overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.6f))
        }

        // Animated card entrance
        AnimatedVisibility(
            visible = showCard,
            enter = scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(200))
        ) {
            // Card container - stop click propagation
            Box(
                modifier = Modifier
                    .padding(horizontal = 36.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // absorb clicks
                    )
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
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
                        cornerRadius = CornerRadius(28.dp.toPx())
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
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Colored title
                    ColoredDialogTitle(text = "LEAVE GAME?")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your current progress will be lost.",
                        color = AppColors.Text.Label,
                        fontSize = 14.sp,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Leave button (Coral - destructive action)
                    ConfirmationBubbleButton(
                        text = "LEAVE",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        baseColor = AppColors.Bubble.Coral,
                        pressedColor = AppColors.Bubble.CoralPressed,
                        onClick = {
                            onConfirm()
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stay button (Mint - safe action)
                    ConfirmationBubbleButton(
                        text = "STAY",
                        icon = Icons.Default.Close,
                        baseColor = AppColors.Bubble.Mint,
                        pressedColor = AppColors.Bubble.MintPressed,
                        onClick = onDismiss,
                        isSmall = true
                    )
                }
            }
        }
    }
}

/**
 * Colored characters for dialog title, matching the app's bubbly style.
 */
@Composable
private fun ColoredDialogTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        AppColors.Bubble.Coral,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Mint,
        AppColors.Bubble.Grape,
        AppColors.Bubble.Lemon,
        AppColors.Bubble.Peach,
        AppColors.Bubble.SkyBlue,
        AppColors.Bubble.Coral,
        AppColors.Bubble.Mint,
        AppColors.Bubble.Grape,
        AppColors.Bubble.Lemon
    )

    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            if (char != ' ') {
                Text(
                    text = char.toString(),
                    color = colors[index % colors.size],
                    fontSize = 26.sp,
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
 * Bubble-style button with gradient, glass highlight, and bounce animation.
 */
@Composable
private fun ConfirmationBubbleButton(
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
        label = "confirmBtnScale"
    )

    val buttonHeight = if (isSmall) 44.dp else 50.dp
    val fontSize = if (isSmall) 13.sp else 15.sp
    val iconSize = if (isSmall) 16.dp else 20.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            )
            .defaultMinSize(minWidth = 160.dp)
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
                cornerRadius = CornerRadius(24.dp.toPx())
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
