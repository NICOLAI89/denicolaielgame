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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.nicolaielgame.data.ProfileRules
import com.nicolaielgame.data.ProfileSummary

@Composable
fun ProfileSelectScreen(
    activeProfile: Int,
    profileSummaries: List<ProfileSummary>,
    onProfileSelected: (Int) -> Unit,
    onResetProfile: (Int) -> Unit,
    onContinue: () -> Unit,
) {
    var resetSlot by remember { mutableStateOf<Int?>(null) }

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
                text = "Choose Profile",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Up to three local save slots live on this device.",
                color = Color(0xFFC7EDE3),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )

            (1..ProfileRules.MaxProfiles).forEach { slot ->
                val summary = profileSummaries.firstOrNull { it.slot == slot }
                ProfileRow(
                    slot = slot,
                    isActive = slot == activeProfile,
                    highestUnlockedLevel = summary?.highestUnlockedLevel ?: 1,
                    bestScore = summary?.bestScore ?: 0,
                    onSelect = { onProfileSelected(slot) },
                    onReset = { resetSlot = slot },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Continue Profile $activeProfile")
            }
        }
    }

    resetSlot?.let { slot ->
        AlertDialog(
            onDismissRequest = { resetSlot = null },
            confirmButton = {
                Button(
                    onClick = {
                        resetSlot = null
                        onResetProfile(slot)
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { resetSlot = null },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Reset profile $slot?") },
            text = { Text("This clears that profile's scores, level unlocks, achievements, and tutorial state.") },
        )
    }
}

@Composable
private fun ProfileRow(
    slot: Int,
    isActive: Boolean,
    highestUnlockedLevel: Int,
    bestScore: Int,
    onSelect: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) Color(0xDD102522) else Color(0xBB15211F),
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
                    text = "Profile $slot${if (isActive) " active" else ""}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Level $highestUnlockedLevel  Best $bestScore",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                )
            }
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text("Reset")
            }
            FilledTonalButton(
                onClick = onSelect,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (isActive) "Use" else "Select")
            }
        }
    }
}
