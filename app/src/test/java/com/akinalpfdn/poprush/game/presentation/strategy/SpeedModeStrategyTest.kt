package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SpeedModeState
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerState
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import com.akinalpfdn.poprush.game.presentation.CoopHandler
import com.akinalpfdn.poprush.game.presentation.strategy.impl.SpeedModeStrategy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SpeedModeStrategyTest {

    private lateinit var strategy: SpeedModeStrategy
    private lateinit var gameStateFlow: MutableStateFlow<GameState>
    private lateinit var dependencies: GameModeDependencies

    private val gameRepository: GameRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val audioRepository: AudioRepository = mockk(relaxed = true)
    private val initializeGameUseCase: InitializeGameUseCase = mockk()
    private val generateLevelUseCase: GenerateLevelUseCase = mockk(relaxed = true)
    private val handleBubblePressUseCase: HandleBubblePressUseCase = mockk(relaxed = true)
    private val timerUseCase: TimerUseCase = mockk(relaxed = true)
    private val speedModeUseCase: SpeedModeUseCase = mockk(relaxed = true)
    private val speedModeTimerUseCase: SpeedModeTimerUseCase = mockk(relaxed = true)
    private val coopUseCase: CoopUseCase = mockk(relaxed = true)
    private val coopHandler: CoopHandler = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createTestBubbles(count: Int = 10): List<Bubble> =
        (0 until count).map { i ->
            Bubble(
                id = i,
                position = i,
                row = i / 5,
                col = i % 5,
                color = BubbleColor.ROSE,
                isActive = false,
                isPressed = false,
                transparency = 1.0f,
                isSpeedModeActive = false
            )
        }

    @Before
    fun setup() {
        dependencies = GameModeDependencies(
            gameRepository = gameRepository,
            settingsRepository = settingsRepository,
            audioRepository = audioRepository,
            initializeGameUseCase = initializeGameUseCase,
            generateLevelUseCase = generateLevelUseCase,
            handleBubblePressUseCase = handleBubblePressUseCase,
            timerUseCase = timerUseCase,
            speedModeUseCase = speedModeUseCase,
            speedModeTimerUseCase = speedModeTimerUseCase,
            coopUseCase = coopUseCase,
            coopHandler = coopHandler
        )

        strategy = SpeedModeStrategy(dependencies)
        gameStateFlow = MutableStateFlow(GameState())

        // Default mock behaviors
        every { speedModeUseCase.speedModeState } returns MutableStateFlow(SpeedModeState())
        every { speedModeTimerUseCase.timerState } returns MutableStateFlow(SpeedModeTimerState.STOPPED)
        every { speedModeTimerUseCase.timerEvents } returns MutableSharedFlow()
        every { settingsRepository.getHapticFeedbackFlow() } returns flowOf(true)
    }

    @Test
    fun `strategy has correct mode id and name`() {
        assertEquals("speed", strategy.modeId)
        assertEquals("Speed Mode", strategy.modeName)
    }

    @Test
    fun `config returns speed configuration`() {
        val config = strategy.getConfig()
        assertEquals("Speed", config.displayName)
        assertFalse(config.hasTimer)
        assertFalse(config.hasLevels)
        assertFalse(config.supportsDurationSelection)
        assertFalse(config.supportsCoop)
    }

    @Test
    fun `startGame initializes speed mode state`() = runTest(testDispatcher) {
        val testBubbles = createTestBubbles()
        coEvery { initializeGameUseCase.execute() } returns testBubbles
        every { speedModeUseCase.initializeSpeedMode() } returns SpeedModeState()

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.startGame()

        verify { speedModeUseCase.initializeSpeedMode() }
        verify { speedModeTimerUseCase.startTimer(any()) }

        val state = gameStateFlow.value
        assertTrue(state.isPlaying)
        assertFalse(state.isGameOver)
        assertEquals(0, state.score)
    }

    @Test
    fun `startGame resets all bubbles to inactive`() = runTest(testDispatcher) {
        val testBubbles = createTestBubbles()
        coEvery { initializeGameUseCase.execute() } returns testBubbles
        every { speedModeUseCase.initializeSpeedMode() } returns SpeedModeState()

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.startGame()

        val state = gameStateFlow.value
        assertTrue(state.bubbles.all { !it.isActive })
        assertTrue(state.bubbles.all { !it.isPressed })
        assertTrue(state.bubbles.all { !it.isSpeedModeActive })
    }

    @Test
    fun `handleBubblePress deactivates speed mode active bubble`() = runTest(testDispatcher) {
        val bubbles = createTestBubbles().map {
            if (it.id == 3) it.copy(isSpeedModeActive = true) else it
        }
        val deactivatedBubbles = bubbles.map {
            if (it.id == 3) it.copy(isSpeedModeActive = false) else it
        }

        gameStateFlow.value = GameState(bubbles = bubbles, isPlaying = true, score = 5)
        every { speedModeUseCase.deactivateBubble(3, any()) } returns deactivatedBubbles

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.handleBubblePress(3)

        assertEquals(deactivatedBubbles, gameStateFlow.value.bubbles)
        assertEquals(6, gameStateFlow.value.score)
    }

    @Test
    fun `handleBubblePress ignores non-speed-mode-active bubble`() = runTest(testDispatcher) {
        val bubbles = createTestBubbles()
        gameStateFlow.value = GameState(bubbles = bubbles, isPlaying = true, score = 5)

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.handleBubblePress(3)

        // Score should remain unchanged
        assertEquals(5, gameStateFlow.value.score)
    }

    @Test
    fun `pauseGame delegates to speed mode timer`() = runTest(testDispatcher) {
        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.pauseGame()

        coVerify { speedModeTimerUseCase.pauseTimer() }
    }

    @Test
    fun `resumeGame delegates to speed mode timer`() = runTest(testDispatcher) {
        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.resumeGame()

        coVerify { speedModeTimerUseCase.resumeTimer() }
    }

    @Test
    fun `endGame stops timer and marks game over`() = runTest(testDispatcher) {
        gameStateFlow.value = GameState(
            isPlaying = true,
            timeRemaining = 45.seconds,
            highScores = mapOf("speed" to 100),
            selectedMod = com.akinalpfdn.poprush.core.domain.model.GameMod.SPEED
        )
        every { speedModeUseCase.speedModeState } returns MutableStateFlow(SpeedModeState(isGameOver = true))

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.endGame()

        verify { speedModeTimerUseCase.stopTimer() }
        coVerify { gameRepository.saveGameResult(any()) }

        val state = gameStateFlow.value
        assertTrue(state.isGameOver)
        assertFalse(state.isPlaying)
    }

    @Test
    fun `resetGame clears speed mode state and resets bubbles`() = runTest(testDispatcher) {
        val activeBubbles = createTestBubbles().map {
            it.copy(isSpeedModeActive = true, transparency = 0.5f)
        }
        gameStateFlow.value = GameState(
            isPlaying = true,
            score = 20,
            bubbles = activeBubbles
        )
        every { speedModeUseCase.resetSpeedMode() } returns SpeedModeState()

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.resetGame()

        verify { speedModeTimerUseCase.stopTimer() }
        verify { speedModeUseCase.resetSpeedMode() }

        val state = gameStateFlow.value
        assertTrue(state.bubbles.all { !it.isSpeedModeActive })
        assertTrue(state.bubbles.all { !it.isActive })
        assertTrue(state.bubbles.all { !it.isPressed })
    }

    @Test
    fun `cleanup stops timer and cancels jobs`() = runTest(testDispatcher) {
        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.cleanup()

        verify { speedModeTimerUseCase.stopTimer() }
    }

    @Test
    fun `score is based on elapsed time in seconds`() = runTest(testDispatcher) {
        gameStateFlow.value = GameState(
            isPlaying = true,
            timeRemaining = 30.seconds,
            selectedMod = com.akinalpfdn.poprush.core.domain.model.GameMod.SPEED
        )
        every { speedModeUseCase.speedModeState } returns MutableStateFlow(SpeedModeState(isGameOver = true))

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.endGame()

        val state = gameStateFlow.value
        assertEquals(30, state.score)
    }
}
