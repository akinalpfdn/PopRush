package com.akinalpfdn.poprush.game.presentation.strategy.impl

import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.game.presentation.CoopHandler
import com.akinalpfdn.poprush.game.presentation.strategy.BaseGameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeConfig
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Strategy implementation for Co-op game mode.
 * Delegates to CoopHandler for connection and game management.
 * Does NOT implement PausableGameMode — multiplayer has no pause.
 */
class CoopModeStrategy(
    dependencies: GameModeDependencies
) : BaseGameModeStrategy(dependencies) {

    override val modeId: String = "coop"
    override val modeName: String = "Co-op Mode"

    private val config = GameModeConfig.coop()

    val coopHandler: CoopHandler
        get() = dependencies.coopHandler

    override suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>) {
        super.initialize(scope, stateFlow)
        dependencies.coopHandler.init(scope, stateFlow)
    }

    override suspend fun startGame() {
        Timber.d("CoopModeStrategy: startGame called (delegated to CoopHandler)")
    }

    override suspend fun handleBubblePress(bubbleId: Int) {
        dependencies.coopHandler.handleCoopClaimBubble(bubbleId)
    }

    override suspend fun endGame() {
        dependencies.coopHandler.handleCoopGameFinished(null)
    }

    override suspend fun resetGame() {
        resetStateToDefaults()
        Timber.d("Coop game reset")
    }

    override fun cleanup() {
        Timber.d("CoopModeStrategy cleanup called")
    }

    override fun getConfig(): GameModeConfig = config

    fun showConnectionDialog() {
        dependencies.coopHandler.handleShowCoopConnectionDialog()
    }

    fun hideConnectionDialog() {
        dependencies.coopHandler.handleHideCoopConnectionDialog()
    }

    fun startConnection() {
        dependencies.coopHandler.handleStartCoopConnection()
    }

    fun disconnect() {
        dependencies.coopHandler.handleDisconnectCoop()
    }

    fun getDiscoveredEndpoints() = dependencies.coopHandler.discoveredEndpoints
}
