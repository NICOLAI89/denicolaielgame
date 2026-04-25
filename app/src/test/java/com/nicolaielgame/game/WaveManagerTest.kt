package com.nicolaielgame.game

import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.WavePhase
import com.nicolaielgame.game.systems.WaveDefinition
import com.nicolaielgame.game.systems.WaveEnemyGroup
import com.nicolaielgame.game.systems.WaveManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun nextWaveButtonStateOnlyAppearsWhenStartWillSucceed() {
        val manager = WaveManager(
            waves = listOf(
                WaveDefinition(listOf(WaveEnemyGroup(EnemyType.Normal, 1)), spawnInterval = 1f),
                WaveDefinition(listOf(WaveEnemyGroup(EnemyType.Fast, 1)), spawnInterval = 1f),
            ),
        )

        assertTrue(manager.snapshot(aliveEnemies = 0).canStart)
        assertTrue(manager.startNextWave())
        assertFalse(manager.snapshot(aliveEnemies = 0).canStart)
        assertFalse(manager.startNextWave())

        assertEquals(listOf(EnemyType.Normal), manager.update(deltaSeconds = 0.1f))
        manager.completeWaveIfCleared(hasAliveEnemies = true)
        assertEquals(WavePhase.InProgress, manager.snapshot(aliveEnemies = 1).phase)

        manager.completeWaveIfCleared(hasAliveEnemies = false)
        val cleared = manager.snapshot(aliveEnemies = 0)
        assertEquals(WavePhase.Cleared, cleared.phase)
        assertEquals(1, cleared.wavesCompleted)
        assertTrue(cleared.canStart)
        assertTrue(manager.startNextWave())
    }

    @Test
    fun finalWaveMovesToFinishedAfterLastEnemyClears() {
        val manager = WaveManager(
            waves = listOf(
                WaveDefinition(listOf(WaveEnemyGroup(EnemyType.Normal, 1)), spawnInterval = 1f),
            ),
        )

        assertTrue(manager.startNextWave())
        manager.update(deltaSeconds = 0.1f)
        manager.completeWaveIfCleared(hasAliveEnemies = false)

        assertTrue(manager.isFinished)
        assertEquals(WavePhase.Finished, manager.snapshot(aliveEnemies = 0).phase)
        assertEquals(1, manager.snapshot(aliveEnemies = 0).wavesCompleted)
        assertFalse(manager.snapshot(aliveEnemies = 0).canStart)
    }
}
