package com.nicolaielgame.game

import com.nicolaielgame.game.model.EnemyType
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyTypeTest {
    @Test
    fun shieldedEnemyHasDamageReduction() {
        assertTrue(EnemyType.Shielded.maxHealth > EnemyType.Fast.maxHealth)
        assertTrue(EnemyType.Shielded.damageTakenMultiplier < 1f)
        assertTrue(EnemyType.Shielded.reward > EnemyType.Normal.reward)
    }

    @Test
    fun swarmEnemyIsFastLowHealthHighCountFriendly() {
        assertTrue(EnemyType.Swarm.maxHealth < EnemyType.Normal.maxHealth)
        assertTrue(EnemyType.Swarm.speed > EnemyType.Fast.speed)
        assertTrue(EnemyType.Swarm.sizeScale < 1f)
    }
}
