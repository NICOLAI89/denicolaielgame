package com.nicolaielgame.game

import com.nicolaielgame.game.model.GameStatus
import com.nicolaielgame.game.model.WavePhase
import com.nicolaielgame.game.model.WaveSnapshot
import com.nicolaielgame.game.systems.AutoWaveStarter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoWaveStarterTest {
    @Test
    fun autoStartTriggersAfterClearedWaveDelay() {
        val delay = AutoWaveStarter.nextDelay(
            enabled = true,
            status = GameStatus.Running,
            wave = clearedIntermediateWave(),
            currentDelaySeconds = AutoWaveStarter.DelaySeconds,
            deltaSeconds = AutoWaveStarter.DelaySeconds,
        )

        assertTrue(
            AutoWaveStarter.shouldStartNextWave(
                enabled = true,
                status = GameStatus.Running,
                wave = clearedIntermediateWave(),
                delaySeconds = delay,
            ),
        )
    }

    @Test
    fun autoStartDoesNotTriggerWhilePaused() {
        val delay = AutoWaveStarter.nextDelay(
            enabled = true,
            status = GameStatus.Paused,
            wave = clearedIntermediateWave(),
            currentDelaySeconds = 0f,
            deltaSeconds = AutoWaveStarter.DelaySeconds,
        )

        assertFalse(
            AutoWaveStarter.shouldStartNextWave(
                enabled = true,
                status = GameStatus.Paused,
                wave = clearedIntermediateWave(),
                delaySeconds = delay,
            ),
        )
    }

    @Test
    fun autoStartDoesNotTriggerAfterFinalWave() {
        assertFalse(
            AutoWaveStarter.shouldStartNextWave(
                enabled = true,
                status = GameStatus.Running,
                wave = clearedIntermediateWave(currentWave = 3, totalWaves = 3),
                delaySeconds = 0f,
            ),
        )
        assertFalse(
            AutoWaveStarter.shouldStartNextWave(
                enabled = true,
                status = GameStatus.Victory,
                wave = finishedWave(),
                delaySeconds = 0f,
            ),
        )
    }

    @Test
    fun autoStartSettingMustBeEnabled() {
        assertFalse(
            AutoWaveStarter.shouldStartNextWave(
                enabled = false,
                status = GameStatus.Running,
                wave = clearedIntermediateWave(),
                delaySeconds = 0f,
            ),
        )
    }

    private fun clearedIntermediateWave(currentWave: Int = 1, totalWaves: Int = 3): WaveSnapshot {
        return WaveSnapshot(
            currentWave = currentWave,
            totalWaves = totalWaves,
            enemiesLeftToSpawn = 0,
            aliveEnemies = 0,
            nextWaveInSeconds = 0f,
            phase = WavePhase.Cleared,
            awaitingNextWave = true,
            enemiesRemaining = 0,
            wavesCompleted = currentWave,
        )
    }

    private fun finishedWave(): WaveSnapshot {
        return WaveSnapshot(
            currentWave = 3,
            totalWaves = 3,
            enemiesLeftToSpawn = 0,
            aliveEnemies = 0,
            nextWaveInSeconds = 0f,
            phase = WavePhase.Finished,
            awaitingNextWave = false,
            enemiesRemaining = 0,
            wavesCompleted = 3,
        )
    }
}
