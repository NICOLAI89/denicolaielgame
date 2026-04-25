package com.nicolaielgame.game

import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.game.pathfinding.PathFinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LevelCatalogTest {
    @Test
    fun catalogContainsFivePlayableLevels() {
        assertEquals(5, LevelCatalog.levels.size)

        LevelCatalog.levels.forEach { level ->
            assertNotNull(
                "Level ${level.id} should have a valid initial path",
                PathFinder(level.map).findPath(emptySet()),
            )
        }
    }
}
