package com.akinalpfdn.poprush.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized color palette for PopRush game.
 * All UI colors should be defined here and referenced throughout the app.
 */
object AppColors {

    // ========================================================================
    // Primary / Stone Colors (Main Palette)
    // ========================================================================

    /** Primary dark background/text color - stone-900 */
    val DarkGray = Color(0xFF1C1917)

    /** Secondary dark gray - stone-700 */
    val StoneGray = Color(0xFF44403C)

    /** Medium gray for secondary text - stone-600 */
    val StoneMedium = Color(0xFF57534E)

    /** Light gray for labels - stone-400 */
    val StoneLight = Color(0xFF78716C)

    /** Very light gray for tertiary text */
    val StonePale = Color(0xFFA8A29E)

    /** Alternative light gray variant */
    val StonePaleAlt = Color(0xFFA8A6A6)

    /** Background light gray - stone-100 */
    val LightGray = Color(0xFFF5F5F4)

    /** Soft white background */
    val SoftWhite = Color(0xFFFAFAFA)

    /** Medium gray - gray-500 */
    val GrayMedium = Color(0xFF6B7280)

    // ========================================================================
    // Rose Colors
    // ========================================================================

    /** Light rose/pink for scores */
    val RoseLight = Color(0xFFFCA5A5)

    /** Medium rose for critical timer / speed mode */
    val RoseMedium = Color(0xFFF87171)

    // ========================================================================
    // Amber Colors
    // ========================================================================

    /** Light amber for high score display */
    val AmberLight = Color(0xFFFCD34D)

    /** Medium amber for trophy background */
    val AmberMedium = Color(0xFFFBBF24)

    /** Dark amber for pause button */
    val AmberDark = Color(0xFFF59E0B)

    /** Very dark amber */
    val AmberDarker = Color(0xFFEAB308)

    // ========================================================================
    // Emerald Colors
    // ========================================================================

    /** Resume button green */
    val EmeraldPrimary = Color(0xFF10B981)

    /** Success state green */
    val EmeraldSuccess = Color(0xFF22C55E)

    /** Classic mode green */
    val EmeraldClassic = Color(0xFF4ADE80)

    // ========================================================================
    // Blue Colors
    // ========================================================================

    /** Selection indicator blue */
    val BluePrimary = Color(0xFF3B82F6)

    /** Info icon blue */
    val BlueInfo = Color(0xFF60A5FA)

    /** Dark gray-blue for toast background */
    val BlueGray = Color(0xFF1F2937)

    // ========================================================================
    // Red Colors
    // ========================================================================

    /** Error/disconnect red */
    val RedError = Color(0xFFEF4444)

    /** Toast warning yellow */
    val YellowWarning = Color(0xFF856404)

    /** Toast warning background */
    val YellowWarningBg = Color(0xFFFFF3CD)

    // ========================================================================
    // Game-Specific Colors
    // ========================================================================

    /** Background overlay with transparency */
    fun backgroundOverlay(alpha: Float = 0.6f) = Color.White.copy(alpha = alpha)

    /** Dialog overlay */
    fun dialogOverlay(alpha: Float = 0.5f) = Color.Black.copy(alpha = alpha)

    /** Game over overlay */
    fun gameOverOverlay(alpha: Float = 0.8f) = Color.Black.copy(alpha = alpha)

    // ========================================================================
    // Color Constants for Specific Use Cases
    // ========================================================================

    object Text {
        val Primary = DarkGray
        val Secondary = StoneGray
        val Tertiary = StoneMedium
        val Label = StoneLight
        val Muted = StonePale
        val OnDark = Color.White
        val OnLight = DarkGray
    }

    object Background {
        val Primary = Color.White
        val Secondary = LightGray
        val Tertiary = SoftWhite
        val Card = Color.White
        val Overlay = backgroundOverlay()
        val Dialog = dialogOverlay()
    }

    object Button {
        val Primary = DarkGray
        val Secondary = LightGray
        val Success = EmeraldPrimary
        val Warning = AmberDark
        val Danger = RedError
        val Disabled = LightGray
        val Text = Color.White
    }

    object Score {
        val Default = RoseLight
        val High = AmberLight
        val Timer = StoneMedium
        val TimerCritical = RoseMedium
        val Best = AmberLight
    }

    object Status {
        val Success = EmeraldSuccess
        val Error = RedError
        val Warning = AmberDark
        val Info = BlueInfo
        val Neutral = StoneMedium
    }
}

/**
 * Extension functions for common color transformations
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

/**
 * Common alpha values
 */
object Alpha {
    const val FULL = 1f
    const val HIGH = 0.9f
    const val MEDIUM_HIGH = 0.7f
    const val MEDIUM = 0.5f
    const val MEDIUM_LOW = 0.3f
    const val LOW = 0.1f
    const val NONE = 0f
}
