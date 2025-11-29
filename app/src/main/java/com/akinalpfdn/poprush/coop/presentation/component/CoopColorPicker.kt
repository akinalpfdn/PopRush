package com.akinalpfdn.poprush.coop.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.R
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
/**
 * Color picker component for coop mode player color selection.
 *
 * @param availableColors List of colors players can choose from
 * @param selectedColor Currently selected color by the player
 * @param opponentColor Color selected by opponent (if known)
 * @param onColorSelected Callback when a color is selected
 * @param title Title text to display above the picker
 * @param modifier Modifier for the component
 */
@Composable
fun CoopColorPicker(
    availableColors: List<BubbleColor>,
    selectedColor: BubbleColor,
    opponentColor: BubbleColor? = null,
    onColorSelected: (BubbleColor) -> Unit,
    title: String = "Choose Your Color",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Color Grid
        val colorsPerRow = 5
        val rows = (availableColors.size + colorsPerRow - 1) / colorsPerRow

        for (rowIndex in 0 until rows) {
            val startIndex = rowIndex * colorsPerRow
            val endIndex = minOf(startIndex + colorsPerRow, availableColors.size)
            val rowColors = availableColors.subList(startIndex, endIndex)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (bubbleColor in rowColors) {
                    CoopColorOption(
                        color = bubbleColor,
                        isSelected = bubbleColor == selectedColor,
                        isOpponentColor = bubbleColor == opponentColor,
                        onClick = { onColorSelected(bubbleColor) }
                    )
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = selectedColor.toPastelColor(),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "You",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Default
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Opponent indicator (if known)
            if (opponentColor != null) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = opponentColor.toPastelColor(),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Opponent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Default
                )
            } else {
                Text(
                    text = "Opponent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

/**
 * Individual color option for the coop color picker.
 *
 * @param color The bubble color this option represents
 * @param isSelected Whether this color is currently selected
 * @param isOpponentColor Whether this color is selected by opponent
 * @param onClick Callback when this color is clicked
 * @param modifier Modifier for the component
 */
@Composable
private fun CoopColorOption(
    color: BubbleColor,
    isSelected: Boolean,
    isOpponentColor: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = color.toPastelColor()
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isOpponentColor -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }
    val borderWidth = when {
        isSelected -> 3.dp
        isOpponentColor -> 2.dp
        else -> 1.dp
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = !isOpponentColor
            )
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        } else if (isOpponentColor) {
            // Show a small lock icon for opponent's color
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Opponent's color",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Extension function to convert BubbleColor to actual Color.
 */
private fun BubbleColor.toPastelColor(): Color {
    return PastelColors.getColor(this)
}

/**
 * Color picker dialog with title and confirmation button.
 *
 * @param availableColors List of colors players can choose from
 * @param initialColor Initially selected color
 * @param opponentColor Color selected by opponent (if known)
 * @param onColorConfirmed Callback when color selection is confirmed
 * @param onDismiss Callback when dialog is dismissed
 * @param playerName Name of the player selecting a color
 * @param modifier Modifier for the dialog
 */
@Composable
fun CoopColorPickerDialog(
    availableColors: List<BubbleColor>,
    initialColor: BubbleColor,
    opponentColor: BubbleColor? = null,
    onColorConfirmed: (BubbleColor) -> Unit,
    onDismiss: () -> Unit,
    playerName: String = "",
    modifier: Modifier = Modifier
) {
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = if (playerName.isNotBlank()) {
                    stringResource(R.string.coop_color_picker_title_with_name, playerName)
                } else {
                    stringResource(R.string.coop_color_picker_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Default
            )
        },
        text = {
            CoopColorPicker(
                availableColors = availableColors,
                selectedColor = selectedColor,
                opponentColor = opponentColor,
                onColorSelected = { selectedColor = it }
            )
        },
        confirmButton = {
            Button(
                onClick = { onColorConfirmed(selectedColor) }
            ) {
                Text(
                    text = stringResource(R.string.coop_color_picker_confirm),
                    fontFamily = FontFamily.Default
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.coop_color_picker_cancel),
                    fontFamily = FontFamily.Default
                )
            }
        },
        )
}

/**
 * Small inline color picker for settings or quick color selection.
 *
 * @param availableColors List of colors players can choose from
 * @param selectedColor Currently selected color
 * @param onColorSelected Callback when a color is selected
 * @param modifier Modifier for the component
 */
@Composable
fun CoopMiniColorPicker(
    availableColors: List<BubbleColor>,
    selectedColor: BubbleColor,
    onColorSelected: (BubbleColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableColors.forEach { color ->
            CoopColorOption(
                color = color,
                isSelected = color == selectedColor,
                isOpponentColor = false,
                onClick = { onColorSelected(color) },
                modifier = Modifier.size(40.dp)
            )
        }
    }
}