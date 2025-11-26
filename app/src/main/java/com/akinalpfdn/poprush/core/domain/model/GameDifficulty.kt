package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents different game difficulty levels for future extensibility.
 */
enum class GameDifficulty(
    val displayName: String,
    val minActiveBubbles: Int,
    val maxActiveBubbles: Int,
    val timeMultiplier: Float = 1.0f
) {
    /** Easy difficulty with slower pace and more forgiving mechanics */
    EASY("Easy", 3, 5, 1.2f),

    /** Normal difficulty with balanced gameplay */
    NORMAL("Normal", 4, 8, 1.0f),

    /** Hard difficulty with faster pace and stricter mechanics */
    HARD("Hard", 6, 9, 0.8f),

    /** Expert difficulty for skilled players */
    EXPERT("Expert", 8, 12, 0.6f)
}