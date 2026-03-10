package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopMod
import com.akinalpfdn.poprush.coop.presentation.component.ColoredCoopTitle
import com.akinalpfdn.poprush.coop.presentation.component.CoopBubbleButton
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha

/**
 * Mode selection screen for Coop Mode, shown to the host before setup.
 * Allows selecting a coop game mode.
 */
@Composable
fun CoopModSelectionScreen(
    coopGameState: CoopGameState,
    onCoopModSelected: (CoopMod) -> Unit,
    onConfirm: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Overlay),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ColoredCoopTitle(text = "GAME MODE", fontSize = 30)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select a mode to play",
                color = AppColors.Text.Label,
                fontSize = 14.sp,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            CoopMod.entries.forEach { mod ->
                val isSelected = coopGameState.selectedCoopMod == mod
                val (icon, baseColor, pressedColor) = getModVisuals(mod)
                CoopModCard(
                    title = mod.displayName,
                    description = mod.description,
                    icon = icon,
                    baseColor = baseColor,
                    pressedColor = pressedColor,
                    isSelected = isSelected,
                    onClick = { onCoopModSelected(mod) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            CoopBubbleButton(
                text = "CONTINUE",
                icon = Icons.Default.ChevronRight,
                baseColor = AppColors.Bubble.Mint,
                pressedColor = AppColors.Bubble.MintPressed,
                onClick = onConfirm
            )

            Spacer(modifier = Modifier.height(14.dp))

            CoopBubbleButton(
                text = "DISCONNECT",
                icon = Icons.Default.LinkOff,
                baseColor = AppColors.Bubble.Coral,
                pressedColor = AppColors.Bubble.CoralPressed,
                onClick = onDisconnect,
                isSmall = true
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class ModVisuals(
    val icon: ImageVector,
    val baseColor: Color,
    val pressedColor: Color
)

private fun getModVisuals(mod: CoopMod): ModVisuals = when (mod) {
    CoopMod.BUBBLE_POP -> ModVisuals(
        icon = Icons.Default.TouchApp,
        baseColor = AppColors.Bubble.SkyBlue,
        pressedColor = AppColors.Bubble.SkyBluePressed
    )
    CoopMod.TERRITORY_WAR -> ModVisuals(
        icon = Icons.Default.Flag,
        baseColor = AppColors.Bubble.Grape,
        pressedColor = AppColors.Bubble.GrapePressed
    )
    CoopMod.BLIND_MODE -> ModVisuals(
        icon = Icons.Default.VisibilityOff,
        baseColor = AppColors.Bubble.Indigo,
        pressedColor = AppColors.Bubble.IndigoPressed
    )
    CoopMod.HOT_POTATO -> ModVisuals(
        icon = Icons.Default.Whatshot,
        baseColor = AppColors.Bubble.Coral,
        pressedColor = AppColors.Bubble.CoralPressed
    )
    CoopMod.CHAIN_REACTION -> ModVisuals(
        icon = Icons.Default.Bolt,
        baseColor = AppColors.Bubble.Amber,
        pressedColor = AppColors.Bubble.AmberPressed
    )
}

@Composable
private fun CoopModCard(
    title: String,
    description: String,
    icon: ImageVector,
    baseColor: Color,
    pressedColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = baseColor.withAlpha(0.4f),
                        spotColor = baseColor.withAlpha(0.4f)
                    )
                } else {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = baseColor.withAlpha(0.15f),
                        spotColor = baseColor.withAlpha(0.15f)
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val lighterColor = baseColor.withAlpha(if (isSelected) 0.85f else 0.6f).compositeOver(Color.White)
            val darkerColor = if (isPressed) {
                pressedColor
            } else {
                baseColor.withAlpha(if (isSelected) 0.95f else 0.7f).compositeOver(Color.Black.withAlpha(0.05f))
            }

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, baseColor.withAlpha(if (isSelected) 1f else 0.75f), darkerColor),
                    center = Offset(size.width * 0.2f, size.height * 0.3f),
                    radius = size.width * 1.0f
                ),
                cornerRadius = CornerRadius(20.dp.toPx())
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.withAlpha(0.12f),
                        Color.White.withAlpha(0f)
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.2f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.15f, size.height * 0.2f)
            )
        }

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.withAlpha(0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AppColors.Text.OnDark,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = AppColors.Text.OnDark.withAlpha(0.85f),
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Text.OnDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
