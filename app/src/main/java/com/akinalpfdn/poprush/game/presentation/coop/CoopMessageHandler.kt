package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.data.model.CoopMessageType
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.model.CoopMod
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Handles incoming coop messages and updates game state accordingly.
 */
class CoopMessageHandler(
    private val coopUseCase: CoopUseCase,
    private val gameManager: CoopGameManager,
    private val clock: Clock,
    private val gameStateFlow: MutableStateFlow<GameState>
) {

    suspend fun collectCoopMessages() {
        coopUseCase.coopMessages.collect { message ->
            Timber.tag("COOP_MESSAGES").d("Received message: type=${message.type}, content=${message.content}")
            when (message.type) {
                CoopMessageType.GAME_SETUP -> {
                    Timber.tag("COOP_MESSAGES").d("Received GAME_SETUP message! Waiting for host...")
                    gameStateFlow.update { currentState ->
                        currentState.coopState?.let { coopState ->
                            val updatedCoopState = coopState.copy(
                                gamePhase = CoopGamePhase.SETUP
                            )
                            currentState.copy(coopState = updatedCoopState, showCoopConnectionDialog = false)
                        } ?: currentState
                    }
                }
                CoopMessageType.PLAYER_PROFILE -> {
                    Timber.tag("COOP_MESSAGES").d("Received PLAYER_PROFILE: name=${message.playerName}, color=${message.playerColor}")
                    gameStateFlow.update { currentState ->
                        currentState.coopState?.let { coopState ->
                            val updatedCoopState = coopState.copy(
                                opponentPlayerName = message.playerName ?: coopState.opponentPlayerName,
                                opponentPlayerColor = message.playerColor?.let { colorName ->
                                    try {
                                        BubbleColor.valueOf(colorName)
                                    } catch (e: IllegalArgumentException) {
                                        coopState.opponentPlayerColor
                                    }
                                } ?: coopState.opponentPlayerColor
                            )
                            currentState.copy(coopState = updatedCoopState)
                        } ?: currentState
                    }
                }
                CoopMessageType.GAME_START -> {
                    Timber.tag("COOP_MESSAGES").d("Received GAME_START message! Starting game... duration=${message.gameDuration}")
                    gameStateFlow.update { currentState ->
                        currentState.coopState?.let { coopState ->
                            // HOT_POTATO: mark bomb bubbles from host's bomb placement
                            val bombIdSet = message.bombBubbleIds?.toSet() ?: emptySet()
                            val bubblesWithBombs = if (bombIdSet.isNotEmpty()) {
                                coopState.bubbles.map { bubble ->
                                    if (bubble.id in bombIdSet) bubble.copy(isBomb = true) else bubble
                                }
                            } else {
                                coopState.bubbles
                            }
                            val resolvedMod = message.coopMod?.let { modName ->
                                try { CoopMod.valueOf(modName) } catch (_: IllegalArgumentException) { null }
                            } ?: coopState.selectedCoopMod
                            val updatedCoopState = coopState.copy(
                                gamePhase = CoopGamePhase.PLAYING,
                                gameStartTime = clock.currentTimeMillis(),
                                gameDuration = message.gameDuration ?: coopState.gameDuration,
                                selectedCoopMod = resolvedMod,
                                opponentPlayerName = message.playerName ?: coopState.opponentPlayerName,
                                opponentPlayerColor = message.playerColor?.let { colorName ->
                                    try {
                                        BubbleColor.valueOf(colorName)
                                    } catch (e: IllegalArgumentException) {
                                        BubbleColor.ROSE
                                    }
                                } ?: BubbleColor.ROSE,
                                bubbles = bubblesWithBombs,
                                // CHAIN_REACTION: host goes first → opponent's turn from client's perspective
                                currentTurnPlayerId = if (resolvedMod.isTurnBased) coopState.opponentPlayerId else null
                            )
                            currentState.copy(coopState = updatedCoopState, showCoopConnectionDialog = false)
                        } ?: currentState
                    }
                    val selectedMod = gameStateFlow.value.coopState?.selectedCoopMod
                    if (selectedMod?.isTimed == true) {
                        gameManager.startCoopTimer()
                    }
                }
                CoopMessageType.BUBBLE_CLAIM -> {
                    val bubbleId = message.bubbleId
                    if (bubbleId != null) {
                        gameStateFlow.update { currentState ->
                            currentState.coopState?.let { coopState ->
                                // In TERRITORY_WAR, skip if bubble already owned by local player
                                if (coopState.selectedCoopMod == CoopMod.TERRITORY_WAR) {
                                    val targetBubble = coopState.bubbles.find { it.id == bubbleId }
                                    if (targetBubble?.owner == coopState.localPlayerId) {
                                        return@let currentState
                                    }
                                }

                                // HOT_POTATO: check bomb before updating bubbles
                                val claimedBubble = coopState.bubbles.find { it.id == bubbleId }
                                val wasBomb = coopState.selectedCoopMod == CoopMod.HOT_POTATO && claimedBubble?.isBomb == true

                                val updatedBubbles = coopState.bubbles.map { bubble ->
                                    if (bubble.id == bubbleId) {
                                        bubble.copy(
                                            owner = coopState.opponentPlayerId,
                                            isBomb = if (wasBomb) false else bubble.isBomb
                                        )
                                    } else {
                                        bubble
                                    }
                                }
                                // HOT_POTATO: count-based scoring + bomb penalty; others: count-based
                                val updatedCoopState = if (coopState.selectedCoopMod == CoopMod.HOT_POTATO) {
                                    val newOpponentBombCount = if (wasBomb) coopState.opponentBombsTriggered + 1 else coopState.opponentBombsTriggered
                                    val opponentOwned = updatedBubbles.count { it.owner == coopState.opponentPlayerId }
                                    coopState.copy(
                                        bubbles = updatedBubbles,
                                        opponentBombsTriggered = newOpponentBombCount,
                                        localScore = updatedBubbles.count { it.owner == coopState.localPlayerId } - (coopState.localBombsTriggered * 3),
                                        opponentScore = opponentOwned - (newOpponentBombCount * 3)
                                    )
                                } else {
                                    coopState.copy(
                                        bubbles = updatedBubbles,
                                        localScore = updatedBubbles.count { it.owner == coopState.localPlayerId },
                                        opponentScore = updatedBubbles.count { it.owner == coopState.opponentPlayerId }
                                    )
                                }

                                // In TERRITORY_WAR, check if all bubbles claimed → game over
                                if (coopState.selectedCoopMod == CoopMod.TERRITORY_WAR && updatedCoopState.allBubblesClaimed) {
                                    val finalState = updatedCoopState.copy(gamePhase = CoopGamePhase.FINISHED)
                                    return@let currentState.copy(coopState = finalState)
                                }

                                currentState.copy(coopState = updatedCoopState)
                            } ?: currentState
                        }
                    }
                }
                CoopMessageType.TURN_END -> {
                    Timber.tag("COOP_MESSAGES").d("Received TURN_END message: claimedIds=${message.claimedBubbleIds}")
                    val claimedIds = message.claimedBubbleIds
                    if (!claimedIds.isNullOrEmpty()) {
                        val claimedIdSet = claimedIds.toSet()
                        gameStateFlow.update { currentState ->
                            currentState.coopState?.let { coopState ->
                                val updatedBubbles = coopState.bubbles.map { bubble ->
                                    if (bubble.id in claimedIdSet) bubble.copy(owner = coopState.opponentPlayerId) else bubble
                                }
                                val updatedCoopState = coopState.copy(
                                    bubbles = updatedBubbles,
                                    localScore = updatedBubbles.count { it.owner == coopState.localPlayerId },
                                    opponentScore = updatedBubbles.count { it.owner == coopState.opponentPlayerId },
                                    currentTurnPlayerId = coopState.localPlayerId
                                )

                                if (updatedCoopState.allBubblesClaimed || !updatedCoopState.hasMovesAvailable) {
                                    val finalState = updatedCoopState.copy(gamePhase = CoopGamePhase.FINISHED)
                                    return@let currentState.copy(coopState = finalState)
                                }

                                currentState.copy(coopState = updatedCoopState)
                            } ?: currentState
                        }
                    }
                }
                CoopMessageType.GAME_END -> {
                    Timber.tag("COOP_MESSAGES").d("Received GAME_END message")
                    val localScore = message.remoteScore ?: 0
                    val opponentScore = message.localScore ?: 0

                    gameStateFlow.update { currentState ->
                        currentState.coopState?.let { coopState ->
                            val updatedCoopState = coopState.copy(
                                gamePhase = CoopGamePhase.FINISHED,
                                localScore = localScore,
                                opponentScore = opponentScore
                            )
                            currentState.copy(coopState = updatedCoopState)
                        } ?: currentState
                    }
                }
                else -> {
                    // Handle other messages
                }
            }
        }
    }
}
