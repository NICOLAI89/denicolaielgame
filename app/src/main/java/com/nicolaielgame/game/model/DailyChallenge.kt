package com.nicolaielgame.game.model

import com.nicolaielgame.game.systems.WaveDefinition
import com.nicolaielgame.game.systems.WaveEnemyGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class DailyModifier(val title: String) {
    ShieldedVanguard("Shielded vanguard"),
    SwarmSurge("Swarm surge"),
    LeanEconomy("Lean economy"),
    BossPressure("Boss pressure"),
}

data class DailyChallenge(
    val dateKey: String,
    val seed: Int,
    val baseLevelId: Int,
    val difficulty: DifficultyMode,
    val modifiers: List<DailyModifier>,
    val level: LevelDefinition,
)

object DailyChallengeRules {
    fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun generate(
        dateKey: String,
        levels: List<LevelDefinition> = LevelCatalog.levels,
    ): DailyChallenge {
        val availableLevels = levels.ifEmpty { listOf(LevelCatalog.firstLevel) }
        val seed = stableSeed(dateKey)
        val baseLevel = availableLevels[seed % availableLevels.size]
        val difficulty = DifficultyMode.entries[(seed / 7) % DifficultyMode.entries.size]
        val modifiers = modifiersFor(seed)
        val level = buildDailyLevel(dateKey, seed, baseLevel, modifiers)

        return DailyChallenge(
            dateKey = dateKey,
            seed = seed,
            baseLevelId = baseLevel.id,
            difficulty = difficulty,
            modifiers = modifiers,
            level = level,
        )
    }

    fun stableSeed(dateKey: String): Int {
        val folded = dateKey.fold(17) { acc, char -> acc * 31 + char.code }
        return abs(folded).coerceAtLeast(1)
    }

    private fun modifiersFor(seed: Int): List<DailyModifier> {
        val pool = DailyModifier.entries
        val first = pool[seed % pool.size]
        val second = pool[(seed / pool.size + 1) % pool.size]
        return if (first == second) listOf(first) else listOf(first, second)
    }

    private fun buildDailyLevel(
        dateKey: String,
        seed: Int,
        baseLevel: LevelDefinition,
        modifiers: List<DailyModifier>,
    ): LevelDefinition {
        val swarmBonus = if (DailyModifier.SwarmSurge in modifiers) 3 else 0
        val shieldBonus = if (DailyModifier.ShieldedVanguard in modifiers) 2 else 0
        val bossPressure = DailyModifier.BossPressure in modifiers
        val leanEconomy = DailyModifier.LeanEconomy in modifiers
        val seedBump = seed % 4

        return baseLevel.copy(
            id = 100 + baseLevel.id,
            title = "Daily Circuit",
            description = "Seeded challenge for $dateKey based on ${baseLevel.title}.",
            startingGold = (baseLevel.startingGold + 35 - if (leanEconomy) 32 else 0).coerceAtLeast(130),
            startingLives = (baseLevel.startingLives - if (bossPressure) 1 else 0).coerceAtLeast(8),
            waves = listOf(
                WaveDefinition(
                    groups = listOf(
                        WaveEnemyGroup(EnemyType.Normal, 8 + seedBump),
                        WaveEnemyGroup(EnemyType.Swarm, 8 + swarmBonus),
                    ),
                    spawnInterval = 0.54f,
                ),
                WaveDefinition(
                    groups = listOf(
                        WaveEnemyGroup(EnemyType.Fast, 8 + seedBump),
                        WaveEnemyGroup(EnemyType.Shielded, 3 + shieldBonus),
                    ),
                    spawnInterval = 0.5f,
                ),
                WaveDefinition(
                    groups = listOf(
                        WaveEnemyGroup(EnemyType.Swarm, 14 + swarmBonus + seedBump),
                        WaveEnemyGroup(EnemyType.Tank, 4),
                    ),
                    spawnInterval = 0.42f,
                ),
                WaveDefinition(
                    groups = listOf(
                        WaveEnemyGroup(EnemyType.Shielded, 6 + shieldBonus),
                        WaveEnemyGroup(EnemyType.Fast, 10),
                        WaveEnemyGroup(EnemyType.Boss, if (bossPressure) 2 else 1),
                    ),
                    spawnInterval = 0.45f,
                ),
                WaveDefinition(
                    groups = listOf(
                        WaveEnemyGroup(EnemyType.Swarm, 18 + swarmBonus),
                        WaveEnemyGroup(EnemyType.Shielded, 7 + shieldBonus),
                        WaveEnemyGroup(EnemyType.Juggernaut, 1),
                    ),
                    spawnInterval = 0.38f,
                ),
            ),
        )
    }
}
