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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
            Timber.tag("COOP_CONNECTION").d("INITIATED: $endpointId, name=${info.endpointName}, isIncoming=${info.isIncomingConnection}")
            _connectionInfo.value = connectionInfo
            _connectionState.value = ConnectionState.CONNECTING

            try {
                connectionsClient.acceptConnection(endpointId, payloadCallback)
                    .addOnSuccessListener {
                        Timber.tag("COOP_CONNECTION").d("ACCEPTED: $endpointId")
                    }
                    .addOnFailureListener { exception ->
                        Timber.tag("COOP_CONNECTION").e("ACCEPT_FAILED: $endpointId - ${exception.message}")
                        _errorChannel.trySend("Failed to accept connection: ${exception.message}")
                    }
            } catch (e: Exception) {
                Timber.tag("COOP_CONNECTION").e("ACCEPT_EXCEPTION: $endpointId - ${e.message}")
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val status = result.status
            Timber.tag("COOP_CONNECTION").d("RESULT: $endpointId - statusCode=${status.statusCode}, isSuccess=${status.isSuccess}")

            if (status.isSuccess) {
                _connectionState.value = ConnectionState.CONNECTED
                Timber.tag("COOP_CONNECTION").d("CONNECTED: $endpointId!")
            } else {
                _connectionState.value = ConnectionState.DISCONNECTED
                val errorMessage = "Connection failed to $endpointId: ${status.statusCode} - ${status.statusMessage ?: "Unknown error"}"
                _errorChannel.trySend(errorMessage)
                Timber.tag("COOP_CONNECTION").e("FAILED: $errorMessage")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Timber.tag("COOP_CONNECTION").d("DISCONNECTED: $endpointId")
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
            Timber.d("Endpoint found: $endpointId, name: ${info.endpointName}")
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
            _discoveredEndpoints.value = _discoveredEndpoints.value.filter { it.id != endpointId }
        }
    }

    override suspend fun startAdvertising(playerName: String, playerColor: BubbleColor) {
        val localEndpointName = "$playerName:${playerColor.name}"
        Timber.tag("COOP_CONNECTION").d("START_ADVERTISING: $localEndpointName, currentState=${_connectionState.value}")

        if (_connectionState.value == ConnectionState.ADVERTISING) {
            Timber.tag("COOP_CONNECTION").d("ALREADY_ADVERTISING: $localEndpointName")
            return
        }

        suspendCancellableCoroutine { cont ->
            val advertisingOptions = AdvertisingOptions.Builder()
                .setStrategy(Strategy.P2P_STAR)
                .build()

            connectionsClient.startAdvertising(
                localEndpointName,
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            ).addOnSuccessListener {
                _connectionState.value = ConnectionState.ADVERTISING
                Timber.tag("COOP_CONNECTION").d("ADVERTISING_STARTED: $localEndpointName")
                cont.resume(Unit)
            }.addOnFailureListener { exception ->
                if (exception is ApiException && exception.statusCode == 8001) {
                    Timber.w("SDK reports already advertising. Recovering state.")
                    _connectionState.value = ConnectionState.ADVERTISING
                    cont.resume(Unit)
                } else {
                    _errorChannel.trySend("Failed to start advertising: ${exception.message}")
                    cont.resumeWithException(exception)
                }
            }
        }
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
            if (_connectionState.value == ConnectionState.ADVERTISING) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    override suspend fun startDiscovery() {
        if (_connectionState.value == ConnectionState.DISCOVERING) {
            Timber.d("Already discovering, skipping")
            return
        }

        suspendCancellableCoroutine { cont ->
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
                cont.resume(Unit)
            }.addOnFailureListener { exception ->
                if (exception is ApiException && exception.statusCode == 8002) {
                    Timber.w("SDK reports already discovering. Recovering state.")
                    _connectionState.value = ConnectionState.DISCOVERING
                    cont.resume(Unit)
                } else {
                    _errorChannel.trySend("Failed to start discovery: ${exception.message}")
                    cont.resumeWithException(exception)
                }
            }
        }
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

    override suspend fun requestConnection(endpointId: String, localEndpointName: String) {
        val currentState = _connectionState.value
        val currentConnectionId = _connectionInfo.value?.endpointId

        Timber.tag("COOP_CONNECTION").d("REQUESTING: $endpointId with local name: $localEndpointName")

        if (currentState == ConnectionState.CONNECTED && currentConnectionId == endpointId) {
            Timber.tag("COOP_CONNECTION").d("ALREADY_CONNECTED: $endpointId")
            return
        }

        _connectionState.value = ConnectionState.CONNECTING

        suspendCancellableCoroutine { cont ->
            connectionsClient.requestConnection(
                localEndpointName,
                endpointId,
                connectionLifecycleCallback
            ).addOnSuccessListener {
                Timber.tag("COOP_CONNECTION").d("REQUEST_SENT: $endpointId")
                cont.resume(Unit)
            }.addOnFailureListener { exception ->
                Timber.tag("COOP_CONNECTION").e("REQUEST_FAILED: $endpointId - ${exception.message}")
                _errorChannel.trySend("Failed to request connection: ${exception.message}")
                cont.resumeWithException(exception)
            }
        }
    }

    override suspend fun acceptConnection(endpointId: String) {
        suspendCancellableCoroutine { cont ->
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Timber.d("Accepted connection from $endpointId")
                    cont.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to accept connection: ${exception.message}")
                    cont.resumeWithException(exception)
                }
        }
    }

    override suspend fun rejectConnection(endpointId: String) {
        suspendCancellableCoroutine { cont ->
            connectionsClient.rejectConnection(endpointId)
                .addOnSuccessListener {
                    Timber.d("Rejected connection from $endpointId")
                    cont.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to reject connection: ${exception.message}")
                    cont.resumeWithException(exception)
                }
        }
    }

    override suspend fun sendMessage(message: String) {
        val connectedEndpointId = _connectionInfo.value?.endpointId
            ?: throw IllegalStateException("No active connection")

        val payload = Payload.fromBytes(message.toByteArray())

        suspendCancellableCoroutine { cont ->
            connectionsClient.sendPayload(connectedEndpointId, payload)
                .addOnSuccessListener {
                    Timber.d("Message sent successfully")
                    cont.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    _errorChannel.trySend("Failed to send message: ${exception.message}")
                    cont.resumeWithException(exception)
                }
        }
    }

    override fun disconnect() {
        try {
            val endpointId = _connectionInfo.value?.endpointId
            Timber.tag("COOP_CONNECTION").d("DISCONNECT_ALL: endpointId=$endpointId")

            try { stopAdvertising() } catch (e: Exception) {
                Timber.tag("COOP_CONNECTION").w("STOP_ADVERTISING_FAILED: ${e.message}")
            }

            try { stopDiscovery() } catch (e: Exception) {
                Timber.tag("COOP_CONNECTION").w("STOP_DISCOVERY_FAILED: ${e.message}")
            }

            endpointId?.let { id ->
                connectionsClient.disconnectFromEndpoint(id)
                Timber.tag("COOP_CONNECTION").d("DISCONNECTED_FROM: $id")
            }

            _connectionState.value = ConnectionState.DISCONNECTED
            _connectionInfo.value = null
            _discoveredEndpoints.value = emptyList()

            Timber.tag("COOP_CONNECTION").d("DISCONNECT_COMPLETE")
        } catch (e: Exception) {
            Timber.tag("COOP_CONNECTION").e("DISCONNECT_ERROR: ${e.message}")
        }
    }

    override fun clearDiscoveredEndpoints() {
        _discoveredEndpoints.value = emptyList()
    }

    companion object {
        private const val SERVICE_ID = "com.akinalpfdn.poprush.coop"
    }
}
