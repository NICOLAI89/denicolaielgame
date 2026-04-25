package com.nicolaielgame.ui.menu

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

@Composable
fun MainMenuScreen(
    bestScore: Int,
    lastUnlockedLevel: Int,
    unlockedAchievements: Int,
    activeProfile: Int,
    onStartGame: () -> Unit,
    onSettings: () -> Unit,
    onAchievements: () -> Unit,
    onProfiles: () -> Unit,
    onTutorial: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413)),
    ) {
        MenuBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Denicolaiel Game",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 39.sp,
            )
            Text(
                text = "Dynamic-path isometric tower defense",
                color = Color(0xFFC7EDE3),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xCC102522),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MenuStat(label = "Best", value = bestScore.toString())
                    MenuStat(label = "Level", value = lastUnlockedLevel.toString())
                    MenuStat(label = "Badges", value = unlockedAchievements.toString())
                    MenuStat(label = "Profile", value = activeProfile.toString())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Start Defense", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onAchievements,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Achievements")
                }
                Button(
                    onClick = onSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Settings")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onProfiles,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Profiles")
                }
                Button(
                    onClick = onTutorial,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Tutorial")
                }
            }
        }
    }
}

@Composable
private fun MenuStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
        Text(
            text = label,
            color = Color(0xFFB8DAD2),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun MenuBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFF071413), Color(0xFF123A35), Color(0xFF251B33)),
            ),
        )

        val tileW = size.width / 5.2f
        val tileH = tileW * 0.52f
        val startY = size.height * 0.18f
        val centerX = size.width * 0.5f

        repeat(5) { row ->
            repeat(5) { col ->
                val center = Offset(
                    x = centerX + (col - row) * tileW / 2f,
                    y = startY + (col + row) * tileH / 2f,
                )
                val path = Path().apply {
                    moveTo(center.x, center.y - tileH / 2f)
                    lineTo(center.x + tileW / 2f, center.y)
                    lineTo(center.x, center.y + tileH / 2f)
                    lineTo(center.x - tileW / 2f, center.y)
                    close()
                }
                drawPath(
                    path = path,
                    color = if ((row + col) % 2 == 0) Color(0x3327D3A8) else Color(0x224CA3FF),
                )
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.08f),
                    style = Stroke(width = 1.2f),
                )
            }
        }

        drawOval(
            color = Color(0x33F6C55D),
            topLeft = Offset(size.width * 0.18f, size.height * 0.72f),
            size = Size(size.width * 0.64f, size.height * 0.16f),
        )
    }
}

