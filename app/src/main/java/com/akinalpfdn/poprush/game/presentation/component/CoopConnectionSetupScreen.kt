package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.withAlpha

@Composable
fun CoopConnectionSetupScreen(
    onShowConnectionDialog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon in bubble circle
            BubbleHeaderIcon(
                icon = Icons.Default.WifiTethering,
                baseColor = AppColors.Bubble.Grape
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Colored title
            ColoredLobbyTitle(text = "CO-OP LOBBY")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Connect with nearby friends",
                color = AppColors.Text.Label,
                fontSize = 14.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Feature list with bubble icons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BubbleFeatureRow(Icons.Default.Bluetooth, "Offline Multiplayer", AppColors.Bubble.SkyBlue)
                BubbleFeatureRow(Icons.Default.Timer, "Time-Based Gameplay", AppColors.Bubble.Mint)
                BubbleFeatureRow(Icons.Default.EmojiEvents, "Competitive Fun", AppColors.Bubble.Lemon)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Start button
            CoopActionButton(
                text = "Start Scanning",
                icon = Icons.Default.PlayArrow,
                baseColor = AppColors.Bubble.Coral,
                pressedColor = AppColors.Bubble.CoralPressed,
                onClick = onShowConnectionDialog
            )
        }
    }
}

@Composable
private fun BubbleHeaderIcon(
    icon: ImageVector,
    baseColor: Color
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = baseColor.withAlpha(0.3f),
                spotColor = baseColor.withAlpha(0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(size.width * 0.35f, size.height * 0.3f),
                    radius = size.width * 0.7f
                )
            )
            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.withAlpha(0.45f), Color.White.withAlpha(0f)),
                    center = Offset(size.width * 0.3f, size.height * 0.28f),
                    radius = size.width * 0.35f
                ),
                radius = size.width * 0.25f,
                center = Offset(size.width * 0.3f, size.height * 0.28f)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppColors.Text.OnDark
        )
    }
}

@Composable
private fun ColoredLobbyTitle(text: String) {
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

    Row {
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

@Composable
private fun BubbleFeatureRow(
    icon: ImageVector,
    text: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Small bubble icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = accentColor.withAlpha(0.2f),
                    spotColor = accentColor.withAlpha(0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lighterColor = accentColor.withAlpha(0.85f).compositeOver(Color.White)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(lighterColor, accentColor),
                        center = Offset(size.width * 0.35f, size.height * 0.3f),
                        radius = size.width * 0.7f
                    )
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            color = AppColors.Text.Secondary,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun CoopActionButton(
    text: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionBtnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighterColor = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = if (isPressed) pressedColor
            else baseColor.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor, darkerColor),
                    center = Offset(width * 0.25f, height * 0.3f),
                    radius = width * 0.9f
                ),
                cornerRadius = CornerRadius(24.dp.toPx())
            )

            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.35f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(width * 0.15f, height * 0.25f),
                    radius = height * 0.5f
                ),
                radius = height * 0.35f,
                center = Offset(width * 0.15f, height * 0.25f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = AppColors.Text.OnDark,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 0.5.sp
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.withAlpha(0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
