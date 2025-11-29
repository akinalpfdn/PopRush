package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.presentation.component.CoopColorPicker
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.ui.theme.PastelColors 

// Theme Colors
private val DarkGray = Color(0xFF1C1917)
private val LightGray = Color(0xFFF5F5F4)

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
    val scrollState = rememberScrollState() // 1. Create scroll state

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding() // 2. Handle Keyboard
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState), // 3. Make column scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // -- Top Navigation --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(LightGray, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -- Title Section --
            Text(
                text = if (isHost) "CREATE LOBBY" else "JOIN LOBBY",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkGray,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Customize your profile",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -- Live Avatar Preview --
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = PastelColors.getColor(playerColor),
                        shape = CircleShape
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
                // Edit badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(Color.White, CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -- Input Forms --
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Name Input
                StyledNameInput(
                    playerName = playerName,
                    onNameChange = onPlayerNameChange,
                    onDone = { focusManager.clearFocus() }
                )

                // Color Picker
                CoopColorPicker(
                    availableColors = BubbleColor.values().toList(),
                    selectedColor = playerColor,
                    opponentColor = opponentColor,
                    onColorSelected = onColorSelected,
                    title = "CHOOSE COLOR",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Replaced weight(1f) with fixed spacing to allow scrolling
            Spacer(modifier = Modifier.height(48.dp))

            // -- Instructions --
            InfoPill(isHost)

            Spacer(modifier = Modifier.height(24.dp))

            // -- Action Button --
            PrimaryActionButton(
                text = if (isHost) "Start Hosting" else "Search Games",
                icon = if (isHost) Icons.Default.WifiTethering else Icons.Default.Search,
                enabled = isNameValid,
                onClick = onContinue
            )

            // Extra padding at the bottom for comfortable scrolling
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledNameInput(
    playerName: String,
    onNameChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "DISPLAY NAME",
            style = MaterialTheme.typography.labelLarge,
            color = DarkGray.copy(alpha = 0.6f),
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "Enter name...",
                    fontFamily = FontFamily.Default,
                    color = Color.Gray
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightGray,
                unfocusedContainerColor = LightGray,
                disabledContainerColor = LightGray,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = DarkGray,
                focusedTextColor = DarkGray,
                unfocusedTextColor = DarkGray
            ),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            trailingIcon = if (playerName.isNotEmpty()) {
                {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(Icons.Default.Cancel, null, tint = Color.Gray)
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoPill(isHost: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isHost) "Bluetooth & Location required to host." else "Bluetooth & Location required to join.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkGray,
            contentColor = Color.White,
            disabledContainerColor = LightGray,
            disabledContentColor = Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (enabled) 8.dp else 0.dp
        ),
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold
            )
        }
    }
}