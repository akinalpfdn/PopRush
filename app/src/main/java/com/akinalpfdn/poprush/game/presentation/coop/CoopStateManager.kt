package com.akinalpfdn.poprush.game.presentation.coop

import com.akinalpfdn.poprush.coop.domain.model.CoopBubble
import com.akinalpfdn.poprush.coop.domain.model.CoopGameState
import com.akinalpfdn.poprush.coop.domain.model.CoopMod
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.repository.PlayerProfileRepository
import com.akinalpfdn.poprush.core.domain.util.Clock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages coop state initialization, player profile updates, and dialog/error state.
 */
class CoopStateManager(
    private val playerProfileRepository: PlayerProfileRepository,
    private val clock: Clock,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<GameState>
) {
    private val _cachedInitialCoopState = CompletableDeferred<CoopGameState>()

    fun initializeCache() {
        scope.launch {
            try {
                val state = createInitialCoopState()
                _cachedInitialCoopState.complete(state)
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize cached coop state")
                _cachedInitialCoopState.complete(
                    CoopGameState(
                        localPlayerId = "player_${clock.currentTimeMillis()}",
                        localPlayerName = "Player",
                        localPlayerColor = BubbleColor.ROSE,
                        bubbles = generateInitialCoopBubbles()
                    )
                )
            }
        }
    }

    fun handleShowCoopConnectionDialog() {
        gameStateFlow.update { it.copy(showCoopConnectionDialog = true) }
    }

    fun handleHideCoopConnectionDialog() {
        gameStateFlow.update { it.copy(showCoopConnectionDialog = false) }
    }

    fun handleShowCoopError(errorMessage: String) {
        gameStateFlow.update { it.copy(coopErrorMessage = errorMessage) }
    }

    fun handleClearCoopError() {
        gameStateFlow.update { it.copy(coopErrorMessage = null) }
    }

    fun handleUpdateCoopPlayerName(playerName: String) {
        scope.launch {
            try {
                playerProfileRepository.savePlayerName(playerName)
                val initial = getCachedInitialCoopState()
                gameStateFlow.update { currentState ->
                    val updatedCoopState = currentState.coopState?.copy(
                        localPlayerName = playerName
                    ) ?: initial.copy(localPlayerName = playerName)
                    currentState.copy(coopState = updatedCoopState)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update coop player name")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to update player name: ${e.message}")
                }
            }
        }
    }

    fun handleUpdateCoopPlayerColor(playerColor: BubbleColor) {
        scope.launch {
            try {
                playerProfileRepository.savePlayerColor(playerColor)
                val initial = getCachedInitialCoopState()
                gameStateFlow.update { currentState ->
                    val updatedCoopState = currentState.coopState?.copy(
                        localPlayerColor = playerColor
                    ) ?: initial.copy(localPlayerColor = playerColor)
                    currentState.copy(coopState = updatedCoopState)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update coop player color")
                gameStateFlow.update {
                    it.copy(coopErrorMessage = "Failed to update player color: ${e.message}")
                }
            }
        }
    }

    fun handleSelectCoopMod(coopMod: CoopMod) {
        gameStateFlow.update { currentState ->
            currentState.coopState?.let { coopState ->
                currentState.copy(coopState = coopState.copy(selectedCoopMod = coopMod))
            } ?: currentState
        }
    }

    fun handleStartCoopAdvertising(playerName: String, selectedColor: String) {
        Timber.d("Coop advertising started for player: $playerName")
    }

    fun handleStartCoopDiscovery(playerName: String, selectedColor: String) {
        Timber.d("Coop discovery started for player: $playerName")
    }

    fun handleStopCoopConnection() {
        Timber.d("Coop connection stopped")
    }

    fun handleCoopSyncBubbles(bubbles: List<CoopBubble>) {
        Timber.d("Coop bubbles synced: ${bubbles.size}")
    }

    fun handleCoopSyncScores(localScore: Int, opponentScore: Int) {
        Timber.d("Coop scores synced: local=$localScore, opponent=$opponentScore")
    }

    suspend fun createInitialCoopState() = CoopGameState(
        localPlayerId = "player_${clock.currentTimeMillis()}",
        localPlayerName = playerProfileRepository.getPlayerName(),
        localPlayerColor = playerProfileRepository.getPlayerColor(),
        bubbles = generateInitialCoopBubbles()
    )

    suspend fun getCachedInitialCoopState(): CoopGameState {
        return _cachedInitialCoopState.await()
    }

    fun generateInitialCoopBubbles(): List<CoopBubble> {
        val rowSizes = listOf(5, 6, 7, 8, 7, 6, 5)
        var bubbleId = 0
        val bubbles = mutableListOf<CoopBubble>()

        for ((rowIndex, size) in rowSizes.withIndex()) {
            for (colIndex in 0 until size) {
                bubbles.add(
                    CoopBubble(
                        id = bubbleId,
                        position = bubbleId,
                        row = rowIndex,
                        col = colIndex
                    )
                )
                bubbleId++
            }
        }
        return bubbles
    }

    fun placeBombs(bubbles: List<CoopBubble>, bombCount: Int = 7): List<CoopBubble> {
        val bombIds = bubbles.map { it.id }.shuffled().take(bombCount).toSet()
        return bubbles.map { bubble ->
            if (bubble.id in bombIds) bubble.copy(isBomb = true) else bubble
        }
    }

    /**
     * Flood fill from a clicked bubble: claims the bubble and all connected unclaimed neighbors.
     * Returns the list of all claimed bubble IDs.
     */
    fun floodFill(bubbles: List<CoopBubble>, startBubbleId: Int, playerId: String): List<Int> {
        val startBubble = bubbles.find { it.id == startBubbleId } ?: return emptyList()
        if (startBubble.owner != null) return emptyList()

        // Claim the clicked bubble + all adjacent unclaimed neighbors (1 level)
        val claimed = mutableListOf(startBubbleId)
        for (neighbor in getHexNeighbors(startBubble.row, startBubble.col, bubbles)) {
            if (neighbor.owner == null) {
                claimed.add(neighbor.id)
            }
        }

        return claimed
    }

    /**
     * Get hex grid neighbors for a bubble at (row, col).
     * Grid layout: rowSizes = [5, 6, 7, 8, 7, 6, 5]
     * Even rows (0, 2, 4, 6) are shorter; odd rows (1, 3, 5) are offset.
     */
    private fun getHexNeighbors(row: Int, col: Int, bubbles: List<CoopBubble>): List<CoopBubble> {
        val rowSizes = listOf(5, 6, 7, 8, 7, 6, 5)
        val currentRowSize = rowSizes.getOrNull(row) ?: return emptyList()

        val neighbors = mutableListOf<Pair<Int, Int>>() // (row, col) pairs

        // Same row: left and right
        if (col > 0) neighbors.add(row to col - 1)
        if (col < currentRowSize - 1) neighbors.add(row to col + 1)

        // Adjacent rows
        val prevRow = row - 1
        val nextRow = row + 1
        val prevRowSize = rowSizes.getOrNull(prevRow)
        val nextRowSize = rowSizes.getOrNull(nextRow)

        // Hex offset logic: wider row → narrower row shifts, narrower → wider shifts
        if (prevRowSize != null) {
            if (prevRowSize > currentRowSize) {
                // Going to a wider row: col and col+1
                neighbors.add(prevRow to col)
                neighbors.add(prevRow to col + 1)
            } else if (prevRowSize < currentRowSize) {
                // Going to a narrower row: col-1 and col
                if (col > 0) neighbors.add(prevRow to col - 1)
                if (col < prevRowSize) neighbors.add(prevRow to col)
            } else {
                // Same size rows
                neighbors.add(prevRow to col)
                if (col < prevRowSize - 1) neighbors.add(prevRow to col + 1)
            }
        }

        if (nextRowSize != null) {
            if (nextRowSize > currentRowSize) {
                // Going to a wider row: col and col+1
                neighbors.add(nextRow to col)
                neighbors.add(nextRow to col + 1)
            } else if (nextRowSize < currentRowSize) {
                // Going to a narrower row: col-1 and col
                if (col > 0) neighbors.add(nextRow to col - 1)
                if (col < nextRowSize) neighbors.add(nextRow to col)
            } else {
                // Same size rows
                neighbors.add(nextRow to col)
                if (col < nextRowSize - 1) neighbors.add(nextRow to col + 1)
            }
        }

        // Filter valid neighbors and find matching bubbles
        return neighbors.mapNotNull { (r, c) ->
            bubbles.find { it.row == r && it.col == c }
        }
    }
}
