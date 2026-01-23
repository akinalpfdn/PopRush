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
    // Bubble / Candy Palette Colors (for UI elements matching bubble style)
    // ========================================================================

    object Bubble {
        // Coral - Primary action buttons (Play, Start, etc.)
        val Coral = Color(0xFFFF6B6B)
        val CoralPressed = Color(0xFFEE5A5A)
        val CoralGlow = Color(0xFFFFAAAA)

        // Sky Blue - Secondary actions, info elements
        val SkyBlue = Color(0xFF5B9EFF)
        val SkyBluePressed = Color(0xFF4A8BE8)
        val SkyBlueGlow = Color(0xFFA3C9FF)

        // Mint - Success, badges, positive indicators
        val Mint = Color(0xFF5AD8A6)
        val MintPressed = Color(0xFF45C795)
        val MintGlow = Color(0xFF9EEFD0)

        // Lemon - Warnings, highlights, attention
        val Lemon = Color(0xFFFFE66D)
        val LemonPressed = Color(0xFFFFD93D)
        val LemonGlow = Color(0xFFFFF3A0)

        // Grape - Special, premium, accent
        val Grape = Color(0xFFA66CFF)
        val GrapePressed = Color(0xFF8B4FE8)
        val GrapeGlow = Color(0xFFCDB4FF)

        // Peach - Warm accent, secondary highlights
        val Peach = Color(0xFFFF9F7F)
        val PeachPressed = Color(0xFFFF8A65)
        val PeachGlow = Color(0xFFFFCBB8)
    }

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

    // ========================================================================
    // Game Animation Colors (Visual Feedback Effects)
    // ========================================================================

    object GameAnimation {
        // Score colors for floating text
        val ScoreGold = Color(0xFFFFD700)
        val ScoreOrange = Color(0xFFFF9500)
        val ScorePink = Color(0xFFFF6B9D)
        val ScorePurple = Color(0xFFB266FF)
        val ScoreCyan = Color(0xFF00E5FF)

        // Combo progression colors
        val ComboLevel1 = Color(0xFF7DD3FC)  // Sky blue
        val ComboLevel2 = Color(0xFF22D3EE)  // Cyan
        val ComboLevel3 = Color(0xFFFBBF24)  // Amber/Gold
        val ComboLevel4 = Color(0xFFFB923C)  // Orange
        val ComboLevel5 = Color(0xFFF472B6)  // Pink
        val ComboLevel6 = Color(0xFFC084FC)  // Purple
        val ComboLevelMax = Color(0xFFFF6B6B) // Coral red

        // Glow colors for effects
        val GlowGold = Color(0x80FFD700)
        val GlowPink = Color(0x80FF6B9D)
        val GlowCyan = Color(0x8000E5FF)
        val GlowPurple = Color(0x80B266FF)

        // Background gradient colors
        val BackgroundTop = Color(0xFFFFFBF5)      // Warm white
        val BackgroundTopAlt = Color(0xFFFFF0F5)    // Lavender blush
        val BackgroundMiddle = Color(0xFFFFF7ED)    // Warm cream
        val BackgroundMiddleAlt = Color(0xFFFCE7F3) // Pink tint
        val BackgroundBottom = Color(0xFFFAFAF9)    // Stone-50
        val BackgroundBottomAlt = Color(0xFFF5F3FF) // Purple tint

        // Particle colors
        val ParticlePink = Color(0xFFFFD6E0)
        val ParticleBlue = Color(0xFFD4E4FF)
        val ParticlePurple = Color(0xFFE8D4FF)
        val ParticleOrange = Color(0xFFFFECD4)

        // Timer colors
        val TimerCritical = Color(0xFFEF4444)
        val TimerWarning = Color(0xFFF97316)
        val TimerNormal = Color(0xFF57534E)

        // Default text color for game elements
        val TextPrimary = Color(0xFF292524)
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