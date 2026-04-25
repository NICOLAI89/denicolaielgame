package com.nicolaielgame.game

import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.systems.WaveDefinition
import com.nicolaielgame.game.systems.WaveEnemyGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class WaveManagerTest {
    @Test
    fun waveDefinition_buildsExpectedMixedEnemyQueue() {
        val wave = WaveDefinition(
            groups = listOf(
                WaveEnemyGroup(EnemyType.Normal, 2),
                WaveEnemyGroup(EnemyType.Fast, 1),
                WaveEnemyGroup(EnemyType.Tank, 1),
            ),
            spawnInterval = 0.5f,
        )

        assertEquals(
            listOf(EnemyType.Normal, EnemyType.Normal, EnemyType.Fast, EnemyType.Tank),
            wave.spawnQueue(),
        )
    }
}
