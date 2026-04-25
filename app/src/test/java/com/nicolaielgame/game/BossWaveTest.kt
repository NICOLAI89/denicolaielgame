package com.nicolaielgame.game

import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.LevelCatalog
import org.junit.Assert.assertTrue
import org.junit.Test

class BossWaveTest {
    @Test
    fun levelThreeContainsBossWaveDefinition() {
        val levelThree = LevelCatalog.find(3)

        assertTrue(levelThree.waves.any { wave -> wave.groups.any { it.type.isBoss } })
        assertTrue(levelThree.waves.any { wave -> wave.groups.any { it.type == EnemyType.Juggernaut } })
        assertTrue(EnemyType.Juggernaut.isBoss)
    }

    @Test
    fun laterLevelsIncludeRegeneratorBossMechanics() {
        val levelFive = LevelCatalog.find(5)

        assertTrue(levelFive.waves.any { wave -> wave.groups.any { it.type == EnemyType.Regenerator } })
        assertTrue(EnemyType.Regenerator.regenPerSecond > 0f)
        assertTrue(EnemyType.Juggernaut.slowVulnerability < 1f)
    }
}
