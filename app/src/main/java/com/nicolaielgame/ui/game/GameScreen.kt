package com.nicolaielgame.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nicolaielgame.data.GameSettings
import com.nicolaielgame.game.assets.GameVisualAssets
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
import com.nicolaielgame.game.systems.SoundPlayer

@Composable
fun GameScreen(
    level: LevelDefinition,
    difficulty: DifficultyMode,
    bestScore: Int,
    settings: GameSettings,
    runBadge: String? = null,
    soundPlayer: SoundPlayer,
    onSettingsChanged: (GameSettings) -> Unit,
    onBackToMenu: () -> Unit,
    onRunFinalized: suspend (GameRunResult) -> Unit,
) {
    val engine = remember(level.id, difficulty) { GameEngine(level, difficulty, soundPlayer) }
    val state by engine.state.collectAsState()
    var savedTerminalStatus by remember { mutableStateOf<GameStatus?>(null) }
    var performanceStats by remember { mutableStateOf(PerformanceStats()) }

    LaunchedEffect(bestScore) {
        engine.setBestScore(bestScore)
    }

    LaunchedEffect(settings.autoStartWavesEnabled) {
        engine.setAutoStartWavesEnabled(settings.autoStartWavesEnabled)
    }

    LaunchedEffect(Unit) {
        var lastFrame = withFrameNanos { it }
        var sampleSeconds = 0f
        var sampleFrames = 0
        var sampleUpdateNanos = 0L
        while (true) {
            val frame = withFrameNanos { it }
            val deltaSeconds = (frame - lastFrame) / 1_000_000_000f
            val updateStarted = System.nanoTime()
            engine.tick(deltaSeconds)
            sampleUpdateNanos += System.nanoTime() - updateStarted
            sampleSeconds += deltaSeconds
            sampleFrames++
            if (sampleSeconds >= 0.5f && sampleFrames > 0) {
                performanceStats = PerformanceStats(
                    fps = (sampleFrames / sampleSeconds).toInt(),
                    averageFrameMs = sampleSeconds * 1000f / sampleFrames,
                    averageUpdateMs = sampleUpdateNanos / 1_000_000f / sampleFrames,
                )
                sampleSeconds = 0f
                sampleFrames = 0
                sampleUpdateNanos = 0L
            }
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
                    runStats = state.runStats,
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
        settings = settings,
        runBadge = runBadge,
        onSettingsChanged = onSettingsChanged,
        performanceStats = performanceStats,
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
    settings: GameSettings,
    runBadge: String?,
    onSettingsChanged: (GameSettings) -> Unit,
    performanceStats: PerformanceStats,
) {
    var showPauseSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071413)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HudBar(
                state = state,
                autoStartWavesEnabled = settings.autoStartWavesEnabled,
                runBadge = runBadge,
                onPause = onPause,
                onStartNextWave = onStartNextWave,
            )
            GameCanvas(
                state = state,
                showGrid = settings.showGrid,
                screenShakeEnabled = settings.screenShakeEnabled,
                damageNumbersEnabled = settings.damageNumbersEnabled,
                highContrastMode = settings.highContrastMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onCellTapped = onCellTapped,
            )
            BottomBar(
                state = state,
                autoStartWavesEnabled = settings.autoStartWavesEnabled,
                onSelectTowerType = onSelectTowerType,
                onUpgradeTower = onUpgradeTower,
                onSellTower = onSellTower,
                onSetTowerTargetingMode = onSetTowerTargetingMode,
                onActivateAbility = onActivateAbility,
            )
        }

        when (state.status) {
            GameStatus.Paused -> {
                if (showPauseSettings) {
                    PauseSettingsOverlay(
                        settings = settings,
                        onSettingsChanged = onSettingsChanged,
                        onBack = { showPauseSettings = false },
                    )
                } else {
                    PauseOverlay(
                        settings = settings,
                        onSettingsChanged = onSettingsChanged,
                        onOpenSettings = { showPauseSettings = true },
                        onResume = {
                            showPauseSettings = false
                            onResume()
                        },
                        onRestart = {
                            showPauseSettings = false
                            onRestart()
                        },
                        onBackToMenu = onBackToMenu,
                    )
                }
            }

            GameStatus.GameOver -> TerminalOverlay(
                title = "Base Lost",
                subtitle = "Score ${state.score}  Gold ${state.gold}  Wave ${state.wave.currentWave}/${state.wave.totalWaves}",
                summaryLines = runSummaryLines(state),
                titleColor = Color(0xFFFF7A90),
                primaryText = "Restart",
                onPrimary = onRestart,
                onBackToMenu = onBackToMenu,
            )

            GameStatus.Victory -> TerminalOverlay(
                title = "Victory",
                subtitle = "Score ${state.score}  Lives ${state.lives}  Bosses ${state.bossesDefeated}",
                summaryLines = runSummaryLines(state),
                titleColor = Color(0xFF78F5C8),
                primaryText = "Play Again",
                onPrimary = onRestart,
                onBackToMenu = onBackToMenu,
            )

            GameStatus.Running -> Unit
        }

        if (state.waveBannerTimeRemaining > 0f && state.waveBanner.isNotBlank() && state.status == GameStatus.Running) {
            WaveBanner(state = state)
        }
        if (settings.fpsCounterEnabled) {
            PerformanceOverlay(performanceStats = performanceStats)
        }
    }
}

