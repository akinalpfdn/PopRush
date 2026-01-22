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
 * Creates fresh instances each time to avoid state pollution between mode switches.
 */
@Singleton
class GameModeStrategyFactory @Inject constructor(
    val dependencies: GameModeDependencies
) {
    /**
     * Create a new strategy instance for the given game mode.
     * Always creates a fresh instance to ensure clean state.
     */
    fun createStrategy(mod: GameMod): GameModeStrategy {
        Timber.d("Creating fresh strategy for mode: $mod")
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
}
