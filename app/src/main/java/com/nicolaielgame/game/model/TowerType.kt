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
        baseCost = 32,
        baseDamage = 28f,
        baseRange = 2.65f,
        baseFireInterval = 0.68f,
        projectileSpeed = 6.8f,
        primaryColor = Color(0xFF2858D8),
        accentColor = Color(0xFF6AD8FF),
    ),
    Sniper(
        title = "Sniper Tower",
        shortLabel = "Sniper",
        baseCost = 70,
        baseDamage = 82f,
        baseRange = 4.25f,
        baseFireInterval = 1.8f,
        projectileSpeed = 9.4f,
        primaryColor = Color(0xFF6B3FD7),
        accentColor = Color(0xFFFFD166),
    ),
    Frost(
        title = "Frost Tower",
        shortLabel = "Frost",
        baseCost = 52,
        baseDamage = 14f,
        baseRange = 2.95f,
        baseFireInterval = 0.95f,
        projectileSpeed = 6.2f,
        slowMultiplier = 0.56f,
        slowDuration = 1.65f,
        primaryColor = Color(0xFF167A8F),
        accentColor = Color(0xFFA9F1FF),
    );

    fun statsForLevel(level: Int): TowerStats {
        val levelIndex = (level - 1).coerceAtLeast(0)
        return TowerStats(
            damage = baseDamage * (1f + levelIndex * 0.34f),
            range = baseRange + levelIndex * 0.22f,
            fireInterval = (baseFireInterval * (1f - levelIndex * 0.08f)).coerceAtLeast(0.32f),
            projectileSpeed = projectileSpeed + levelIndex * 0.45f,
            slowMultiplier = slowMultiplier,
            slowDuration = slowDuration + if (slowDuration > 0f) levelIndex * 0.22f else 0f,
        )
    }

    fun upgradeCostForLevel(level: Int): Int {
        return (baseCost * (0.65f + level * 0.52f)).roundToInt()
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

