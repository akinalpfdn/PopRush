package com.akinalpfdn.poprush.coop.domain.model

import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Connection states for Nearby Connections
 */
enum class ConnectionState {
    DISCONNECTED,
    ADVERTISING,
    DISCOVERING,
    CONNECTING,
    CONNECTED
}

/**
 * Information about a discovered endpoint
 */
data class EndpointInfo(
    val id: String,
    val name: String,
    val serviceId: String
) {
    /**
     * Extracts player name from endpoint name (format: "PlayerName:Color")
     */
    fun getPlayerName(): String {
        return name.substringBeforeLast(":")
    }

    /**
     * Extracts player color from endpoint name
     */
    fun getPlayerColor(): BubbleColor? {
        return try {
            val colorName = name.substringAfterLast(":")
            BubbleColor.valueOf(colorName)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Connection information when establishing a connection
 */
data class ConnectionInfo(
    val endpointId: String,
    val endpointName: String,
    val authenticationToken: String,
    val isIncomingConnection: Boolean,
    val rawAuthenticationToken: ByteArray
) {
    /**
     * Extracts player name from endpoint name
     */
    fun getPlayerName(): String {
        return endpointName.substringBeforeLast(":")
    }

    /**
     * Extracts player color from endpoint name
     */
    fun getPlayerColor(): BubbleColor? {
        return try {
            val colorName = endpointName.substringAfterLast(":")
            BubbleColor.valueOf(colorName)
        } catch (e: Exception) {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionInfo

        if (endpointId != other.endpointId) return false
        if (endpointName != other.endpointName) return false
        if (authenticationToken != other.authenticationToken) return false
        if (isIncomingConnection != other.isIncomingConnection) return false
        if (!rawAuthenticationToken.contentEquals(other.rawAuthenticationToken)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpointId.hashCode()
        result = 31 * result + endpointName.hashCode()
        result = 31 * result + authenticationToken.hashCode()
        result = 31 * result + isIncomingConnection.hashCode()
        result = 31 * result + rawAuthenticationToken.contentHashCode()
        return result
    }
}

/**
 * Manager interface for Nearby Connections API
 * Handles P2P communication between devices for offline coop gameplay
 */
interface NearbyConnectionsManager {

    /**
     * Current connection state
     */
    val connectionState: StateFlow<ConnectionState>

    /**
     * List of discovered endpoints
     */
    val discoveredEndpoints: StateFlow<List<EndpointInfo>>

    /**
     * Information about the current connection
     */
    val connectionInfo: StateFlow<ConnectionInfo?>

    /**
     * Flow of incoming messages
     */
    val messageFlow: Flow<String>

    /**
     * Flow of error messages
     */
    val errorFlow: Flow<String>

    /**
     * Start advertising this device for discovery
     */
    fun startAdvertising(playerName: String, playerColor: BubbleColor): Flow<Result<Unit>>

    /**
     * Stop advertising this device
     */
    fun stopAdvertising()

    /**
     * Start discovering nearby devices
     */
    fun startDiscovery(): Flow<Result<Unit>>

    /**
     * Stop discovering nearby devices
     */
    fun stopDiscovery()

    /**
     * Request connection to a discovered endpoint
     */
    fun requestConnection(endpointId: String): Flow<Result<Unit>>

    /**
     * Accept an incoming connection request
     */
    fun acceptConnection(endpointId: String): Flow<Result<Unit>>

    /**
     * Reject an incoming connection request
     */
    fun rejectConnection(endpointId: String): Flow<Result<Unit>>

    /**
     * Send a message to the connected device
     */
    fun sendMessage(message: String): Flow<Result<Unit>>

    /**
     * Disconnect from the current connection
     */
    fun disconnect()

    /**
     * Clear the list of discovered endpoints
     */
    fun clearDiscoveredEndpoints()
}