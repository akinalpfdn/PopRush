package com.akinalpfdn.poprush.game.domain.usecase

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
import com.akinalpfdn.poprush.core.domain.model.Bubble

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

    // FIX: Use SharedFlow instead of StateFlow to prevent event dropping
    private val _timerEvents = MutableSharedFlow<SpeedModeTimerEvent>(extraBufferCapacity = 64)
    val timerEvents: SharedFlow<SpeedModeTimerEvent> = _timerEvents.asSharedFlow()

    // Private state
    private var timerJob: Job? = null
    private val isTimerRunning = AtomicBoolean(false)
    private var lastActivationTime = 0L
    private var elapsedTime = Duration.ZERO
    private var lastUpdateTime = 0L

    /**
     * Starts the speed mode timer.
     * @param resetElapsed If true, resets elapsed time to zero (default: true for fresh starts)
     */
    fun startTimer(resetElapsed: Boolean = true) {
        if (isTimerRunning.compareAndSet(false, true)) {
            // Cancel any existing timer job first
            timerJob?.cancel()
            timerJob = null

            _timerState.value = SpeedModeTimerState.RUNNING
            lastUpdateTime = System.currentTimeMillis()

            // Only reset elapsed time if this is a fresh start (not a resume)
            if (resetElapsed) {
                elapsedTime = Duration.ZERO
                lastActivationTime = System.currentTimeMillis() + INITIAL_DELAY_MS
            } else {
                // When resuming, set lastActivationTime to trigger soon
                lastActivationTime = System.currentTimeMillis()
            }

            timerJob = timerScope.launch {
                // If resuming, trigger immediately
                if (!resetElapsed && isActive) {
                    delay(100)
                    if (isActive) {
                        triggerBubbleActivation()
                    }
                } else {
                    // Initial delay before first activation
                    delay(INITIAL_DELAY_MS)
                    if (isActive) {
                        triggerBubbleActivation()
                    }
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
                    val timeSinceLastActivation = currentTime - lastActivationTime
                    val currentInterval = speedModeUseCase.getCurrentIntervalMs()

                    if (timeSinceLastActivation >= currentInterval) {
                        if (isActive && isTimerRunning.get()) {
                            triggerBubbleActivation()
                        }
                    }

                    // Emit tick event for score/time updates
                    _timerEvents.tryEmit(SpeedModeTimerEvent.Tick)

                    delay(TICK_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * Stops the speed mode timer.
     */
    fun stopTimer() {
        val wasRunning = isTimerRunning.compareAndSet(true, false)
        if (wasRunning) {
            timerJob?.cancel()
            timerJob = null
            _timerState.value = SpeedModeTimerState.STOPPED
        }
    }

    /**
     * Pauses the speed mode timer.
     */
    fun pauseTimer() {
        if (isTimerRunning.get()) {
            isTimerRunning.set(false)  // Important: reset the flag so startTimer() can work again
            timerJob?.cancel()
            timerJob = null
            _timerState.value = SpeedModeTimerState.PAUSED
        }
    }

    /**
     * Resumes the speed mode timer.
     */
    fun resumeTimer() {
        if (_timerState.value == SpeedModeTimerState.PAUSED) {
            startTimer(resetElapsed = false)  // Preserve elapsed time when resuming
        }
    }

    /**
     * Triggers a bubble activation event.
     * This preserves the original method signature but delegates to the new event flow if possible.
     */
    suspend fun triggerBubbleActivation(bubbles: List<Bubble>) {
        val speedModeState = speedModeUseCase.speedModeState.value

        // Check if game is already over
        if (speedModeState.isGameOver || speedModeUseCase.isGameOver(bubbles)) {
            _timerState.value = SpeedModeTimerState.GAME_OVER
            _timerEvents.emit(SpeedModeTimerEvent.GameOver)
            stopTimer()
            return
        }

        // Emit request for random bubble activation
        _timerEvents.emit(SpeedModeTimerEvent.ActivateBubble(-1))
        lastActivationTime = System.currentTimeMillis()
    }

    /**
     * Internal trigger method used by the timer loop.
     */
    private suspend fun triggerBubbleActivation() {
        val speedModeState = speedModeUseCase.speedModeState.value

        if (speedModeState.isGameOver) {
            _timerState.value = SpeedModeTimerState.GAME_OVER
            _timerEvents.emit(SpeedModeTimerEvent.GameOver)
            stopTimer()
            return
        }

        // Trigger random activation request
        _timerEvents.emit(SpeedModeTimerEvent.ActivateBubble(-1))
        lastActivationTime = System.currentTimeMillis()
    }

    /**
     * Manually triggers bubble activation (for external use).
     */
    fun triggerManualActivation(bubbleId: Int) {
        if (isTimerRunning.get()) {
            _timerEvents.tryEmit(SpeedModeTimerEvent.ActivateBubble(bubbleId))
            lastActivationTime = System.currentTimeMillis()
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