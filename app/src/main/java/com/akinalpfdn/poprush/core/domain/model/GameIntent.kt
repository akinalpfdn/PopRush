package com.akinalpfdn.poprush.core.domain.model

/**
 * Sealed interface representing all possible user actions and system events in the PopRush game.
 * Each intent represents a specific action that can modify the game state.
 */
sealed interface GameIntent {

    /**
     * Starts a new game session.
     */
    data object StartGame : GameIntent

    /**
     * Returns to the main menu.
     */
    data object BackToMenu : GameIntent

    /**
     * Ends the current game session.
     */
    data object EndGame : GameIntent

    /**
     * Pauses or resumes the current game.
     */
    data object TogglePause : GameIntent

    /**
     * Restarts the game with the same settings.
     */
    data object RestartGame : GameIntent

    /**
     * Handles a bubble press/tap event.
     * @param bubbleId The unique identifier of the pressed bubble
     */
    data class PressBubble(val bubbleId: Int) : GameIntent

    /**
     * Changes the selected bubble shape for visual rendering.
     * @param shape The new bubble shape to use
     */
    data class SelectShape(val shape: BubbleShape) : GameIntent

    /**
     * Adjusts the zoom level of the game board.
     * @param zoomLevel The new zoom level (0.5f to 1.5f)
     */
    data class UpdateZoom(val zoomLevel: Float) : GameIntent

    /**
     * Zooms in the game board by a small increment.
     */
    data object ZoomIn : GameIntent

    /**
     * Zooms out the game board by a small increment.
     */
    data object ZoomOut : GameIntent

    /**
     * Toggles the visibility of the settings menu.
     */
    data object ToggleSettings : GameIntent

    /**
     * Shows the back confirmation dialog.
     */
    data object ShowBackConfirmation : GameIntent

    /**
     * Hides the back confirmation dialog.
     */
    data object HideBackConfirmation : GameIntent

    /**
     * Updates the high score (typically called when a new high score is achieved).
     * @param newHighScore The new high score value
     */
    data class UpdateHighScore(val newHighScore: Int) : GameIntent

    /**
     * Toggles sound effects on/off.
     */
    data object ToggleSound : GameIntent

    /**
     * Toggles background music on/off.
     */
    data object ToggleMusic : GameIntent

    /**
     * Updates the sound effects volume.
     * @param volume The new volume level (0.0f to 1.0f)
     */
    data class UpdateSoundVolume(val volume: Float) : GameIntent

    /**
     * Updates the background music volume.
     * @param volume The new volume level (0.0f to 1.0f)
     */
    data class UpdateMusicVolume(val volume: Float) : GameIntent

    /**
     * Changes the game difficulty level.
     * @param difficulty The new difficulty level
     */
    data class ChangeDifficulty(val difficulty: GameDifficulty) : GameIntent

    /**
     * Updates the selected game duration.
     * @param duration The new game duration (10-60 seconds)
     */
    data class UpdateSelectedDuration(val duration: kotlin.time.Duration) : GameIntent

    /**
     * Updates the timer (typically called by the timer system).
     * @param timeRemaining The new remaining time
     */
    data class UpdateTimer(val timeRemaining: kotlin.time.Duration) : GameIntent

    /**
     * Generates a new level with random active bubbles.
     */
    data object GenerateNewLevel : GameIntent

    /**
     * Resets the game to its initial state (same as StartGame but clears score).
     */
    data object ResetGame : GameIntent

    /**
     * Loads saved game settings and high scores.
     */
    data object LoadGameData : GameIntent

    /**
     * Saves current game data and settings.
     */
    data object SaveGameData : GameIntent

    /**
     * Handles audio-related events.
     */
    sealed interface AudioIntent : GameIntent {
        /**
         * Plays a sound effect.
         * @param soundType The type of sound to play
         */
        data class PlaySound(val soundType: SoundType) : AudioIntent

        /**
         * Plays background music.
         * @param musicTrack The music track to play
         */
        data class PlayMusic(val musicTrack: MusicTrack) : AudioIntent

        /**
         * Stops all audio playback.
         */
        data object StopAllAudio : AudioIntent

        /**
         * Pauses audio playback.
         */
        data object PauseAudio : AudioIntent

        /**
         * Resumes audio playback.
         */
        data object ResumeAudio : AudioIntent
    }
}