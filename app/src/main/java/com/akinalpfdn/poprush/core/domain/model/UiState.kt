package com.akinalpfdn.poprush.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents UI-related state: dialogs, navigation, and selected duration.
 */
data class UiState(
    val showSettings: Boolean = false,
    val showBackConfirmation: Boolean = false,
    val currentScreen: StartScreenFlow = StartScreenFlow.MODE_SELECTION,
    val selectedDuration: Duration = 30.seconds
)
