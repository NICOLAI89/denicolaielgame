package com.nicolaielgame.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicolaielgame.game.engine.GameEngine
import com.nicolaielgame.game.model.AbilityType
import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult
import com.nicolaielgame.game.model.GameState
import com.nicolaielgame.game.model.GameStatus
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.LevelDefinition
import com.nicolaielgame.game.model.TargetingMode
import com.nicolaielgame.game.model.TowerType
import com.nicolaielgame.game.model.WavePhase
import com.nicolaielgame.game.rendering.IsoRenderer
import com.nicolaielgame.game.systems.AndroidToneSoundPlayer

@Composable
fun GameScreen(
    level: LevelDefinition,
    difficulty: DifficultyMode,
    bestScore: Int,
    soundEnabled: Boolean,
    showGrid: Boolean,
    onBackToMenu: () -> Unit,
    onRunFinalized: suspend (GameRunResult) -> Unit,
) {
    val context = LocalContext.current
    val soundPlayer = remember { AndroidToneSoundPlayer(context) }
    val engine = remember(level.id, difficulty) { GameEngine(level, difficulty, soundPlayer) }
    val state by engine.state.collectAsState()
    var savedTerminalStatus by remember { mutableStateOf<GameStatus?>(null) }

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    LaunchedEffect(soundEnabled) {
        soundPlayer.enabled = soundEnabled
    }

    LaunchedEffect(bestScore) {
        engine.setBestScore(bestScore)
    }

    LaunchedEffect(Unit) {
        var lastFrame = withFrameNanos { it }
        while (true) {
            val frame = withFrameNanos { it }
            engine.tick((frame - lastFrame) / 1_000_000_000f)
            lastFrame = frame
        }
    }

    LaunchedEffect(state.status, state.score) {
        if (state.isTerminal && savedTerminalStatus != state.status) {
            savedTerminalStatus = state.status
            onRunFinalized(
                GameRunResult(
                    levelId = level.id,
                    score = state.score,
                    won = state.status == GameStatus.Victory,
                    livesRemaining = state.lives,
                    startingLives = difficulty.applyStartingLives(level.startingLives),
                    difficulty = difficulty,
                    bossesDefeated = state.bossesDefeated,
                    towersPlacedByType = state.towersPlacedByType,
                ),
            )
        }
    }

    GameScaffold(
        state = state,
        onPause = engine::pause,
        onResume = engine::resume,
        onRestart = {
            savedTerminalStatus = null
            engine.reset(bestScore, level, difficulty)
        },
        onBackToMenu = onBackToMenu,
        onCellTapped = engine::handleCellTap,
        onSelectTowerType = engine::selectTowerType,
        onUpgradeTower = engine::upgradeTower,
        onSellTower = engine::sellTower,
        onSetTowerTargetingMode = engine::setTowerTargetingMode,
        onActivateAbility = engine::activateAbility,
        onStartNextWave = engine::startNextWave,
        showGrid = showGrid,
    )
}

@Composable
private fun GameScaffold(
    state: GameState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit,
    onCellTapped: (GridCell) -> Unit,
    onSelectTowerType: (TowerType) -> Unit,
    onUpgradeTower: (Int) -> Unit,
    onSellTower: (Int) -> Unit,
    onSetTowerTargetingMode: (Int, TargetingMode) -> Unit,
    onActivateAbility: (AbilityType) -> Unit,
    onStartNextWave: () -> Unit,
    showGrid: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HudBar(state = state, onPause = onPause, onStartNextWave = onStartNextWave)
            GameCanvas(
                state = state,
                showGrid = showGrid,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onCellTapped = onCellTapped,
            )
            BottomBar(
                state = state,
                onSelectTowerType = onSelectTowerType,
                onUpgradeTower = onUpgradeTower,
                onSellTower = onSellTower,
                onSetTowerTargetingMode = onSetTowerTargetingMode,
                onActivateAbility = onActivateAbility,
            )
        }

        when (state.status) {
            GameStatus.Paused -> PauseOverlay(
                onResume = onResume,
                onRestart = onRestart,
                onBackToMenu = onBackToMenu,
            )

            GameStatus.GameOver -> TerminalOverlay(
                title = "Base Lost",
                subtitle = "Score ${state.score}  Gold ${state.gold}  Wave ${state.wave.currentWave}/${state.wave.totalWaves}",
                primaryText = "Restart",
                onPrimary = onRestart,
                onBackToMenu = onBackToMenu,
            )

            GameStatus.Victory -> TerminalOverlay(
                title = "Victory",
                subtitle = "Score ${state.score}  Lives ${state.lives}  Bosses ${state.bossesDefeated}",
                primaryText = "Play Again",
                onPrimary = onRestart,
                onBackToMenu = onBackToMenu,
            )

            GameStatus.Running -> Unit
        }
    }
}

