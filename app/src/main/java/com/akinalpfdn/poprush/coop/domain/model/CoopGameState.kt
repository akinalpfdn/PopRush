package com.akinalpfdn.poprush.coop.domain.model

import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Represents the connection phase for coop multiplayer gameplay.
 */
enum class CoopConnectionPhase {
    DISCONNECTED,
    ADVERTISING,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Represents the current phase of coop gameplay.
 */
enum class CoopGamePhase {
    WAITING,
    SETUP,
    PLAYING,
    PAUSED,
    FINISHED
}

/**
 * Represents a bubble in coop mode with player ownership and transition states.
 *
 * @param id Unique identifier for the bubble
 * @param position Position in the linear array (0-43)
 * @param row Row in the hexagonal grid (0-6)
 * @param col Column within the row
 * @param owner Player ID who owns this bubble, null if unclaimed
 * @param isTransitioning Whether this bubble is currently transitioning ownership
 * @param transitionStartTime Timestamp when transition started for animation timing
 */
@Serializable
data class CoopBubble(
    val id: Int,
    val position: Int,
    val row: Int,
    val col: Int,
    val owner: String? = null, // Player ID or null for unclaimed
    val isTransitioning: Boolean = false,
    val transitionStartTime: Long = 0L
)

/**
 * Represents the complete state of coop multiplayer gameplay.
 *
 * @param isHost Whether this player is the host of the game
 * @param localPlayerId Unique identifier for the local player
 * @param localPlayerName Display name for the local player
 * @param opponentPlayerId Unique identifier for the opponent player
 * @param opponentPlayerName Display name for the opponent player
 * @param isConnectionEstablished Whether connection with opponent is established
 * @param connectionPhase Current phase of the connection process
 * @param localScore Number of bubbles owned by local player
 * @param opponentScore Number of bubbles owned by opponent player
 * @param localPlayerColor Color selected by local player
 * @param opponentPlayerColor Color selected by opponent player
 * @param bubbles List of all bubbles in the game grid
 * @param gamePhase Current phase of the game
 * @param connectionStartTime Timestamp when connection was established
 * @param gameStartTime Timestamp when the game started
 * @param errorMessage Error message if connection failed
 */
data class CoopGameState(
    val isHost: Boolean = false,
    val localPlayerId: String = "",
    val localPlayerName: String = "Player",
    val opponentPlayerId: String = "",
    val opponentPlayerName: String = "Opponent",
    val isConnectionEstablished: Boolean = false,
    val connectionPhase: CoopConnectionPhase = CoopConnectionPhase.DISCONNECTED,
    val localScore: Int = 0,
    val opponentScore: Int = 0,
    val localPlayerColor: BubbleColor = BubbleColor.ROSE,
    val opponentPlayerColor: BubbleColor = BubbleColor.SKY,
    val bubbles: List<CoopBubble> = emptyList(),
    val gamePhase: CoopGamePhase = CoopGamePhase.WAITING,
    val connectionStartTime: Long = 0L,
    val gameStartTime: Long = 0L,
    val errorMessage: String? = null
) {
    /**
     * Check if the game is currently active (playing and not paused).
     */
    val isGameActive: Boolean
        get() = isConnectionEstablished && gamePhase == CoopGamePhase.PLAYING

    /**
     * Get the elapsed time since the game started.
     */
    val elapsedTime: Duration
        get() = if (gameStartTime > 0L) {
            (System.currentTimeMillis() - gameStartTime).milliseconds
        } else {
            Duration.ZERO
        }

    /**
     * Get total number of bubbles in the game.
     */
    val totalBubbles: Int
        get() = bubbles.size

    /**
     * Get number of unclaimed bubbles.
     */
    val unclaimedBubbles: Int
        get() = bubbles.count { it.owner == null }

    /**
     * Check if all bubbles are claimed (game over condition).
     */
    val allBubblesClaimed: Boolean
        get() = bubbles.all { it.owner != null }

    /**
     * Get the current winner based on score.
     */
    val currentWinner: String?
        get() = when {
            localScore > opponentScore -> localPlayerName.ifBlank { "Player 1" }
            opponentScore > localScore -> opponentPlayerName.ifBlank { "Player 2" }
            else -> null // Tie
        }

    /**
     * Create a copy with updated scores based on current bubble ownership.
     */
    fun withUpdatedScores(): CoopGameState {
        val newLocalScore = bubbles.count { it.owner == localPlayerId }
        val newOpponentScore = bubbles.count { it.owner == opponentPlayerId }

        return copy(
            localScore = newLocalScore,
            opponentScore = newOpponentScore
        )
    }

    /**
     * Create a copy with a bubble ownership transition.
     */
    fun withBubbleTransition(
        bubbleId: Int,
        newOwner: String,
        transitionTime: Long = System.currentTimeMillis()
    ): CoopGameState {
        return copy(
            bubbles = bubbles.map { bubble ->
                if (bubble.id == bubbleId) {
                    bubble.copy(
                        owner = newOwner,
                        isTransitioning = true,
                        transitionStartTime = transitionTime
                    )
                } else {
                    bubble
                }
            }
        )
    }
}