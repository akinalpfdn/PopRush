package com.akinalpfdn.poprush.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                    color = Color(0xFF1C1917), // stone-900
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure? Your current progress will be lost.",
                    color = Color(0xFF57534E), // stone-600
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
                            color = Color(0xFF000000), // stone-400
                            shape = CircleShape
                        )
                        .clickable { onConfirm()
                            onDismiss() },
                    contentAlignment = Alignment.Center
                ) {

                        Text(
                            text = "Yes",
                            color = Color.White,
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
                            color = Color(0xFFA8A6A6), // stone-400
                            shape = CircleShape
                        )
                        .clickable {
                            onDismiss() },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "No",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )

                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}