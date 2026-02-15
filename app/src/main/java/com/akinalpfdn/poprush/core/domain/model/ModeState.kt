package com.akinalpfdn.poprush.core.domain.model

import com.akinalpfdn.poprush.coop.domain.model.CoopGameState

/**
 * Represents game mode selection and coop-related state.
 */
data class ModeState(
    val gameMode: GameMode = GameMode.SINGLE,
    val selectedMod: GameMod = GameMod.CLASSIC,
    val speedModeState: SpeedModeState = SpeedModeState(),
    val isCoopMode: Boolean = false,
    val coopState: CoopGameState? = null,
    val showCoopConnectionDialog: Boolean = false,
    val coopErrorMessage: String? = null
)
