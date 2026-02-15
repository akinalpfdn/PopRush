package com.akinalpfdn.poprush.core.domain.model

/**
 * Sealed interface representing all possible user actions and system events in the PopRush game.
 *
 * Organized into sub-interfaces by domain:
 * - [GameplayIntent] — core gameplay actions (start, pause, bubble press, etc.)
 * - [SettingsIntent] — audio/visual/game configuration changes
 * - [NavigationIntent] — screen navigation and mode selection
 * - [CoopIntent] — cooperative multiplayer actions
 * - [AudioIntent] — audio playback control
 */
sealed interface GameIntent
