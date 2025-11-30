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
import kotlinx.coroutines.runBlocking
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
     * Combined flow of all coop messages (both incoming and outgoing)
     */
    val coopMessages: Flow<CoopMessage> = merge(
        // Incoming messages
        nearbyConnectionsManager.messageFlow.map { message ->
            try {
                gson.fromJson(message, CoopMessage::class.java)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse coop message: $message")
                null
            }
        }.flatMapLatest { flowOf(it) }.filterNotNull(),

        // Local messages will be added when we implement message sending
        flowOf<CoopMessage>() // Empty flow for now
    )

    /**
     * Error messages from connection manager
     */
    val errorMessages: Flow<String> = nearbyConnectionsManager.errorFlow

    /**
     * Start advertising this device as host
     */
    fun startHosting(playerName: String, playerColor: BubbleColor): Flow<Result<Unit>> {
        Timber.tag("COOP_CONNECTION").d("🏠 USECASE_START_HOSTING: $playerName, $playerColor")
        return nearbyConnectionsManager.startAdvertising(playerName, playerColor)
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
    fun startDiscovering(): Flow<Result<Unit>> {
        return nearbyConnectionsManager.startDiscovery()
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
    fun requestConnection(endpointId: String, localEndpointName: String): Flow<Result<Unit>> {
        return nearbyConnectionsManager.requestConnection(endpointId, localEndpointName)
    }

    /**
     * Accept an incoming connection request
     */
    fun acceptConnection(endpointId: String): Flow<Result<Unit>> {
        return nearbyConnectionsManager.acceptConnection(endpointId)
    }

    /**
     * Reject an incoming connection request
     */
    fun rejectConnection(endpointId: String): Flow<Result<Unit>> {
        return nearbyConnectionsManager.rejectConnection(endpointId)
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
    fun sendChatMessage(message: String): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.CHAT,
            content = message
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send bubble claim message
     */
    fun sendBubbleClaim(bubbleId: Int, playerColor: BubbleColor): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.BUBBLE_CLAIM,
            bubbleId = bubbleId,
            playerColor = playerColor.name
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send game start message
     */
    fun sendGameStart(): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.GAME_START
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send game end message with scores
     */
    fun sendGameEnd(localScore: Int, remoteScore: Int): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.GAME_END,
            localScore = localScore,
            remoteScore = remoteScore
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send score synchronization message
     */
    fun sendScoreUpdate(localScore: Int, remoteScore: Int): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.SCORE_UPDATE,
            localScore = localScore,
            remoteScore = remoteScore
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send color selection message
     */
    fun sendColorSelection(playerColor: BubbleColor): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.COLOR_SELECTION,
            playerColor = playerColor.name
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send ready state message
     */
    fun sendReadyState(isReady: Boolean): Flow<Result<Unit>> {
        val coopMessage = CoopMessage(
            type = CoopMessageType.READY_STATE,
            content = isReady.toString()
        )
        return sendMessage(coopMessage)
    }

    /**
     * Send a generic coop message
     */
    private fun sendMessage(coopMessage: CoopMessage): Flow<Result<Unit>> {
        return try {
            val messageJson = gson.toJson(coopMessage)
            nearbyConnectionsManager.sendMessage(messageJson)
        } catch (e: Exception) {
            Timber.e(e, "Failed to serialize coop message")
            flowOf(Result.failure(e))
        }
    }

    /**
     * Get whether this device is the host (first to start advertising)
     */
    fun isHost(): Boolean {
        // This will be determined by the connection flow - for now assume advertising means host
        return runBlocking {
            try {
                connectionState.first() == ConnectionState.ADVERTISING
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Get the current connection state
     */
    fun getCurrentConnectionState(): ConnectionState {
        return runBlocking {
            try {
                connectionState.first()
            } catch (e: Exception) {
                ConnectionState.DISCONNECTED
            }
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
            // No opponent yet, any color is fine
            opponentColor == null -> true
            // Host has priority, can choose any color
            isHost -> true
            // Client cannot choose host's color
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