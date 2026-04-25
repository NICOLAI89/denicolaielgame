package com.nicolaielgame.game.model

data class Projectile(
    val id: Int,
    val towerType: TowerType = TowerType.Basic,
    val row: Float,
    val col: Float,
    val targetEnemyId: Int,
    val damage: Float,
    val slowMultiplier: Float = 1f,
    val slowDuration: Float = 0f,
    val speed: Float = 6.8f,
)
