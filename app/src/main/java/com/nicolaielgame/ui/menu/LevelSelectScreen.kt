package com.nicolaielgame.ui.menu

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.R
import com.nicolaielgame.data.CampaignNode
import com.nicolaielgame.data.CampaignNodeState
import com.nicolaielgame.data.CampaignProgression
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
    onDailyChallenge: () -> Unit,
    onLeaderboard: () -> Unit,
    onBack: () -> Unit,
) {
    val campaignNodes = CampaignProgression.nodes(
        levels = LevelCatalog.levels,
        highestUnlockedLevel = highestUnlockedLevel,
        bestScoresByLevel = bestScoresByLevel,
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
                text = "Campaign Map",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Follow the circuit route, then branch into daily runs or records.",
                color = Color(0xFFC7EDE3),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            CampaignMap(
                nodes = campaignNodes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = onDailyChallenge,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.kenney_icon_daily),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(18.dp),
                    )
                    Text("Daily")
                }
                FilledTonalButton(
                    onClick = onLeaderboard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.kenney_icon_leaderboard),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(18.dp),
                    )
                    Text("Records")
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
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

            Spacer(modifier = Modifier.height(12.dp))
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
private fun CampaignMap(
    nodes: List<CampaignNode>,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD102522),
        tonalElevation = 4.dp,
        modifier = modifier,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            if (nodes.isEmpty()) return@Canvas
            val points = nodes.mapIndexed { index, _ ->
                val progress = if (nodes.size == 1) 0f else index / (nodes.size - 1f)
                val x = size.width * (0.09f + progress * 0.82f)
                val y = size.height * when (index % 4) {
                    0 -> 0.66f
                    1 -> 0.34f
                    2 -> 0.48f
                    else -> 0.24f
                }
                Offset(x, y)
            }

            points.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = Color(0xFF18E0B5).copy(alpha = 0.38f),
                    start = start,
                    end = end,
                    strokeWidth = 5f,
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = start,
                    end = end,
                    strokeWidth = 1.5f,
                )
            }

            nodes.forEachIndexed { index, node ->
                val center = points[index]
                val color = when (node.state) {
                    CampaignNodeState.Completed -> Color(0xFF18E0B5)
                    CampaignNodeState.Unlocked -> Color(0xFFFFD166)
                    CampaignNodeState.Locked -> Color(0xFF465B59)
                }
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = 28f,
                    center = center,
                )
                drawCircle(
                    color = color,
                    radius = 16f,
                    center = center,
                )
                drawCircle(
                    color = Color.White.copy(alpha = if (node.state == CampaignNodeState.Locked) 0.22f else 0.72f),
                    radius = 19f,
                    center = center,
                    style = Stroke(width = 3f),
                )
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
                    text = "Best $bestScore  Waves ${level.totalWaves}${if (level.waves.any { it.isBossWave }) "  Boss" else ""}",
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

