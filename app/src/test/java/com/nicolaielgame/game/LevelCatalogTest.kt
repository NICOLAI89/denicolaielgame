package com.nicolaielgame.game

import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.game.pathfinding.PathFinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCatalogTest {
    @Test
    fun catalogContainsSevenPlayableLevels() {
        assertEquals(7, LevelCatalog.levels.size)

        LevelCatalog.levels.forEach { level ->
            assertNotNull(
                "Level ${level.id} should have a valid initial path",
                PathFinder(level.map).findPath(emptySet()),
            )
        }
    }

    @Test
    fun laterLevelsIntroduceNewEnemyFamilies() {
        val levelSixTypes = LevelCatalog.find(6).waves.flatMap { wave -> wave.groups.map { it.type } }.toSet()
        val levelSevenTypes = LevelCatalog.find(7).waves.flatMap { wave -> wave.groups.map { it.type } }.toSet()

        assertTrue(EnemyType.Shielded in levelSixTypes)
        assertTrue(EnemyType.Swarm in levelSixTypes)
        assertTrue(EnemyType.Shielded in levelSevenTypes)
        assertTrue(EnemyType.Swarm in levelSevenTypes)
    }
}
