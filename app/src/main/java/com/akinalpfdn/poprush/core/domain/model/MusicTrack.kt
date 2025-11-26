package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents a music track that can be played in the game.
 */
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String? = null,
    val duration: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    val resourcePath: String? = null,
    val isLooping: Boolean = true
)