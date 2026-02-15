package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.util.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages coop game lifecycle: bubble claims, game start/finish, and timer.
 */
class CoopGameManager(
    private val coopUseCase: CoopUseCase,
    private val stateManager: CoopStateManager,
    private val clock: Clock,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<GameState>
) {
    private var coopTimerJob: Job? = null

    fun handleCoopClaimBubble(bubbleId: Int) {
        scope.launch {
            val currentState = gameStateFlow.value
            val coopState = currentState.coopState ?: return@launch

            if (coopState.gamePhase != CoopGamePhase.PLAYING) return@launch

            val updatedBubbles = coopState.bubbles.map { bubble ->
                if (bubble.id == bubbleId) {
                    bubble.copy(owner = coopState.localPlayerId)
                } else {
                    bubble
                }
            }

            val updatedCoopState = coopState.copy(
                bubbles = updatedBubbles,
                localScore = updatedBubbles.count { it.owner == coopState.localPlayerId },
                opponentScore = updatedBubbles.count { it.owner == coopState.opponentPlayerId }
            )

            gameStateFlow.update { it.copy(coopState = updatedCoopState) }

            try {
                coopUseCase.sendBubbleClaim(bubbleId, coopState.localPlayerColor)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send bubble claim")
            }
        }
    }

    fun handleCoopGameFinished(winnerId: String?) {
        scope.launch {
            val currentState = gameStateFlow.value
            val coopState = currentState.coopState ?: return@launch

            val localScore = coopState.bubbles.count { it.owner == coopState.localPlayerId }
            val opponentScore = coopState.bubbles.count { it.owner == coopState.opponentPlayerId }

            val finalWinnerId = winnerId ?: if (localScore > opponentScore) {
                coopState.localPlayerId
            } else if (opponentScore > localScore) {
                coopState.opponentPlayerId
            } else {
                null
            }

            Timber.d("Coop game finished. Winner: $finalWinnerId")

            gameStateFlow.update { state ->
                state.coopState?.let { currentCoopState ->
                    val updatedCoopState = currentCoopState.copy(
                        gamePhase = CoopGamePhase.FINISHED,
                        localScore = localScore,
                        opponentScore = opponentScore
                    )
                    state.copy(coopState = updatedCoopState)
                } ?: state
            }

            if (coopState.isHost) {
                try {
                    coopUseCase.sendGameEnd(localScore, opponentScore)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to send game end message")
                }
            }
        }
    }

    fun handleStartCoopGame() {
        Timber.tag("COOP_CONNECTION").d("HANDLE_START_COOP_GAME: Entering setup phase")
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

                coopUseCase.sendGameSetup()
                Timber.tag("COOP_CONNECTION").d("GAME_SETUP message sent successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to enter coop setup")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to enter setup: ${e.message}")
                }
            }
        }
    }

    fun handleStartCoopMatch() {
        Timber.tag("COOP_CONNECTION").d("HANDLE_START_COOP_MATCH: Starting coop match!")
        scope.launch {
            try {
                gameStateFlow.update { currentState ->
                    currentState.coopState?.let { coopState ->
                        val updatedCoopState = coopState.copy(
                            gamePhase = CoopGamePhase.PLAYING,
                            gameStartTime = clock.currentTimeMillis(),
                            gameDuration = currentState.selectedDuration.inWholeMilliseconds
                        )
                        currentState.copy(coopState = updatedCoopState)
                    } ?: currentState
                }

                val currentCoopState = gameStateFlow.value.coopState
                Timber.tag("COOP_CONNECTION").d("COOP_GAME_STARTED: Game is now in progress (Host)")

                startCoopTimer()

                coopUseCase.sendGameStart(
                    playerName = currentCoopState?.localPlayerName,
                    playerColor = currentCoopState?.localPlayerColor?.name,
                    gameDuration = currentCoopState?.gameDuration
                )
                Timber.tag("COOP_CONNECTION").d("GAME_START message sent successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start coop game")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to start game: ${e.message}")
                }
            }
        }
    }

    fun handlePlayAgain() {
        coopTimerJob?.cancel()
        coopTimerJob = null
        scope.launch {
            try {
                val freshBubbles = stateManager.generateInitialCoopBubbles()
                gameStateFlow.update { currentState ->
                    currentState.coopState?.let { coopState ->
                        val updatedCoopState = coopState.copy(
                            gamePhase = CoopGamePhase.SETUP,
                            bubbles = freshBubbles,
                            localScore = 0,
                            opponentScore = 0,
                            gameStartTime = 0L,
                            lastTimerTick = 0L
                        )
                        currentState.copy(coopState = updatedCoopState)
                    } ?: currentState
                }

                val isHost = gameStateFlow.value.coopState?.isHost == true
                if (isHost) {
                    coopUseCase.sendGameSetup()
                    Timber.tag("COOP_CONNECTION").d("PLAY_AGAIN: Host sent GAME_SETUP")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to restart coop game")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to restart: ${e.message}")
                }
            }
        }
    }

    fun startCoopTimer() {
        coopTimerJob?.cancel()
        coopTimerJob = scope.launch {
            while (isActive) {
                val currentState = gameStateFlow.value
                val coopState = currentState.coopState ?: break

                if (coopState.gamePhase != CoopGamePhase.PLAYING) break

                val now = clock.currentTimeMillis()
                gameStateFlow.update { it.copy(coopState = coopState.copy(lastTimerTick = now)) }

                if (coopState.gameStartTime > 0) {
                    val elapsed = clock.currentTimeMillis() - coopState.gameStartTime
                    if (elapsed >= coopState.gameDuration) {
                        handleCoopGameFinished(null)
                        break
                    }
                }

                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun cancelTimer() {
        coopTimerJob?.cancel()
        coopTimerJob = null
    }
}
