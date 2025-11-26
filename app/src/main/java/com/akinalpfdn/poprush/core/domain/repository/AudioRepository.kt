package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.domain.model.MusicTrack
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.model.AudioState
import com.akinalpfdn.poprush.core.domain.model.AudioSystemInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing audio playback in the game.
 * Handles sound effects, background music, and audio focus management.
 */
interface AudioRepository {

    // Sound Effects Management
    /**
     * Plays a sound effect.
     *
     * @param soundType The type of sound to play
     * @param volume Optional volume override (0.0f to 1.0f)
     */
    suspend fun playSound(soundType: SoundType, volume: Float? = null)

    /**
     * Plays a sound effect with haptic feedback if enabled.
     *
     * @param soundType The type of sound to play
     * @param enableHaptic Whether to trigger haptic feedback
     */
    suspend fun playSoundWithHaptic(soundType: SoundType, enableHaptic: Boolean = true)

    /**
     * Stops all currently playing sound effects.
     */
    suspend fun stopAllSounds()

    /**
     * Gets a stream indicating if any sound effects are currently playing.
     *
     * @return Flow that emits true when sounds are playing
     */
    fun getSoundsPlayingFlow(): Flow<Boolean>

    // Background Music Management
    /**
     * Plays background music.
     *
     * @param musicTrack The music track to play
     * @param loop Whether to loop the music continuously
     */
    suspend fun playMusic(musicTrack: MusicTrack, loop: Boolean = true)

    /**
     * Stops the currently playing background music.
     */
    suspend fun stopMusic()

    /**
     * Pauses the currently playing background music.
     */
    suspend fun pauseMusic()

    /**
     * Resumes the paused background music.
     */
    suspend fun resumeMusic()

    /**
     * Gets a stream indicating if music is currently playing.
     *
     * @return Flow that emits true when music is playing
     */
    fun getMusicPlayingFlow(): Flow<Boolean>

    /**
     * Gets a stream of the currently playing music track.
     *
     * @return Flow that emits the current MusicTrack or null
     */
    fun getCurrentMusicTrackFlow(): Flow<MusicTrack?>

    // Volume Control
    /**
     * Sets the master volume for all audio.
     *
     * @param volume Volume level between 0.0f and 1.0f
     */
    suspend fun setMasterVolume(volume: Float)

    /**
     * Gets the current master volume.
     *
     * @return Current volume level between 0.0f and 1.0f
     */
    suspend fun getMasterVolume(): Float

    /**
     * Gets a stream of master volume changes.
     *
     * @return Flow that emits volume updates
     */
    fun getMasterVolumeFlow(): Flow<Float>

    // Audio Focus Management
    /**
     * Requests audio focus for the game.
     *
     * @return True if audio focus was granted, false otherwise
     */
    suspend fun requestAudioFocus(): Boolean

    /**
     * Releases audio focus.
     */
    suspend fun releaseAudioFocus()

    /**
     * Gets a stream indicating if the app has audio focus.
     *
     * @return Flow that emits true when app has audio focus
     */
    fun getAudioFocusFlow(): Flow<Boolean>

    // Audio State Management
    /**
     * Initializes the audio system.
     * Should be called when the app starts.
     */
    suspend fun initialize()

    /**
     * Releases all audio resources.
     * Should be called when the app is destroyed.
     */
    suspend fun release()

    /**
     * Gets the current audio system state.
     *
     * @return AudioState representing the current system status
     */
    suspend fun getAudioState(): AudioState

    /**
     * Gets a stream of audio system state changes.
     *
     * @return Flow that emits AudioState updates
     */
    fun getAudioStateFlow(): Flow<AudioState>

    // Utility Methods
    /**
     * Preloads commonly used sound effects for better performance.
     */
    suspend fun preloadSounds()

    /**
     * Checks if the device has audio capabilities.
     *
     * @return True if device supports audio playback
     */
    suspend fun isAudioSupported(): Boolean

    /**
     * Gets information about the audio system.
     *
     * @return AudioSystemInfo with system capabilities
     */
    suspend fun getAudioSystemInfo(): AudioSystemInfo
}