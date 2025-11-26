package com.akinalpfdn.poprush.game.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.ui.theme.PastelColors

/**
 * Settings overlay that appears as a dropdown from the bottom-right corner.
 * Allows users to change bubble shapes and other game settings.
 *
 * @param gameState Current game state
 * @param onSelectShape Callback when a shape is selected
 * @param onClose Callback when the overlay should close
 * @param modifier Additional modifier for the overlay
 */
@Composable
fun SettingsOverlay(
    gameState: GameState,
    onSelectShape: (BubbleShape) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background overlay to catch clicks
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                .clickable(onClick = onClose)
        )

        // Settings dropdown positioned at bottom-right
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .widthIn(max = 200.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header
                SettingsHeader(
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Shape selector
                ShapeSelector(
                    selectedShape = gameState.selectedShape,
                    onShapeSelected = onSelectShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Additional settings could go here in the future
                // (Audio settings, difficulty, etc.)
            }
        }
    }
}

/**
 * Settings header with title and close button.
 */
@Composable
private fun SettingsHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shape",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Shape selector allowing users to choose bubble shapes.
 */
@Composable
private fun ShapeSelector(
    selectedShape: BubbleShape,
    onShapeSelected: (BubbleShape) -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = BubbleShape.values()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(shapes) { shape ->
            ShapeItem(
                shape = shape,
                isSelected = shape == selectedShape,
                onClick = { onShapeSelected(shape) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Individual shape item in the selector.
 */
@Composable
private fun ShapeItem(
    shape: BubbleShape,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shape icon
        Icon(
            imageVector = getShapeIcon(shape),
            contentDescription = shape.name,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Shape name
        Text(
            text = getShapeDisplayName(shape),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Gets the appropriate icon for each bubble shape.
 */
private fun getShapeIcon(shape: BubbleShape): ImageVector {
    return when (shape) {
        BubbleShape.CIRCLE -> Icons.Default.Circle
        BubbleShape.SQUARE -> Icons.Default.Square
        BubbleShape.TRIANGLE -> Icons.Default.ArrowDropDown // Placeholder, might need a better icon
        BubbleShape.STAR -> Icons.Default.Star
        BubbleShape.HEART -> Icons.Default.Favorite
    }
}

/**
 * Gets the display name for each bubble shape.
 */
private fun getShapeDisplayName(shape: BubbleShape): String {
    return when (shape) {
        BubbleShape.CIRCLE -> "Circle"
        BubbleShape.SQUARE -> "Square"
        BubbleShape.TRIANGLE -> "Triangle"
        BubbleShape.STAR -> "Star"
        BubbleShape.HEART -> "Heart"
    }
}

/**
 * Advanced settings section for future expansion.
 * This would include audio settings, difficulty selection, etc.
 */
@Composable
private fun AdvancedSettings(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Audio settings header
        Text(
            text = "Audio",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        // Sound toggle
        SettingsToggleItem(
            title = "Sound Effects",
            subtitle = "Tap sounds and level complete sounds",
            icon = Icons.Default.VolumeUp,
            isToggled = gameState.soundEnabled,
            onToggle = { /* Handle sound toggle */ },
            modifier = Modifier.fillMaxWidth()
        )

        // Music toggle
        SettingsToggleItem(
            title = "Background Music",
            subtitle = "Background music during gameplay",
            icon = Icons.Default.MusicNote,
            isToggled = gameState.musicEnabled,
            onToggle = { /* Handle music toggle */ },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Divider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Visual settings header
        Text(
            text = "Visual",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        // Haptic feedback toggle
        SettingsToggleItem(
            title = "Haptic Feedback",
            subtitle = "Vibration on bubble press",
            icon = Icons.Default.Settings,
            isToggled = true, // Would come from game state
            onToggle = { /* Handle haptic toggle */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Toggle item for settings with icon, title, subtitle, and switch.
 */
@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isToggled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isToggled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.8f)
        )
    }
}