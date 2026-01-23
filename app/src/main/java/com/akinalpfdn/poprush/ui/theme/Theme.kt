package com.akinalpfdn.poprush.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Light color scheme using AppColors.
 * All colors are centralized in AppColors.kt for easy theming.
 */
private val LightColorScheme = lightColorScheme(
    // Primary brand colors - using our stone palette
    primary = AppColors.DarkGray,
    onPrimary = AppColors.Background.Primary,
    primaryContainer = AppColors.LightGray,
    onPrimaryContainer = AppColors.DarkGray,

    // Secondary colors
    secondary = AppColors.StoneGray,
    onSecondary = AppColors.Background.Primary,
    secondaryContainer = AppColors.LightGray,
    onSecondaryContainer = AppColors.DarkGray,

    // Tertiary colors - for accents and highlights
    tertiary = AppColors.RoseMedium,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.RoseLight,
    onTertiaryContainer = AppColors.DarkGray,

    // Background colors
    background = AppColors.Background.Primary,
    onBackground = AppColors.DarkGray,

    // Surface colors (cards, dialogs, etc.)
    surface = AppColors.Background.Card,
    onSurface = AppColors.DarkGray,
    surfaceVariant = AppColors.LightGray,
    onSurfaceVariant = AppColors.StoneMedium,

    // Error colors
    error = AppColors.RedError,
    onError = Color.White,
    errorContainer = AppColors.RoseLight,
    onErrorContainer = AppColors.DarkGray,

    // Outline and borders
    outline = AppColors.StonePale,
    outlineVariant = AppColors.LightGray,

    // Inverse surfaces for dialogs/overlays
    inverseSurface = AppColors.DarkGray,
    inverseOnSurface = AppColors.Background.Primary,

    // Inverse primary
    inversePrimary = AppColors.StoneGray
)

/**
 * Dark color scheme using AppColors (if needed in future).
 * Currently not used but kept for structure.
 */
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.StoneLight,
    onPrimary = AppColors.DarkGray,
    primaryContainer = AppColors.StoneGray,
    onPrimaryContainer = AppColors.LightGray,

    secondary = AppColors.StoneMedium,
    onSecondary = AppColors.LightGray,
    secondaryContainer = AppColors.StoneGray,
    onSecondaryContainer = AppColors.LightGray,

    tertiary = AppColors.RoseLight,
    onTertiary = AppColors.DarkGray,
    tertiaryContainer = AppColors.RoseMedium,
    onTertiaryContainer = AppColors.DarkGray,

    background = AppColors.DarkGray,
    onBackground = AppColors.LightGray,

    surface = AppColors.StoneGray,
    onSurface = AppColors.LightGray,
    surfaceVariant = AppColors.StoneMedium,
    onSurfaceVariant = AppColors.StonePale,

    error = AppColors.RedError,
    onError = Color.White,
    errorContainer = AppColors.RoseMedium,
    onErrorContainer = AppColors.LightGray,

    outline = AppColors.StoneMedium,
    outlineVariant = AppColors.StoneGray,

    inverseSurface = AppColors.LightGray,
    inverseOnSurface = AppColors.DarkGray,

    inversePrimary = AppColors.StoneLight
)

@Composable
fun PopRushTheme(
    darkTheme: Boolean = false, // Force light theme for consistent branding
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to maintain consistent theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}