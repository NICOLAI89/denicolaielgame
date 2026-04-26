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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.R
import com.nicolaielgame.data.GameSettings

@Composable
fun SettingsScreen(
    settings: GameSettings,
    onSettingsChanged: (GameSettings) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }

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
                text = "Settings",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xDD102522),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingRow(
                        icon = R.drawable.ic_ui_settings,
                        label = "Sound",
                        checked = settings.soundEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(soundEnabled = it)) },
                    )
                    SettingRow(
                        icon = R.drawable.ic_ui_settings,
                        label = "Music",
                        checked = settings.musicEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(musicEnabled = it)) },
                    )
                    SettingRow(
                        icon = R.drawable.ic_ui_campaign,
                        label = "Show grid",
                        checked = settings.showGrid,
                        onCheckedChange = { onSettingsChanged(settings.copy(showGrid = it)) },
                    )
                    SettingRow(
                        icon = R.drawable.ic_ui_campaign,
                        label = "Screen shake",
                        checked = settings.screenShakeEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(screenShakeEnabled = it)) },
                    )
                    SettingRow(
                        icon = R.drawable.ic_ui_leaderboard,
                        label = "Damage numbers",
                        checked = settings.damageNumbersEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(damageNumbersEnabled = it)) },
                    )
                    SettingRow(
                        icon = R.drawable.ic_ui_campaign,
                        label = "High contrast",
                        checked = settings.highContrastMode,
                        onCheckedChange = { onSettingsChanged(settings.copy(highContrastMode = it)) },
                    )
                    SettingRow(
                        icon = R.drawable.ic_ui_leaderboard,
                        label = "FPS counter",
                        checked = settings.fpsCounterEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(fpsCounterEnabled = it)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { confirmReset = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Reset Active Profile")
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

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            confirmButton = {
                Button(
                    onClick = {
                        confirmReset = false
                        onResetProgress()
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmReset = false },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Reset active profile?") },
            text = { Text("Scores, unlocked levels, achievements, and tutorial state for this profile will be reset.") },
        )
    }
}

@Composable
private fun SettingRow(
    icon: Int,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color(0xFFA9F1FF),
                modifier = Modifier.padding(end = 10.dp),
            )
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
