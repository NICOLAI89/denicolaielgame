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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.data.DailyChallengeBest
import com.nicolaielgame.game.model.DailyChallenge

@Composable
fun DailyChallengeScreen(
    challenge: DailyChallenge,
    best: DailyChallengeBest?,
    onStart: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Daily Challenge",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                text = challenge.dateKey,
                color = Color(0xFFC7EDE3),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            DailyChallengeMapPreview(
                challenge = challenge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(232.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xDD102522),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${challenge.level.title} - ${challenge.difficulty.title}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = "Based on Level ${challenge.baseLevelId}. Same map and rules for everyone on this device today.",
                        color = Color(0xFFC7EDE3),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        text = "Modifiers: ${challenge.modifiers.joinToString { it.title }}",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        text = best?.let {
                            "Best ${it.score}  Time ${it.completionTimeSeconds}s  Lives ${it.livesRemaining}"
                        } ?: "No daily score saved yet.",
                        color = Color(0xFFF6C55D),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Start Daily", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
private fun DailyChallengeMapPreview(
    challenge: DailyChallenge,
    modifier: Modifier = Modifier,
) {
    val map = challenge.level.map
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xEE102522),
        tonalElevation = 8.dp,
        modifier = modifier,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF12302F), Color(0xFF071413), Color(0xFF19112E)),
                ),
            )
            drawCircle(
                color = Color(0xFF7C4DFF).copy(alpha = 0.16f),
                radius = size.minDimension * 0.38f,
                center = Offset(size.width * 0.82f, size.height * 0.22f),
            )
            drawCircle(
                color = Color(0xFF18E0B5).copy(alpha = 0.12f),
                radius = size.minDimension * 0.44f,
                center = Offset(size.width * 0.2f, size.height * 0.7f),
            )

            val boardFactor = (map.rows + map.cols) / 2f
            val tileWidth = minOf(size.width * 0.84f / boardFactor, size.height * 0.78f / (boardFactor * 0.55f))
                .coerceAtLeast(30f)
            val tileHeight = tileWidth * 0.54f
            val origin = Offset(size.width / 2f, size.height * 0.28f)
            val sortedCells = map.cells.sortedWith(compareBy({ it.row + it.col }, { it.row }))

            sortedCells.forEach { cell ->
                val center = Offset(
                    x = origin.x + (cell.col - cell.row) * tileWidth / 2f,
                    y = origin.y + (cell.col + cell.row) * tileHeight / 2f,
                )
                val depth = tileHeight * 0.28f
                val color = when {
                    cell == map.spawn -> Color(0xFF18E0B5)
                    cell == map.base -> Color(0xFFFFD166)
                    cell in map.scenicPath -> Color(0xFF3A7568)
                    else -> Color(0xFF26554C)
                }
                val top = diamond(center, tileWidth, tileHeight)
                drawPath(
                    diamond(center + Offset(0f, depth), tileWidth * 0.94f, tileHeight * 0.9f),
                    Color.Black.copy(alpha = 0.2f),
                )
                drawPath(top, color.copy(alpha = if (cell in map.buildLockedCells) 0.9f else 0.72f))
                drawPath(top, Color.White.copy(alpha = 0.12f), style = Stroke(width = 1.2f))
                if (cell in map.scenicPath) {
                    drawPath(top, Color(0xFF18E0B5).copy(alpha = 0.18f))
                }
            }

            val portal = Offset(size.width * 0.18f, size.height * 0.18f)
            drawCircle(Color(0xFF7C4DFF).copy(alpha = 0.2f), radius = 35f, center = portal)
            drawCircle(Color(0xFF7C4DFF), radius = 21f, center = portal, style = Stroke(width = 5f))
            drawCircle(Color(0xFFFFD166), radius = 7f, center = portal)

            val badgeTopLeft = Offset(size.width * 0.52f, size.height * 0.06f)
            drawRoundRect(
                color = Color(0xDD183732),
                topLeft = badgeTopLeft,
                size = Size(size.width * 0.42f, 38f),
            )
            drawRoundRect(
                color = Color(0xFF18E0B5).copy(alpha = 0.55f),
                topLeft = badgeTopLeft,
                size = Size(size.width * 0.42f, 38f),
                style = Stroke(width = 2.5f),
            )
        }
    }
}

private fun diamond(center: Offset, width: Float, height: Float): Path {
    return Path().apply {
        moveTo(center.x, center.y - height / 2f)
        lineTo(center.x + width / 2f, center.y)
        lineTo(center.x, center.y + height / 2f)
        lineTo(center.x - width / 2f, center.y)
        close()
    }
}
