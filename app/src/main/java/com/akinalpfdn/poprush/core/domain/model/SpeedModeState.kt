package com.akinalpfdn.poprush.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the state management for speed mode gameplay.
 *
 * @param currentInterval Current spawn interval in seconds between bubble activations
 * @param elapsedTimeInPhase Time elapsed in the current 3-second phase
 * @param phaseCount Number of 3-second phases completed (affects interval reduction)
 * @param nextBubbleId ID of the next bubble to be activated randomly
 * @param lastActivationTime Timestamp when the last bubble was activated
 * @param isGameOver Whether speed mode has ended (all cells active)
 */
data class SpeedModeState(
    val currentInterval: Float = 1.5f,           // Current spawn interval in seconds
    val elapsedTimeInPhase: Duration = 0.seconds, // Time in current 3-second phase
    val phaseCount: Int = 0,                     // Number of 3-second phases completed
    val nextBubbleId: Int? = null,               // Next bubble to activate randomly
    val lastActivationTime: Long = 0L,           // When last bubble was activated
    val isGameOver: Boolean = false              // Whether speed mode has ended
) {
    /**
     * Calculates the new interval based on the current phase count.
     * Speed increases by 0.05 seconds every 3 seconds (every phase).
     */
    fun calculateNewInterval(): Float {
        return (1.5f - (phaseCount * 0.05f)).coerceAtLeast(0.1f) // Minimum 0.1s interval
    }

    /**
     * Checks if it's time to progress to the next phase.
     */
    fun shouldAdvanceToNextPhase(): Boolean {
        return elapsedTimeInPhase >= 3.seconds
    }

    /**
     * Creates a new state with advanced phase and updated interval.
     */
    fun advanceToNextPhase(): SpeedModeState {
        return copy(
            phaseCount = phaseCount + 1,
            elapsedTimeInPhase = 0.seconds,
            currentInterval = calculateNewInterval()
        )
    }

    companion object {
        /**
         * Initial interval for speed mode bubble activation.
         */
        const val INITIAL_INTERVAL: Float = 1.5f

        /**
         * Interval reduction amount per phase.
         */
        const val INTERVAL_REDUCTION: Float = 0.05f

        /**
         * Phase duration in seconds.
         */
        val PHASE_DURATION: Duration = 3.seconds

        /**
         * Minimum allowed interval to prevent impossible gameplay.
         */
        const val MINIMUM_INTERVAL: Float = 0.1f
    }
}