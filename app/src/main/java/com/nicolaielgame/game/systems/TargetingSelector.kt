package com.nicolaielgame.game.systems

import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.TargetingMode
import com.nicolaielgame.game.model.Tower
import com.nicolaielgame.game.model.gridDistance

object TargetingSelector {
    fun selectTarget(tower: Tower, enemies: List<Enemy>): Enemy? {
        val inRange = enemies.filter { enemy ->
            gridDistance(enemy.row, enemy.col, tower.cell) <= tower.range
        }

        return when (tower.targetingMode) {
            TargetingMode.First -> inRange.maxWithOrNull(
                compareBy<Enemy> { it.pathIndex }.thenByDescending { it.health },
            )

            TargetingMode.Last -> inRange.minWithOrNull(
                compareBy<Enemy> { it.pathIndex }.thenBy { it.health },
            )

            TargetingMode.Strongest -> inRange.maxWithOrNull(
                compareBy<Enemy> { it.health }.thenBy { it.pathIndex },
            )

            TargetingMode.Weakest -> inRange.minWithOrNull(
                compareBy<Enemy> { it.health }.thenByDescending { it.pathIndex },
            )

            TargetingMode.Closest -> inRange.minByOrNull { enemy ->
                gridDistance(enemy.row, enemy.col, tower.cell)
            }
        }
    }
}
