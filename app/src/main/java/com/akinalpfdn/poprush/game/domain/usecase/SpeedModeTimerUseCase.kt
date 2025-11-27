package com.akinalpfdn.poprush.game.domain.usecase

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Speed mode timer state for managing bubble activation intervals.
 */
enum class SpeedModeTimerState {
    STOPPED,
    RUNNING,
    PAUSED,
    GAME_OVER
}

/**
 * Timer event representing different speed mode timer actions.
 */
sealed class SpeedModeTimerEvent {
    data object Tick : SpeedModeTimerEvent()
    data class ActivateBubble(val bubbleId: Int) : SpeedModeTimerEvent()
    data object IntervalChanged : SpeedModeTimerEvent()
    data object GameOver : SpeedModeTimerEvent()
}

/**
 * Use case for managing speed mode timer mechanics.
 * Coordinates bubble activation intervals with the main game timer.
 */
@Singleton
class SpeedModeTimerUseCase @Inject constructor(
    private val speedModeUseCase: SpeedModeUseCase
) {

    companion object {
        private const val TICK_INTERVAL_MS = 50L // Update every 50ms for precise timing
        private const val INITIAL_DELAY_MS = 1000L // Initial delay before first activation
    }

    // Coroutine scope for speed mode timer operations
    private val timerScope = CoroutineScope(SupervisorJob())

    // Timer state
    private val _timerState = MutableStateFlow(SpeedModeTimerState.STOPPED)
    val timerState: StateFlow<SpeedModeTimerState> = _timerState.asStateFlow()

    // Timer events flow
    private val _timerEvents = MutableStateFlow<SpeedModeTimerEvent?>(null)
    val timerEvents: StateFlow<SpeedModeTimerEvent?> = _timerEvents.asStateFlow()

    // Private state
    private var timerJob: Job? = null
    private val isTimerRunning = AtomicBoolean(false)
    private var lastActivationTime = 0L
    private var elapsedTime = Duration.ZERO
    private var lastUpdateTime = 0L

    /**
     * Starts the speed mode timer.
     */
    fun startTimer() {
        if (isTimerRunning.compareAndSet(false, true)) {
            stopTimer() // Cancel any existing timer

            _timerState.value = SpeedModeTimerState.RUNNING
            lastUpdateTime = System.currentTimeMillis()
            elapsedTime = Duration.ZERO
            lastActivationTime = System.currentTimeMillis() + INITIAL_DELAY_MS

            timerJob = timerScope.launch {
                Timber.d("Speed mode timer started")

                // Initial delay before first activation
                delay(INITIAL_DELAY_MS)
                if (isActive) {
                    triggerBubbleActivation()
                }

                // Main timer loop
                while (isActive && isTimerRunning.get()) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = currentTime - lastUpdateTime
                    lastUpdateTime = currentTime

                    elapsedTime += deltaTime.milliseconds

                    // Update speed mode state
                    speedModeUseCase.updateSpeedMode(deltaTime.milliseconds)

                    // Check if it's time to activate a new bubble
                    if (currentTime >= lastActivationTime + speedModeUseCase.getCurrentIntervalMs()) {
                        if (isActive) {
                            triggerBubbleActivation()
                        }
                    }

                    // Emit tick event
                    _timerEvents.value = SpeedModeTimerEvent.Tick

                    // Small delay for the next tick
                    delay(TICK_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * Stops the speed mode timer.
     */
    fun stopTimer() {
        if (isTimerRunning.compareAndSet(true, false)) {
            timerJob?.cancel()
            timerJob = null
            _timerState.value = SpeedModeTimerState.STOPPED
            Timber.d("Speed mode timer stopped")
        }
    }

    /**
     * Pauses the speed mode timer.
     */
    fun pauseTimer() {
        if (isTimerRunning.get()) {
            timerJob?.cancel()
            timerJob = null
            _timerState.value = SpeedModeTimerState.PAUSED
            Timber.d("Speed mode timer paused")
        }
    }

    /**
     * Resumes the speed mode timer.
     */
    fun resumeTimer() {
        if (_timerState.value == SpeedModeTimerState.PAUSED) {
            startTimer()
            Timber.d("Speed mode timer resumed")
        }
    }

    /**
     * Triggers a bubble activation event.
     */
    private suspend fun triggerBubbleActivation() {
        val speedModeState = speedModeUseCase.speedModeState.value

        if (speedModeState.isGameOver) {
            _timerState.value = SpeedModeTimerState.GAME_OVER
            _timerEvents.value = SpeedModeTimerEvent.GameOver
            stopTimer()
            return
        }

        // Select a random bubble to activate
        val (bubbleId, _) = speedModeUseCase.selectRandomBubble(emptyList()) // We'll pass actual bubbles from ViewModel

        bubbleId?.let { id ->
            lastActivationTime = System.currentTimeMillis()
            _timerEvents.value = SpeedModeTimerEvent.ActivateBubble(id)
            Timber.d("Triggered activation for bubble $id")
        } ?: run {
            // No more bubbles to activate
            _timerState.value = SpeedModeTimerState.GAME_OVER
            _timerEvents.value = SpeedModeTimerEvent.GameOver
            stopTimer()
        }
    }

    /**
     * Manually triggers bubble activation (for external use).
     */
    fun triggerManualActivation(bubbleId: Int) {
        if (isTimerRunning.get()) {
            _timerEvents.value = SpeedModeTimerEvent.ActivateBubble(bubbleId)
            lastActivationTime = System.currentTimeMillis()
            Timber.d("Manual activation triggered for bubble $bubbleId")
        }
    }

    /**
     * Resets the speed mode timer to initial state.
     */
    fun resetTimer() {
        stopTimer()
        speedModeUseCase.resetSpeedMode()
        elapsedTime = Duration.ZERO
        lastActivationTime = 0L
        lastUpdateTime = 0L
        _timerEvents.value = null
        Timber.d("Speed mode timer reset")
    }

    /**
     * Gets the current elapsed time.
     */
    fun getElapsedTime(): Duration {
        return elapsedTime
    }

    /**
     * Gets the time until next bubble activation.
     */
    fun getTimeUntilNextActivation(): Duration {
        if (!isTimerRunning.get()) return Duration.ZERO

        val currentTime = System.currentTimeMillis()
        val nextActivationTime = lastActivationTime + speedModeUseCase.getCurrentIntervalMs()
        val timeUntil = (nextActivationTime - currentTime).coerceAtLeast(0L)
        return timeUntil.milliseconds
    }

    /**
     * Checks if the timer is currently running.
     */
    fun isRunning(): Boolean {
        return isTimerRunning.get()
    }

    /**
     * Gets the current interval from the speed mode use case.
     */
    fun getCurrentInterval(): Float {
        return speedModeUseCase.speedModeState.value.currentInterval
    }

    /**
     * Cleanup when the use case is no longer needed.
     */
    fun cleanup() {
        stopTimer()
        timerScope.cancel()
        Timber.d("Speed mode timer cleaned up")
    }
}