package com.akinalpfdn.poprush.game.domain.usecase

import app.cash.turbine.test
import com.akinalpfdn.poprush.core.domain.util.TestClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TimerUseCaseTest {

    private lateinit var testClock: TestClock
    private lateinit var timerUseCase: TimerUseCase

    @Before
    fun setup() {
        testClock = TestClock(1000L)
        timerUseCase = TimerUseCase(testClock)
    }

    @Test
    fun `start timer sets running state`() = runTest {
        timerUseCase.startTimer(5.seconds)

        assertEquals(TimerState.RUNNING, timerUseCase.timerState.value)
        assertTrue(timerUseCase.isTimerRunning)
    }

    @Test
    fun `stop timer sets stopped state`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.stopTimer()

        assertEquals(TimerState.STOPPED, timerUseCase.timerState.value)
        assertFalse(timerUseCase.isTimerRunning)
    }

    @Test
    fun `pause timer sets paused state`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.pauseTimer()

        assertEquals(TimerState.PAUSED, timerUseCase.timerState.value)
        assertTrue(timerUseCase.isTimerPaused)
    }

    @Test
    fun `resume timer sets running state`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.pauseTimer()
        testClock.advanceBy(1000)
        timerUseCase.resumeTimer()

        assertEquals(TimerState.RUNNING, timerUseCase.timerState.value)
        assertTrue(timerUseCase.isTimerRunning)
    }

    @Test
    fun `initial time remaining matches duration`() = runTest {
        timerUseCase.startTimer(10.seconds)

        assertEquals(10.seconds, timerUseCase.timeRemaining.value)
    }

    @Test
    fun `add time increases remaining`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.addTime(3.seconds)

        assertEquals(8.seconds, timerUseCase.timeRemaining.value)
    }

    @Test
    fun `remove time decreases remaining`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.removeTime(2.seconds)

        assertEquals(3.seconds, timerUseCase.timeRemaining.value)
    }

    @Test
    fun `remove time does not go below zero`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.removeTime(10.seconds)

        assertEquals(0.milliseconds, timerUseCase.timeRemaining.value)
    }

    @Test
    fun `remove all time sets finished state`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.removeTime(5.seconds)

        assertEquals(TimerState.FINISHED, timerUseCase.timerState.value)
    }

    @Test
    fun `reset timer returns to initial state`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.resetTimer()

        assertEquals(TimerState.STOPPED, timerUseCase.timerState.value)
        assertEquals(5.seconds, timerUseCase.timeRemaining.value)
    }

    @Test
    fun `timer state flow emits correct states`() = runTest {
        timerUseCase.timerState.test {
            assertEquals(TimerState.STOPPED, awaitItem())

            timerUseCase.startTimer(5.seconds)
            assertEquals(TimerState.RUNNING, awaitItem())

            timerUseCase.pauseTimer()
            assertEquals(TimerState.PAUSED, awaitItem())

            testClock.advanceBy(500)
            timerUseCase.resumeTimer()
            assertEquals(TimerState.RUNNING, awaitItem())

            timerUseCase.stopTimer()
            assertEquals(TimerState.STOPPED, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `progress starts at zero`() = runTest {
        timerUseCase.startTimer(5.seconds)

        assertEquals(0f, timerUseCase.progress, 0.01f)
    }

    @Test
    fun `formatted time displays correctly`() = runTest {
        timerUseCase.startTimer(65.seconds)

        val displayInfo = timerUseCase.getFormattedTime()
        assertEquals("01", displayInfo.minutesDisplay)
        assertEquals("05", displayInfo.secondsDisplay)
        assertEquals("01:05", displayInfo.totalDisplay)
    }

    @Test
    fun `cannot pause when not running`() = runTest {
        timerUseCase.pauseTimer()

        assertEquals(TimerState.STOPPED, timerUseCase.timerState.value)
    }

    @Test
    fun `cannot resume when not paused`() = runTest {
        timerUseCase.startTimer(5.seconds)
        timerUseCase.resumeTimer() // Already running, should have no effect

        assertEquals(TimerState.RUNNING, timerUseCase.timerState.value)
    }
}
