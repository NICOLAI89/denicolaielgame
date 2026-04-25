package com.nicolaielgame.game.model

data class Tower(
    val id: Int,
    val cell: GridCell,
    val type: TowerType = TowerType.Basic,
    val level: Int = 1,
    val totalInvested: Int = type.baseCost,
    val cooldown: Float = 0f,
) {
    val stats: TowerStats
        get() = type.statsForLevel(level)

    val damage: Float
        get() = stats.damage

    val range: Float
        get() = stats.range

    val fireInterval: Float
        get() = stats.fireInterval

    val upgradeCost: Int
        get() = type.upgradeCostForLevel(level)

    fun upgraded(): Tower {
        return copy(
            level = level + 1,
            totalInvested = totalInvested + upgradeCost,
        )
    }

    fun sellRefund(): Int {
        return (totalInvested * 0.7f).toInt().coerceAtLeast(1)
    }
}
