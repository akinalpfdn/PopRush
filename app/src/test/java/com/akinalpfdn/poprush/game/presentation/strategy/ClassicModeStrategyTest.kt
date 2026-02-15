package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.game.domain.usecase.BubblePressResult
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerState
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import com.akinalpfdn.poprush.game.presentation.CoopHandler
import com.akinalpfdn.poprush.game.presentation.strategy.impl.ClassicModeStrategy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ClassicModeStrategyTest {

    private lateinit var strategy: ClassicModeStrategy
    private lateinit var gameStateFlow: MutableStateFlow<GameState>
    private lateinit var dependencies: GameModeDependencies

    private val gameRepository: GameRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val audioRepository: AudioRepository = mockk(relaxed = true)
    private val initializeGameUseCase: InitializeGameUseCase = mockk()
    private val generateLevelUseCase: GenerateLevelUseCase = mockk()
    private val handleBubblePressUseCase: HandleBubblePressUseCase = mockk()
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
                isPressed = false
            )
        }

    private fun createActiveBubbles(count: Int = 10, activeIds: List<Int> = listOf(0, 1, 2)): List<Bubble> =
        createTestBubbles(count).map { bubble ->
            bubble.copy(isActive = bubble.id in activeIds)
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

        strategy = ClassicModeStrategy(dependencies)
        gameStateFlow = MutableStateFlow(GameState())

        // Default mock behaviors
        every { timerUseCase.timeRemaining } returns MutableStateFlow(30.seconds)
        every { timerUseCase.timerState } returns MutableStateFlow(TimerState.STOPPED)
        every { settingsRepository.getGameDifficultyFlow() } returns flowOf(GameDifficulty.NORMAL)
        every { settingsRepository.getHapticFeedbackFlow() } returns flowOf(true)
    }

    @Test
    fun `strategy has correct mode id and name`() {
        assertEquals("classic", strategy.modeId)
        assertEquals("Classic Mode", strategy.modeName)
    }

    @Test
    fun `config returns classic configuration`() {
        val config = strategy.getConfig()
        assertEquals("Classic", config.displayName)
        assertTrue(config.hasTimer)
        assertTrue(config.hasLevels)
        assertTrue(config.supportsDurationSelection)
        assertFalse(config.supportsCoop)
    }

    @Test
    fun `startGame initializes game state correctly`() = runTest(testDispatcher) {
        val testBubbles = createTestBubbles()
        val activeBubbles = createActiveBubbles()

        coEvery { initializeGameUseCase.execute() } returns testBubbles
        coEvery { generateLevelUseCase.execute(any(), any(), any()) } returns activeBubbles

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.startGame()

        val state = gameStateFlow.value
        assertTrue(state.isPlaying)
        assertFalse(state.isGameOver)
        assertFalse(state.isPaused)
        assertEquals(0, state.score)
        assertEquals(1, state.currentLevel)
        assertEquals(activeBubbles, state.bubbles)
    }

    @Test
    fun `startGame starts timer with selected duration`() = runTest(testDispatcher) {
        val testBubbles = createTestBubbles()
        gameStateFlow.value = GameState(selectedDuration = 45.seconds)

        coEvery { initializeGameUseCase.execute() } returns testBubbles
        coEvery { generateLevelUseCase.execute(any(), any(), any()) } returns testBubbles

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.startGame()

        coVerify { timerUseCase.startTimer(45.seconds) }
    }

    @Test
    fun `handleBubblePress updates state on valid press`() = runTest(testDispatcher) {
        val bubbles = createActiveBubbles(activeIds = listOf(0, 1, 2))
        val updatedBubbles = bubbles.map {
            if (it.id == 0) it.copy(isPressed = true) else it
        }

        gameStateFlow.value = GameState(bubbles = bubbles, isPlaying = true)

        coEvery { handleBubblePressUseCase.execute(any(), eq(0)) } returns BubblePressResult(
            success = true,
            updatedBubbles = updatedBubbles,
            isLevelComplete = false,
            pressedBubbleId = 0,
            wasValidPress = true,
            scoreIncrement = 1
        )

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.handleBubblePress(0)

        assertEquals(updatedBubbles, gameStateFlow.value.bubbles)
    }

    @Test
    fun `handleBubblePress does not update state on invalid press`() = runTest(testDispatcher) {
        val bubbles = createActiveBubbles()
        gameStateFlow.value = GameState(bubbles = bubbles, isPlaying = true)

        coEvery { handleBubblePressUseCase.execute(any(), eq(5)) } returns BubblePressResult(
            success = false,
            updatedBubbles = bubbles,
            isLevelComplete = false,
            pressedBubbleId = 5,
            wasValidPress = false,
            scoreIncrement = 0
        )

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.handleBubblePress(5)

        // State should remain unchanged since press was invalid
        assertEquals(bubbles, gameStateFlow.value.bubbles)
    }

    @Test
    fun `level complete increments score and generates new level`() = runTest(testDispatcher) {
        val bubbles = createActiveBubbles(activeIds = listOf(0))
        val pressedBubbles = bubbles.map {
            if (it.id == 0) it.copy(isPressed = true) else it
        }
        val newLevelBubbles = createActiveBubbles(activeIds = listOf(3, 4, 5))

        gameStateFlow.value = GameState(bubbles = bubbles, isPlaying = true, score = 5, currentLevel = 3)

        coEvery { handleBubblePressUseCase.execute(any(), eq(0)) } returns BubblePressResult(
            success = true,
            updatedBubbles = pressedBubbles,
            isLevelComplete = true,
            pressedBubbleId = 0,
            wasValidPress = true,
            scoreIncrement = 2
        )
        coEvery { generateLevelUseCase.execute(any(), any(), any()) } returns newLevelBubbles

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.handleBubblePress(0)

        // Score should increment by 1 (strategy adds 1 on level complete)
        assertEquals(6, gameStateFlow.value.score)
    }

    @Test
    fun `pauseGame delegates to timer`() = runTest(testDispatcher) {
        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.pauseGame()

        coVerify { timerUseCase.pauseTimer() }
    }

    @Test
    fun `resumeGame delegates to timer`() = runTest(testDispatcher) {
        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.resumeGame()

        coVerify { timerUseCase.resumeTimer() }
    }

    @Test
    fun `endGame stops timer and saves result`() = runTest(testDispatcher) {
        gameStateFlow.value = GameState(
            isPlaying = true,
            score = 10,
            currentLevel = 5,
            highScores = mapOf("classic" to 8)
        )

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.endGame()

        coVerify { timerUseCase.stopTimer() }
        coVerify { gameRepository.saveGameResult(any()) }

        val state = gameStateFlow.value
        assertTrue(state.isGameOver)
        assertFalse(state.isPlaying)
    }

    @Test
    fun `endGame announces high score when score exceeds previous`() = runTest(testDispatcher) {
        gameStateFlow.value = GameState(
            isPlaying = true,
            score = 15,
            highScores = mapOf("classic" to 10)
        )

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.endGame()

        coVerify { gameRepository.updateHighScore("classic", 15) }
    }

    @Test
    fun `resetGame returns state to defaults`() = runTest(testDispatcher) {
        gameStateFlow.value = GameState(
            isPlaying = true,
            score = 10,
            currentLevel = 5,
            bubbles = createTestBubbles()
        )

        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.resetGame()

        coVerify { timerUseCase.stopTimer() }
        val state = gameStateFlow.value
        assertFalse(state.isPlaying)
        assertFalse(state.isGameOver)
        assertEquals(0, state.score)
        assertEquals(1, state.currentLevel)
        assertTrue(state.bubbles.isEmpty())
    }

    @Test
    fun `cleanup stops timer`() = runTest(testDispatcher) {
        strategy.initialize(TestScope(testDispatcher), gameStateFlow)
        strategy.cleanup()

        coVerify { timerUseCase.stopTimer() }
    }
}
