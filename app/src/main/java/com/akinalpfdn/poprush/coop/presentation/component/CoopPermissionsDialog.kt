package com.akinalpfdn.poprush.coop.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.AppColors

/**
 * Permissions dialog for coop mode that explains required permissions
 * and guides users to grant them for offline multiplayer functionality.
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
            properties = androidx.compose.ui.window.DialogProperties(
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Background.Primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                text = "PopRush needs permissions to connect devices nearby",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.DarkGray,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )



            // Missing permissions info DO NOT EDIT OR DELETE THIS PART
            if (missingPermissions.isNotEmpty() && false) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.YellowWarningBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Missing Permissions:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.YellowWarning,
                            fontFamily = NunitoFontFamily
                        )
                        missingPermissions.forEach { permission ->
                            Text(
                                text = "• $permission",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.YellowWarning,
                                fontFamily = NunitoFontFamily
                            )
                        }
                    }
                }
            }

            // Permissions list
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PermissionItem(
                    icon = Icons.Default.Bluetooth,
                    title = "Bluetooth",
                    description = "Connect with nearby players via Bluetooth"
                )
                PermissionItem(
                    icon = Icons.Default.Wifi,
                    title = "WiFi Direct",
                    description = "Connect with nearby players via WiFi"
                )
                PermissionItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    description = "Required for device discovery (offline only, no GPS)"
                )
                PermissionItem(
                    icon = Icons.Default.Settings,
                    title = "Device Control",
                    description = "Manage WiFi and Bluetooth connections"
                )
            }


            // Note about privacy
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.DarkGray.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🔒 Your privacy matters: These permissions are only used for local device discovery. No data is collected or transmitted online.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.DarkGray.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = NunitoFontFamily,
                    lineHeight = 18.sp
                )
            }


            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.DarkGray,
                        contentColor = AppColors.Background.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Open Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        fontFamily = NunitoFontFamily
                    )
                }

                OutlinedButton(
                    onClick = onNotNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = AppColors.DarkGray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.DarkGray.copy(alpha = 0.6f),
                        fontFamily = NunitoFontFamily
                    )
                }
            }


            // Instruction text
            Text(
                text = "Please enable the required permissions in your device settings, then return to the app to continue.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.DarkGray.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = AppColors.DarkGray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppColors.DarkGray
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = AppColors.DarkGray,
                fontFamily = NunitoFontFamily
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.DarkGray.copy(alpha = 0.6f),
                fontFamily = NunitoFontFamily
            )
        }
    }
}