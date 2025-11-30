package com.akinalpfdn.poprush.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.akinalpfdn.poprush.core.domain.model.BubbleColor

/**
 * Object containing pastel color definitions for bubbles.
 * Matches the React version's color scheme.
 */
object PastelColors {

    // Base pastel colors (inactive/normal state)
    private val roseBase = Color(0xFFFCA5A5) // Light red/pink
    private val skyBase = Color(0xFF93C5FD) // Light blue
    private val emeraldBase = Color(0xFF86EFAC) // Light green
    private val amberBase = Color(0xFFFCD34D) // Light yellow
    private val violetBase = Color(0xFFC4B5FD) // Light purple
    private val grayBase = Color(0xFFE5E7EB) // Light gray (Gray-200)

    // Pressed/darker variants
    private val rosePressed = Color(0xFFF87171) // Darker red/pink
    private val skyPressed = Color(0xFF60A5FA) // Darker blue
    private val emeraldPressed = Color(0xFF34D399) // Darker green
    private val amberPressed = Color(0xFFFBBF24) // Darker yellow
    private val violetPressed = Color(0xFFA78BFA) // Darker purple
    private val grayPressed = Color(0xFF9CA3AF) // Darker gray (Gray-400)

    // Glow/highlight colors
    private val roseGlow = Color(0xFFFFD4D4) // Very light pink
    private val skyGlow = Color(0xFFBFDBFE) // Very light blue
    private val emeraldGlow = Color(0xFFBBF7D0) // Very light green
    private val amberGlow = Color(0xFFFDE68A) // Very light yellow
    private val violetGlow = Color(0xFFDDD6FE) // Very light purple
    private val grayGlow = Color(0xFFF3F4F6) // Very light gray (Gray-100)

    /**
     * Gets the base color for a bubble color.
     *
     * @param bubbleColor The bubble color enum
     * @return The corresponding Color object
     */
    fun getColor(bubbleColor: BubbleColor): Color {
        return when (bubbleColor) {
            BubbleColor.ROSE -> roseBase
            BubbleColor.SKY -> skyBase
            BubbleColor.EMERALD -> emeraldBase
            BubbleColor.AMBER -> amberBase
            BubbleColor.VIOLET -> violetBase
            BubbleColor.GRAY -> grayBase
        }
    }

    /**
     * Gets the pressed/darker variant for a bubble color.
     *
     * @param bubbleColor The bubble color enum
     * @return The corresponding pressed Color object
     */
    fun getPressedColor(bubbleColor: BubbleColor): Color {
        return when (bubbleColor) {
            BubbleColor.ROSE -> rosePressed
            BubbleColor.SKY -> skyPressed
            BubbleColor.EMERALD -> emeraldPressed
            BubbleColor.AMBER -> amberPressed
            BubbleColor.VIOLET -> violetPressed
            BubbleColor.GRAY -> grayPressed
        }
    }

    /**
     * Gets the glow/highlight color for a bubble.
     *
     * @param bubbleColor The bubble color enum
     * @return The corresponding glow Color object
     */
    fun getGlowColor(bubbleColor: BubbleColor): Color {
        return when (bubbleColor) {
            BubbleColor.ROSE -> roseGlow
            BubbleColor.SKY -> skyGlow
            BubbleColor.EMERALD -> emeraldGlow
            BubbleColor.AMBER -> amberGlow
            BubbleColor.VIOLET -> violetGlow
            BubbleColor.GRAY -> grayGlow
        }
    }

    /**
     * Gets all available base colors.
     *
     * @return Array of all base bubble colors
     */
    fun getAllBaseColors(): Array<Color> {
        return arrayOf(roseBase, skyBase, emeraldBase, amberBase, violetBase, grayBase)
    }

    /**
     * Gets all available pressed colors.
     *
     * @return Array of all pressed bubble colors
     */
    fun getAllPressedColors(): Array<Color> {
        return arrayOf(rosePressed, skyPressed, emeraldPressed, amberPressed, violetPressed, grayPressed)
    }

    /**
     * Gets all available glow colors.
     *
     * @return Array of all glow bubble colors
     */
    fun getAllGlowColors(): Array<Color> {
        return arrayOf(roseGlow, skyGlow, emeraldGlow, amberGlow, violetGlow, grayGlow)
    }

    /**
     * Creates a gradient from the base color to the glow color.
     *
     * @param bubbleColor The bubble color
     * @return Brush with gradient effect
     */
    fun createGradientBrush(bubbleColor: BubbleColor): androidx.compose.ui.graphics.Brush {
        return androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(
                getGlowColor(bubbleColor),
                getColor(bubbleColor)
            )
        )
    }

    /**
     * Gets color information as a data class for easy access.
     */
    fun getColorSet(bubbleColor: BubbleColor): BubbleColorSet {
        return when (bubbleColor) {
            BubbleColor.ROSE -> BubbleColorSet(
                base = roseBase,
                pressed = rosePressed,
                glow = roseGlow,
                name = "Rose"
            )
            BubbleColor.SKY -> BubbleColorSet(
                base = skyBase,
                pressed = skyPressed,
                glow = skyGlow,
                name = "Sky"
            )
            BubbleColor.EMERALD -> BubbleColorSet(
                base = emeraldBase,
                pressed = emeraldPressed,
                glow = emeraldGlow,
                name = "Emerald"
            )
            BubbleColor.AMBER -> BubbleColorSet(
                base = amberBase,
                pressed = amberPressed,
                glow = amberGlow,
                name = "Amber"
            )
            BubbleColor.VIOLET -> BubbleColorSet(
                base = violetBase,
                pressed = violetPressed,
                glow = violetGlow,
                name = "Violet"
            )
            BubbleColor.GRAY -> BubbleColorSet(
                base = grayBase,
                pressed = grayPressed,
                glow = grayGlow,
                name = "Gray"
            )
        }
    }
}

/**
 * Data class containing all color variants for a bubble color.
 */
data class BubbleColorSet(
    val base: Color,
    val pressed: Color,
    val glow: Color,
    val name: String
)