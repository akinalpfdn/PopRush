package com.akinalpfdn.poprush.coop.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

/**
 * Permissions dialog with "Pop Rush" styling.
 */
@Composable
fun CoopPermissionsDialog(
    isVisible: Boolean,
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit,
    onNotNow: () -> Unit,
    missingPermissions: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onNotNow,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            CoopPermissionsDialogContent(
                missingPermissions = missingPermissions,
                onRequestPermissions = onRequestPermissions,
                onDismiss = onDismiss,
                onNotNow = onNotNow,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CoopPermissionsDialogContent(
    missingPermissions: List<String>,
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.withAlpha(0.2f),
                spotColor = Color.Black.withAlpha(0.2f)
            )
            .background(AppColors.Background.Primary, RoundedCornerShape(32.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Text(
                text = "Permissions Needed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.Text.Primary,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )

            Text(
                text = "PopRush needs access to connect with friends nearby.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Text.Secondary,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )

            // Permissions List
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BubblePermissionItem(
                    icon = Icons.Default.Bluetooth,
                    title = "Bluetooth",
                    description = "Connect with nearby players via Bluetooth",
                    color = AppColors.Bubble.SkyBlue
                )
                BubblePermissionItem(
                    icon = Icons.Default.Wifi,
                    title = "WiFi Direct",
                    description = "Connect with nearby players via WiFi (Offline)",
                    color = AppColors.Bubble.Mint
                )
                BubblePermissionItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    description = "Required for device discovery only (No GPS data used)",
                    color = AppColors.Bubble.Coral
                )
            }

            // Privacy Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Status.Info.withAlpha(0.1f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "🔒 Privacy: Used only for local discovery. No data is collected or transmitted online.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Text.Secondary,
                    textAlign = TextAlign.Start,
                    fontFamily = NunitoFontFamily,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BubbleDialogButton(
                    text = "Open Settings",
                    icon = Icons.Default.Settings,
                    baseColor = AppColors.Bubble.Grape,
                    pressedColor = AppColors.Bubble.GrapePressed,
                    onClick = onRequestPermissions
                )

                TextButton(
                    onClick = onNotNow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Not Now",
                        fontFamily = NunitoFontFamily,
                        color = AppColors.Text.Secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BubblePermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon Bubble
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.withAlpha(0.2f),
                            color.withAlpha(0.1f)
                        ),
                        center = center,
                        radius = width * 0.5f
                    )
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppColors.Text.Primary
            )
            Text(
                text = description,
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                color = AppColors.Text.Secondary
            )
        }
    }
}

@Composable
private fun BubbleDialogButton(
    text: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = baseColor.withAlpha(0.4f),
                spotColor = baseColor.withAlpha(0.4f)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val lighter = baseColor.withAlpha(0.85f).compositeOver(Color.White)
            val darker = if (isPressed) pressedColor else baseColor.withAlpha(0.95f)

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighter, baseColor, darker),
                    center = Offset(width * 0.5f, height * 0.2f),
                    radius = width * 1.5f
                ),
                cornerRadius = CornerRadius(20.dp.toPx())
            )
            
            // Subtle Glass Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.withAlpha(0.15f), Color.White.withAlpha(0f)),
                    center = Offset(width * 0.5f, height * 0.1f),
                    radius = width * 0.6f
                ),
                radius = width * 0.5f,
                center = Offset(width * 0.5f, height * 0.1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = AppColors.Text.OnDark,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}