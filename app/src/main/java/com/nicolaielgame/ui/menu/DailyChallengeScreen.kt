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
