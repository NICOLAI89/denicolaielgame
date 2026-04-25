package com.nicolaielgame.game

import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.TargetingMode
import com.nicolaielgame.game.model.Tower
import com.nicolaielgame.game.model.TowerType
import com.nicolaielgame.game.systems.TargetingSelector
import org.junit.Assert.assertEquals
import org.junit.Test

class TargetingSelectorTest {
    @Test
    fun selectTarget_honorsTowerTargetingMode() {
        val enemies = listOf(
            enemy(id = 1, row = 3.4f, col = 2f, pathIndex = 1, health = 10f),
            enemy(id = 2, row = 3f, col = 2f, pathIndex = 5, health = 30f),
            enemy(id = 3, row = 2.5f, col = 2f, pathIndex = 3, health = 100f),
            enemy(id = 4, row = 0.5f, col = 0f, pathIndex = 2, health = 50f),
        )

        assertEquals(2, TargetingSelector.selectTarget(tower(TargetingMode.First), enemies)?.id)
        assertEquals(1, TargetingSelector.selectTarget(tower(TargetingMode.Last), enemies)?.id)
        assertEquals(3, TargetingSelector.selectTarget(tower(TargetingMode.Strongest), enemies)?.id)
        assertEquals(1, TargetingSelector.selectTarget(tower(TargetingMode.Weakest), enemies)?.id)
        assertEquals(4, TargetingSelector.selectTarget(tower(TargetingMode.Closest), enemies)?.id)
    }

    private fun tower(mode: TargetingMode): Tower {
        return Tower(
            id = 1,
            cell = GridCell(0, 0),
            type = TowerType.Sniper,
            targetingMode = mode,
        )
    }

    private fun enemy(
        id: Int,
        row: Float,
        col: Float,
        pathIndex: Int,
        health: Float,
    ): Enemy {
        return Enemy(
            id = id,
            type = EnemyType.Normal,
            row = row,
            col = col,
            path = listOf(GridCell(0, 0), GridCell(1, 1)),
            pathIndex = pathIndex,
            health = health,
            maxHealth = 100f,
            speed = 1f,
            reward = 1,
        )
    }
}
