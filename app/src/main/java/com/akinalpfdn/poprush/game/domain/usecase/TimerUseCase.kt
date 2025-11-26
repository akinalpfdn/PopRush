package com.akinalpfdn.poprush.game.domain.usecase

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Timer state representing different timer conditions.
 */
enum class TimerState {
    STOPPED,
    RUNNING,
    PAUSED,
    FINISHED
}

/**
 * Use case for managing the game timer.
 * Handles countdown, pause/resume, and timer state management.
 */
@Singleton
class TimerUseCase @Inject constructor() {

    companion object {
        private const val TICK_INTERVAL_MS = 100L // Update every 100ms for smooth countdown
    }

    // Coroutine scope for timer operations
    private val timerScope = CoroutineScope(SupervisorJob())

    // Timer state
    private val _timeRemaining = MutableStateFlow(Duration.ZERO)
    val timeRemaining: StateFlow<Duration> = _timeRemaining.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    // Internal timer management
    private var timerJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private var totalDuration: Duration = Duration.ZERO
    private var startTime: Long = 0L
    private var pausedTime: Long = 0L
    private var pauseStartTime: Long = 0L

    /**
     * Starts the timer with the specified duration.
     *
     * @param duration The total duration for the timer
     */
    suspend fun startTimer(duration: Duration = 5.seconds) {
        try {
            // Stop any existing timer
            stopTimer()

            totalDuration = duration
            _timeRemaining.value = duration
            _timerState.value = TimerState.RUNNING
            isRunning.set(true)
            startTime = System.currentTimeMillis()
            pausedTime = 0L

            timerJob = timerScope.launch {
                while (isActive && _timeRemaining.value > Duration.ZERO) {
                    delay(TICK_INTERVAL_MS)

                    if (isRunning.get()) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val remaining = (totalDuration.inWholeMilliseconds - elapsed).coerceAtLeast(0)

                        _timeRemaining.value = remaining.milliseconds

                        // Check if timer has finished
                        if (remaining <= 0L) {
                            _timerState.value = TimerState.FINISHED
                            break
                        }

                        // Emit critical time warning
                        if (remaining <= 10_000L && remaining > 9_900L) {
                            Timber.d("Timer critical: ${remaining / 1000}s remaining")
                        }
                    }
                }
            }

            Timber.d("Timer started with duration: $duration")

        } catch (e: Exception) {
            Timber.e(e, "Error starting timer")
            _timerState.value = TimerState.STOPPED
        }
    }

    /**
     * Stops the timer completely.
     */
    suspend fun stopTimer() {
        try {
            timerJob?.cancel()
            timerJob = null
            isRunning.set(false)
            _timerState.value = TimerState.STOPPED
            _timeRemaining.value = Duration.ZERO
            Timber.d("Timer stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping timer")
        }
    }

    /**
     * Pauses the timer temporarily.
     */
    suspend fun pauseTimer() {
        try {
            if (isRunning.get() && _timerState.value == TimerState.RUNNING) {
                isRunning.set(false)
                _timerState.value = TimerState.PAUSED
                // Store the pause time to adjust startTime when resuming
                pauseStartTime = System.currentTimeMillis()
                Timber.d("Timer paused at: ${_timeRemaining.value}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error pausing timer")
        }
    }

    /**
     * Resumes a paused timer.
     */
    suspend fun resumeTimer() {
        try {
            if (!isRunning.get() && _timerState.value == TimerState.PAUSED) {
                isRunning.set(true)
                _timerState.value = TimerState.RUNNING
                // Adjust startTime to account for the pause duration
                val pauseDuration = System.currentTimeMillis() - pauseStartTime
                startTime += pauseDuration
                pauseStartTime = 0L
                Timber.d("Timer resumed from: ${_timeRemaining.value}, pauseDuration: ${pauseDuration}ms")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error resuming timer")
        }
    }

    /**
     * Resets the timer to its initial duration.
     */
    suspend fun resetTimer() {
        try {
            stopTimer()
            _timeRemaining.value = totalDuration
            _timerState.value = TimerState.STOPPED
            pausedTime = 0L
            startTime = 0L
            Timber.d("Timer reset to: $totalDuration")
        } catch (e: Exception) {
            Timber.e(e, "Error resetting timer")
        }
    }

    /**
     * Adds time to the current timer.
     *
     * @param additionalTime Time to add
     */
    suspend fun addTime(additionalTime: Duration) {
        try {
            val currentRemaining = _timeRemaining.value
            val newRemaining = currentRemaining + additionalTime
            _timeRemaining.value = newRemaining

            // If timer was finished, restart it with the new time
            if (_timerState.value == TimerState.FINISHED && newRemaining > Duration.ZERO) {
                _timerState.value = TimerState.RUNNING
                isRunning.set(true)
            }

            // Also extend total duration for consistency
            totalDuration += additionalTime

            Timber.d("Added $additionalTime to timer. New remaining: $newRemaining")
        } catch (e: Exception) {
            Timber.e(e, "Error adding time to timer")
        }
    }

    /**
     * Reduces time from the current timer.
     *
     * @param timeReduction Time to remove
     */
    suspend fun removeTime(timeReduction: Duration) {
        try {
            val currentRemaining = _timeRemaining.value
            val newRemaining = (currentRemaining - timeReduction).coerceAtLeast(Duration.ZERO)
            _timeRemaining.value = newRemaining
            totalDuration = (totalDuration - timeReduction).coerceAtLeast(Duration.ZERO)

            if (newRemaining <= Duration.ZERO && _timerState.value == TimerState.RUNNING) {
                _timerState.value = TimerState.FINISHED
                isRunning.set(false)
            }

            Timber.d("Removed $timeReduction from timer. New remaining: $newRemaining")
        } catch (e: Exception) {
            Timber.e(e, "Error removing time from timer")
        }
    }

    /**
     * Gets the timer flow for reactive UI updates.
     * Emits the remaining time every tick.
     */
    fun getTimerFlow(): Flow<Duration> = _timeRemaining.asStateFlow()

    /**
     * Gets the timer state flow for reactive UI updates.
     */
    fun getTimerStateFlow(): Flow<TimerState> = _timerState.asStateFlow()

    /**
     * Checks if the timer is currently running.
     */
    val isTimerRunning: Boolean
        get() = isRunning.get() && _timerState.value == TimerState.RUNNING

    /**
     * Checks if the timer is currently paused.
     */
    val isTimerPaused: Boolean
        get() = _timerState.value == TimerState.PAUSED

    /**
     * Checks if the timer has finished.
     */
    val isTimerFinished: Boolean
        get() = _timerState.value == TimerState.FINISHED

    /**
     * Gets the elapsed time since the timer started.
     */
    val elapsedTime: Duration
        get() {
            val elapsed = if (isRunning.get()) {
                System.currentTimeMillis() - startTime - pausedTime
            } else {
                pausedTime
            }
            return elapsed.milliseconds
        }

    /**
     * Gets the progress as a float between 0.0 and 1.0.
     */
    val progress: Float
        get() {
            if (totalDuration <= Duration.ZERO) return 1f
            val remaining = _timeRemaining.value.inWholeMilliseconds.toFloat()
            val total = totalDuration.inWholeMilliseconds.toFloat()
            return 1f - (remaining / total)
        }

    /**
     * Gets formatted time strings for display.
     */
    fun getFormattedTime(): TimerDisplayInfo {
        val remaining = _timeRemaining.value
        val elapsed = elapsedTime

        return TimerDisplayInfo(
            minutesDisplay = String.format("%02d", remaining.inWholeMinutes),
            secondsDisplay = String.format("%02d", remaining.inWholeSeconds % 60),
            millisecondsDisplay = String.format("%03d", remaining.inWholeMilliseconds % 1000),
            totalDisplay = String.format("%02d:%02d", remaining.inWholeMinutes, remaining.inWholeSeconds % 60),
            isCritical = remaining <= 10.seconds,
            percentageRemaining = if (totalDuration > Duration.ZERO) {
                (remaining.inWholeMilliseconds.toFloat() / totalDuration.inWholeMilliseconds * 100f)
            } else 0f,
            elapsedDisplay = String.format("%02d:%02d", elapsed.inWholeMinutes, elapsed.inWholeSeconds % 60)
        )
    }

    /**
     * Cleanup resources when the timer is no longer needed.
     */
    suspend fun cleanup() {
        stopTimer()
        Timber.d("TimerUseCase cleaned up")
    }
}

/**
 * Information for displaying timer in the UI.
 */
data class TimerDisplayInfo(
    val minutesDisplay: String,
    val secondsDisplay: String,
    val millisecondsDisplay: String,
    val totalDisplay: String,
    val isCritical: Boolean,
    val percentageRemaining: Float,
    val elapsedDisplay: String
)