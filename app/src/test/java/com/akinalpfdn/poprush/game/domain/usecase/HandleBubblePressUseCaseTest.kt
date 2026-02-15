package com.akinalpfdn.poprush.game.domain.usecase

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HandleBubblePressUseCaseTest {

    private lateinit var useCase: HandleBubblePressUseCase

    private fun createBubble(
        id: Int,
        isActive: Boolean = false,
        isPressed: Boolean = false,
        row: Int = 0,
        col: Int = 0
    ) = Bubble(
        id = id,
        position = id,
        row = row,
        col = col,
        color = BubbleColor.ROSE,
        isActive = isActive,
        isPressed = isPressed
    )

    @Before
    fun setup() {
        useCase = HandleBubblePressUseCase()
    }

    @Test
    fun `pressing active bubble returns success`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true),
            createBubble(1, isActive = false),
            createBubble(2, isActive = true)
        )

        val result = useCase.execute(bubbles, 0)

        assertTrue(result.success)
        assertTrue(result.wasValidPress)
        assertTrue(result.updatedBubbles[0].isPressed)
    }

    @Test
    fun `pressing inactive bubble returns failure`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = false),
            createBubble(1, isActive = true)
        )

        val result = useCase.execute(bubbles, 0)

        assertFalse(result.success)
        assertFalse(result.wasValidPress)
    }

    @Test
    fun `pressing already pressed bubble returns failure`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true, isPressed = true),
            createBubble(1, isActive = true)
        )

        val result = useCase.execute(bubbles, 0)

        assertFalse(result.success)
    }

    @Test
    fun `pressing nonexistent bubble returns failure`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true)
        )

        val result = useCase.execute(bubbles, 99)

        assertFalse(result.success)
    }

    @Test
    fun `level complete when all active bubbles pressed`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true, isPressed = true),
            createBubble(1, isActive = true),
            createBubble(2, isActive = false)
        )

        val result = useCase.execute(bubbles, 1)

        assertTrue(result.success)
        assertTrue(result.isLevelComplete)
    }

    @Test
    fun `level not complete when active bubbles remain`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true),
            createBubble(1, isActive = true),
            createBubble(2, isActive = true)
        )

        val result = useCase.execute(bubbles, 0)

        assertTrue(result.success)
        assertFalse(result.isLevelComplete)
    }

    @Test
    fun `multi press with valid bubbles succeeds`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true),
            createBubble(1, isActive = true),
            createBubble(2, isActive = false)
        )

        val result = useCase.executeMultiPress(bubbles, listOf(0, 1))

        assertTrue(result.success)
        assertTrue(result.updatedBubbles[0].isPressed)
        assertTrue(result.updatedBubbles[1].isPressed)
        assertTrue(result.isLevelComplete)
    }

    @Test
    fun `multi press with empty list returns failure`() = runTest {
        val bubbles = listOf(createBubble(0, isActive = true))

        val result = useCase.executeMultiPress(bubbles, emptyList())

        assertFalse(result.success)
    }

    @Test
    fun `score increment includes level complete bonus`() = runTest {
        val bubbles = listOf(
            createBubble(0, isActive = true)
        )

        val result = useCase.execute(bubbles, 0)

        assertTrue(result.isLevelComplete)
        assertEquals(2, result.scoreIncrement) // 1 base + 1 level complete
    }

    @Test
    fun `statistics tracks progress correctly`() {
        val bubbles = listOf(
            createBubble(0, isActive = true, isPressed = true),
            createBubble(1, isActive = true, isPressed = false),
            createBubble(2, isActive = false)
        )

        val stats = useCase.getBubblePressStatistics(bubbles)

        assertEquals(3, stats.totalBubbles)
        assertEquals(2, stats.activeBubbles)
        assertEquals(1, stats.pressedBubbles)
        assertEquals(1, stats.remainingActiveBubbles)
        assertEquals(0.5f, stats.levelProgress, 0.01f)
        assertFalse(stats.isLevelComplete)
    }
}
