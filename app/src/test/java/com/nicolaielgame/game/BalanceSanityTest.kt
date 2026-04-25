package com.nicolaielgame.game

import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.game.model.TowerType
import com.nicolaielgame.game.pathfinding.PathFinder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceSanityTest {
    @Test
    fun allLevelsHaveValidPathsAndEnoughOpeningGold() {
        LevelCatalog.levels.forEach { level ->
            assertNotNull("Level ${level.id} should have an open route", PathFinder(level.map).findPath(emptySet()))

            DifficultyMode.entries.forEach { difficulty ->
                val startingGold = difficulty.applyStartingGold(level.startingGold)
                assertTrue(
                    "Level ${level.id} ${difficulty.title} should afford at least two basic towers",
                    startingGold >= TowerType.Basic.baseCost * 2,
                )
            }
        }
    }

    @Test
    fun wavesScaleUpAcrossCampaign() {
        val firstLevelEnemies = LevelCatalog.find(1).waves.sumOf { it.totalEnemies }
        val finalLevelEnemies = LevelCatalog.find(7).waves.sumOf { it.totalEnemies }

        assertTrue(finalLevelEnemies > firstLevelEnemies)
        assertTrue(LevelCatalog.find(7).waves.any { it.isBossWave })
    }

    @Test
    fun lateCampaignUsesShieldedSwarmAndBossPressure() {
        val lateTypes = (6..7)
            .flatMap { levelId -> LevelCatalog.find(levelId).waves }
            .flatMap { wave -> wave.groups.map { it.type } }
            .toSet()

        assertTrue(EnemyType.Shielded in lateTypes)
        assertTrue(EnemyType.Swarm in lateTypes)
        assertTrue(lateTypes.any { it.isBoss })
    }

    @Test
    fun allDifficultiesKeepReasonableLivesThroughLevelSeven() {
        LevelCatalog.levels.forEach { level ->
            DifficultyMode.entries.forEach { difficulty ->
                assertTrue(
                    "Level ${level.id} ${difficulty.title} should leave at least eight lives",
                    difficulty.applyStartingLives(level.startingLives) >= 8,
                )
            }
        }
    }
}
