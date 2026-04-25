package com.nicolaielgame.game

import com.nicolaielgame.game.model.AbilityType
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.RunStats
import org.junit.Assert.assertEquals
import org.junit.Test

class RunStatsTest {
    @Test
    fun runStats_tracksAbilitiesAndKills() {
        val stats = RunStats()
            .withAbilityUsed(AbilityType.MeteorStrike)
            .withAbilityUsed(AbilityType.MeteorStrike)
            .withKill(EnemyType.Fast)
            .withKill(EnemyType.Juggernaut)

        assertEquals(2, stats.totalAbilitiesUsed)
        assertEquals(2, stats.abilitiesUsed[AbilityType.MeteorStrike])
        assertEquals(2, stats.enemiesKilled)
        assertEquals(1, stats.bossesKilled)
    }
}
