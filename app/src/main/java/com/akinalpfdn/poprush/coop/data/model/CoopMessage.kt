package com.akinalpfdn.poprush.coop.data.model

import com.google.gson.annotations.SerializedName

/**
 * Types of coop messages for real-time synchronization
 */
enum class CoopMessageType {
    @SerializedName("chat")
    CHAT,

    @SerializedName("bubble_claim")
    BUBBLE_CLAIM,

    @SerializedName("game_start")
    GAME_START,

    @SerializedName("game_end")
    GAME_END,

    @SerializedName("score_update")
    SCORE_UPDATE,

    @SerializedName("color_selection")
    COLOR_SELECTION,

    @SerializedName("ready_state")
    READY_STATE,

    @SerializedName("heartbeat")
    HEARTBEAT
}

/**
 * Message model for coop communication between devices
 * Used for real-time game synchronization via Nearby Connections
 */
data class CoopMessage(
    @SerializedName("type")
    val type: CoopMessageType,

    @SerializedName("content")
    val content: String = "",

    @SerializedName("bubbleId")
    val bubbleId: Int? = null,

    @SerializedName("playerColor")
    val playerColor: String? = null,

    @SerializedName("localScore")
    val localScore: Int? = null,

    @SerializedName("remoteScore")
    val remoteScore: Int? = null,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Create a chat message
     */
    companion object {
        fun chat(message: String): CoopMessage {
            return CoopMessage(
                type = CoopMessageType.CHAT,
                content = message
            )
        }

        /**
         * Create a bubble claim message
         */
        fun bubbleClaim(bubbleId: Int, playerColor: String): CoopMessage {
            return CoopMessage(
                type = CoopMessageType.BUBBLE_CLAIM,
                bubbleId = bubbleId,
                playerColor = playerColor
            )
        }

        /**
         * Create a game start message
         */
        fun gameStart(): CoopMessage {
            return CoopMessage(type = CoopMessageType.GAME_START)
        }

        /**
         * Create a game end message
         */
        fun gameEnd(localScore: Int, remoteScore: Int): CoopMessage {
            return CoopMessage(
                type = CoopMessageType.GAME_END,
                localScore = localScore,
                remoteScore = remoteScore
            )
        }

        /**
         * Create a score update message
         */
        fun scoreUpdate(localScore: Int, remoteScore: Int): CoopMessage {
            return CoopMessage(
                type = CoopMessageType.SCORE_UPDATE,
                localScore = localScore,
                remoteScore = remoteScore
            )
        }

        /**
         * Create a color selection message
         */
        fun colorSelection(playerColor: String): CoopMessage {
            return CoopMessage(
                type = CoopMessageType.COLOR_SELECTION,
                playerColor = playerColor
            )
        }

        /**
         * Create a ready state message
         */
        fun readyState(isReady: Boolean): CoopMessage {
            return CoopMessage(
                type = CoopMessageType.READY_STATE,
                content = isReady.toString()
            )
        }

        /**
         * Create a heartbeat message
         */
        fun heartbeat(): CoopMessage {
            return CoopMessage(type = CoopMessageType.HEARTBEAT)
        }
    }
}