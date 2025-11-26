package com.akinalpfdn.poprush.core.domain.model

import com.akinalpfdn.poprush.core.domain.model.MusicTrack

/**
 * Represents the current state of the audio system.
 */
data class AudioState(
    val isInitialized: Boolean = false,
    val hasAudioFocus: Boolean = false,
    val isMusicPlaying: Boolean = false,
    val areSoundsPlaying: Boolean = false,
    val masterVolume: Float = 1.0f,
    val currentMusicTrack: MusicTrack? = null,
    val isAudioSupported: Boolean = true
)

/**
 * Information about the audio system capabilities.
 */
data class AudioSystemInfo(
    val hasAudioOutput: Boolean,
    val hasSpeakers: Boolean,
    val hasHeadphones: Boolean,
    val supportedSampleRates: List<Int>,
    val maxConcurrentSounds: Int,
    val audioLatencyMs: Int
)