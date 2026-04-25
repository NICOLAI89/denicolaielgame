package com.nicolaielgame.game

import com.nicolaielgame.game.engine.GameEngine
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.GameMap
import com.nicolaielgame.game.model.GameStatus
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.LevelDefinition
import com.nicolaielgame.game.systems.WaveDefinition
import com.nicolaielgame.game.systems.WaveEnemyGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineWaveLifecycleTest {
    @Test
    fun firstWaveStartsManuallyAndFinalWaveTriggersVictory() {
        val engine = GameEngine(testLevelWithEmptyFinalWave())

        assertTrue(engine.state.value.wave.canStart)
        engine.startNextWave()
        engine.tick(0.1f)

        assertEquals(GameStatus.Victory, engine.state.value.status)
        assertEquals(1, engine.state.value.runStats.wavesCompleted)
    }

    private fun testLevelWithEmptyFinalWave(): LevelDefinition {
        val spawn = GridCell(0, 0)
        val base = GridCell(0, 1)
        return LevelDefinition(
            id = 77,
            title = "Wave Test",
            description = "Wave lifecycle test map",
            map = GameMap(
                rows = 1,
                cols = 2,
                spawn = spawn,
                base = base,
                buildLockedCells = setOf(spawn, base),
                scenicPath = setOf(spawn, base),
            ),
            startingGold = 100,
            startingLives = 5,
            waves = listOf(
                WaveDefinition(
                    groups = listOf(WaveEnemyGroup(EnemyType.Normal, 0)),
                    spawnInterval = 1f,
                ),
            ),
        )
    }
}
