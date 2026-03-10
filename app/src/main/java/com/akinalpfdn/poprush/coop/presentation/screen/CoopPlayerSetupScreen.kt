package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.coop.presentation.component.BubbleIconButton
import com.akinalpfdn.poprush.coop.presentation.component.CoopBubbleButton
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

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            ColoredCoopTitle(text = "PROFILE")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Customize your profile",
                color = AppColors.Text.Label,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Compact profile card
            ProfileSetupCard(
                playerName = playerName,
                playerColor = playerColor,
                opponentColor = opponentColor,
                onNameChange = onPlayerNameChange,
                onColorSelected = onColorSelected,
                onDone = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            CoopBubbleButton(
                text = "DONE",
                icon = Icons.Default.Check,
                baseColor = AppColors.Bubble.Mint,
                pressedColor = AppColors.Bubble.MintPressed,
                enabled = isNameValid,
                onClick = onContinue
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileSetupCard(
    playerName: String,
    playerColor: BubbleColor,
    opponentColor: BubbleColor?,
    onNameChange: (String) -> Unit,
    onColorSelected: (BubbleColor) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AppColors.Bubble.SkyBlue.withAlpha(0.1f),
                spotColor = AppColors.Bubble.SkyBlue.withAlpha(0.1f)
            )
            .background(AppColors.Background.Secondary, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Name row: color preview bubble + text input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CompactBubbleAvatar(playerColor = playerColor)

            Spacer(modifier = Modifier.width(12.dp))

            CompactNameInput(
                playerName = playerName,
                onNameChange = onNameChange,
                onDone = onDone,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            color = AppColors.Text.Muted.withAlpha(0.12f),
            thickness = 1.dp
        )

        // Color selection
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "COLOR",
                color = AppColors.Text.Label,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BubbleColor.values().forEach { bubbleColor ->
                    CompactColorOption(
                        color = bubbleColor,
                        isSelected = bubbleColor == playerColor,
                        isOpponent = bubbleColor == opponentColor,
                        onClick = { onColorSelected(bubbleColor) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactBubbleAvatar(playerColor: BubbleColor) {
    val color = PastelColors.getColor(playerColor)

    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 4.dp,
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
            modifier = Modifier.size(24.dp),
            tint = AppColors.Text.OnDark
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactNameInput(
    playerName: String,
    onNameChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(Unit) { mutableStateOf(TextFieldValue(playerName)) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onNameChange(newValue.text)
        },
        placeholder = {
            Text(
                "Enter name...",
                fontFamily = NunitoFontFamily,
                color = AppColors.Text.Muted
            )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppColors.Background.Primary,
            unfocusedContainerColor = AppColors.Background.Primary,
            disabledContainerColor = AppColors.Background.Primary,
            focusedBorderColor = AppColors.Bubble.SkyBlue,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = AppColors.Bubble.Grape,
            focusedTextColor = AppColors.Text.Primary,
            unfocusedTextColor = AppColors.Text.Primary
        ),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = if (textFieldValue.text.isNotEmpty()) {
            {
                IconButton(onClick = {
                    textFieldValue = TextFieldValue("")
                    onNameChange("")
                }) {
                    Icon(Icons.Default.Cancel, null, tint = AppColors.Text.Muted)
                }
            }
        } else null,
        modifier = modifier
    )
}

@Composable
private fun CompactColorOption(
    color: BubbleColor,
    isSelected: Boolean,
    isOpponent: Boolean,
    onClick: () -> Unit
) {
    val pastelColor = PastelColors.getColor(color)

    Box(
        modifier = Modifier
            .size(40.dp)
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, AppColors.Text.Primary, CircleShape)
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .clickable(enabled = !isOpponent, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lighterColor = pastelColor.withAlpha(0.85f).compositeOver(Color.White)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lighterColor, pastelColor),
                    center = Offset(size.width * 0.35f, size.height * 0.3f),
                    radius = size.width * 0.7f
                )
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AppColors.Text.OnDark,
                modifier = Modifier.size(20.dp)
            )
        } else if (isOpponent) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = AppColors.Text.OnDark.withAlpha(0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

