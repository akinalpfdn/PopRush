package com.akinalpfdn.poprush.core.ui.component

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.akinalpfdn.poprush.R
import timber.log.Timber

/**
 * Sound manager for bubble pop effects.
 * Uses SoundPool for low-latency audio playback.
 *
 * Usage:
 * 1. Place your pop.mp3 file in res/raw/pop.mp3
 * 2. Create the manager: val soundManager = rememberPopSoundManager()
 * 3. Pass to Bubble: Bubble(..., soundManager = soundManager)
 */
class PopSoundManager(context: Context) {

    private var soundPool: SoundPool? = null
    private var popSoundId: Int = 0
    private var isLoaded = false

    // Sound variations for more natural feel
    private val pitchVariations = listOf(0.9f, 0.95f, 1.0f, 1.05f, 1.1f)
    private var variationIndex = 0

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(10) // Allow multiple simultaneous pops
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                isLoaded = status == 0
                Timber.d("PopSoundManager: Sound loaded, status = $status")
            }

            // Load the pop sound - make sure you have res/raw/pop.mp3
            popSoundId = soundPool?.load(context, R.raw.pop, 1) ?: 0

        } catch (e: Exception) {
            Timber.e(e, "PopSoundManager: Failed to initialize")
        }
    }

    /**
     * Play the pop sound with slight pitch variation for natural feel
     */
    fun playPop(volume: Float = 1.0f) {
        if (!isLoaded || soundPool == null) return

        try {
            // Cycle through pitch variations
            val pitch = pitchVariations[variationIndex]
            variationIndex = (variationIndex + 1) % pitchVariations.size

            soundPool?.play(
                popSoundId,
                volume,      // left volume
                volume,      // right volume
                1,           // priority
                0,           // loop (0 = no loop)
                pitch        // playback rate
            )
        } catch (e: Exception) {
            Timber.e(e, "PopSoundManager: Failed to play sound")
        }
    }

    /**
     * Play combo pop sound (slightly louder/different)
     */
    fun playComboSound(comboCount: Int) {
        if (!isLoaded || soundPool == null) return

        try {
            // Higher pitch and volume for combos
            val pitch = 1.0f + (comboCount * 0.05f).coerceAtMost(0.5f)
            val volume = (0.8f + comboCount * 0.05f).coerceAtMost(1.0f)

            soundPool?.play(
                popSoundId,
                volume,
                volume,
                2,  // Higher priority for combo sounds
                0,
                pitch
            )
        } catch (e: Exception) {
            Timber.e(e, "PopSoundManager: Failed to play combo sound")
        }
    }

    /**
     * Release resources when done
     */
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            isLoaded = false
        } catch (e: Exception) {
            Timber.e(e, "PopSoundManager: Failed to release")
        }
    }
}

/**
 * Remember a PopSoundManager instance scoped to the composition.
 * Automatically releases resources when leaving composition.
 */
@Composable
fun rememberPopSoundManager(): PopSoundManager {
    val context = LocalContext.current

    val soundManager = remember {
        PopSoundManager(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    return soundManager
}

/**
 * Alternative: Sound manager with multiple sound variations
 * Use this if you have multiple pop sound files (pop1.mp3, pop2.mp3, etc.)
 */
class MultiPopSoundManager(context: Context) {

    private var soundPool: SoundPool? = null
    private val soundIds = mutableListOf<Int>()
    private var isLoaded = false
    private var loadedCount = 0
    private val totalSounds = 3 // Number of sound variations

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    loadedCount++
                    if (loadedCount >= totalSounds) {
                        isLoaded = true
                    }
                }
            }

            // Load multiple variations if available
            // Uncomment and add your sound files:
            // soundIds.add(soundPool?.load(context, R.raw.pop1, 1) ?: 0)
            // soundIds.add(soundPool?.load(context, R.raw.pop2, 1) ?: 0)
            // soundIds.add(soundPool?.load(context, R.raw.pop3, 1) ?: 0)

            // Fallback to single sound
            soundIds.add(soundPool?.load(context, R.raw.pop, 1) ?: 0)

        } catch (e: Exception) {
            Timber.e(e, "MultiPopSoundManager: Failed to initialize")
        }
    }

    fun playRandomPop(volume: Float = 1.0f) {
        if (!isLoaded || soundPool == null || soundIds.isEmpty()) return

        try {
            val soundId = soundIds.random()
            val pitch = 0.95f + (Math.random() * 0.1f).toFloat()

            soundPool?.play(soundId, volume, volume, 1, 0, pitch)
        } catch (e: Exception) {
            Timber.e(e, "MultiPopSoundManager: Failed to play sound")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        isLoaded = false
    }
}