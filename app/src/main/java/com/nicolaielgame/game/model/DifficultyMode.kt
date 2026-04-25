package com.nicolaielgame.game.model

import kotlin.math.roundToInt

enum class DifficultyMode(
    val title: String,
    val enemyHealthMultiplier: Float,
    val enemySpeedMultiplier: Float,
    val rewardMultiplier: Float,
    val scoreMultiplier: Float,
    val startingGoldMultiplier: Float,
    val livesBonus: Int,
) {
    Easy(
        title = "Easy",
        enemyHealthMultiplier = 0.82f,
        enemySpeedMultiplier = 0.9f,
        rewardMultiplier = 1.15f,
        scoreMultiplier = 0.85f,
        startingGoldMultiplier = 1.22f,
        livesBonus = 5,
    ),
    Normal(
        title = "Normal",
        enemyHealthMultiplier = 1f,
        enemySpeedMultiplier = 1f,
        rewardMultiplier = 1f,
        scoreMultiplier = 1f,
        startingGoldMultiplier = 1f,
        livesBonus = 0,
    ),
    Hard(
        title = "Hard",
        enemyHealthMultiplier = 1.28f,
        enemySpeedMultiplier = 1.12f,
        rewardMultiplier = 0.88f,
        scoreMultiplier = 1.35f,
        startingGoldMultiplier = 0.86f,
        livesBonus = -3,
    );

    fun applyStartingGold(baseGold: Int): Int {
        return (baseGold * startingGoldMultiplier).roundToInt().coerceAtLeast(40)
    }

    fun applyStartingLives(baseLives: Int): Int {
        return (baseLives + livesBonus).coerceAtLeast(1)
    }

    fun applyEnemyHealth(baseHealth: Float): Float {
        return baseHealth * enemyHealthMultiplier
    }

    fun applyEnemySpeed(baseSpeed: Float): Float {
        return baseSpeed * enemySpeedMultiplier
    }

    fun applyReward(baseReward: Int): Int {
        return (baseReward * rewardMultiplier).roundToInt().coerceAtLeast(1)
    }

    fun applyScore(baseScore: Int): Int {
        return (baseScore * scoreMultiplier).roundToInt().coerceAtLeast(1)
    }
}
