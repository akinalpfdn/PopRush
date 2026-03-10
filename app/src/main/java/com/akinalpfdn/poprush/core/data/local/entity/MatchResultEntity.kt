package com.akinalpfdn.poprush.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a coop match result.
 * Stores match outcome with player pair info for stats and history.
 */
@Entity(tableName = "match_results")
data class MatchResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val localPlayerId: String,
    val localPlayerName: String,
    val opponentPlayerId: String,
    val opponentPlayerName: String,

    val localScore: Int,
    val opponentScore: Int,

    /** Name of the CoopMod enum entry */
    val coopMod: String,

    /** Local player's ID if they won, opponent's ID if they won, null if tie */
    val winnerId: String?,

    val timestamp: Long = System.currentTimeMillis()
)
