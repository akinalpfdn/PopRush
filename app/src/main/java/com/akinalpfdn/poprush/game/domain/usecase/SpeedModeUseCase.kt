package com.akinalpfdn.poprush.game.domain.usecase

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.SpeedModeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for managing speed mode gameplay mechanics.
 * Handles bubble activation, interval management, and speed mode state.
 */
@Singleton
class SpeedModeUseCase @Inject constructor() {

    // Speed mode state
    private val _speedModeState = MutableStateFlow(SpeedModeState())
    val speedModeState: StateFlow<SpeedModeState> = _speedModeState.asStateFlow()

    /**
     * Initializes speed mode with fresh state.
     */
    fun initializeSpeedMode(): SpeedModeState {
        val newState = SpeedModeState()
        _speedModeState.value = newState
        Timber.d("Speed mode initialized with interval: ${newState.currentInterval}s")
        return newState
    }

    /**
     * Updates the speed mode state based on elapsed time.
     * Handles phase progression and interval reduction.
     */
    fun updateSpeedMode(deltaTime: Duration): SpeedModeState {
        val currentState = _speedModeState.value
        val newElapsedTime = currentState.elapsedTimeInPhase + deltaTime

        return if (currentState.shouldAdvanceToNextPhase()) {
            val advancedState = currentState.advanceToNextPhase()
            _speedModeState.value = advancedState
            Timber.d("Speed mode advanced to phase ${advancedState.phaseCount}, new interval: ${advancedState.currentInterval}s")
            advancedState
        } else {
            val updatedState = currentState.copy(elapsedTimeInPhase = newElapsedTime)
            _speedModeState.value = updatedState
            updatedState
        }
    }

    /**
     * Selects a random inactive bubble to activate.
     * Returns the selected bubble ID and updated state.
     */
    fun selectRandomBubble(bubbles: List<Bubble>): Pair<Int?, SpeedModeState> {
        val inactiveBubbles = bubbles.filter { !it.isSpeedModeActive }

        if (inactiveBubbles.isEmpty()) {
            // All bubbles are active, game over
            val gameOverState = _speedModeState.value.copy(isGameOver = true)
            _speedModeState.value = gameOverState
            Timber.d("Speed mode game over - all bubbles are active")
            return null to gameOverState
        }

        val selectedBubble = inactiveBubbles.random()
        val updatedState = _speedModeState.value.copy(
            nextBubbleId = selectedBubble.id,
            lastActivationTime = System.currentTimeMillis()
        )
        _speedModeState.value = updatedState

        Timber.d("Selected bubble ${selectedBubble.id} for activation")
        return selectedBubble.id to updatedState
    }

    /**
     * Activates the specified bubble in speed mode.
     * Makes the bubble colored (visible) when active.
     */
    fun activateBubble(bubbleId: Int, bubbles: List<Bubble>): List<Bubble> {
        return bubbles.map { bubble ->
            if (bubble.id == bubbleId) {
                bubble.copy(isSpeedModeActive = true, transparency = 1.0f)
            } else {
                bubble
            }
        }
    }

    /**
     * Deactivates the specified bubble when clicked in speed mode.
     * Makes the bubble disappear (transparent) when clicked.
     */
    fun deactivateBubble(bubbleId: Int, bubbles: List<Bubble>): List<Bubble> {
        return bubbles.map { bubble ->
            if (bubble.id == bubbleId && bubble.isSpeedModeActive) {
                bubble.copy(isSpeedModeActive = false, transparency = 0.0f)
            } else {
                bubble
            }
        }
    }

    /**
     * Checks if it's time to activate a new bubble based on the current interval.
     */
    fun shouldActivateBubble(lastActivationTime: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastActivation = currentTime - lastActivationTime
        val currentInterval = _speedModeState.value.currentInterval
        return timeSinceLastActivation >= (currentInterval * 1000) // Convert to milliseconds
    }

    /**
     * Gets all currently active speed mode bubbles.
     */
    fun getActiveBubbles(bubbles: List<Bubble>): List<Bubble> {
        return bubbles.filter { it.isSpeedModeActive }
    }

    /**
     * Checks if speed mode is game over (all bubbles active).
     */
    fun isGameOver(bubbles: List<Bubble>): Boolean {
        val allBubblesActive = bubbles.all { it.isSpeedModeActive }
        return allBubblesActive || _speedModeState.value.isGameOver
    }

    /**
     * Resets speed mode state to initial values.
     */
    fun resetSpeedMode(): SpeedModeState {
        val resetState = SpeedModeState()
        _speedModeState.value = resetState
        Timber.d("Speed mode reset to initial state")
        return resetState
    }

    /**
     * Updates the current interval manually (for testing or debugging).
     */
    fun updateInterval(newInterval: Float) {
        val validInterval = newInterval.coerceIn(0.1f, 5.0f) // Clamp between 0.1s and 5s
        val updatedState = _speedModeState.value.copy(currentInterval = validInterval)
        _speedModeState.value = updatedState
        Timber.d("Speed mode interval updated to: ${validInterval}s")
    }

    /**
     * Gets current interval in milliseconds for timer calculations.
     */
    fun getCurrentIntervalMs(): Long {
        return (_speedModeState.value.currentInterval * 1000).toLong()
    }

    companion object {
        /**
         * Minimum interval to prevent impossible gameplay.
         */
        const val MINIMUM_INTERVAL: Float = 0.1f

        /**
         * Maximum initial interval for speed mode.
         */
        const val MAXIMUM_INITIAL_INTERVAL: Float = 3.0f

        /**
         * Number of bubbles that need to be active for difficulty increase.
         */
        const val DIFFICULTY_THRESHOLD = 10
    }
}