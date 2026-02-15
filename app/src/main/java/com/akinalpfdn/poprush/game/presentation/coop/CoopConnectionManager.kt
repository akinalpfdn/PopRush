package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages coop connection lifecycle: hosting, discovery, connecting, disconnecting.
 */
class CoopConnectionManager(
    private val coopUseCase: CoopUseCase,
    private val stateManager: CoopStateManager,
    private val gameManager: CoopGameManager,
    private val messageHandler: CoopMessageHandler,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<GameState>
) {
    private var stateCollectionJob: Job? = null
    private var messageCollectionJob: Job? = null
    private var errorCollectionJob: Job? = null

    private fun ensureCollectorsRunning() {
        if (stateCollectionJob?.isActive != true) {
            stateCollectionJob = scope.launch {
                try {
                    collectConnectionState()
                } catch (e: Exception) {
                    Timber.e(e, "Connection state collection failed")
                }
            }
        }
        if (messageCollectionJob?.isActive != true) {
            messageCollectionJob = scope.launch {
                try {
                    messageHandler.collectCoopMessages()
                } catch (e: Exception) {
                    Timber.e(e, "Message collection failed")
                }
            }
        }
        if (errorCollectionJob?.isActive != true) {
            errorCollectionJob = scope.launch {
                try {
                    coopUseCase.errorMessages.collect { errorMessage ->
                        gameStateFlow.update {
                            it.copy(coopErrorMessage = errorMessage)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error collection failed")
                }
            }
        }
    }

    private fun cancelAllCollectors() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        messageCollectionJob?.cancel()
        messageCollectionJob = null
        errorCollectionJob?.cancel()
        errorCollectionJob = null
    }

    fun handleStartCoopConnection() {
        scope.launch {
            try {
                gameStateFlow.update { it.copy(showCoopConnectionDialog = true) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start coop connection")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to start connection: ${e.message}")
                }
            }
        }
    }

    fun handleStartHosting() {
        Timber.tag("COOP_CONNECTION").d("HANDLE_START_HOSTING: called")
        scope.launch {
            try {
                val initialState = stateManager.getCachedInitialCoopState()
                gameStateFlow.update { currentState ->
                    currentState.copy(coopState = (currentState.coopState ?: initialState).copy(isHost = true))
                }
                val coopState = gameStateFlow.value.coopState!!
                Timber.tag("COOP_CONNECTION").d("HOST_STATE: isHost=${coopState.isHost}, name=${coopState.localPlayerName}, color=${coopState.localPlayerColor}")

                coopUseCase.startHosting(coopState.localPlayerName, coopState.localPlayerColor)
                ensureCollectorsRunning()
            } catch (e: Exception) {
                Timber.e(e, "Failed to start hosting")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Hosting failed: ${e.message}")
                }
            }
        }
    }

    fun handleStopHosting() {
        scope.launch {
            try {
                coopUseCase.stopHosting()
            } catch (e: Exception) {
                Timber.e(e, "Failed to stop hosting")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to stop hosting: ${e.message}")
                }
            }
        }
    }

    fun handleStartDiscovery() {
        scope.launch {
            try {
                val currentState = gameStateFlow.value
                val connectionPhase = currentState.coopState?.connectionPhase

                if (connectionPhase == CoopConnectionPhase.DISCOVERING ||
                    connectionPhase == CoopConnectionPhase.CONNECTED ||
                    connectionPhase == CoopConnectionPhase.CONNECTING) {
                    Timber.w("Ignoring startDiscovery: Already in phase $connectionPhase")
                    return@launch
                }

                val initialState = stateManager.getCachedInitialCoopState()
                gameStateFlow.update { state ->
                    state.copy(coopState = (state.coopState ?: initialState).copy(isHost = false))
                }

                coopUseCase.startDiscovering()
                ensureCollectorsRunning()
            } catch (e: Exception) {
                Timber.e(e, "Failed to start discovery")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Discovery failed: ${e.message}")
                }
            }
        }
    }

    fun handleStopDiscovery() {
        scope.launch {
            try {
                coopUseCase.stopDiscovering()
            } catch (e: Exception) {
                Timber.e(e, "Failed to stop discovery")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to stop discovery: ${e.message}")
                }
            }
        }
    }

    fun handleConnectToEndpoint(endpointId: String) {
        scope.launch {
            try {
                val coopState = gameStateFlow.value.coopState
                if (coopState != null) {
                    val localEndpointName = "${coopState.localPlayerName}:${coopState.localPlayerColor.name}"
                    coopUseCase.requestConnection(endpointId, localEndpointName)
                    ensureCollectorsRunning()
                } else {
                    gameStateFlow.update {
                        it.copy(coopErrorMessage = "Cannot connect: Coop state not initialized")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect to endpoint")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Connection failed: ${e.message}")
                }
            }
        }
    }

    fun handleDisconnectCoop() {
        cancelAllCollectors()
        gameManager.cancelTimer()
        scope.launch {
            try {
                coopUseCase.disconnect()
                val initialState = stateManager.getCachedInitialCoopState()
                gameStateFlow.update { currentState ->
                    currentState.copy(
                        coopState = initialState,
                        showCoopConnectionDialog = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to disconnect")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to disconnect: ${e.message}")
                }
            }
        }
    }

    fun handleCloseCoopConnection() {
        cancelAllCollectors()
        gameManager.cancelTimer()
        coopUseCase.disconnect()
        gameStateFlow.update {
            it.copy(
                showCoopConnectionDialog = false,
                coopErrorMessage = null,
                currentScreen = StartScreenFlow.MODE_SELECTION,
                isCoopMode = false,
                gameMode = GameMode.SINGLE
            )
        }
    }

    private suspend fun collectConnectionState() {
        coopUseCase.connectionState.collect { connectionState ->
            val initialState = stateManager.getCachedInitialCoopState()

            gameStateFlow.update { currentState ->
                val currentCoopState = currentState.coopState

                val connectionPhase = when (connectionState) {
                    ConnectionState.DISCONNECTED -> CoopConnectionPhase.DISCONNECTED
                    ConnectionState.ADVERTISING -> CoopConnectionPhase.ADVERTISING
                    ConnectionState.DISCOVERING -> CoopConnectionPhase.DISCOVERING
                    ConnectionState.CONNECTING -> CoopConnectionPhase.CONNECTING
                    ConnectionState.CONNECTED -> CoopConnectionPhase.CONNECTED
                }

                val updatedCoopState = if (currentCoopState != null) {
                    currentCoopState.copy(
                        isConnectionEstablished = connectionState == ConnectionState.CONNECTED,
                        connectionPhase = connectionPhase
                    )
                } else {
                    initialState.copy(
                        isConnectionEstablished = connectionState == ConnectionState.CONNECTED,
                        connectionPhase = connectionPhase
                    )
                }

                currentState.copy(coopState = updatedCoopState)
            }

            // Send profile when client connects
            val currentState = gameStateFlow.value
            if (connectionState == ConnectionState.CONNECTED && currentState.coopState?.isHost == false) {
                val coopState = currentState.coopState!!
                Timber.tag("COOP_CONNECTION").d("CLIENT_CONNECTED: Sending profile - name=${coopState.localPlayerName}")
                try {
                    coopUseCase.sendPlayerProfile(
                        playerName = coopState.localPlayerName,
                        playerColor = coopState.localPlayerColor.name
                    )
                    Timber.tag("COOP_CONNECTION").d("CLIENT_PROFILE_SENT")
                } catch (e: Exception) {
                    Timber.tag("COOP_CONNECTION").e(e, "CLIENT_PROFILE_FAILED")
                }
            }

            // Auto-start game setup when host connects
            val isHost = currentState.coopState?.isHost == true
            val gamePhase = currentState.coopState?.gamePhase

            Timber.tag("COOP_CONNECTION").d("AUTO_START_CHECK: State=$connectionState, isHost=$isHost, Phase=$gamePhase")

            if (connectionState == ConnectionState.CONNECTED &&
                isHost &&
                gamePhase == CoopGamePhase.WAITING
            ) {
                Timber.tag("COOP_CONNECTION").d("AUTO_START_TRIGGERED: Starting game setup automatically")
                gameManager.handleStartCoopGame()
            }
        }
    }
}
