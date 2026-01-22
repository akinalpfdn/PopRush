package com.akinalpfdn.poprush.game.presentation.strategy

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.akinalpfdn.poprush.core.domain.model.GameMod

/**
 * Configuration data for a game mode.
 * Provides metadata and UI configuration for each game mode.
 */
data class GameModeConfig(
    /**
     * The GameMod enum value for this mode.
     */
    val mod: GameMod,

    /**
     * Human-readable display name.
     */
    val displayName: String,

    /**
     * Short description of the mode.
     */
    val description: String,

    /**
     * Icon resource for the mode selection screen.
     */
    val iconRes: String,

    /**
     * Whether this mode uses a countdown timer.
     */
    val hasTimer: Boolean,

    /**
     * Whether this mode has multiple levels.
     */
    val hasLevels: Boolean,

    /**
     * Whether this mode supports duration selection.
     */
    val supportsDurationSelection: Boolean = true,

    /**
     * Whether this mode supports co-op multiplayer.
     */
    val supportsCoop: Boolean = false,

    /**
     * Optional composable for mode-specific setup screen.
     * If null, uses the default duration picker.
     */
    val setupScreen: (@Composable () -> Unit)? = null
) {
    companion object {
        /**
         * Default config for Classic mode.
         */
        fun classic() = GameModeConfig(
            mod = GameMod.CLASSIC,
            displayName = "Classic",
            description = "Pop as many bubbles as you can before time runs out!",
            iconRes = "classic_icon",
            hasTimer = true,
            hasLevels = true,
            supportsDurationSelection = true,
            supportsCoop = false
        )

        /**
         * Default config for Speed mode.
         */
        fun speed() = GameModeConfig(
            mod = GameMod.SPEED,
            displayName = "Speed",
            description = "Bubbles light up randomly - tap them fast! Survive as long as you can.",
            iconRes = "speed_icon",
            hasTimer = false,  // No countdown timer, uses elapsed time instead
            hasLevels = false,
            supportsDurationSelection = false,
            supportsCoop = false
        )

        /**
         * Default config for Coop mode.
         */
        fun coop() = GameModeConfig(
            mod = GameMod.CLASSIC,  // Coop uses Classic gameplay rules
            displayName = "Co-op",
            description = "Play with a friend on the same device!",
            iconRes = "coop_icon",
            hasTimer = true,
            hasLevels = false,
            supportsDurationSelection = true,
            supportsCoop = true
        )
    }
}
