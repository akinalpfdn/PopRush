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

    // Game Mode Selection Intents

    /**
     * Selects the game mode (single or co-op).
     * @param mode The game mode to select
     */
    data class SelectGameMode(val mode: GameMode) : GameIntent

    /**
     * Selects the game mod (classic or speed).
     * @param mod The game mod to select
     */
    data class SelectGameMod(val mod: GameMod) : GameIntent

    // Speed Mode Specific Intents

    /**
     * Activates a random bubble in speed mode.
     * @param bubbleId The ID of the bubble to activate
     */
    data class ActivateRandomBubble(val bubbleId: Int) : GameIntent

    /**
     * Updates the speed mode interval based on elapsed time.
     */
    data object UpdateSpeedModeInterval : GameIntent

    /**
     * Starts the speed mode timer system.
     */
    data object StartSpeedModeTimer : GameIntent

    /**
     * Resets the speed mode state to initial values.
     */
    data object ResetSpeedModeState : GameIntent

    // UI Navigation Intents

    /**
     * Navigates to the mod picker screen.
     */
    data object NavigateToModPicker : GameIntent

    /**
     * Navigates to the game setup screen.
     */
    data object NavigateToGameSetup : GameIntent

    /**
     * Navigates back to the previous screen in the start screen flow.
     */
    data object NavigateBack : GameIntent

      // Coop Mode Intents

    /**
     * Starts advertising for a coop game (hosting).
     * @param playerName The name of the local player
     * @param selectedColor The color selected by the local player
     */
    data class StartCoopAdvertising(val playerName: String, val selectedColor: String) : GameIntent

    /**
     * Starts discovery for coop games (joining).
     * @param playerName The name of the local player
     * @param selectedColor The color selected by the local player
     */
    data class StartCoopDiscovery(val playerName: String, val selectedColor: String) : GameIntent

    /**
     * Stops the current coop connection.
     */
    data object StopCoopConnection : GameIntent

    /**
     * Claims a bubble for the local player in coop mode.
     * @param bubbleId The ID of the bubble to claim
     */
    data class CoopClaimBubble(val bubbleId: Int) : GameIntent

    /**
     * Synchronizes bubble states with opponent.
     * @param bubbles Current list of all bubbles with their owners
     */
    data class CoopSyncBubbles(val bubbles: List<com.akinalpfdn.poprush.coop.domain.model.CoopBubble>) : GameIntent

    /**
     * Synchronizes scores with opponent.
     * @param localScore Current local player score
     * @param opponentScore Current opponent score
     */
    data class CoopSyncScores(val localScore: Int, val opponentScore: Int) : GameIntent

    /**
     * Handles coop game over.
     * @param winnerId The ID of the winning player (null for tie)
     */
    data class CoopGameFinished(val winnerId: String? = null) : GameIntent

    /**
     * Shows the coop connection dialog.
     */
    data object ShowCoopConnectionDialog : GameIntent

    /**
     * Hides the coop connection dialog.
     */
    data object HideCoopConnectionDialog : GameIntent

    /**
     * Shows coop error message.
     * @param errorMessage The error message to display
     */
    data class ShowCoopError(val errorMessage: String) : GameIntent

    /**
     * Clears coop error message.
     */
    data object ClearCoopError : GameIntent

    /**
     * Updates the coop player name.
     * @param playerName The new player name
     */
    data class UpdateCoopPlayerName(val playerName: String) : GameIntent

    /**
     * Updates the coop player color.
     * @param playerColor The new player color
     */
    data class UpdateCoopPlayerColor(val playerColor: BubbleColor) : GameIntent

    /**
     * Starts the coop connection process.
     */
    data object StartCoopConnection : GameIntent

    /**
     * Starts hosting a coop game.
     */
    data object StartHosting : GameIntent

    /**
     * Stops hosting a coop game.
     */
    data object StopHosting : GameIntent

    /**
     * Starts the coop game after players are connected.
     */
    data object StartCoopGame : GameIntent

    /**
     * Starts discovering coop games.
     */
    data object StartDiscovery : GameIntent

    /**
     * Stops discovering coop games.
     */
    data object StopDiscovery : GameIntent

    /**
     * Connects to a specific endpoint.
     * @param endpointId The ID of the endpoint to connect to
     */
    data class ConnectToEndpoint(val endpointId: String) : GameIntent

    /**
     * Disconnects from coop game.
     */
    data object DisconnectCoop : GameIntent

    /**
     * Closes the coop connection dialog.
     */
    data object CloseCoopConnection : GameIntent

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