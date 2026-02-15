package com.akinalpfdn.poprush.core.domain.model

/**
 * Intents related to screen navigation and mode selection.
 */
sealed interface NavigationIntent : GameIntent {
    data object NavigateToModPicker : NavigationIntent
    data object NavigateToGameSetup : NavigationIntent
    data object NavigateBack : NavigationIntent
    data object BackToMenu : NavigationIntent
    data object ShowBackConfirmation : NavigationIntent
    data object HideBackConfirmation : NavigationIntent
    data class SelectGameMode(val mode: GameMode) : NavigationIntent
    data class SelectGameMod(val mod: GameMod) : NavigationIntent
}
