package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents the game mode selection for single player vs co-op gameplay.
 */
enum class GameMode(val displayName: String) {
    SINGLE("Single Player"),
    COOP("Co-op")
}

/**
 * Represents the different game mods available for gameplay.
 * @param displayName The display name for the mod
 * @param durationRequired Whether this mod requires duration selection
 */
enum class GameMod(val displayName: String, val durationRequired: Boolean) {
    CLASSIC("Classic Mode", true),      // Mod 1 - requires duration picker
    SPEED("Speed Mode", false)          // Mod 2 - no duration needed
}

