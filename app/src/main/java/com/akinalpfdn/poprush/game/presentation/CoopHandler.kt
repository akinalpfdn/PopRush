package com.akinalpfdn.poprush.game.presentation

import com.akinalpfdn.poprush.coop.domain.model.ConnectionState
import com.akinalpfdn.poprush.coop.domain.model.CoopConnectionPhase
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CoopHandler @Inject constructor(
    private val coopUseCase: CoopUseCase,
    private val settingsRepository: SettingsRepository
) {
    private lateinit var scope: CoroutineScope
    private lateinit var gameStateFlow: MutableStateFlow<GameState>
    private var _cachedInitialCoopState: CoopGameState? = null

    val discoveredEndpoints = coopUseCase.discoveredEndpoints

    fun init(scope: CoroutineScope, gameStateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.gameStateFlow = gameStateFlow
        
        scope.launch {
            try {
                _cachedInitialCoopState = createInitialCoopState()
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize cached coop state")
                _cachedInitialCoopState = CoopGameState(
                    localPlayerId = "player_${System.currentTimeMillis()}",
                    localPlayerName = "Player",
                    localPlayerColor = BubbleColor.ROSE,
                    bubbles = generateInitialCoopBubbles()
                )
            }
        }
    }

    fun handleStartCoopAdvertising(playerName: String, selectedColor: String) {
        Timber.d("Coop advertising started for player: $playerName")
    }

    fun handleStartCoopDiscovery(playerName: String, selectedColor: String) {
        Timber.d("Coop discovery started for player: $playerName")
    }

    fun handleStopCoopConnection() {
        Timber.d("Coop connection stopped")
    }

    fun handleCoopClaimBubble(bubbleId: Int) {
        Timber.d("Coop bubble claimed: $bubbleId")
    }

    fun handleCoopSyncBubbles(bubbles: List<com.akinalpfdn.poprush.coop.domain.model.CoopBubble>) {
        Timber.d("Coop bubbles synced: ${bubbles.size}")
    }

    fun handleCoopSyncScores(localScore: Int, opponentScore: Int) {
        Timber.d("Coop scores synced: local=$localScore, opponent=$opponentScore")
    }

    fun handleCoopGameFinished(winnerId: String?) {
        Timber.d("Coop game finished. Winner: $winnerId")
    }

    fun handleShowCoopConnectionDialog() {
        gameStateFlow.update { it.copy(showCoopConnectionDialog = true) }
    }

    fun handleHideCoopConnectionDialog() {
        gameStateFlow.update { it.copy(showCoopConnectionDialog = false) }
    }

    fun handleShowCoopError(errorMessage: String) {
        gameStateFlow.update { it.copy(coopErrorMessage = errorMessage) }
    }

    fun handleClearCoopError() {
        gameStateFlow.update { it.copy(coopErrorMessage = null) }
    }

    fun handleUpdateCoopPlayerName(playerName: String) {
        scope.launch {
            try {
                settingsRepository.savePlayerName(playerName)
                gameStateFlow.update { currentState ->
                    val updatedCoopState = currentState.coopState?.copy(
                        localPlayerName = playerName
                    ) ?: getCachedInitialCoopState().copy(localPlayerName = playerName)
                    currentState.copy(coopState = updatedCoopState)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update coop player name")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to update player name: ${e.message}")
                }
            }
        }
    }

    fun handleUpdateCoopPlayerColor(playerColor: BubbleColor) {
        scope.launch {
            try {
                settingsRepository.savePlayerColor(playerColor)
                gameStateFlow.update { currentState ->
                    val updatedCoopState = currentState.coopState?.copy(
                        localPlayerColor = playerColor
                    ) ?: getCachedInitialCoopState().copy(localPlayerColor = playerColor)
                    currentState.copy(coopState = updatedCoopState)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update coop player color")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to update player color: ${e.message}")
                }
            }
        }
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
        Timber.tag("COOP_CONNECTION").d("🏠 HANDLE_START_HOSTING: called")
        scope.launch {
            try {
                gameStateFlow.update { currentState ->
                    currentState.copy(coopState = (currentState.coopState ?: getCachedInitialCoopState()).copy(isHost = true))
                }
                val coopState = gameStateFlow.value.coopState!!
                Timber.tag("COOP_CONNECTION").d("🏠 HOST_STATE: isHost=${coopState.isHost}, name=${coopState.localPlayerName}, color=${coopState.localPlayerColor}")
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
                gameStateFlow.update { currentState ->
                    currentState.copy(coopState = (currentState.coopState ?: getCachedInitialCoopState()).copy(isHost = false))
                }
                coopUseCase.startDiscovering()
                    .collect { result ->
                        result.onSuccess {
                            collectCoopConnectionState()
                        }.onFailure { exception ->
                            gameStateFlow.update {
                                it.copy(coopErrorMessage = "Discovery failed: ${exception.message}")
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

    fun handleStartCoopGame() {
        Timber.tag("COOP_CONNECTION").d("🎮 HANDLE_START_COOP_GAME: Entering setup phase")
        scope.launch {
            try {
                gameStateFlow.update { currentState ->
                    currentState.coopState?.let { coopState ->
                        val updatedCoopState = coopState.copy(
                            gamePhase = CoopGamePhase.SETUP
                        )
                        currentState.copy(coopState = updatedCoopState, showCoopConnectionDialog = false)
                    } ?: currentState
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to enter coop setup")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to enter setup: ${e.message}")
                }
            }
        }
    }

    fun handleStartCoopMatch() {
        Timber.tag("COOP_CONNECTION").d("🎮 HANDLE_START_COOP_MATCH: Starting coop match!")
        scope.launch {
            try {
                coopUseCase.sendGameStart().collect { result ->
                    result.onSuccess {
                        Timber.tag("COOP_CONNECTION").d("✅ GAME_START message sent successfully")
                    }.onFailure { e ->
                        Timber.tag("COOP_CONNECTION").e(e, "❌ Failed to send GAME_START message")
                    }
                }

                gameStateFlow.update { currentState ->
                    currentState.coopState?.let { coopState ->
                        val updatedCoopState = coopState.copy(
                            gamePhase = CoopGamePhase.PLAYING,
                            gameStartTime = System.currentTimeMillis()
                        )
                        currentState.copy(coopState = updatedCoopState)
                    } ?: currentState
                }
                Timber.tag("COOP_CONNECTION").d("🎮 COOP_GAME_STARTED: Game is now in progress")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start coop game")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to start game: ${e.message}")
                }
            }
        }
    }

    fun handleDisconnectCoop() {
        scope.launch {
            try {
                coopUseCase.disconnect()
                gameStateFlow.update { currentState ->
                    currentState.copy(
                        coopState = getCachedInitialCoopState(),
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
        gameStateFlow.update {
            it.copy(
                showCoopConnectionDialog = false,
                coopErrorMessage = null
            )
        }
    }

    fun collectCoopMessages() {
        scope.launch {
            coopUseCase.coopMessages.collect { message ->
                Timber.tag("COOP_MESSAGES").d("📩 Received message: type=${message.type}, content=${message.content}")
                when (message.type) {
                    com.akinalpfdn.poprush.coop.data.model.CoopMessageType.GAME_START -> {
                        Timber.tag("COOP_MESSAGES").d("🎮 Received GAME_START message! Starting game...")
                        gameStateFlow.update { currentState ->
                            currentState.coopState?.let { coopState ->
                                val updatedCoopState = coopState.copy(
                                    gamePhase = CoopGamePhase.PLAYING,
                                    gameStartTime = System.currentTimeMillis()
                                )
                                currentState.copy(coopState = updatedCoopState, showCoopConnectionDialog = false)
                            } ?: currentState
                        }
                    }
                    com.akinalpfdn.poprush.coop.data.model.CoopMessageType.GAME_END -> {
                        Timber.tag("COOP_MESSAGES").d("🏁 Received GAME_END message")
                    }
                    else -> {
                        // Handle other messages
                    }
                }
            }
        }
    }

    private suspend fun createInitialCoopState() = CoopGameState(
        localPlayerId = "player_${System.currentTimeMillis()}",
        localPlayerName = settingsRepository.getPlayerName(),
        localPlayerColor = settingsRepository.getPlayerColor(),
        bubbles = generateInitialCoopBubbles()
    )

    private fun getCachedInitialCoopState(): CoopGameState {
        return _cachedInitialCoopState ?: CoopGameState(
            localPlayerId = "player_${System.currentTimeMillis()}",
            localPlayerName = "Player",
            localPlayerColor = BubbleColor.ROSE,
            bubbles = generateInitialCoopBubbles()
        )
    }

    private fun generateInitialCoopBubbles(): List<com.akinalpfdn.poprush.coop.domain.model.CoopBubble> {
        val rowSizes = listOf(5, 6, 7, 8, 7, 6, 5)
        var bubbleId = 0
        val bubbles = mutableListOf<com.akinalpfdn.poprush.coop.domain.model.CoopBubble>()

        for ((rowIndex, size) in rowSizes.withIndex()) {
            for (colIndex in 0 until size) {
                bubbles.add(
                    com.akinalpfdn.poprush.coop.domain.model.CoopBubble(
                        id = bubbleId,
                        position = bubbleId,
                        row = rowIndex,
                        col = colIndex
                    )
                )
                bubbleId++
            }
        }
        return bubbles
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
                                getCachedInitialCoopState().copy(
                                    isConnectionEstablished = connectionState == ConnectionState.CONNECTED,
                                    connectionPhase = connectionPhase
                                )
                            }
                            currentState.copy(coopState = updatedCoopState)
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
