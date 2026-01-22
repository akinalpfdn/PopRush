package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.GameMod

/**
 * Registry for all available game modes.
 * Provides metadata and discovery for game modes without needing to instantiate strategies.
 *
 * To add a new game mode:
 * 1. Add the enum value to GameMod
 * 2. Create a strategy implementing GameModeStrategy
 * 3. Add the mode configuration to this registry
 * 4. Update GameModeStrategyFactory.createStrategy()
 */
object GameModeRegistry {

    /**
     * Get configuration for a game mode.
     */
    fun getModeConfig(mod: GameMod): GameModeConfig {
        return when (mod) {
            GameMod.CLASSIC -> GameModeConfig.classic()
            GameMod.SPEED -> GameModeConfig.speed()
        }
    }

    /**
     * Get all available game mode configurations.
     */
    fun getAllModes(): List<GameModeConfig> {
        return GameMod.entries.map { getModeConfig(it) }
    }

    /**
     * Get all single-player game modes.
     */
    fun getSinglePlayerModes(): List<GameModeConfig> {
        return getAllModes().filter { !it.supportsCoop }
    }

    /**
     * Check if a mode supports co-op multiplayer.
     */
    fun supportsCoop(mod: GameMod): Boolean {
        return getModeConfig(mod).supportsCoop
    }

    /**
     * Check if a mode has a timer.
     */
    fun hasTimer(mod: GameMod): Boolean {
        return getModeConfig(mod).hasTimer
    }

    /**
     * Check if a mode supports duration selection.
     */
    fun supportsDurationSelection(mod: GameMod): Boolean {
        return getModeConfig(mod).supportsDurationSelection
    }

    /**
     * Get the display name for a mode.
     */
    fun getDisplayName(mod: GameMod): String {
        return getModeConfig(mod).displayName
    }

    /**
     * Get the description for a mode.
     */
    fun getDescription(mod: GameMod): String {
        return getModeConfig(mod).description
    }
}
