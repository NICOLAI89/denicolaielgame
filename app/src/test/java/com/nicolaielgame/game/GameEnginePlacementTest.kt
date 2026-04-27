package com.nicolaielgame.game

import com.nicolaielgame.game.engine.GameEngine
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.GameMap
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.LevelDefinition
import com.nicolaielgame.game.model.TowerType
import com.nicolaielgame.game.systems.WaveDefinition
import com.nicolaielgame.game.systems.WaveEnemyGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class GameEnginePlacementTest {
    @Test
    fun placeTower_rejectsPathBlockingPlacement() {
        val level = testLevel(
            map = GameMap(
                rows = 1,
                cols = 3,
                spawn = GridCell(0, 0),
                base = GridCell(0, 2),
                buildLockedCells = setOf(GridCell(0, 0), GridCell(0, 2)),
                scenicPath = setOf(GridCell(0, 0), GridCell(0, 1), GridCell(0, 2)),
            ),
            startingGold = 200,
        )
        val engine = GameEngine(level)

        engine.placeTower(GridCell(0, 1))

        val state = engine.state.value
        assertTrueNoTowersOrSpend(stateGold = state.gold, towerCount = state.towers.size, expectedGold = 200)
        assertFalse(state.placementAccepted)
    }

    @Test
    fun placeTower_rejectsInsufficientGold() {
        val level = testLevel(
            map = GameMap(
                rows = 3,
                cols = 3,
                spawn = GridCell(1, 0),
                base = GridCell(1, 2),
                buildLockedCells = setOf(GridCell(1, 0), GridCell(1, 2)),
                scenicPath = setOf(GridCell(1, 0), GridCell(1, 1), GridCell(1, 2)),
            ),
            startingGold = TowerType.Sniper.baseCost - 1,
        )
        val engine = GameEngine(level)

        engine.selectTowerType(TowerType.Sniper)
        engine.placeTower(GridCell(0, 1))

        val state = engine.state.value
        assertTrueNoTowersOrSpend(
            stateGold = state.gold,
            towerCount = state.towers.size,
            expectedGold = TowerType.Sniper.baseCost - 1,
        )
        assertFalse(state.placementAccepted)
    }

    @Test
    fun selectingActiveTowerTypeAgainDisablesPlacementMode() {
        val level = openBuildLevel(startingGold = 200)
        val engine = GameEngine(level)

        engine.selectTowerType(TowerType.Basic)
        val goldBeforeTap = engine.state.value.gold
        engine.handleCellTap(GridCell(0, 1))

        val state = engine.state.value
        assertNull(state.selectedTowerType)
        assertEquals(goldBeforeTap, state.gold)
        assertEquals(0, state.towers.size)
        assertFalse(state.placementAccepted)
    }

    @Test
    fun tappingExistingTowerInspectsInsteadOfPlacingWhilePlacementModeIsActive() {
        val level = openBuildLevel(startingGold = 400)
        val engine = GameEngine(level)

        engine.placeTower(GridCell(0, 1))
        val tower = engine.state.value.towers.single()
        engine.selectTowerType(TowerType.Sniper)
        engine.handleCellTap(tower.cell)

        val state = engine.state.value
        assertEquals(1, state.towers.size)
        assertEquals(tower.id, state.selectedTowerId)
        assertNull(state.selectedTowerType)
    }

    private fun assertTrueNoTowersOrSpend(stateGold: Int, towerCount: Int, expectedGold: Int) {
        assertEquals(expectedGold, stateGold)
        assertEquals(0, towerCount)
    }

    private fun testLevel(map: GameMap, startingGold: Int): LevelDefinition {
        return LevelDefinition(
            id = 99,
            title = "Test",
            description = "Test map",
            map = map,
            startingGold = startingGold,
            startingLives = 5,
            waves = listOf(
                WaveDefinition(
                    groups = listOf(WaveEnemyGroup(EnemyType.Normal, 1)),
                    spawnInterval = 1f,
                ),
            ),
        )
    }

    private fun openBuildLevel(startingGold: Int): LevelDefinition {
        val spawn = GridCell(1, 0)
        val base = GridCell(1, 2)
        return testLevel(
            map = GameMap(
                rows = 3,
                cols = 3,
                spawn = spawn,
                base = base,
                buildLockedCells = setOf(spawn, base),
                scenicPath = setOf(spawn, GridCell(1, 1), base),
            ),
            startingGold = startingGold,
        )
    }
}
