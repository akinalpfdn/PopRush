package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.model.CoopMod
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

            // In TERRITORY_WAR, bubbles already owned by opponent cannot be reclaimed
            if (coopState.selectedCoopMod == CoopMod.TERRITORY_WAR) {
                val targetBubble = coopState.bubbles.find { it.id == bubbleId }
                if (targetBubble?.owner != null && targetBubble.owner != coopState.localPlayerId) {
                    return@launch
                }
            }

            // HOT_POTATO: check bomb before updating bubbles
            val claimedBubble = coopState.bubbles.find { it.id == bubbleId }
            val wasBomb = coopState.selectedCoopMod == CoopMod.HOT_POTATO && claimedBubble?.isBomb == true

            val updatedBubbles = coopState.bubbles.map { bubble ->
                if (bubble.id == bubbleId) {
                    bubble.copy(
                        owner = coopState.localPlayerId,
                        isBomb = if (wasBomb) false else bubble.isBomb // Bomb deactivates after first claim
                    )
                } else {
                    bubble
                }
            }

            // HOT_POTATO: count-based scoring + bomb penalty tracking; others: count-based scoring
            val updatedCoopState = if (coopState.selectedCoopMod == CoopMod.HOT_POTATO) {
                val newBombCount = if (wasBomb) coopState.localBombsTriggered + 1 else coopState.localBombsTriggered
                val ownedCount = updatedBubbles.count { it.owner == coopState.localPlayerId }
                coopState.copy(
                    bubbles = updatedBubbles,
                    localBombsTriggered = newBombCount,
                    localScore = ownedCount - (newBombCount * 3),
                    opponentScore = updatedBubbles.count { it.owner == coopState.opponentPlayerId } - (coopState.opponentBombsTriggered * 3)
                )
            } else {
                coopState.copy(
                    bubbles = updatedBubbles,
                    localScore = updatedBubbles.count { it.owner == coopState.localPlayerId },
                    opponentScore = updatedBubbles.count { it.owner == coopState.opponentPlayerId }
                )
            }

            gameStateFlow.update { it.copy(coopState = updatedCoopState) }

            // In TERRITORY_WAR, check if all bubbles are claimed → game over
            if (coopState.selectedCoopMod == CoopMod.TERRITORY_WAR && updatedCoopState.allBubblesClaimed) {
                handleCoopGameFinished(null)
            }

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

            // HOT_POTATO: bubbles held - (bombs triggered × 3); others: bubble count
            val localScore = if (coopState.selectedCoopMod == CoopMod.HOT_POTATO) {
                coopState.bubbles.count { it.owner == coopState.localPlayerId } - (coopState.localBombsTriggered * 3)
            } else {
                coopState.bubbles.count { it.owner == coopState.localPlayerId }
            }
            val opponentScore = if (coopState.selectedCoopMod == CoopMod.HOT_POTATO) {
                coopState.bubbles.count { it.owner == coopState.opponentPlayerId } - (coopState.opponentBombsTriggered * 3)
            } else {
                coopState.bubbles.count { it.owner == coopState.opponentPlayerId }
            }

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
        Timber.tag("COOP_CONNECTION").d("HANDLE_START_COOP_GAME: Entering mode selection phase")
        gameStateFlow.update { currentState ->
            currentState.coopState?.let { coopState ->
                val updatedCoopState = coopState.copy(
                    gamePhase = CoopGamePhase.MODE_SELECTION
                )
                currentState.copy(coopState = updatedCoopState, showCoopConnectionDialog = false)
            } ?: currentState
        }
    }

    fun handleConfirmCoopMod() {
        Timber.tag("COOP_CONNECTION").d("HANDLE_CONFIRM_COOP_MOD: Entering setup phase")
        scope.launch {
            try {
                gameStateFlow.update { currentState ->
                    currentState.coopState?.let { coopState ->
                        val updatedCoopState = coopState.copy(
                            gamePhase = CoopGamePhase.SETUP
                        )
                        currentState.copy(coopState = updatedCoopState)
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
                var bombIds: List<Int>? = null
                gameStateFlow.update { currentState ->
                    currentState.coopState?.let { coopState ->
                        val duration = if (coopState.selectedCoopMod.isTimed) {
                            currentState.selectedDuration.inWholeMilliseconds
                        } else {
                            0L // No time limit
                        }
                        // HOT_POTATO: place bombs on bubbles
                        val bubblesWithBombs = if (coopState.selectedCoopMod == CoopMod.HOT_POTATO) {
                            val placed = stateManager.placeBombs(coopState.bubbles)
                            bombIds = placed.filter { it.isBomb }.map { it.id }
                            placed
                        } else {
                            coopState.bubbles
                        }
                        val updatedCoopState = coopState.copy(
                            gamePhase = CoopGamePhase.PLAYING,
                            gameStartTime = clock.currentTimeMillis(),
                            gameDuration = duration,
                            bubbles = bubblesWithBombs
                        )
                        currentState.copy(coopState = updatedCoopState)
                    } ?: currentState
                }

                val currentCoopState = gameStateFlow.value.coopState
                Timber.tag("COOP_CONNECTION").d("COOP_GAME_STARTED: Game is now in progress (Host)")

                if (currentCoopState?.selectedCoopMod?.isTimed == true) {
                    startCoopTimer()
                }

                coopUseCase.sendGameStart(
                    playerName = currentCoopState?.localPlayerName,
                    playerColor = currentCoopState?.localPlayerColor?.name,
                    gameDuration = currentCoopState?.gameDuration,
                    coopMod = currentCoopState?.selectedCoopMod?.name,
                    bombBubbleIds = bombIds
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
                            gamePhase = CoopGamePhase.MODE_SELECTION,
                            bubbles = freshBubbles,
                            localScore = 0,
                            opponentScore = 0,
                            localBombsTriggered = 0,
                            opponentBombsTriggered = 0,
                            gameStartTime = 0L,
                            lastTimerTick = 0L
                        )
                        currentState.copy(coopState = updatedCoopState)
                    } ?: currentState
                }

                Timber.tag("COOP_CONNECTION").d("PLAY_AGAIN: Returning to mode selection")
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
