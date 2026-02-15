package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.data.model.CoopMessageType
import com.akinalpfdn.poprush.coop.domain.model.CoopGamePhase
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.util.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles incoming coop messages and updates game state accordingly.
 */
class CoopMessageHandler(
    private val coopUseCase: CoopUseCase,
    private val gameManager: CoopGameManager,
    private val clock: Clock,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<GameState>
) {

    fun collectCoopMessages() {
        scope.launch {
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
                        Timber.tag("COOP_MESSAGES").d("Received GAME_START message! Starting game...")
                        gameStateFlow.update { currentState ->
                            currentState.coopState?.let { coopState ->
                                val updatedCoopState = coopState.copy(
                                    gamePhase = CoopGamePhase.PLAYING,
                                    gameStartTime = clock.currentTimeMillis(),
                                    opponentPlayerName = message.playerName ?: coopState.opponentPlayerName,
                                    opponentPlayerColor = message.playerColor?.let { colorName ->
                                        try {
                                            BubbleColor.valueOf(colorName)
                                        } catch (e: IllegalArgumentException) {
                                            BubbleColor.ROSE
                                        }
                                    } ?: BubbleColor.ROSE
                                )
                                currentState.copy(coopState = updatedCoopState, showCoopConnectionDialog = false)
                            } ?: currentState
                        }
                        gameManager.startCoopTimer()
                    }
                    CoopMessageType.BUBBLE_CLAIM -> {
                        val bubbleId = message.bubbleId
                        if (bubbleId != null) {
                            gameStateFlow.update { currentState ->
                                currentState.coopState?.let { coopState ->
                                    val updatedBubbles = coopState.bubbles.map { bubble ->
                                        if (bubble.id == bubbleId) {
                                            bubble.copy(owner = coopState.opponentPlayerId)
                                        } else {
                                            bubble
                                        }
                                    }
                                    val updatedCoopState = coopState.copy(
                                        bubbles = updatedBubbles,
                                        localScore = updatedBubbles.count { it.owner == coopState.localPlayerId },
                                        opponentScore = updatedBubbles.count { it.owner == coopState.opponentPlayerId }
                                    )
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
}
