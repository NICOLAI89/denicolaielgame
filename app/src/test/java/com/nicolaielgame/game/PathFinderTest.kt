package com.nicolaielgame.game

import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.pathfinding.PathFinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PathFinderTest {
    @Test
    fun findPath_returnsValidSpawnToBasePath() {
        val map = LevelCatalog.firstLevel.map
        val path = PathFinder(map).findPath(emptySet())

        assertNotNull(path)
        val cells = requireNotNull(path)
        assertEquals(map.spawn, cells.first())
        assertEquals(map.base, cells.last())
        assertTrue(cells.zipWithNext().all { (from, to) -> from.manhattanDistanceTo(to) == 1 })
    }

    @Test
    fun findPath_returnsNullForOutOfBoundsStartWithoutThrowing() {
        val map = LevelCatalog.firstLevel.map
        val path = PathFinder(map).findPath(
            start = GridCell(-1, 0),
            goal = map.base,
            blockedCells = emptySet(),
        )

        assertNull(path)
    }
}
