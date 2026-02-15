package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.game.presentation.strategy.impl.ClassicModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.impl.SpeedModeStrategy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating game mode strategy instances using a registry pattern.
 * New modes can be added by registering a creator in the registry map.
 */
@Singleton
class GameModeStrategyFactory @Inject constructor(
    val dependencies: GameModeDependencies
) {
    private val strategies = mutableMapOf<GameMod, GameModeStrategy>()

    private val registry: Map<GameMod, () -> GameModeStrategy> = mapOf(
        GameMod.CLASSIC to { ClassicModeStrategy(dependencies) },
        GameMod.SPEED to { SpeedModeStrategy(dependencies) }
    )

    /**
     * Get or create a strategy for the given game mode.
     * Strategies are cached to preserve state across game sessions.
     */
    fun getStrategy(mod: GameMod): GameModeStrategy {
        return strategies.getOrPut(mod) {
            Timber.d("Creating NEW strategy for mode: $mod")
            registry[mod]?.invoke()
                ?: throw IllegalArgumentException("No strategy registered for mode: $mod")
        }
    }

    fun isSupported(mod: GameMod): Boolean = registry.containsKey(mod)

    fun getAvailableModes(): List<GameMod> = registry.keys.toList()
}
