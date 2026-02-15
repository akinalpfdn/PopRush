package com.akinalpfdn.poprush.core.domain.model

/**
 * Intents related to audio playback.
 */
sealed interface AudioIntent : GameIntent {
    data class PlaySound(val soundType: SoundType) : AudioIntent
    data class PlayMusic(val musicTrack: MusicTrack) : AudioIntent
    data object StopAllAudio : AudioIntent
    data object PauseAudio : AudioIntent
    data object ResumeAudio : AudioIntent
}
