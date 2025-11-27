package com.akinalpfdn.poprush.game.presentation.component

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// This grabs the built-in Android "sans-serif-rounded" font
val roundedFont = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        Typeface.create("sans-serif-rounded", Typeface.BOLD)
    )
)

/**
 * Toast/Snackbar component for showing "Coming Soon" messages for Co-op mode.
 * This component appears at the bottom of the screen and auto-dismisses.
 *
 * @param isVisible Whether the toast should be visible
 * @param onDismiss Callback when the toast is dismissed or times out
 * @param modifier Additional modifier for the toast
 */
@Composable
fun ToastSnackbar(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1F2937), // gray-800
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Info icon
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF60A5FA), // blue-400
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Message text
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Coming Soon!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = roundedFont,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Co-op mode will be available in a future update",
                            color = Color(0xFFD1D5DB), // gray-300
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

/**
 * Coming Soon toast specifically for Co-op mode.
 * This is a simplified version that can be used directly in screens.
 */
@Composable
fun CoopComingSoonToast(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    ToastSnackbar(
        isVisible = isVisible,
        onDismiss = { /* Auto-dismiss handled by parent */ },
        modifier = modifier
    )
}