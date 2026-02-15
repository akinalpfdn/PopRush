package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import kotlinx.coroutines.CoroutineScope
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
                gameStateFlow.update { currentState ->
                    currentState.copy(coopState = (currentState.coopState ?: stateManager.getCachedInitialCoopState()).copy(isHost = true))
                }
                val coopState = gameStateFlow.value.coopState!!
                Timber.tag("COOP_CONNECTION").d("HOST_STATE: isHost=${coopState.isHost}, name=${coopState.localPlayerName}, color=${coopState.localPlayerColor}")
                coopUseCase.startHosting(coopState.localPlayerName, coopState.localPlayerColor)
                    .collect { result ->
                        result.onSuccess {
                            collectCoopConnectionState()
                        }.onFailure { exception ->
                            gameStateFlow.update {
                                it.copy(coopErrorMessage = "Hosting failed: ${exception.message}")
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start hosting")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to start hosting: ${e.message}")
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

                gameStateFlow.update { state ->
                    state.copy(coopState = (state.coopState ?: stateManager.getCachedInitialCoopState()).copy(isHost = false))
                }
                coopUseCase.startDiscovering()
                    .collect { result ->
                        result.onSuccess {
                            collectCoopConnectionState()
                        }.onFailure { exception ->
                            if (exception.message?.contains("8002") == true) {
                                Timber.w("Ignored STATUS_ALREADY_DISCOVERING error")
                            } else {
                                gameStateFlow.update {
                                    it.copy(coopErrorMessage = "Discovery failed: ${exception.message}")
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start discovery")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to start discovery: ${e.message}")
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
                        .collect { result ->
                            result.onSuccess {
                                collectCoopConnectionState()
                            }.onFailure { exception ->
                                gameStateFlow.update {
                                    it.copy(coopErrorMessage = "Connection failed: ${exception.message}")
                                }
                            }
                        }
                } else {
                    gameStateFlow.update {
                        it.copy(coopErrorMessage = "Cannot connect: Coop state not initialized")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to connect to endpoint")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to connect: ${e.message}")
                }
            }
        }
    }

    fun handleDisconnectCoop() {
        gameManager.cancelTimer()
        scope.launch {
            try {
                coopUseCase.disconnect()
                gameStateFlow.update { currentState ->
                    currentState.copy(
                        coopState = stateManager.getCachedInitialCoopState(),
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
        gameManager.cancelTimer()
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

    private fun collectCoopConnectionState() {
        scope.launch {
            try {
                coopUseCase.connectionState
                    .collect { connectionState ->
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
                                stateManager.getCachedInitialCoopState().copy(
                                    isConnectionEstablished = connectionState == ConnectionState.CONNECTED,
                                    connectionPhase = connectionPhase
                                )
                            }

                            if (connectionState == ConnectionState.CONNECTED && !updatedCoopState.isHost) {
                                val clientProfile = updatedCoopState
                                Timber.tag("COOP_CONNECTION").d("CLIENT_CONNECTED: Sending profile to host - name=${clientProfile.localPlayerName}, color=${clientProfile.localPlayerColor.name}")

                                scope.launch {
                                    coopUseCase.sendPlayerProfile(
                                        playerName = clientProfile.localPlayerName,
                                        playerColor = clientProfile.localPlayerColor.name
                                    ).collect { result ->
                                        result.onSuccess {
                                            Timber.tag("COOP_CONNECTION").d("CLIENT_PROFILE_SENT: Profile sent to host")
                                        }.onFailure { e ->
                                            Timber.tag("COOP_CONNECTION").e(e, "CLIENT_PROFILE_FAILED: Failed to send profile to host")
                                        }
                                    }
                                }
                            }

                            currentState.copy(coopState = updatedCoopState)
                        }

                        val currentState = gameStateFlow.value
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

                        if (connectionState == ConnectionState.CONNECTED) {
                            messageHandler.collectCoopMessages()
                        }
                    }

                coopUseCase.errorMessages
                    .collect { errorMessage ->
                        gameStateFlow.update {
                            it.copy(coopErrorMessage = errorMessage)
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to collect coop connection state")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Connection error: ${e.message}")
                }
            }
        }
    }
}
