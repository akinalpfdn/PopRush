package com.akinalpfdn.poprush.audio.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.akinalpfdn.poprush.core.domain.model.MusicTrack
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.model.AudioState
import com.akinalpfdn.poprush.core.domain.model.AudioSystemInfo
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AudioRepository using SoundPool for sound effects.
 * Provides basic audio functionality for the game.
 */
@Singleton
class AudioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : AudioRepository {

    private var soundPool: SoundPool? = null
    private var audioManager: AudioManager? = null

    // Audio state
    private val _audioState = MutableStateFlow(
        AudioState(
            isInitialized = false,
            hasAudioFocus = false,
            isMusicPlaying = false,
            areSoundsPlaying = false,
            masterVolume = 1.0f,
            currentMusicTrack = null,
            isAudioSupported = true
        )
    )
    private val _soundsPlaying = MutableStateFlow(false)
    private val _musicPlaying = MutableStateFlow(false)
    private val _currentMusicTrack = MutableStateFlow<MusicTrack?>(null)
    private val _audioFocus = MutableStateFlow(false)
    private val _masterVolume = MutableStateFlow(1.0f)

    // Sound IDs mapping (would load actual sound files in production)
    private val soundIds = mutableMapOf<SoundType, Int>()

    override suspend fun playSound(soundType: SoundType, volume: Float?) {
        try {
            if (!_audioState.value.isAudioSupported) return

            val soundEnabled = settingsRepository.isSoundEnabled()
            if (!soundEnabled) return

            val soundVolume = volume ?: settingsRepository.getSoundVolume()
            val finalVolume = (soundVolume * _masterVolume.value).coerceIn(0f, 1f)

            soundPool?.play(
                soundIds[soundType] ?: 0,
                finalVolume,
                finalVolume,
                0,
                0,
                1.0f
            )

            _soundsPlaying.value = true

        } catch (e: Exception) {
            Timber.e(e, "Error playing sound: $soundType")
        }
    }

    override suspend fun playSoundWithHaptic(soundType: SoundType, enableHaptic: Boolean) {
        playSound(soundType)
        if (enableHaptic) {
            // Trigger haptic feedback if enabled in settings
            val hapticEnabled = settingsRepository.isHapticFeedbackEnabled()
            if (hapticEnabled) {
                // In a full implementation, would trigger device vibration
                Timber.d("Haptic feedback triggered for sound: $soundType")
            }
        }
    }

    override suspend fun stopAllSounds() {
        try {
            soundPool?.stop(0) // Stop all sounds
            _soundsPlaying.value = false
        } catch (e: Exception) {
            Timber.e(e, "Error stopping all sounds")
        }
    }

    override fun getSoundsPlayingFlow(): Flow<Boolean> = _soundsPlaying.asStateFlow()

    // Music functionality (simplified implementation)
    override suspend fun playMusic(musicTrack: MusicTrack, loop: Boolean) {
        try {
            if (!_audioState.value.isAudioSupported) return

            val musicEnabled = settingsRepository.isMusicEnabled()
            if (!musicEnabled) return

            _musicPlaying.value = true
            _currentMusicTrack.value = musicTrack

            Timber.d("Playing music track: $musicTrack (loop: $loop)")

        } catch (e: Exception) {
            Timber.e(e, "Error playing music: $musicTrack")
        }
    }

    override suspend fun stopMusic() {
        try {
            _musicPlaying.value = false
            _currentMusicTrack.value = null
            Timber.d("Music stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping music")
        }
    }

    override suspend fun pauseMusic() {
        try {
            _musicPlaying.value = false
            Timber.d("Music paused")
        } catch (e: Exception) {
            Timber.e(e, "Error pausing music")
        }
    }

    override suspend fun resumeMusic() {
        try {
            val musicEnabled = settingsRepository.isMusicEnabled()
            if (musicEnabled && _currentMusicTrack.value != null) {
                _musicPlaying.value = true
                Timber.d("Music resumed")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error resuming music")
        }
    }

    override fun getMusicPlayingFlow(): Flow<Boolean> = _musicPlaying.asStateFlow()

    override fun getCurrentMusicTrackFlow(): Flow<MusicTrack?> = _currentMusicTrack.asStateFlow()

    // Volume control
    override suspend fun setMasterVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _masterVolume.value = clampedVolume
        _audioState.value = _audioState.value.copy(masterVolume = clampedVolume)
    }

