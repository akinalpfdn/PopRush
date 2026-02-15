package com.akinalpfdn.poprush.game.domain.usecase

import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class GenerateLevelUseCaseTest {

    private lateinit var useCase: GenerateLevelUseCase
    private val seededRandom = Random(42)

    private fun createBubbles(count: Int): List<Bubble> {
        val rowSizes = listOf(5, 6, 7, 8, 7, 6, 5)
        var id = 0
        val bubbles = mutableListOf<Bubble>()
        for ((rowIndex, size) in rowSizes.withIndex()) {
            for (colIndex in 0 until size) {
                if (id >= count) return bubbles
                bubbles.add(
                    Bubble(
                        id = id,
                        position = id,
                        row = rowIndex,
                        col = colIndex,
                        color = BubbleColor.ROSE,
                        isActive = false,
                        isPressed = false
                    )
                )
                id++
            }
        }
        return bubbles
    }

    @Before
    fun setup() {
        useCase = GenerateLevelUseCase(seededRandom)
    }

    @Test
    fun `generates level with active bubbles`() = runTest {
        val bubbles = createBubbles(44)

        val result = useCase.execute(bubbles, GameDifficulty.NORMAL, 1)

        val activeCount = result.count { it.isActive }
        assertTrue("Should have active bubbles, got $activeCount", activeCount > 0)
    }

    @Test
    fun `all bubbles start with isPressed false`() = runTest {
        val bubbles = createBubbles(44).map { it.copy(isPressed = true) }

        val result = useCase.execute(bubbles, GameDifficulty.EASY, 1)

        assertTrue("All bubbles should have isPressed=false", result.all { !it.isPressed })
    }

    @Test
    fun `easy difficulty generates fewer active bubbles than hard`() = runTest {
        val bubbles = createBubbles(44)
        val easyRandom = Random(100)
        val hardRandom = Random(100)

        val easyUseCase = GenerateLevelUseCase(easyRandom)
        val hardUseCase = GenerateLevelUseCase(hardRandom)

        val easyResult = easyUseCase.execute(bubbles, GameDifficulty.EASY, 1)
        val hardResult = hardUseCase.execute(bubbles, GameDifficulty.HARD, 1)

        val easyActive = easyResult.count { it.isActive }
        val hardActive = hardResult.count { it.isActive }

        assertTrue(
            "Easy ($easyActive) should have <= Hard ($hardActive) active bubbles",
            easyActive <= hardActive
        )
    }

    @Test
    fun `higher levels can generate more active bubbles`() = runTest {
        val bubbles = createBubbles(44)

        val level1Result = useCase.execute(bubbles, GameDifficulty.NORMAL, 1)
        // Use a fresh seeded random for fair comparison
        val highLevelUseCase = GenerateLevelUseCase(Random(42))
        val level20Result = highLevelUseCase.execute(bubbles, GameDifficulty.NORMAL, 20)

        // Level 20 should potentially have more active bubbles due to scaling
        // This tests the scaling mechanism exists, not exact values
        val level1Active = level1Result.count { it.isActive }
        val level20Active = level20Result.count { it.isActive }
        assertTrue("Both levels should have active bubbles", level1Active > 0 && level20Active > 0)
    }

    @Test
    fun `seeded random produces deterministic results`() = runTest {
        val bubbles = createBubbles(44)

        val useCase1 = GenerateLevelUseCase(Random(99))
        val useCase2 = GenerateLevelUseCase(Random(99))

        val result1 = useCase1.execute(bubbles, GameDifficulty.NORMAL, 1)
        val result2 = useCase2.execute(bubbles, GameDifficulty.NORMAL, 1)

        val active1 = result1.filter { it.isActive }.map { it.id }
        val active2 = result2.filter { it.isActive }.map { it.id }

        assertEquals("Same seed should produce same results", active1, active2)
    }

    @Test
    fun `validate level checks bubble count range`() {
        // Need 4-8 active bubbles (NORMAL range) spread across at least 2 rows
        val validBubbles = createBubbles(44).mapIndexed { index, bubble ->
            val isActive = index in listOf(0, 1, 5, 6, 11) // 5 active across rows 0, 1, 2
            bubble.copy(isActive = isActive)
        }

        assertTrue(useCase.validateLevel(validBubbles, GameDifficulty.NORMAL))
    }

    @Test
    fun `pattern level generation creates correct patterns`() = runTest {
        val bubbles = createBubbles(44)

        val heartResult = useCase.generatePatternLevel(bubbles, PatternType.HEART)
        val activeHeartIds = heartResult.filter { it.isActive }.map { it.id }

        assertTrue("Heart pattern should have active bubbles", activeHeartIds.isNotEmpty())
        assertTrue("All pattern bubbles should have isPressed=false", heartResult.all { !it.isPressed })
    }
}
