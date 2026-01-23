package com.akinalpfdn.poprush.game.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.akinalpfdn.poprush.ui.theme.AppColors
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Back confirmation dialog that asks the user to confirm if they want to leave the game.
 *
 * @param onConfirm Callback when the user confirms they want to go back
 * @param onDismiss Callback when the user dismisses the dialog
 * @param isVisible Whether the dialog should be shown
 */
@Composable
fun BackConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            title = {
                Text(
                    text = "Leave Game?",
                    color = AppColors.Text.Primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure? Your current progress will be lost.",
                    color = AppColors.Text.Tertiary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 100.dp)
                        .height(44.dp)
                        .background(
                            color = AppColors.Button.Primary,
                            shape = CircleShape
                        )
                        .clickable { onConfirm()
                            onDismiss() },
                    contentAlignment = Alignment.Center
                ) {

                        Text(
                            text = "Yes",
                            color = AppColors.Button.Text,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                }

            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 100.dp)
                        .height(44.dp)
                        .background(
                            color = AppColors.StonePaleAlt,
                            shape = CircleShape
                        )
                        .clickable {
                            onDismiss() },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "No",
                        color = AppColors.Button.Text,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )

                }
            },
            containerColor = AppColors.Background.Primary,
            shape = RoundedCornerShape(16.dp)
        )
    }
}