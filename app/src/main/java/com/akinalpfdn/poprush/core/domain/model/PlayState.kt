package com.akinalpfdn.poprush.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the core gameplay state: scores, timer, level, and bubbles.
 */
data class PlayState(
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val score: Int = 0,
    val highScore: Int = 0,
    val timeRemaining: Duration = 60.seconds,
    val currentLevel: Int = 1,
    val bubbles: List<Bubble> = emptyList()
) {
    val activeBubbleCount: Int
        get() = bubbles.count { it.canBePressed }

    val pressedBubbleCount: Int
        get() = bubbles.count { it.isPressed }

    val isLevelComplete: Boolean
        get() = bubbles.any { it.isActive } &&
                bubbles.filter { it.isActive }.all { it.isPressed }

    val isTimerCritical: Boolean
        get() = timeRemaining <= 10.seconds

    val timeDisplay: String
        get() = String.format(
            "%02d:%02d",
            timeRemaining.inWholeMinutes,
            timeRemaining.inWholeSeconds % 60
        )
}
