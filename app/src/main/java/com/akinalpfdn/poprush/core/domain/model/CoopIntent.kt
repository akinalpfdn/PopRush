package com.akinalpfdn.poprush.core.domain.model

/**
 * Intents related to cooperative multiplayer gameplay.
 */
sealed interface CoopIntent : GameIntent {
    data class StartCoopAdvertising(val playerName: String, val selectedColor: String) : CoopIntent
    data class StartCoopDiscovery(val playerName: String, val selectedColor: String) : CoopIntent
    data object StopCoopConnection : CoopIntent
    data class CoopClaimBubble(val bubbleId: Int) : CoopIntent
    data class CoopSyncBubbles(val bubbles: List<com.akinalpfdn.poprush.coop.domain.model.CoopBubble>) : CoopIntent
    data class CoopSyncScores(val localScore: Int, val opponentScore: Int) : CoopIntent
    data class CoopGameFinished(val winnerId: String? = null) : CoopIntent
    data object ShowCoopConnectionDialog : CoopIntent
    data object HideCoopConnectionDialog : CoopIntent
    data class ShowCoopError(val errorMessage: String) : CoopIntent
    data object ClearCoopError : CoopIntent
    data class UpdateCoopPlayerName(val playerName: String) : CoopIntent
    data class UpdateCoopPlayerColor(val playerColor: BubbleColor) : CoopIntent
    data object StartCoopConnection : CoopIntent
    data object StartHosting : CoopIntent
    data object StopHosting : CoopIntent
    data object StartDiscovery : CoopIntent
    data object StopDiscovery : CoopIntent
    data class ConnectToEndpoint(val endpointId: String) : CoopIntent
    data object StartCoopGame : CoopIntent
    data object StartCoopMatch : CoopIntent
    data object DisconnectCoop : CoopIntent
    data object CloseCoopConnection : CoopIntent
    data object PlayAgain : CoopIntent
    data class SelectCoopMod(val coopMod: com.akinalpfdn.poprush.coop.domain.model.CoopMod) : CoopIntent
    data object ConfirmCoopMod : CoopIntent
}
