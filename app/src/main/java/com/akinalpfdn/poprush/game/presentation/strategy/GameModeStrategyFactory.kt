package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.game.presentation.strategy.impl.ClassicModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.impl.CoopModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.impl.SpeedModeStrategy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating game mode strategy instances.
 * Uses a registry pattern to support easy addition of new game modes.
 */
@Singleton
class GameModeStrategyFactory @Inject constructor(
    val dependencies: GameModeDependencies
) {
    private val strategies = mutableMapOf<GameMod, GameModeStrategy>()

    /**
     * Get or create a strategy for the given game mode.
     * Strategies are cached for reuse.
     */
    fun getStrategy(mod: GameMod): GameModeStrategy {
        return strategies.getOrPut(mod) {
            createStrategy(mod)
        }
    }

    /**
     * Create a new strategy instance for the given game mode.
     */
    private fun createStrategy(mod: GameMod): GameModeStrategy {
        Timber.d("Creating strategy for mode: $mod")
        return when (mod) {
            GameMod.CLASSIC -> ClassicModeStrategy(dependencies)
            GameMod.SPEED -> SpeedModeStrategy(dependencies)
            // Future modes can be added here
            // GameMod.ARCADE -> ArcadeModeStrategy(dependencies)
            // GameMod.ZEN -> ZenModeStrategy(dependencies)
            // GameMod.BLITZ -> BlitzModeStrategy(dependencies)
        }
    }

    /**
     * Check if a strategy is registered for the given mode.
     */
    fun isSupported(mod: GameMod): Boolean {
        return when (mod) {
            GameMod.CLASSIC, GameMod.SPEED -> true
            // Add future modes here
        }
    }

    /**
     * Get all available game modes.
     */
    fun getAvailableModes(): List<GameMod> {
        return GameMod.entries
    }

    /**
     * Clear cached strategies (useful for testing or memory management).
     */
    fun clearCache() {
        strategies.forEach { (_, strategy) -> strategy.cleanup() }
        strategies.clear()
    }
}
