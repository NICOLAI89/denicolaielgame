package com.nicolaielgame.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.data.LeaderboardEntry
import com.nicolaielgame.data.LocalLeaderboard
import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.LevelCatalog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    onBack: () -> Unit,
) {
    var levelFilter by remember { mutableStateOf<Int?>(null) }
    var difficultyFilter by remember { mutableStateOf<DifficultyMode?>(null) }
    val topRuns = LocalLeaderboard.topRuns(
        entries = entries,
        levelId = levelFilter,
        difficulty = difficultyFilter,
        limit = 25,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Local Leaderboard",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Completed campaign runs saved on this device.",
                color = Color(0xFFC7EDE3),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )

            FilterRow(
                label = "Level",
                selected = levelFilter?.toString() ?: "All",
                options = listOf("All") + LevelCatalog.levels.map { it.id.toString() },
                onSelected = { selected -> levelFilter = selected.toIntOrNull() },
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(
                label = "Difficulty",
                selected = difficultyFilter?.title ?: "All",
                options = listOf("All") + DifficultyMode.entries.map { it.title },
                onSelected = { selected ->
                    difficultyFilter = DifficultyMode.entries.firstOrNull { it.title == selected }
                },
            )

            Spacer(modifier = Modifier.height(14.dp))
            if (topRuns.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD102522),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "No completed runs match this filter yet.",
                        color = Color(0xFFC7EDE3),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(18.dp),
                    )
                }
            } else {
                topRuns.forEachIndexed { index, entry ->
                    LeaderboardRow(rank = index + 1, entry = entry)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun FilterRow(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color(0xFFC7EDE3),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { option ->
                FilledTonalButton(
                    onClick = { onSelected(option) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = option,
                        color = if (option == selected) MaterialTheme.colorScheme.secondary else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (option == selected) FontWeight.Black else FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD102522),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "#$rank",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level ${entry.levelId} - ${entry.difficulty.title} - Profile ${entry.profileSlot}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                )
                Text(
                    text = "Time ${entry.completionTimeSeconds}s  Lives ${entry.livesRemaining}  ${formatDate(entry.completedAtEpochMillis)}",
                    color = Color(0xFFC7EDE3),
                    fontSize = 12.sp,
                )
            }
            Text(
                text = entry.score.toString(),
                color = Color(0xFFF6C55D),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    return SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(epochMillis))
}
