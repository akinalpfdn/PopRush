package com.akinalpfdn.poprush.coop.domain.model

/**
 * Represents the different game mods available for coop gameplay.
 * Currently only BUBBLE_POP exists; new mods can be added here.
 */
enum class CoopMod(
    val displayName: String,
    val description: String
) {
    BUBBLE_POP(
        displayName = "Bubble Pop",
        description = "Race to claim as many bubbles as you can before time runs out!"
    ),
    TERRITORY_WAR(
        displayName = "Territory War",
        description = "Claim all the bubbles! Once taken, a bubble can't be stolen back."
    ),
    BLIND_MODE(
        displayName = "Blind Mode",
        description = "Opponent's bubbles are hidden! Scores revealed when time runs out."
    );

    /**
     * Whether this mod uses a time limit.
     */
    val isTimed: Boolean
        get() = when (this) {
            BUBBLE_POP -> true
            TERRITORY_WAR -> false
            BLIND_MODE -> true
        }

    /**
     * Whether opponent's bubbles are hidden during gameplay.
     */
    val isBlind: Boolean
        get() = this == BLIND_MODE
}