    override suspend fun getMasterVolume(): Float = _masterVolume.value

    override fun getMasterVolumeFlow(): Flow<Float> = _masterVolume.asStateFlow()

    // Audio focus management
    override suspend fun requestAudioFocus(): Boolean {
        return try {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Simplified audio focus request
            val result = audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } catch (e: Exception) {
            Timber.e(e, "Error requesting audio focus")
            false
        }
    }

    override suspend fun releaseAudioFocus() {
        try {
            audioManager?.abandonAudioFocus(null)
            _audioFocus.value = false
            Timber.d("Audio focus released")
        } catch (e: Exception) {
            Timber.e(e, "Error releasing audio focus")
        }
    }

    override fun getAudioFocusFlow(): Flow<Boolean> = _audioFocus.asStateFlow()

    // Audio state management
    override suspend fun initialize() {
        try {
            // Check if device supports audio
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val hasAudioOutput = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.isNotEmpty() == true

            if (!hasAudioOutput) {
                _audioState.value = _audioState.value.copy(
                    isInitialized = true,
                    isAudioSupported = false
                )
                return
            }

            // Initialize SoundPool
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
            }

            // Load sound effects (would load actual sound files in production)
            loadSounds()

            _audioState.value = _audioState.value.copy(
                isInitialized = true,
                isAudioSupported = true
            )

            // Request audio focus
            requestAudioFocus()

            Timber.d("AudioRepository initialized successfully")

        } catch (e: Exception) {
            Timber.e(e, "Error initializing AudioRepository")
            _audioState.value = _audioState.value.copy(
                isInitialized = true,
                isAudioSupported = false
            )
        }
    }

    override suspend fun release() {
        try {
            stopAllSounds()
            stopMusic()
            releaseAudioFocus()

            soundPool?.release()
            soundPool = null

            _audioState.value = _audioState.value.copy(isInitialized = false)

            Timber.d("AudioRepository released")

        } catch (e: Exception) {
            Timber.e(e, "Error releasing AudioRepository")
        }
    }

    override suspend fun getAudioState(): AudioState = _audioState.value

    override fun getAudioStateFlow(): Flow<AudioState> = _audioState.asStateFlow()

    override suspend fun preloadSounds() {
        loadSounds()
    }

    override suspend fun isAudioSupported(): Boolean = _audioState.value.isAudioSupported

    override suspend fun getAudioSystemInfo(): AudioSystemInfo {
        return try {
            val hasAudioOutput = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.isNotEmpty() == true
            val sampleRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                audioManager?.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 44100
            } else 44100

            AudioSystemInfo(
                hasAudioOutput = hasAudioOutput,
                hasSpeakers = true, // Simplified
                hasHeadphones = false, // Would check actual device state
                supportedSampleRates = listOf(sampleRate),
                maxConcurrentSounds = 10,
                audioLatencyMs = 100 // Estimated
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting audio system info")
            AudioSystemInfo(false, false, false, emptyList(), 0, 0)
        }
    }

    // Private helper methods
    private suspend fun loadSounds() {
        try {
            // In a full implementation, this would load actual sound files
            // For now, we'll create placeholder IDs

            // Note: These are placeholder IDs. In production, you would:
            // 1. Place sound files in res/raw/
            // 2. Load them with soundPool.load(context, R.raw.sound_name, 1)
            // 3. Store the returned sound IDs

            soundIds[SoundType.BUBBLE_PRESS] = 1
            soundIds[SoundType.LEVEL_COMPLETE] = 2
            soundIds[SoundType.GAME_OVER] = 3
            soundIds[SoundType.HIGH_SCORE] = 4
            soundIds[SoundType.BUTTON_PRESS] = 5
            soundIds[SoundType.SETTINGS_OPEN] = 6
            soundIds[SoundType.SETTINGS_CLOSE] = 7
            soundIds[SoundType.COUNTDOWN_CRITICAL] = 8

            Timber.d("Sound effects loaded (placeholder implementation)")

        } catch (e: Exception) {
            Timber.e(e, "Error loading sound effects")
        }
    }

    private fun pauseAudio() {
        runBlocking { pauseMusic() }
    }

    private fun resumeAudio() {
        runBlocking { resumeMusic() }
    }
}