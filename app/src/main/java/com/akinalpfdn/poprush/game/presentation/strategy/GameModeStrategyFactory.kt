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
 * Strategies are cached and initialized once to match the original handler behavior.
 */
@Singleton
class GameModeStrategyFactory @Inject constructor(
    val dependencies: GameModeDependencies
) {
    private val strategies = mutableMapOf<GameMod, GameModeStrategy>()

    /**
     * Get or create a strategy for the given game mode.
     * Strategies are cached to preserve state across game sessions.
     */
    fun getStrategy(mod: GameMod): GameModeStrategy {
        return strategies.getOrPut(mod) {
            Timber.d("Creating NEW strategy for mode: $mod")
            when (mod) {
                GameMod.CLASSIC -> ClassicModeStrategy(dependencies)
                GameMod.SPEED -> SpeedModeStrategy(dependencies)
            }
        }
    }

    /**
     * Check if a strategy is registered for the given mode.
     */
    fun isSupported(mod: GameMod): Boolean {
        return when (mod) {
            GameMod.CLASSIC, GameMod.SPEED -> true
        }
    }

    /**
     * Get all available game modes.
     */
    fun getAvailableModes(): List<GameMod> {
        return GameMod.entries
    }
}
