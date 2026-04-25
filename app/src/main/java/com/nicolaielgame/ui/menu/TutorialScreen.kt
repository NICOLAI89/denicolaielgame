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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class TutorialPage(
    val title: String,
    val body: String,
)

private val TutorialPages = listOf(
    TutorialPage(
        title = "Build the Maze",
        body = "Tap buildable isometric tiles to place towers. Spawn, base, locked tiles, and occupied tiles stay open.",
    ),
    TutorialPage(
        title = "Keep a Route Open",
        body = "Towers block movement. If a placement would fully block every enemy path, the game rejects it and refunds the action.",
    ),
    TutorialPage(
        title = "Upgrade or Sell",
        body = "Tap a tower to view range, stats, upgrade cost, refund value, and targeting mode. Upgrades increase damage, range, and fire rate.",
    ),
    TutorialPage(
        title = "Control Waves",
        body = "Start each wave manually. When all spawned enemies are gone, the next wave becomes ready. Final wave victory unlocks progress.",
    ),
    TutorialPage(
        title = "Use Abilities",
        body = "Meteor damages an area, Freeze slows a group, and Emergency Gold gives a limited cash boost. Cooldowns appear in the HUD.",
    ),
)

@Composable
fun TutorialScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    val page = TutorialPages[pageIndex]
    val isLast = pageIndex == TutorialPages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xDD102522),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Tutorial ${pageIndex + 1}/${TutorialPages.size}",
                    color = Color(0xFFF6C55D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = page.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = page.body,
                    color = Color(0xFFC7EDE3),
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 14.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Skip")
                    }
                    if (isLast) {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("Done")
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { pageIndex++ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}