private data class PerformanceStats(
    val fps: Int = 0,
    val averageFrameMs: Float = 0f,
    val averageUpdateMs: Float = 0f,
)

@Composable
private fun PerformanceOverlay(performanceStats: PerformanceStats) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp, end = 10.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xDD050A12),
        ) {
            Text(
                text = "FPS ${performanceStats.fps}  Frame ${oneDecimal(performanceStats.averageFrameMs)}ms  Update ${oneDecimal(performanceStats.averageUpdateMs)}ms",
                color = Color(0xFFA9F1FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun HudBar(
    state: GameState,
    autoStartWavesEnabled: Boolean,
    runBadge: String?,
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
                    text = buildString {
                        append("Wave ${state.wave.currentWave}/${state.wave.totalWaves}")
                        if (autoStartWavesEnabled) append("  Auto")
                        if (!runBadge.isNullOrBlank()) append("  $runBadge")
                    },
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
private fun WaveBanner(state: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 76.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (state.wave.isBossWave) Color(0xEE4D1830) else Color(0xEE102522),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.waveBanner,
                    color = if (state.wave.isBossWave) Color(0xFFFFD166) else Color.White,
                    fontSize = if (state.wave.isBossWave) 24.sp else 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                if (state.wave.nextWavePreview.isNotBlank()) {
                    Text(
                        text = state.wave.nextWavePreview,
                        color = Color(0xFFC7EDE3),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCanvas(
    state: GameState,
    showGrid: Boolean,
    screenShakeEnabled: Boolean,
    damageNumbersEnabled: Boolean,
    highContrastMode: Boolean,
    modifier: Modifier = Modifier,
    onCellTapped: (GridCell) -> Unit,
) {
    val context = LocalContext.current
    val visualAssets = remember(context) { GameVisualAssets.load(context) }
    val rendererWithAssets = remember(state.map, visualAssets) { IsoRenderer(state.map, visualAssets) }
    Canvas(
        modifier = modifier.pointerInput(state.status) {
            detectTapGestures { tap ->
                if (state.status == GameStatus.Running) {
                    rendererWithAssets.cellForOffset(
                        offset = tap,
                        canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                    )?.let(onCellTapped)
                }
            }
        },
    ) {
        rendererWithAssets.draw(
            drawScope = this,
            state = state,
            showGrid = showGrid,
            screenShakeEnabled = screenShakeEnabled,
            showDamageNumbers = damageNumbersEnabled,
            highContrast = highContrastMode,
        )
    }
}

@Composable
private fun BottomBar(
    state: GameState,
    autoStartWavesEnabled: Boolean,
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
                    text = state.selectedTowerType?.let { type ->
                        "Selected ${type.shortLabel} ${type.baseCost}g"
                    } ?: "Placement off",
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
                    text = if (autoStartWavesEnabled) {
                        "Auto wave in ${state.wave.nextWaveInSeconds.toInt() + 1}s"
                    } else {
                        "Next wave in ${state.wave.nextWaveInSeconds.toInt() + 1}s"
                    },
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
    selectedType: TowerType?,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = when {
                            ability == AbilityType.EmergencyGold -> "${state.abilities.emergencyGoldUsesRemaining} uses"
                            cooldown > 0f -> "CD ${cooldown.toInt() + 1}s"
                            else -> "Ready"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    val readyFraction = (1f - cooldown / ability.cooldownSeconds).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Black.copy(alpha = 0.28f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(readyFraction)
                                .height(4.dp)
                                .background(if (enabled) Color.White else ability.color),
                        )
                    }
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
                text = "Targeting ${tower.targetingMode.shortLabel}: ${tower.targetingMode.title}",
                color = Color(0xFFF6C55D),
                fontSize = 14.sp,
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
                            text = mode.shortLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
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
    settings: GameSettings,
    onSettingsChanged: (GameSettings) -> Unit,
    onOpenSettings: () -> Unit,
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
        PauseToggleRow(
            label = "Sound",
            checked = settings.soundEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(soundEnabled = it)) },
        )
        PauseToggleRow(
            label = "Auto-start waves",
            checked = settings.autoStartWavesEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(autoStartWavesEnabled = it)) },
        )
        Spacer(modifier = Modifier.height(12.dp))
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
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Settings")
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
private fun PauseSettingsOverlay(
    settings: GameSettings,
    onSettingsChanged: (GameSettings) -> Unit,
    onBack: () -> Unit,
) {
    OverlayShell {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(14.dp))
        PauseToggleRow("Sound", settings.soundEnabled) { onSettingsChanged(settings.copy(soundEnabled = it)) }
        PauseToggleRow("Music", settings.musicEnabled) { onSettingsChanged(settings.copy(musicEnabled = it)) }
        PauseToggleRow("Auto-start waves", settings.autoStartWavesEnabled) {
            onSettingsChanged(settings.copy(autoStartWavesEnabled = it))
        }
        PauseToggleRow("Show grid", settings.showGrid) { onSettingsChanged(settings.copy(showGrid = it)) }
        PauseToggleRow("Screen shake", settings.screenShakeEnabled) {
            onSettingsChanged(settings.copy(screenShakeEnabled = it))
        }
        PauseToggleRow("Damage numbers", settings.damageNumbersEnabled) {
            onSettingsChanged(settings.copy(damageNumbersEnabled = it))
        }
        PauseToggleRow("High contrast", settings.highContrastMode) {
            onSettingsChanged(settings.copy(highContrastMode = it))
        }
        PauseToggleRow("FPS counter", settings.fpsCounterEnabled) {
            onSettingsChanged(settings.copy(fpsCounterEnabled = it))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Back to Pause")
        }
    }
}

@Composable
private fun PauseToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Color(0xFFEAF7F2),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TerminalOverlay(
    title: String,
    subtitle: String,
    summaryLines: List<String>,
    titleColor: Color,
    primaryText: String,
    onPrimary: () -> Unit,
    onBackToMenu: () -> Unit,
) {
    OverlayShell {
        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = titleColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            color = Color(0xFFF6C55D),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
        )
        summaryLines.forEach { line ->
            Text(
                text = line,
                color = Color(0xFFC7EDE3),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
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
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

private fun oneDecimal(value: Float): String {
    return (kotlin.math.round(value * 10f) / 10f).toString()
}

private fun runSummaryLines(state: GameState): List<String> {
    val stats = state.runStats
    return listOf(
        "Time ${stats.timeSeconds.toInt()}s  Waves ${stats.wavesCompleted}/${state.wave.totalWaves}",
        "Built ${stats.towersBuilt}  Upgraded ${stats.towersUpgraded}  Sold ${stats.towersSold}",
        "Abilities ${stats.totalAbilitiesUsed}  Kills ${stats.enemiesKilled}  Bosses ${stats.bossesKilled}",
    )
}
