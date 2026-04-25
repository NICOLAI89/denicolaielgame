package com.nicolaielgame.game.model

data class GameRunResult(
    val levelId: Int,
    val score: Int,
    val won: Boolean,
    val livesRemaining: Int,
    val startingLives: Int,
    val difficulty: DifficultyMode,
    val bossesDefeated: Int,
    val towersPlacedByType: Map<TowerType, Int>,
    val runStats: RunStats = RunStats(),
)
