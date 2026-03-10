package com.akinalpfdn.poprush.coop.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akinalpfdn.poprush.coop.domain.model.CoopMod
import com.akinalpfdn.poprush.core.data.local.entity.MatchResultEntity
import com.akinalpfdn.poprush.core.domain.repository.MatchHistoryRepository
import com.akinalpfdn.poprush.ui.theme.AppColors
import com.akinalpfdn.poprush.ui.theme.NunitoFontFamily
import com.akinalpfdn.poprush.ui.theme.withAlpha
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stats data for a single coop mod between two players.
 */
private data class ModStats(
    val mod: CoopMod,
    val localWins: Int,
    val opponentWins: Int,
    val totalMatches: Int
)

/**
 * Full-screen dialog showing match stats and history between two players.
 */
@Composable
fun CoopStatsDialog(
    localPlayerId: String,
    localPlayerName: String,
    opponentPlayerId: String,
    opponentPlayerName: String,
    matchHistoryRepository: MatchHistoryRepository,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var modStatsList by remember { mutableStateOf<List<ModStats>>(emptyList()) }
    var matchHistory by remember { mutableStateOf<List<MatchResultEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(localPlayerId, opponentPlayerId) {
        isLoading = true
        scope.launch {
            // Load stats per mod
            val stats = CoopMod.entries.map { mod ->
                ModStats(
                    mod = mod,
                    localWins = matchHistoryRepository.getWinCount(localPlayerId, opponentPlayerId, mod.name),
                    opponentWins = matchHistoryRepository.getWinCount(opponentPlayerId, localPlayerId, mod.name),
                    totalMatches = matchHistoryRepository.getTotalMatchCount(localPlayerId, opponentPlayerId, mod.name)
                )
            }
            modStatsList = stats

            // Load match history
            matchHistory = matchHistoryRepository.getRecentMatches(localPlayerId, opponentPlayerId)
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background.Dialog)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.8f)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .background(AppColors.Background.Primary, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // consume click
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    StatsHeader(
                        localPlayerName = localPlayerName,
                        opponentPlayerName = opponentPlayerName,
                        onDismiss = onDismiss
                    )

                    // Tab Row
                    val tabTitles = listOf("Stats", "History")
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = AppColors.Text.Primary,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = AppColors.Bubble.SkyBlue
                                )
                            }
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == index) AppColors.Bubble.SkyBlue else AppColors.Text.Label
                                    )
                                }
                            )
                        }
                    }

                    // Content
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.Bubble.SkyBlue)
                        }
                    } else {
                        when (selectedTab) {
                            0 -> StatsTabContent(
                                modStatsList = modStatsList,
                                localPlayerName = localPlayerName,
                                opponentPlayerName = opponentPlayerName
                            )
                            1 -> HistoryTabContent(
                                matchHistory = matchHistory,
                                localPlayerId = localPlayerId,
                                localPlayerName = localPlayerName,
                                opponentPlayerName = opponentPlayerName
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsHeader(
    localPlayerName: String,
    opponentPlayerName: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.Bubble.SkyBlue.withAlpha(0.15f),
                        Color.Transparent
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(40.dp))

                Text(
                    text = "MATCH STATS",
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = AppColors.Text.Primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppColors.Text.Label
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${localPlayerName.take(12)} vs ${opponentPlayerName.take(12)}",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = AppColors.Text.Tertiary
            )
        }
    }
}

@Composable
private fun StatsTabContent(
    modStatsList: List<ModStats>,
    localPlayerName: String,
    opponentPlayerName: String
) {
    val hasAnyMatches = modStatsList.any { it.totalMatches > 0 }

    if (!hasAnyMatches) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = AppColors.Text.Label,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No matches played yet!",
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = AppColors.Text.Label
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Play some games to see stats here.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = AppColors.Text.Muted
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall summary
        val totalLocalWins = modStatsList.sumOf { it.localWins }
        val totalOpponentWins = modStatsList.sumOf { it.opponentWins }
        val totalMatches = modStatsList.sumOf { it.totalMatches }

        OverallSummaryCard(
            localPlayerName = localPlayerName,
            opponentPlayerName = opponentPlayerName,
            localWins = totalLocalWins,
            opponentWins = totalOpponentWins,
            totalMatches = totalMatches
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Per-mod breakdown
        modStatsList.filter { it.totalMatches > 0 }.forEach { stats ->
            ModStatsCard(
                stats = stats,
                localPlayerName = localPlayerName,
                opponentPlayerName = opponentPlayerName
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OverallSummaryCard(
    localPlayerName: String,
    opponentPlayerName: String,
    localWins: Int,
    opponentWins: Int,
    totalMatches: Int
) {
    val draws = totalMatches - localWins - opponentWins

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "OVERALL",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = AppColors.Text.Label,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local player
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$localWins",
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = AppColors.Bubble.Mint
                    )
                    Text(
                        text = localPlayerName.take(10),
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AppColors.Text.Tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Separator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "—",
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = AppColors.Text.Label
                    )
                    if (draws > 0) {
                        Text(
                            text = "$draws draw${if (draws > 1) "s" else ""}",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = AppColors.Text.Muted
                        )
                    }
                }

                // Opponent
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$opponentWins",
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = AppColors.Bubble.Coral
                    )
                    Text(
                        text = opponentPlayerName.take(10),
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AppColors.Text.Tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$totalMatches match${if (totalMatches != 1) "es" else ""} played",
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                color = AppColors.Text.Muted
            )
        }
    }
}

