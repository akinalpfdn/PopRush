package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents the current flow state of the start screen navigation.
 */
enum class StartScreenFlow {
    MODE_SELECTION,     // Single vs Co-op selection
    MOD_PICKER,         // Classic vs Speed selection
    GAME_SETUP,         // Duration picker and play button
    COOP_CONNECTION     // Co-op connection (hosting/joining)
}