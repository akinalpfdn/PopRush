package com.akinalpfdn.poprush.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.akinalpfdn.poprush.core.domain.model.BubbleColor

/**
 * Enhanced "Candy/Juicy" color palette for bubbles.
 * More saturated and playful than standard pastels.
 * Designed for better visual feedback in fast-paced gameplay.
 */
object PastelColors {

    // ========================================================================
    // OPTION A: CANDY/JUICY PALETTE
    // More saturated, game-like colors with great visual pop
    // ========================================================================

    // Base colors (normal state) - Vibrant but not harsh
    private val coralBase = Color(0xFFFF6B6B)      // Vibrant coral red
    private val skyBlueBase = Color(0xFF5B9EFF)    // True sky blue (not teal)
    private val lemonBase = Color(0xFFFFE66D)      // Bright sunny yellow
    private val grapeBase = Color(0xFFA66CFF)      // Rich purple/grape
    private val mintBase = Color(0xFF5AD8A6)       // Fresh mint/emerald green
    private val peachBase = Color(0xFFFF9F7F)      // Warm peach/orange

    // Pressed/darker variants - Richer, deeper tones
    private val coralPressed = Color(0xFFEE5A5A)   // Deeper coral
    private val skyBluePressed = Color(0xFF4A8BE8) // Deeper blue
    private val lemonPressed = Color(0xFFFFD93D)   // Deeper golden yellow
    private val grapePressed = Color(0xFF8B4FE8)   // Deeper purple
    private val mintPressed = Color(0xFF45C795)    // Deeper mint
    private val peachPressed = Color(0xFFFF8A65)   // Deeper peach

    // Glow/highlight colors - Lighter, ethereal versions
    private val coralGlow = Color(0xFFFFAAAA)      // Soft coral glow
    private val skyBlueGlow = Color(0xFFA3C9FF)    // Soft blue glow
    private val lemonGlow = Color(0xFFFFF3A0)      // Soft yellow glow
    private val grapeGlow = Color(0xFFCDB4FF)      // Soft purple glow
    private val mintGlow = Color(0xFF9EEFD0)       // Soft mint glow
    private val peachGlow = Color(0xFFFFCBB8)      // Soft peach glow

    // ========================================================================
    // ORIGINAL PALETTE (kept for reference/rollback)
    // ========================================================================
    /*
    // Base pastel colors (inactive/normal state)
    private val roseBase = Color(0xFFFCA5A5)    // Light red/pink
    private val skyBase = Color(0xFF93C5FD)     // Light blue
    private val emeraldBase = Color(0xFF86EFAC) // Light green
    private val amberBase = Color(0xFFFCD34D)   // Light yellow
    private val violetBase = Color(0xFFC4B5FD)  // Light purple
    private val grayBase = Color(0xFFE5E7EB)    // Light gray

    // Pressed/darker variants
    private val rosePressed = Color(0xFFF87171)
    private val skyPressed = Color(0xFF60A5FA)
    private val emeraldPressed = Color(0xFF34D399)
    private val amberPressed = Color(0xFFFBBF24)
    private val violetPressed = Color(0xFFA78BFA)
    private val grayPressed = Color(0xFF9CA3AF)

    // Glow/highlight colors
    private val roseGlow = Color(0xFFFFD4D4)
    private val skyGlow = Color(0xFFBFDBFE)
    private val emeraldGlow = Color(0xFFBBF7D0)
    private val amberGlow = Color(0xFFFDE68A)
    private val violetGlow = Color(0xFFDDD6FE)
    private val grayGlow = Color(0xFFF3F4F6)
    */

    /**
     * Gets the base color for a bubble color.
     * Maps BubbleColor enum to the new Candy/Juicy palette.
     *
     * @param bubbleColor The bubble color enum
     * @return The corresponding Color object
     */
    fun getColor(bubbleColor: BubbleColor): Color {
        return when (bubbleColor) {
            BubbleColor.ROSE -> coralBase      // Rose -> Coral
            BubbleColor.SKY -> skyBlueBase     // Sky -> Sky Blue
            BubbleColor.EMERALD -> mintBase    // Emerald -> Mint
            BubbleColor.AMBER -> lemonBase     // Amber -> Lemon
            BubbleColor.VIOLET -> grapeBase    // Violet -> Grape
            BubbleColor.GRAY -> peachBase      // Gray -> Peach (no more boring gray!)
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
            BubbleColor.ROSE -> coralPressed
            BubbleColor.SKY -> skyBluePressed
            BubbleColor.EMERALD -> mintPressed
            BubbleColor.AMBER -> lemonPressed
            BubbleColor.VIOLET -> grapePressed
            BubbleColor.GRAY -> peachPressed
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
            BubbleColor.ROSE -> coralGlow
            BubbleColor.SKY -> skyBlueGlow
            BubbleColor.EMERALD -> mintGlow
            BubbleColor.AMBER -> lemonGlow
            BubbleColor.VIOLET -> grapeGlow
            BubbleColor.GRAY -> peachGlow
        }
    }

    /**
     * Gets all available base colors.
     *
     * @return Array of all base bubble colors
     */
    fun getAllBaseColors(): Array<Color> {
        return arrayOf(coralBase, skyBlueBase, mintBase, lemonBase, grapeBase, peachBase)
    }

    /**
     * Gets all available pressed colors.
     *
     * @return Array of all pressed bubble colors
     */
    fun getAllPressedColors(): Array<Color> {
        return arrayOf(coralPressed, skyBluePressed, mintPressed, lemonPressed, grapePressed, peachPressed)
    }

    /**
     * Gets all available glow colors.
     *
     * @return Array of all glow bubble colors
     */
    fun getAllGlowColors(): Array<Color> {
        return arrayOf(coralGlow, skyBlueGlow, mintGlow, lemonGlow, grapeGlow, peachGlow)
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
                base = coralBase,
                pressed = coralPressed,
                glow = coralGlow,
                name = "Coral"
            )
            BubbleColor.SKY -> BubbleColorSet(
                base = skyBlueBase,
                pressed = skyBluePressed,
                glow = skyBlueGlow,
                name = "Sky Blue"
            )
            BubbleColor.EMERALD -> BubbleColorSet(
                base = mintBase,
                pressed = mintPressed,
                glow = mintGlow,
                name = "Mint"
            )
            BubbleColor.AMBER -> BubbleColorSet(
                base = lemonBase,
                pressed = lemonPressed,
                glow = lemonGlow,
                name = "Lemon"
            )
            BubbleColor.VIOLET -> BubbleColorSet(
                base = grapeBase,
                pressed = grapePressed,
                glow = grapeGlow,
                name = "Grape"
            )
            BubbleColor.GRAY -> BubbleColorSet(
                base = peachBase,
                pressed = peachPressed,
                glow = peachGlow,
                name = "Peach"
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