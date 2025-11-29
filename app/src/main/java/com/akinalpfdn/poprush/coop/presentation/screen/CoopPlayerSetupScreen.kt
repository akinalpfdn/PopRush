package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.presentation.component.CoopColorPicker
import com.akinalpfdn.poprush.core.domain.model.BubbleColor

/**
 * Player setup screen for coop mode
 * Allows players to enter their name and select their color before connection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoopPlayerSetupScreen(
    playerName: String,
    playerColor: BubbleColor,
    opponentColor: BubbleColor? = null,
    isHost: Boolean = false,
    onPlayerNameChange: (String) -> Unit,
    onColorSelected: (BubbleColor) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isNameValid = playerName.trim().isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = if (isHost) "Host Game" else "Join Game",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default
            )

            Text(
                text = "Set up your player profile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Player setup card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Player name input
                    PlayerNameInput(
                        playerName = playerName,
                        onNameChange = onPlayerNameChange,
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Color picker
                    CoopColorPicker(
                        availableColors = BubbleColor.values().toList(),
                        selectedColor = playerColor,
                        opponentColor = opponentColor,
                        onColorSelected = onColorSelected,
                        title = "Choose Your Color",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Player preview
                    PlayerPreview(
                        playerName = playerName.trim(),
                        playerColor = playerColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection instructions
            ConnectionInstructions(
                isHost = isHost,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            Button(
                onClick = onContinue,
                enabled = isNameValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isHost) Icons.Default.WifiTethering else Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isHost) "Start Hosting" else "Search for Games",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerNameInput(
    playerName: String,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Your Name",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Default
        )

        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = "Enter your name",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (playerName.isNotEmpty()) {
                    IconButton(
                        onClick = { onNameChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onSubmit() }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun PlayerPreview(
    playerName: String,
    playerColor: BubbleColor,
    modifier: Modifier = Modifier
) {
    val displayName = if (playerName.isEmpty()) "Player" else playerName

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player avatar with color
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = com.akinalpfdn.poprush.core.ui.theme.PastelColors.getColor(playerColor),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Profile",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Ready",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ConnectionInstructions(
    isHost: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isHost) Icons.Default.WifiTethering else Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = if (isHost) "Host Instructions" else "Join Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Default
            )

            Text(
                text = if (isHost) {
                    "Other players will be able to discover and join your game. Make sure Bluetooth and Location are enabled."
                } else {
                    "We'll search for nearby games you can join. Make sure Bluetooth and Location are enabled."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
}