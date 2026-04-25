package com.nicolaielgame.game.model

enum class GameStatus {
    Running,
    Paused,
    GameOver,
    Victory,
}

data class WaveSnapshot(
    val currentWave: Int,
    val totalWaves: Int,
    val enemiesLeftToSpawn: Int,
    val aliveEnemies: Int,
    val nextWaveInSeconds: Float,
    val phase: WavePhase = WavePhase.Ready,
    val awaitingNextWave: Boolean = false,
    val enemiesRemaining: Int = enemiesLeftToSpawn + aliveEnemies,
    val isBossWave: Boolean = false,
    val nextWavePreview: String = "",
    val statusText: String = "Wave Ready",
    val wavesCompleted: Int = 0,
) {
    val canStart: Boolean
        get() = phase == WavePhase.Ready || phase == WavePhase.Cleared
}

data class GameState(
    val map: GameMap = GameMap.default(),
    val level: LevelDefinition = LevelCatalog.firstLevel,
    val difficulty: DifficultyMode = DifficultyMode.Normal,
    val status: GameStatus = GameStatus.Running,
    val towers: List<Tower> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val hitEffects: List<HitEffect> = emptyList(),
    val abilityEffects: List<AbilityEffect> = emptyList(),
    val abilities: AbilityState = AbilityState(),
    val pathPreview: List<GridCell> = emptyList(),
    val selectedCell: GridCell? = null,
    val selectedTowerType: TowerType = TowerType.Basic,
    val selectedTowerId: Int? = null,
    val rangePreview: RangePreview? = null,
    val placementMessage: String = "Tap a buildable tile to place a tower.",
    val placementAccepted: Boolean = true,
    val lives: Int = 20,
    val gold: Int = 120,
    val score: Int = 0,
    val bestScore: Int = 0,
    val towersPlacedByType: Map<TowerType, Int> = emptyMap(),
    val bossesDefeated: Int = 0,
    val runStats: RunStats = RunStats(),
    val shakeTimeRemaining: Float = 0f,
    val shakeIntensity: Float = 0f,
    val waveBanner: String = "",
    val waveBannerTimeRemaining: Float = 0f,
    val wave: WaveSnapshot = WaveSnapshot(
        currentWave = 1,
        totalWaves = 5,
        enemiesLeftToSpawn = 0,
        aliveEnemies = 0,
        nextWaveInSeconds = 0f,
        phase = WavePhase.Ready,
    ),
) {
    val isTerminal: Boolean
        get() = status == GameStatus.GameOver || status == GameStatus.Victory
}
