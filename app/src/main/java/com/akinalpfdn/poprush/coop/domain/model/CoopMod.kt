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
    )
}