@Composable
private fun ModStatsCard(
    stats: ModStats,
    localPlayerName: String,
    opponentPlayerName: String
) {
    val modColor = getModColor(stats.mod)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp))
            .background(Color.White, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mod icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(modColor.withAlpha(0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getModIcon(stats.mod),
                    contentDescription = null,
                    tint = modColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.mod.displayName,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppColors.Text.Primary
                )
                Text(
                    text = "${stats.totalMatches} match${if (stats.totalMatches != 1) "es" else ""}",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = AppColors.Text.Muted
                )
            }

            // Score
            Text(
                text = "${stats.localWins}",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = AppColors.Bubble.Mint
            )

            Text(
                text = " - ",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AppColors.Text.Label
            )

            Text(
                text = "${stats.opponentWins}",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = AppColors.Bubble.Coral
            )
        }
    }
}

@Composable
private fun HistoryTabContent(
    matchHistory: List<MatchResultEntity>,
    localPlayerId: String,
    localPlayerName: String,
    opponentPlayerName: String
) {
    if (matchHistory.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = AppColors.Text.Label,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No match history yet!",
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = AppColors.Text.Label
                )
            }
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        matchHistory.forEach { match ->
            HistoryCard(
                match = match,
                localPlayerId = localPlayerId,
                localPlayerName = localPlayerName,
                opponentPlayerName = opponentPlayerName,
                dateFormat = dateFormat
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HistoryCard(
    match: MatchResultEntity,
    localPlayerId: String,
    localPlayerName: String,
    opponentPlayerName: String,
    dateFormat: SimpleDateFormat
) {
    val isLocalWin = match.winnerId == localPlayerId
    val isOpponentWin = match.winnerId != null && match.winnerId != localPlayerId
    val isDraw = match.winnerId == null

    // Determine scores from local player's perspective
    val displayLocalScore: Int
    val displayOpponentScore: Int
    if (match.localPlayerId == localPlayerId) {
        displayLocalScore = match.localScore
        displayOpponentScore = match.opponentScore
    } else {
        displayLocalScore = match.opponentScore
        displayOpponentScore = match.localScore
    }

    val resultColor = when {
        isLocalWin -> AppColors.Bubble.Mint
        isOpponentWin -> AppColors.Bubble.Coral
        else -> AppColors.Text.Label
    }

    val resultText = when {
        isLocalWin -> "WIN"
        isOpponentWin -> "LOSS"
        else -> "DRAW"
    }

    val modName = try {
        CoopMod.valueOf(match.coopMod).displayName
    } catch (_: Exception) {
        match.coopMod
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Result badge
            Box(
                modifier = Modifier
                    .background(resultColor.withAlpha(0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = resultText,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = resultColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modName,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = AppColors.Text.Primary
                )
                Text(
                    text = dateFormat.format(Date(match.timestamp)),
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = AppColors.Text.Muted
                )
            }

            // Score
            Text(
                text = "$displayLocalScore - $displayOpponentScore",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = AppColors.Text.Primary
            )
        }
    }
}

private fun getModColor(mod: CoopMod): Color = when (mod) {
    CoopMod.BUBBLE_POP -> AppColors.Bubble.SkyBlue
    CoopMod.TERRITORY_WAR -> AppColors.Bubble.Grape
    CoopMod.BLIND_MODE -> AppColors.Bubble.Indigo
    CoopMod.HOT_POTATO -> AppColors.Bubble.Coral
    CoopMod.CHAIN_REACTION -> AppColors.Bubble.Amber
}

private fun getModIcon(mod: CoopMod): androidx.compose.ui.graphics.vector.ImageVector = when (mod) {
    CoopMod.BUBBLE_POP -> Icons.Default.TouchApp
    CoopMod.TERRITORY_WAR -> Icons.Default.Flag
    CoopMod.BLIND_MODE -> Icons.Default.VisibilityOff
    CoopMod.HOT_POTATO -> Icons.Default.Whatshot
    CoopMod.CHAIN_REACTION -> Icons.Default.Bolt
}
