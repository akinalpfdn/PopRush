package com.akinalpfdn.poprush.game.presentation

import com.akinalpfdn.poprush.coop.domain.model.CoopBubble
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.repository.PlayerProfileRepository
import com.akinalpfdn.poprush.game.presentation.coop.CoopConnectionManager
import com.akinalpfdn.poprush.game.presentation.coop.CoopGameManager
import com.akinalpfdn.poprush.game.presentation.coop.CoopMessageHandler
import com.akinalpfdn.poprush.game.presentation.coop.CoopStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

/**
 * Facade for coop game functionality.
 * Delegates to specialized managers: CoopStateManager, CoopGameManager,
 * CoopMessageHandler, and CoopConnectionManager.
 */
class CoopHandler @Inject constructor(
    private val coopUseCase: CoopUseCase,
    private val playerProfileRepository: PlayerProfileRepository
) {
    private lateinit var stateManager: CoopStateManager
    private lateinit var connectionManager: CoopConnectionManager
    private lateinit var gameManager: CoopGameManager
    private lateinit var messageHandler: CoopMessageHandler

    val discoveredEndpoints = coopUseCase.discoveredEndpoints

    fun init(scope: CoroutineScope, gameStateFlow: MutableStateFlow<GameState>) {
        stateManager = CoopStateManager(playerProfileRepository, scope, gameStateFlow)
        gameManager = CoopGameManager(coopUseCase, stateManager, scope, gameStateFlow)
        messageHandler = CoopMessageHandler(coopUseCase, gameManager, scope, gameStateFlow)
        connectionManager = CoopConnectionManager(coopUseCase, stateManager, gameManager, messageHandler, scope, gameStateFlow)
        stateManager.initializeCache()
        Timber.d("CoopHandler initialized with sub-managers")
    }

    // State management
    fun handleShowCoopConnectionDialog() = stateManager.handleShowCoopConnectionDialog()
    fun handleHideCoopConnectionDialog() = stateManager.handleHideCoopConnectionDialog()
    fun handleShowCoopError(errorMessage: String) = stateManager.handleShowCoopError(errorMessage)
    fun handleClearCoopError() = stateManager.handleClearCoopError()
    fun handleUpdateCoopPlayerName(playerName: String) = stateManager.handleUpdateCoopPlayerName(playerName)
    fun handleUpdateCoopPlayerColor(playerColor: BubbleColor) = stateManager.handleUpdateCoopPlayerColor(playerColor)
    fun handleStartCoopAdvertising(playerName: String, selectedColor: String) = stateManager.handleStartCoopAdvertising(playerName, selectedColor)
    fun handleStartCoopDiscovery(playerName: String, selectedColor: String) = stateManager.handleStartCoopDiscovery(playerName, selectedColor)
    fun handleStopCoopConnection() = stateManager.handleStopCoopConnection()
    fun handleCoopSyncBubbles(bubbles: List<CoopBubble>) = stateManager.handleCoopSyncBubbles(bubbles)
    fun handleCoopSyncScores(localScore: Int, opponentScore: Int) = stateManager.handleCoopSyncScores(localScore, opponentScore)

    // Game management
    fun handleCoopClaimBubble(bubbleId: Int) = gameManager.handleCoopClaimBubble(bubbleId)
    fun handleCoopGameFinished(winnerId: String?) = gameManager.handleCoopGameFinished(winnerId)
    fun handleStartCoopGame() = gameManager.handleStartCoopGame()
    fun handleStartCoopMatch() = gameManager.handleStartCoopMatch()

    // Connection management
    fun handleStartCoopConnection() = connectionManager.handleStartCoopConnection()
    fun handleStartHosting() = connectionManager.handleStartHosting()
    fun handleStopHosting() = connectionManager.handleStopHosting()
    fun handleStartDiscovery() = connectionManager.handleStartDiscovery()
    fun handleStopDiscovery() = connectionManager.handleStopDiscovery()
    fun handleConnectToEndpoint(endpointId: String) = connectionManager.handleConnectToEndpoint(endpointId)
    fun handleDisconnectCoop() = connectionManager.handleDisconnectCoop()
    fun handleCloseCoopConnection() = connectionManager.handleCloseCoopConnection()
}
