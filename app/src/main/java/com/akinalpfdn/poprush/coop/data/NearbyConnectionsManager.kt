package com.akinalpfdn.poprush.coop.data

import android.content.Context
import com.akinalpfdn.poprush.coop.domain.model.ConnectionInfo
import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.EndpointInfo
import com.akinalpfdn.poprush.coop.domain.model.NearbyConnectionsManager
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NearbyConnectionsManager using Google Nearby Connections API.
 * Handles P2P communication between devices for offline coop gameplay.
 */
@Singleton
class NearbyConnectionsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : NearbyConnectionsManager {

    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)

    // State management
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredEndpoints = MutableStateFlow<List<EndpointInfo>>(emptyList())
    override val discoveredEndpoints: StateFlow<List<EndpointInfo>> = _discoveredEndpoints.asStateFlow()

    private val _connectionInfo = MutableStateFlow<ConnectionInfo?>(null)
    override val connectionInfo: StateFlow<ConnectionInfo?> = _connectionInfo.asStateFlow()

    // Message channels
    private val _messageChannel = Channel<String>(Channel.UNLIMITED)
    override val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

    private val _errorChannel = Channel<String>(Channel.UNLIMITED)
    override val errorFlow: Flow<String> = _errorChannel.receiveAsFlow()

    // Connection callback implementation
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: com.google.android.gms.nearby.connection.ConnectionInfo) {
            val connectionInfo = ConnectionInfo(
                endpointId = endpointId,
                endpointName = info.endpointName,
                authenticationToken = info.authenticationToken,
                isIncomingConnection = info.isIncomingConnection,
                rawAuthenticationToken = info.rawAuthenticationToken
            )
            Timber.tag("COOP_CONNECTION").d("🔗 INITIATED: $endpointId, name=${info.endpointName}, isIncoming=${info.isIncomingConnection}, token=${info.authenticationToken}")
            _connectionInfo.value = connectionInfo
            _connectionState.value = ConnectionState.CONNECTING

            // Both incoming and outgoing connections need to be accepted for the connection to complete
            Timber.tag("COOP_CONNECTION").d("🤝 CONNECTION_READY: $endpointId, isIncoming=${info.isIncomingConnection}")
            try {
                connectionsClient.acceptConnection(endpointId, payloadCallback)
                    .addOnSuccessListener {
                        Timber.tag("COOP_CONNECTION").d("✅ ACCEPTED: $endpointId (${if (info.isIncomingConnection) "incoming" else "outgoing"})")
                    }
                    .addOnFailureListener { exception ->
                        Timber.tag("COOP_CONNECTION").e("❌ ACCEPT_FAILED: $endpointId (${if (info.isIncomingConnection) "incoming" else "outgoing"}) - ${exception.message}")
                        _errorChannel.trySend("Failed to accept connection: ${exception.message}")
                    }
            } catch (e: Exception) {
                Timber.tag("COOP_CONNECTION").e("❌ ACCEPT_EXCEPTION: $endpointId - ${e.message}")
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val status = result.status
            Timber.tag("COOP_CONNECTION").d("🎯 RESULT: $endpointId - statusCode=${status.statusCode}, statusMessage=${status.statusMessage?:"N/A"}, isSuccess=${status.isSuccess}")

            if (status.isSuccess) {
                _connectionState.value = ConnectionState.CONNECTED
                Timber.tag("COOP_CONNECTION").d("✅ CONNECTED: $endpointId!")
            } else {
                _connectionState.value = ConnectionState.DISCONNECTED
                val errorMessage = "Connection failed to $endpointId: ${status.statusCode} - ${status.statusMessage ?: "Unknown error"}"
                _errorChannel.trySend(errorMessage)
                Timber.tag("COOP_CONNECTION").e("❌ FAILED: $errorMessage")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Timber.tag("COOP_CONNECTION").d("🔌 DISCONNECTED: $endpointId")
            _connectionState.value = ConnectionState.DISCONNECTED
            _connectionInfo.value = null
        }
    }

    // Payload callback implementation
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val data = String(payload.asBytes() ?: return)
                Timber.d("Received message from $endpointId: $data")
                _messageChannel.trySend(data)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Handle payload transfer progress if needed
        }
    }

    // Discovery callback implementation
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Timber.d("Endpoint found: $endpointId, name: ${info.serviceId}")
            val endpointInfo = EndpointInfo(
                id = endpointId,
                name = info.endpointName,
                serviceId = info.serviceId
            )

            val currentEndpoints = _discoveredEndpoints.value.toMutableList()
            if (!currentEndpoints.any { it.id == endpointId }) {
                currentEndpoints.add(endpointInfo)
                _discoveredEndpoints.value = currentEndpoints
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Timber.d("Endpoint lost: $endpointId")
            val currentEndpoints = _discoveredEndpoints.value.toMutableList()
            currentEndpoints.removeAll { it.id == endpointId }
            _discoveredEndpoints.value = currentEndpoints
        }
    }

    override fun startAdvertising(playerName: String, playerColor: BubbleColor): Flow<Result<Unit>> = callbackFlow {
        try {
            val localEndpointName = "$playerName:${playerColor.name}"
            Timber.tag("COOP_CONNECTION").d("📢 START_ADVERTISING: $localEndpointName, currentState=${_connectionState.value}")

            // Check if already advertising to prevent "Status already advertising" error
            if (_connectionState.value == ConnectionState.ADVERTISING) {
                Timber.tag("COOP_CONNECTION").d("📢 ALREADY_ADVERTISING: $localEndpointName")
                trySend(Result.success(Unit))
            } else {
                val advertisingOptions = AdvertisingOptions.Builder()
                    .setStrategy(Strategy.P2P_STAR)
                    .build()

                val localEndpointName = "$playerName:${playerColor.name}"

                connectionsClient.startAdvertising(
                    localEndpointName,
                    SERVICE_ID,
                    connectionLifecycleCallback,
                    advertisingOptions
                ).addOnSuccessListener {
                    _connectionState.value = ConnectionState.ADVERTISING
                    Timber.tag("COOP_CONNECTION").d("📢 ADVERTISING_STARTED: $localEndpointName")
                    trySend(Result.success(Unit))
                }.addOnFailureListener { exception ->
                    // FIX: Handle 8001 STATUS_ALREADY_ADVERTISING
                    if (exception is ApiException && exception.statusCode == 8001) {
                        Timber.w("SDK reports already advertising. Recovering state to ADVERTISING.")
                        _connectionState.value = ConnectionState.ADVERTISING
                        trySend(Result.success(Unit))
                    } else {
                        _errorChannel.trySend("Failed to start advertising: ${exception.message}")
                        trySend(Result.failure(exception))
                    }
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose { stopAdvertising() }
    }

    override fun stopAdvertising() {
        try {
            connectionsClient.stopAdvertising()
            if (_connectionState.value == ConnectionState.ADVERTISING) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
            Timber.d("Stopped advertising")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping advertising")
            // Ensure state is consistent even if stopAdvertising fails
            if (_connectionState.value == ConnectionState.ADVERTISING) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    override fun startDiscovery(): Flow<Result<Unit>> = callbackFlow {
        try {
            // Check if already discovering to prevent duplicate discovery requests
            if (_discoveredEndpoints.value.isNotEmpty()) {
                Timber.d("Already discovering, skipping duplicate discovery request")
                _connectionState.value = ConnectionState.DISCOVERING
                trySend(Result.success(Unit))
                // REMOVED: return@callbackFlow
                // Execution must fall through to awaitClose to avoid the crash.
            } else {
                val discoveryOptions = DiscoveryOptions.Builder()
                    .setStrategy(Strategy.P2P_STAR)
                    .build()

                connectionsClient.startDiscovery(
                    SERVICE_ID,
                    endpointDiscoveryCallback,
                    discoveryOptions
                ).addOnSuccessListener {
                    _connectionState.value = ConnectionState.DISCOVERING
                    Timber.d("Started discovery")
                    trySend(Result.success(Unit))
                }.addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to start discovery: ${exception.message}")
                    trySend(Result.failure(exception))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose { stopDiscovery() }
    }
    override fun stopDiscovery() {
        try {
            connectionsClient.stopDiscovery()
            _discoveredEndpoints.value = emptyList()
            Timber.d("Stopped discovery")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping discovery")
        }
    }

    override fun requestConnection(endpointId: String, localEndpointName: String): Flow<Result<Unit>> = callbackFlow {
        try {
            // Check current connection state to avoid duplicates
            val currentState = _connectionState.value
            val currentConnectionId = _connectionInfo.value?.endpointId

            Timber.tag("COOP_CONNECTION").d("📞 REQUESTING: $endpointId with local name: $localEndpointName")
            Timber.tag("COOP_CONNECTION").d("📊 CURRENT_STATE: $currentState, currentConnectionId: $currentConnectionId")

            // If we're already connected to this endpoint, just return success
            if (currentState == ConnectionState.CONNECTED && currentConnectionId == endpointId) {
                Timber.tag("COOP_CONNECTION").d("✅ ALREADY_CONNECTED: $endpointId")
                trySend(Result.success(Unit))
                return@callbackFlow
            }

            // Set state to connecting before making the request
            _connectionState.value = ConnectionState.CONNECTING

            connectionsClient.requestConnection(
                localEndpointName,
                endpointId,
                connectionLifecycleCallback
            ).addOnSuccessListener {
                Timber.tag("COOP_CONNECTION").d("✅ REQUEST_SENT: $endpointId")
                trySend(Result.success(Unit))
            }.addOnFailureListener { exception ->
                Timber.tag("COOP_CONNECTION").e("❌ REQUEST_FAILED: $endpointId - ${exception.message}")
                _errorChannel.trySend("Failed to request connection: ${exception.message}")
                trySend(Result.failure(exception))
            }
        } catch (e: Exception) {
            Timber.tag("COOP_CONNECTION").e("❌ REQUEST_EXCEPTION: ${e.message}")
            trySend(Result.failure(e))
        }

        awaitClose { /* Connection cleanup handled in callback */ }
    }

    override fun acceptConnection(endpointId: String): Flow<Result<Unit>> = callbackFlow {
        try {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Timber.d("Accepted connection from $endpointId")
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to accept connection: ${exception.message}")
                    trySend(Result.failure(exception))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose { /* Connection cleanup handled in callback */ }
    }

    override fun rejectConnection(endpointId: String): Flow<Result<Unit>> = callbackFlow {
        try {
            connectionsClient.rejectConnection(endpointId)
                .addOnSuccessListener {
                    Timber.d("Rejected connection from $endpointId")
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to reject connection: ${exception.message}")
                    trySend(Result.failure(exception))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose { /* No cleanup needed */ }
    }

    override fun sendMessage(message: String): Flow<Result<Unit>> = callbackFlow {
        try {
            val connectedEndpointId = _connectionInfo.value?.endpointId
            if (connectedEndpointId == null) {
                val error = IllegalStateException("No active connection")
                trySend(Result.failure(error))
                return@callbackFlow
            }

            val payload = Payload.fromBytes(message.toByteArray())

            connectionsClient.sendPayload(connectedEndpointId, payload)
                .addOnSuccessListener {
                    Timber.d("Message sent: $message")
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to send message: ${exception.message}")
                    trySend(Result.failure(exception))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose { /* No cleanup needed */ }
    }

    override fun disconnect() {
        try {
            val endpointId = _connectionInfo.value?.endpointId
            Timber.tag("COOP_CONNECTION").d("🔌 DISCONNECT_ALL: endpointId=$endpointId")

            // Stop advertising and discovery first
            try {
                stopAdvertising()
            } catch (e: Exception) {
                Timber.tag("COOP_CONNECTION").w("⚠️ STOP_ADVERTISING_FAILED: ${e.message}")
            }

            try {
                stopDiscovery()
            } catch (e: Exception) {
                Timber.tag("COOP_CONNECTION").w("⚠️ STOP_DISCOVERY_FAILED: ${e.message}")
            }

            // Disconnect from endpoint
            endpointId?.let { id ->
                connectionsClient.disconnectFromEndpoint(id)
                Timber.tag("COOP_CONNECTION").d("🔌 DISCONNECTED_FROM: $id")
            }

            // Reset all states
            _connectionState.value = ConnectionState.DISCONNECTED
            _connectionInfo.value = null
            _discoveredEndpoints.value = emptyList()

            Timber.tag("COOP_CONNECTION").d("✅ DISCONNECT_COMPLETE")
        } catch (e: Exception) {
            Timber.tag("COOP_CONNECTION").e("❌ DISCONNECT_ERROR: ${e.message}")
        }
    }

    override fun clearDiscoveredEndpoints() {
        _discoveredEndpoints.value = emptyList()
    }

    companion object {
        private const val SERVICE_ID = "com.akinalpfdn.poprush.coop"
    }
}