@Composable
private fun HudBar(
    state: GameState,
    onPause: () -> Unit,
    onStartNextWave: () -> Unit,
) {
    Surface(
        color = Color(0xEE102522),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "${state.level.title} - ${state.difficulty.title}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Wave ${state.wave.currentWave}/${state.wave.totalWaves}",
                    color = Color(0xFFB8DAD2),
                    fontSize = 11.sp,
                )
            }
            HudStat(label = "Lives", value = state.lives.toString())
            HudStat(label = "Gold", value = state.gold.toString())
            HudStat(label = "Score", value = state.score.toString())
            if (state.wave.canStart && state.status == GameStatus.Running) {
                FilledTonalButton(
                    onClick = onStartNextWave,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(if (state.wave.phase == WavePhase.Ready) "Start" else "Next")
                }
            }
            FilledTonalButton(
                onClick = onPause,
                shape = RoundedCornerShape(8.dp),
                enabled = state.status == GameStatus.Running,
            ) {
                Text("Pause")
            }
        }
    }
}

@Composable
private fun HudStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = label,
            color = Color(0xFFB8DAD2),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun GameCanvas(
    state: GameState,
    showGrid: Boolean,
    modifier: Modifier = Modifier,
    onCellTapped: (GridCell) -> Unit,
) {
    val renderer = remember(state.map) { IsoRenderer(state.map) }
    Canvas(
        modifier = modifier.pointerInput(state.status) {
            detectTapGestures { tap ->
                if (state.status == GameStatus.Running) {
                    renderer.cellForOffset(
                        offset = tap,
                        canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                    )?.let(onCellTapped)
                }
            }
        },
    ) {
        renderer.draw(this, state, showGrid)
    }
}

