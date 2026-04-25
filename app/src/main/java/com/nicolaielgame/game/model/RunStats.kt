package com.nicolaielgame.game.model

data class RunStats(
    val levelId: Int = 1,
    val difficulty: DifficultyMode = DifficultyMode.Normal,
    val timeSeconds: Float = 0f,
    val wavesCompleted: Int = 0,
    val towersBuilt: Int = 0,
    val towersUpgraded: Int = 0,
    val towersSold: Int = 0,
    val abilitiesUsed: Map<AbilityType, Int> = emptyMap(),
    val enemiesKilled: Int = 0,
    val bossesKilled: Int = 0,
) {
    val totalAbilitiesUsed: Int
        get() = abilitiesUsed.values.sum()

    fun withAbilityUsed(type: AbilityType): RunStats {
        return copy(
            abilitiesUsed = abilitiesUsed + (type to ((abilitiesUsed[type] ?: 0) + 1)),
        )
    }

    fun withKill(enemyType: EnemyType): RunStats {
        return copy(
            enemiesKilled = enemiesKilled + 1,
            bossesKilled = bossesKilled + if (enemyType.isBoss) 1 else 0,
        )
    }
}
