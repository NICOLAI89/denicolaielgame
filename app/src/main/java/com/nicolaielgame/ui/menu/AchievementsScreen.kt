package com.nicolaielgame.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.data.Achievement

@Composable
fun AchievementsScreen(
    unlockedAchievements: Set<Achievement>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413))
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Achievements",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Achievement.entries.forEach { achievement ->
            val unlocked = achievement in unlockedAchievements
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (unlocked) Color(0xDD102522) else Color(0xAA15211F),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = achievement.title,
                        color = if (unlocked) Color.White else Color(0xFF79928B),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = if (unlocked) achievement.description else "Locked",
                        color = if (unlocked) Color(0xFFC7EDE3) else Color(0xFF6F8881),
                        fontSize = 12.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Back")
        }
    }
}
