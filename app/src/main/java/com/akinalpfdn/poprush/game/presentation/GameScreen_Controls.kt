package com.akinalpfdn.poprush.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.GameState

/**
 * Floating controls for zoom and settings buttons positioned at bottom right.
 */
@Composable
fun FloatingControls(
    gameState: GameState,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Settings button with dropdown
        Box {
            IconButton(
                onClick = onToggleSettings,
                modifier = Modifier
                    .background(
                        color = if (gameState.showSettings) {
                            Color.White
                        } else {
                            Color.White.copy(alpha = 0.8f)
                        },
                        shape = CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (gameState.showSettings) {
                        Color(0xFF1C1917) // stone-800
                    } else {
                        Color(0xFF57534E) // stone-600
                    }
                )
            }
        }

        // Zoom out button
        IconButton(
            onClick = onZoomOut,
            enabled = gameState.zoomLevel > 0.5f,
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                tint = Color(0xFF57534E) // stone-600
            )
        }

        // Zoom in button
        IconButton(
            onClick = onZoomIn,
            enabled = gameState.zoomLevel < 1.5f,
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In",
                tint = Color(0xFF57534E) // stone-600
            )
        }
    }
}