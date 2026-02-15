package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.presentation.component.BubbleIconButton
import com.akinalpfdn.poprush.coop.presentation.component.CoopBubbleButton
import com.akinalpfdn.poprush.coop.presentation.component.CoopColorPicker
import com.akinalpfdn.poprush.coop.presentation.component.ColoredCoopTitle
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.withAlpha

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
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background.Primary)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BubbleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    baseColor = AppColors.Bubble.SkyBlue,
                    onClick = onBack
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Colored title
            ColoredCoopTitle(
                text = if (isHost) "CREATE LOBBY" else "JOIN LOBBY"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Customize your profile",
                color = AppColors.Text.Label,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar preview with bubble style
            BubbleAvatar(playerColor = playerColor)

            Spacer(modifier = Modifier.height(32.dp))

            // Input forms
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StyledNameInput(
                    playerName = playerName,
                    onNameChange = onPlayerNameChange,
                    onDone = { focusManager.clearFocus() }
                )

                CoopColorPicker(
                    availableColors = BubbleColor.values().toList(),
                    selectedColor = playerColor,
                    opponentColor = opponentColor,
                    onColorSelected = onColorSelected,
                    title = "CHOOSE COLOR",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Info pill
            InfoPill(isHost)

            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            CoopBubbleButton(
                text = if (isHost) "START HOSTING" else "SEARCH GAMES",
                icon = if (isHost) Icons.Default.WifiTethering else Icons.Default.Search,
                baseColor = AppColors.Bubble.Coral,
                pressedColor = AppColors.Bubble.CoralPressed,
                enabled = isNameValid,
                onClick = onContinue
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BubbleAvatar(playerColor: BubbleColor) {
    val color = PastelColors.getColor(playerColor)

    Box(
        modifier = Modifier
            .size(120.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = color.withAlpha(0.3f),
                spotColor = color.withAlpha(0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lighterColor = color.withAlpha(0.85f).compositeOver(Color.White)
            val darkerColor = color.withAlpha(0.95f).compositeOver(Color.Black.withAlpha(0.05f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, color, darkerColor),
                    center = Offset(size.width * 0.35f, size.height * 0.3f),
                    radius = size.width * 0.7f
                )
            )
            // Glass highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.withAlpha(0.45f), Color.White.withAlpha(0f)),
                    center = Offset(size.width * 0.3f, size.height * 0.28f),
                    radius = size.width * 0.35f
                ),
                radius = size.width * 0.25f,
                center = Offset(size.width * 0.3f, size.height * 0.28f)
            )
        }

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = AppColors.Text.OnDark
        )

        // Edit badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(32.dp)
                .shadow(4.dp, CircleShape)
                .background(AppColors.Background.Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppColors.Text.Secondary
            )
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
            color = AppColors.Text.Label,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )

        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "Enter name...",
                    fontFamily = NunitoFontFamily,
                    color = AppColors.Text.Muted
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.Background.Secondary,
                unfocusedContainerColor = AppColors.Background.Secondary,
                disabledContainerColor = AppColors.Background.Secondary,
                focusedBorderColor = AppColors.Bubble.SkyBlue,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = AppColors.Bubble.Grape,
                focusedTextColor = AppColors.Text.Primary,
                unfocusedTextColor = AppColors.Text.Primary
            ),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            trailingIcon = if (playerName.isNotEmpty()) {
                {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(Icons.Default.Cancel, null, tint = AppColors.Text.Muted)
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
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = AppColors.Bubble.SkyBlue.withAlpha(0.1f),
                spotColor = AppColors.Bubble.SkyBlue.withAlpha(0.1f)
            )
            .background(AppColors.Background.Secondary, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = AppColors.Bubble.SkyBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isHost) "Bluetooth & Location required to host." else "Bluetooth & Location required to join.",
            color = AppColors.Text.Label,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
