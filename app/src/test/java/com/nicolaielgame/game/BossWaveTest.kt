package com.nicolaielgame.game

import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.LevelCatalog
import org.junit.Assert.assertTrue
import org.junit.Test

class BossWaveTest {
    @Test
    fun levelThreeContainsBossWaveDefinition() {
        val levelThree = LevelCatalog.find(3)

        assertTrue(levelThree.waves.any { wave -> wave.groups.any { it.type == EnemyType.Boss } })
        assertTrue(EnemyType.Boss.isBoss)
    }
}
