package com.nicolaielgame.game

import com.nicolaielgame.game.model.DifficultyMode
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
        val finalLevelEnemies = LevelCatalog.find(5).waves.sumOf { it.totalEnemies }

        assertTrue(finalLevelEnemies > firstLevelEnemies)
        assertTrue(LevelCatalog.find(5).waves.any { it.isBossWave })
    }
}
