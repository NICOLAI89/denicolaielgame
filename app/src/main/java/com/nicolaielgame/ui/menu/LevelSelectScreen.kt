package com.nicolaielgame.ui.menu

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
                    .height(336.dp),
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
        color = Color(0xEE0E201F),
        tonalElevation = 8.dp,
        modifier = modifier,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        ) {
            if (nodes.isEmpty()) return@Canvas
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF102A29), Color(0xFF071413), Color(0xFF16122C)),
                ),
            )
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3336F0D2), Color.Transparent),
                    center = Offset(size.width * 0.24f, size.height * 0.18f),
                    radius = size.minDimension * 0.72f,
                ),
                size = size,
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                size = size,
                style = Stroke(width = 2f),
            )

            repeat(8) { index ->
                val x = size.width * (index / 7f)
                drawLine(
                    color = Color(0xFF55F0D4).copy(alpha = 0.055f),
                    start = Offset(x - size.width * 0.16f, 0f),
                    end = Offset(x + size.width * 0.24f, size.height),
                    strokeWidth = 1.4f,
                )
            }

            listOf(
                Offset(size.width * 0.14f, size.height * 0.22f) to Size(size.width * 0.28f, size.height * 0.18f),
                Offset(size.width * 0.48f, size.height * 0.58f) to Size(size.width * 0.32f, size.height * 0.18f),
                Offset(size.width * 0.64f, size.height * 0.18f) to Size(size.width * 0.24f, size.height * 0.16f),
            ).forEach { (topLeft, terrainSize) ->
                drawOval(
                    color = Color(0xFF1A4942).copy(alpha = 0.36f),
                    topLeft = topLeft,
                    size = terrainSize,
                )
            }

            val points = nodes.mapIndexed { index, _ ->
                val progress = if (nodes.size == 1) 0f else index / (nodes.size - 1f)
                val x = size.width * (0.1f + progress * 0.8f)
                val y = size.height * when (index) {
                    0 -> 0.72f
                    1 -> 0.56f
                    2 -> 0.35f
                    3 -> 0.44f
                    4 -> 0.25f
                    5 -> 0.42f
                    else -> 0.2f
                }
                Offset(x, y)
            }

            val route = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEachIndexed { index, point ->
                    val previous = points[index]
                    val control = Offset(
                        x = (previous.x + point.x) / 2f,
                        y = minOf(previous.y, point.y) - size.height * 0.08f,
                    )
                    quadraticBezierTo(control.x, control.y, point.x, point.y)
                }
            }
            drawPath(route, Color.Black.copy(alpha = 0.36f), style = Stroke(width = 16f))
            drawPath(route, Color(0xFF18E0B5).copy(alpha = 0.42f), style = Stroke(width = 9f))
            drawPath(route, Color.White.copy(alpha = 0.28f), style = Stroke(width = 2f))

            val dailyPortal = Offset(size.width * 0.18f, size.height * 0.16f)
            drawCircle(Color(0xFF7C4DFF).copy(alpha = 0.2f), radius = 38f, center = dailyPortal)
            drawCircle(Color(0xFF7C4DFF), radius = 24f, center = dailyPortal, style = Stroke(width = 5f))
            drawCircle(Color(0xFF18E0B5), radius = 10f, center = dailyPortal)
            drawMapText("DAILY", dailyPortal + Offset(0f, 54f), 22f, Color(0xFFC7EDE3), Paint.Align.CENTER)

            val recordsVault = Offset(size.width * 0.84f, size.height * 0.78f)
            drawRoundRect(
                color = Color(0xFF183A36),
                topLeft = recordsVault - Offset(36f, 24f),
                size = Size(72f, 48f),
            )
            drawRoundRect(
                color = Color(0xFFFFD166).copy(alpha = 0.72f),
                topLeft = recordsVault - Offset(28f, 14f),
                size = Size(56f, 28f),
                style = Stroke(width = 4f),
            )
            drawMapText("RECORDS", recordsVault + Offset(0f, 48f), 20f, Color(0xFFFFD166), Paint.Align.CENTER)

            val currentLevelId = CampaignProgression.nextPlayableLevelId(nodes)
            nodes.forEachIndexed { index, node ->
                val center = points[index]
                val level = LevelCatalog.find(node.levelId)
                val isBoss = level.waves.any { it.isBossWave }
                val isCurrent = node.levelId == currentLevelId
                val color = when (node.state) {
                    CampaignNodeState.Completed -> Color(0xFF18E0B5)
                    CampaignNodeState.Unlocked -> Color(0xFFFFD166)
                    CampaignNodeState.Locked -> Color(0xFF465B59)
                }
                drawSpriteNodeBase(center, color, isCurrent)
                if (isBoss) {
                    drawCircle(
                        color = Color(0xFFFF5A73).copy(alpha = if (node.state == CampaignNodeState.Locked) 0.3f else 0.86f),
                        radius = 13f,
                        center = center - Offset(0f, 30f),
                    )
                    drawMapText("B", center - Offset(0f, 23f), 17f, Color.White, Paint.Align.CENTER)
                }
                drawMapText(
                    text = "L${node.levelId}",
                    anchor = center + Offset(0f, 7f),
                    textSize = 26f,
                    color = if (node.state == CampaignNodeState.Locked) Color(0xFFB1C1BD) else Color.White,
                    align = Paint.Align.CENTER,
                )
                drawMapText(
                    text = when (node.state) {
                        CampaignNodeState.Completed -> "CLEAR"
                        CampaignNodeState.Unlocked -> if (isCurrent) "CURRENT" else "OPEN"
                        CampaignNodeState.Locked -> "LOCKED"
                    },
                    anchor = center + Offset(0f, 42f),
                    textSize = 18f,
                    color = color,
                    align = Paint.Align.CENTER,
                )
            }
        }
    }
}

private fun DrawScope.drawSpriteNodeBase(center: Offset, color: Color, isCurrent: Boolean) {
    drawOval(
        color = Color.Black.copy(alpha = 0.42f),
        topLeft = center + Offset(-38f, 22f),
        size = Size(76f, 24f),
    )
    drawCircle(
        color = color.copy(alpha = if (isCurrent) 0.34f else 0.2f),
        radius = if (isCurrent) 42f else 34f,
        center = center,
    )
    val base = Path().apply {
        moveTo(center.x, center.y - 28f)
        lineTo(center.x + 34f, center.y - 4f)
        lineTo(center.x, center.y + 22f)
        lineTo(center.x - 34f, center.y - 4f)
        close()
    }
    val side = Path().apply {
        moveTo(center.x + 34f, center.y - 4f)
        lineTo(center.x, center.y + 22f)
        lineTo(center.x, center.y + 34f)
        lineTo(center.x + 34f, center.y + 8f)
        close()
    }
    drawPath(side, Color(0xFF0A211F))
    drawPath(base, color.copy(alpha = if (isCurrent) 0.95f else 0.78f))
    drawPath(
        base,
        Color.White.copy(alpha = if (isCurrent) 0.82f else 0.36f),
        style = Stroke(width = if (isCurrent) 4f else 2.5f),
    )
}

private fun DrawScope.drawMapText(
    text: String,
    anchor: Offset,
    textSize: Float,
    color: Color,
    align: Paint.Align,
) {
    drawIntoCanvas { canvas ->
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.Black.copy(alpha = 0.55f).toArgb()
            textAlign = align
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toArgb()
            textAlign = align
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.nativeCanvas.drawText(text, anchor.x + 1.5f, anchor.y + 1.5f, shadow)
        canvas.nativeCanvas.drawText(text, anchor.x, anchor.y, paint)
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

