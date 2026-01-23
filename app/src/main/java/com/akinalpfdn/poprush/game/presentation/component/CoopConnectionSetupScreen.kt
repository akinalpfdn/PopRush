package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.AppColors

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Main Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(AppColors.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AppColors.DarkGray
                    )
                }


                Text(
                    text = "CO-OP LOBBY",
                    color = AppColors.DarkGray,
                    fontSize = 32.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Connect with nearby friends",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Compact Features List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompactFeatureRow(Icons.Default.Bluetooth, "Offline Multiplayer")
                CompactFeatureRow(Icons.Default.Timer, "Time-Based Gameplay")
                CompactFeatureRow(Icons.Default.EmojiEvents, "Competitive Fun")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Button (Styled like ModeSelection)
            CoopActionButton(
                text = "Start Scanning",
                icon = Icons.Default.PlayArrow,
                onClick = onShowConnectionDialog
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CompactFeatureRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.DarkGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = AppColors.DarkGray.copy(alpha = 0.8f),
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
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bounce animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "buttonScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.DarkGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = AppColors.Background.Primary,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Background.Primary,
                modifier = Modifier
                    .size(32.dp)
                    .background(AppColors.Background.Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(6.dp)
            )
        }
    }
}