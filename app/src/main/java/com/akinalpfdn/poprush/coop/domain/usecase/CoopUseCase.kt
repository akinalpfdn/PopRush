package com.akinalpfdn.poprush.coop.domain.usecase

import com.akinalpfdn.poprush.coop.domain.model.NearbyConnectionsManager
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.EndpointInfo
import com.akinalpfdn.poprush.coop.domain.model.ConnectionInfo
import com.akinalpfdn.poprush.coop.data.model.CoopMessage
import com.akinalpfdn.poprush.coop.data.model.CoopMessageType
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for handling coop mode business logic
 * Manages connection flow, message passing, and game synchronization
 */
@Singleton
class CoopUseCase @Inject constructor(
    private val nearbyConnectionsManager: NearbyConnectionsManager,
    private val gson: Gson
) {

    /**
     * Current connection state
     */
    val connectionState: Flow<ConnectionState> = nearbyConnectionsManager.connectionState

    /**
     * List of discovered endpoints
     */
    val discoveredEndpoints: Flow<List<EndpointInfo>> = nearbyConnectionsManager.discoveredEndpoints

    /**
     * Information about the current connection
     */
    val connectionInfo: Flow<ConnectionInfo?> = nearbyConnectionsManager.connectionInfo

    /**
     * Combined flow of all coop messages
     */
    val coopMessages: Flow<CoopMessage> = nearbyConnectionsManager.messageFlow.map { message ->
        try {
            gson.fromJson(message, CoopMessage::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse coop message: $message")
            null
        }
    }.filterNotNull()

    /**
     * Error messages from connection manager
     */
    val errorMessages: Flow<String> = nearbyConnectionsManager.errorFlow

    /**
     * Start advertising this device as host
     */
    suspend fun startHosting(playerName: String, playerColor: BubbleColor) {
        Timber.tag("COOP_CONNECTION").d("USECASE_START_HOSTING: $playerName, $playerColor")
        nearbyConnectionsManager.startAdvertising(playerName, playerColor)
    }

    /**
     * Stop hosting
     */
    fun stopHosting() {
        nearbyConnectionsManager.stopAdvertising()
    }

    /**
     * Start discovering nearby hosts
     */
    suspend fun startDiscovering() {
        nearbyConnectionsManager.startDiscovery()
    }

    /**
     * Stop discovering
     */
    fun stopDiscovering() {
        nearbyConnectionsManager.stopDiscovery()
    }

    /**
     * Request connection to a specific host
     */
    suspend fun requestConnection(endpointId: String, localEndpointName: String) {
        nearbyConnectionsManager.requestConnection(endpointId, localEndpointName)
    }

    /**
     * Disconnect from current connection
     */
    fun disconnect() {
        nearbyConnectionsManager.disconnect()
    }

    /**
     * Clear discovered endpoints list
     */
    fun clearDiscoveredEndpoints() {
        nearbyConnectionsManager.clearDiscoveredEndpoints()
    }

    /**
     * Send a chat message to the connected device
     */
    suspend fun sendChatMessage(message: String) {
        val coopMessage = CoopMessage(
            type = CoopMessageType.CHAT,
            content = message
        )
        sendMessage(coopMessage)
    }

    /**
     * Send bubble claim message
     */
    suspend fun sendBubbleClaim(bubbleId: Int, playerColor: BubbleColor) {
        val coopMessage = CoopMessage(
            type = CoopMessageType.BUBBLE_CLAIM,
            bubbleId = bubbleId,
            playerColor = playerColor.name
        )
        sendMessage(coopMessage)
    }

    /**
     * Send game setup message
     */
    suspend fun sendGameSetup() {
        val coopMessage = CoopMessage(
            type = CoopMessageType.GAME_SETUP
        )
        sendMessage(coopMessage)
    }

    /**
     * Send game start message
     */
    suspend fun sendGameStart(playerName: String? = null, playerColor: String? = null, gameDuration: Long? = null, coopMod: String? = null, bombBubbleIds: List<Int>? = null) {
        val coopMessage = CoopMessage.gameStart(
            playerName = playerName,
            playerColor = playerColor,
            gameDuration = gameDuration,
            coopMod = coopMod,
            bombBubbleIds = bombBubbleIds
        )
        sendMessage(coopMessage)
    }

    /**
     * Send player profile message
     */
    suspend fun sendPlayerProfile(playerName: String, playerColor: String) {
        val coopMessage = CoopMessage.playerProfile(playerName, playerColor)
        sendMessage(coopMessage)
    }

    /**
     * Send game end message with scores
     */
    suspend fun sendGameEnd(localScore: Int, remoteScore: Int) {
        val coopMessage = CoopMessage(
            type = CoopMessageType.GAME_END,
            localScore = localScore,
            remoteScore = remoteScore
        )
        sendMessage(coopMessage)
    }

    /**
     * Send score synchronization message
     */
    suspend fun sendScoreUpdate(localScore: Int, remoteScore: Int) {
        val coopMessage = CoopMessage(
            type = CoopMessageType.SCORE_UPDATE,
            localScore = localScore,
            remoteScore = remoteScore
        )
        sendMessage(coopMessage)
    }

    /**
     * Send color selection message
     */
    suspend fun sendColorSelection(playerColor: BubbleColor) {
        val coopMessage = CoopMessage(
            type = CoopMessageType.COLOR_SELECTION,
            playerColor = playerColor.name
        )
        sendMessage(coopMessage)
    }

    /**
     * Send ready state message
     */
    suspend fun sendReadyState(isReady: Boolean) {
        val coopMessage = CoopMessage(
            type = CoopMessageType.READY_STATE,
            content = isReady.toString()
        )
        sendMessage(coopMessage)
    }

    /**
     * Send turn end message with claimed bubble IDs (Chain Reaction)
     */
    suspend fun sendTurnEnd(claimedBubbleIds: List<Int>) {
        val coopMessage = CoopMessage.turnEnd(claimedBubbleIds)
        sendMessage(coopMessage)
    }

    /**
     * Send a generic coop message
     */
    private suspend fun sendMessage(coopMessage: CoopMessage) {
        val messageJson = gson.toJson(coopMessage)
        nearbyConnectionsManager.sendMessage(messageJson)
    }

    /**
     * Get whether this device is the host (first to start advertising)
     */
    suspend fun isHost(): Boolean {
        return try {
            connectionState.first() == ConnectionState.ADVERTISING
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the current connection state
     */
    suspend fun getCurrentConnectionState(): ConnectionState {
        return try {
            connectionState.first()
        } catch (e: Exception) {
            ConnectionState.DISCONNECTED
        }
    }

    /**
     * Validate that a player can select a specific color
     */
    fun canSelectColor(
        selectedColor: BubbleColor,
        opponentColor: BubbleColor?,
        isHost: Boolean
    ): Boolean {
        return when {
            opponentColor == null -> true
            isHost -> true
            else -> selectedColor != opponentColor
        }
    }

    /**
     * Get a suggestion for the client player's color
     */
    fun getSuggestedClientColor(hostColor: BubbleColor): BubbleColor {
        return BubbleColor.values().firstOrNull { it != hostColor } ?: BubbleColor.ROSE
    }
}