@Composable
private fun BottomBar(
    state: GameState,
    onSelectTowerType: (TowerType) -> Unit,
    onUpgradeTower: (Int) -> Unit,
    onSellTower: (Int) -> Unit,
    onSetTowerTargetingMode: (Int, TargetingMode) -> Unit,
    onActivateAbility: (AbilityType) -> Unit,
) {
    Surface(
        color = Color(0xEE102522),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TowerTypeSelector(
                selectedType = state.selectedTowerType,
                gold = state.gold,
                onSelectTowerType = onSelectTowerType,
            )
            SelectedTowerPanel(
                state = state,
                onUpgradeTower = onUpgradeTower,
                onSellTower = onSellTower,
                onSetTowerTargetingMode = onSetTowerTargetingMode,
            )
            AbilityBar(
                state = state,
                onActivateAbility = onActivateAbility,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Selected ${state.selectedTowerType.shortLabel} ${state.selectedTowerType.baseCost}g",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Best ${state.bestScore}",
                    color = Color(0xFFC7EDE3),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.placementMessage,
                color = if (state.placementAccepted) Color(0xFFC7EDE3) else Color(0xFFFF9AAF),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = state.wave.statusText,
                color = if (state.wave.isBossWave) Color(0xFFFFD166) else Color(0xFF8CCFC3),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (state.wave.nextWavePreview.isNotBlank() && state.wave.canStart) {
                Text(
                    text = "Next: ${state.wave.nextWavePreview}",
                    color = Color(0xFFC7EDE3),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (state.wave.nextWaveInSeconds > 0f) {
                Text(
                    text = "Next wave in ${state.wave.nextWaveInSeconds.toInt() + 1}s",
                    color = Color(0xFFF6C55D),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = "Enemies remaining ${state.wave.enemiesRemaining}",
                color = Color(0xFF8CCFC3),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun TowerTypeSelector(
    selectedType: TowerType,
    gold: Int,
    onSelectTowerType: (TowerType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TowerType.entries.forEach { type ->
            val selected = type == selectedType
            FilledTonalButton(
                onClick = { onSelectTowerType(type) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selected) type.primaryColor else Color(0xFF1B3632),
                    contentColor = Color.White,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = type.shortLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = "${type.baseCost}g",
                        fontSize = 11.sp,
                        color = if (gold >= type.baseCost) Color(0xFFEAF7F2) else Color(0xFFFF9AAF),
                    )
                }
            }
        }
    }
}

@Composable
private fun AbilityBar(
    state: GameState,
    onActivateAbility: (AbilityType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AbilityType.entries.forEach { ability ->
            val cooldown = state.abilities.cooldownFor(ability)
            val needsEnemy = ability != AbilityType.EmergencyGold
            val enabled = state.status == GameStatus.Running &&
                state.abilities.canUse(ability) &&
                (!needsEnemy || state.enemies.isNotEmpty())
            FilledTonalButton(
                onClick = { onActivateAbility(ability) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ability.color.copy(alpha = if (enabled) 0.82f else 0.32f),
                    contentColor = Color.White,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ability.shortLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = when {
                            ability == AbilityType.EmergencyGold -> "${state.abilities.emergencyGoldUsesRemaining} uses"
                            cooldown > 0f -> "${cooldown.toInt() + 1}s"
                            else -> "Ready"
                        },
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedTowerPanel(
    state: GameState,
    onUpgradeTower: (Int) -> Unit,
    onSellTower: (Int) -> Unit,
    onSetTowerTargetingMode: (Int, TargetingMode) -> Unit,
) {
    val tower = state.selectedTowerId?.let { towerId ->
        state.towers.firstOrNull { it.id == towerId }
    } ?: return

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD183732),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "${tower.type.shortLabel} Lv ${tower.level}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "DMG ${tower.damage.toInt()}  RNG ${oneDecimal(tower.range)}  RATE ${oneDecimal(1f / tower.fireInterval)}/s",
                        color = Color(0xFFC7EDE3),
                        fontSize = 12.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Upgrade ${tower.upgradeCost}g",
                        color = if (state.gold >= tower.upgradeCost) Color(0xFFF6C55D) else Color(0xFFFF9AAF),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "Sell ${tower.sellRefund()}g",
                        color = Color(0xFFC7EDE3),
                        fontSize = 12.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Targeting ${tower.targetingMode.title}",
                color = Color(0xFFF6C55D),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TargetingMode.entries.forEach { mode ->
                    val selected = tower.targetingMode == mode
                    OutlinedButton(
                        onClick = { onSetTowerTargetingMode(tower.id, mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) tower.type.primaryColor.copy(alpha = 0.7f) else Color.Transparent,
                            contentColor = if (selected) Color.White else Color(0xFFC7EDE3),
                        ),
                        contentPadding = ButtonDefaults.ContentPadding,
                    ) {
                        Text(
                            text = mode.title.take(3),
                            fontSize = 10.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onUpgradeTower(tower.id) },
                    enabled = state.gold >= tower.upgradeCost,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Upgrade")
                }
                OutlinedButton(
                    onClick = { onSellTower(tower.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Sell")
                }
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit,
) {
    OverlayShell {
        Text(
            text = "Paused",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onResume,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Resume")
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Restart")
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onBackToMenu,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Back to Menu")
        }
    }
}

@Composable
private fun TerminalOverlay(
    title: String,
    subtitle: String,
    primaryText: String,
    onPrimary: () -> Unit,
    onBackToMenu: () -> Unit,
) {
    OverlayShell {
        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            color = Color(0xFFF6C55D),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
        )
        Button(
            onClick = onPrimary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(primaryText)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onBackToMenu,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Back to Menu")
        }
    }
}

@Composable
private fun OverlayShell(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF102522),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

private fun oneDecimal(value: Float): String {
    return (kotlin.math.round(value * 10f) / 10f).toString()
}
