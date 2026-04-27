package com.nicolaielgame.game.systems

import com.nicolaielgame.game.model.GameStatus
import com.nicolaielgame.game.model.WavePhase
import com.nicolaielgame.game.model.WaveSnapshot

object AutoWaveStarter {
    const val DelaySeconds = 1.35f

    fun nextDelay(
        enabled: Boolean,
        status: GameStatus,
        wave: WaveSnapshot,
        currentDelaySeconds: Float,
        deltaSeconds: Float,
    ): Float {
        if (!enabled || status != GameStatus.Running || !wave.isAwaitingIntermediateWave()) {
            return DelaySeconds
        }
        return (currentDelaySeconds - deltaSeconds.coerceAtLeast(0f)).coerceAtLeast(0f)
    }

    fun shouldStartNextWave(
        enabled: Boolean,
        status: GameStatus,
        wave: WaveSnapshot,
        delaySeconds: Float,
    ): Boolean {
        return enabled &&
            status == GameStatus.Running &&
            wave.isAwaitingIntermediateWave() &&
            delaySeconds <= 0f
    }

    private fun WaveSnapshot.isAwaitingIntermediateWave(): Boolean {
        return phase == WavePhase.Cleared &&
            canStart &&
            enemiesRemaining == 0 &&
            currentWave < totalWaves
    }
}
