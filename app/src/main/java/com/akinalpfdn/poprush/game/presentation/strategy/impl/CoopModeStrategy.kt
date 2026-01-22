package com.akinalpfdn.poprush.game.presentation.strategy.impl

import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.game.presentation.CoopHandler
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeConfig
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Strategy implementation for Co-op game mode.
 * Wraps the existing CoopHandler functionality.
 * Note: Coop mode has its own connection and game state management,
 * so this strategy primarily delegates to CoopHandler.
 */
class CoopModeStrategy(
    private val dependencies: GameModeDependencies
) : GameModeStrategy {

    override val modeId: String = "coop"
    override val modeName: String = "Co-op Mode"

    private lateinit var scope: CoroutineScope
    private lateinit var stateFlow: MutableStateFlow<GameState>

    private val config = GameModeConfig.coop()

    /**
     * Get the underlying CoopHandler for direct access to coop-specific methods.
     * This is exposed for GameViewModel to delegate coop-specific intents.
     */
    val coopHandler: CoopHandler
        get() = dependencies.coopHandler

    override suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.stateFlow = stateFlow
        dependencies.coopHandler.init(scope, stateFlow)
        Timber.d("CoopModeStrategy initialized")
    }

    override suspend fun startGame() {
        // Coop game start is handled through CoopHandler's connection flow
        // This is a no-op here as the actual game start is triggered by
        // handleStartCoopGame() and handleStartCoopMatch() in CoopHandler
        Timber.d("CoopModeStrategy: startGame called (delegated to CoopHandler)")
    }

    override suspend fun handleBubblePress(bubbleId: Int) {
        // Delegate to CoopHandler
        dependencies.coopHandler.handleCoopClaimBubble(bubbleId)
    }

    override suspend fun pauseGame() {
        // Coop mode doesn't have a traditional pause mechanism
        // The timer continues running for both players
        Timber.d("CoopModeStrategy: pauseGame called (no-op in coop)")
    }

    override suspend fun resumeGame() {
        // Coop mode doesn't have a traditional pause mechanism
        Timber.d("CoopModeStrategy: resumeGame called (no-op in coop)")
    }

    override suspend fun endGame() {
        // Delegate to CoopHandler
        dependencies.coopHandler.handleCoopGameFinished(null)
    }

    override suspend fun resetGame() {
        // Reset coop state
        stateFlow.update { currentState ->
            currentState.copy(
                isPlaying = false,
                isGameOver = false,
                isPaused = false,
                score = 0,
                currentLevel = 1,
                bubbles = emptyList(),
                timeRemaining = GameState.GAME_DURATION
            )
        }
        Timber.d("Coop game reset")
    }

    override fun cleanup() {
        // CoopHandler cleanup is handled by disconnect
        Timber.d("CoopModeStrategy cleanup called")
    }

    override fun getConfig(): GameModeConfig = config

    // Coop-specific methods that need to be accessible

    /**
     * Show the coop connection dialog.
     */
    fun showConnectionDialog() {
        dependencies.coopHandler.handleShowCoopConnectionDialog()
    }

    /**
     * Hide the coop connection dialog.
     */
    fun hideConnectionDialog() {
        dependencies.coopHandler.handleHideCoopConnectionDialog()
    }

    /**
     * Start the coop connection process.
     */
    fun startConnection() {
        dependencies.coopHandler.handleStartCoopConnection()
    }

    /**
     * Disconnect from coop game.
     */
    fun disconnect() {
        dependencies.coopHandler.handleDisconnectCoop()
    }

    /**
     * Get the discovered endpoints flow for connection UI.
     */
    fun getDiscoveredEndpoints() = dependencies.coopHandler.discoveredEndpoints
}
