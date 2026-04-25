package com.nicolaielgame.ui.menu

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.game.model.LevelDefinition

@Composable
fun LevelSelectScreen(
    highestUnlockedLevel: Int,
    bestScoresByLevel: Map<Int, Int>,
    selectedDifficulty: DifficultyMode,
    onDifficultySelected: (DifficultyMode) -> Unit,
    onLevelSelected: (LevelDefinition) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Select Level",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Victory unlocks the next battlefield.",
                color = Color(0xFFC7EDE3),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            DifficultySelector(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = onDifficultySelected,
            )
            Spacer(modifier = Modifier.height(14.dp))

            LevelCatalog.levels.forEach { level ->
                val unlocked = level.id <= highestUnlockedLevel
                LevelRow(
                    level = level,
                    unlocked = unlocked,
                    bestScore = bestScoresByLevel[level.id] ?: 0,
                    onLevelSelected = onLevelSelected,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Back to Menu")
            }
        }
    }
}

@Composable
private fun DifficultySelector(
    selectedDifficulty: DifficultyMode,
    onDifficultySelected: (DifficultyMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DifficultyMode.entries.forEach { difficulty ->
            val selected = difficulty == selectedDifficulty
            FilledTonalButton(
                onClick = { onDifficultySelected(difficulty) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = difficulty.title,
                    color = if (selected) MaterialTheme.colorScheme.secondary else Color.White,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun LevelRow(
    level: LevelDefinition,
    unlocked: Boolean,
    bestScore: Int,
    onLevelSelected: (LevelDefinition) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (unlocked) Color(0xDD102522) else Color(0xAA15211F),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level ${level.id}: ${level.title}",
                    color = if (unlocked) Color.White else Color(0xFF79928B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = level.description,
                    color = if (unlocked) Color(0xFFC7EDE3) else Color(0xFF6F8881),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                )
                Text(
                    text = "Best $bestScore  Waves ${level.totalWaves}",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (unlocked) {
                FilledTonalButton(
                    onClick = { onLevelSelected(level) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 10.dp),
                ) {
                    Text("Play")
                }
            } else {
                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 10.dp),
                ) {
                    Text("Locked")
                }
            }
        }
    }
}

