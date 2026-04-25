package com.nicolaielgame.game.model

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

enum class TowerType(
    val title: String,
    val shortLabel: String,
    val baseCost: Int,
    val baseDamage: Float,
    val baseRange: Float,
    val baseFireInterval: Float,
    val projectileSpeed: Float,
    val slowMultiplier: Float = 1f,
    val slowDuration: Float = 0f,
    val primaryColor: Color,
    val accentColor: Color,
) {
    Basic(
        title = "Basic Tower",
        shortLabel = "Basic",
        baseCost = 30,
        baseDamage = 32f,
        baseRange = 2.75f,
        baseFireInterval = 0.62f,
        projectileSpeed = 7.1f,
        primaryColor = Color(0xFF2858D8),
        accentColor = Color(0xFF6AD8FF),
    ),
    Sniper(
        title = "Sniper Tower",
        shortLabel = "Sniper",
        baseCost = 68,
        baseDamage = 96f,
        baseRange = 4.45f,
        baseFireInterval = 1.74f,
        projectileSpeed = 10.2f,
        primaryColor = Color(0xFF6B3FD7),
        accentColor = Color(0xFFFFD166),
    ),
    Frost(
        title = "Frost Tower",
        shortLabel = "Frost",
        baseCost = 50,
        baseDamage = 17f,
        baseRange = 3.05f,
        baseFireInterval = 0.92f,
        projectileSpeed = 6.4f,
        slowMultiplier = 0.52f,
        slowDuration = 2.0f,
        primaryColor = Color(0xFF167A8F),
        accentColor = Color(0xFFA9F1FF),
    );

    fun statsForLevel(level: Int): TowerStats {
        val levelIndex = (level - 1).coerceAtLeast(0)
        return TowerStats(
            damage = baseDamage * (1f + levelIndex * 0.34f),
            range = baseRange + levelIndex * 0.24f,
            fireInterval = (baseFireInterval * (1f - levelIndex * 0.085f)).coerceAtLeast(0.3f),
            projectileSpeed = projectileSpeed + levelIndex * 0.45f,
            slowMultiplier = slowMultiplier,
            slowDuration = slowDuration + if (slowDuration > 0f) levelIndex * 0.22f else 0f,
        )
    }

    fun upgradeCostForLevel(level: Int): Int {
        return (baseCost * (0.62f + level * 0.5f)).roundToInt()
    }
}

data class TowerStats(
    val damage: Float,
    val range: Float,
    val fireInterval: Float,
    val projectileSpeed: Float,
    val slowMultiplier: Float,
    val slowDuration: Float,
)

