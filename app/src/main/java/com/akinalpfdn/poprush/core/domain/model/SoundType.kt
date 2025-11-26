package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents the different types of sound effects used in the game.
 */
enum class SoundType {
    /** Sound when a bubble is pressed */
    BUBBLE_PRESS,

    /** Sound when a level is completed */
    LEVEL_COMPLETE,

    /** Sound when the game is over */
    GAME_OVER,

    /** Sound when a new high score is achieved */
    HIGH_SCORE,

    /** Sound when a UI button is pressed */
    BUTTON_PRESS,

    /** Sound when settings menu is opened */
    SETTINGS_OPEN,

    /** Sound when settings menu is closed */
    SETTINGS_CLOSE,

    /** Critical countdown sound when time is running out */
    COUNTDOWN_CRITICAL
}