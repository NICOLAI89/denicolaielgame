package com.nicolaielgame.game

import com.nicolaielgame.game.model.AbilityState
import com.nicolaielgame.game.model.AbilityType
import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.systems.AbilitySystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AbilitySystemTest {
    @Test
    fun spendAbility_startsCooldownAndConsumesEmergencyGoldUse() {
        val spent = AbilitySystem.spendAbility(AbilityState(), AbilityType.EmergencyGold)

        assertFalse(spent.canUse(AbilityType.EmergencyGold))
        assertEquals(1, spent.emergencyGoldUsesRemaining)
        assertTrue(spent.cooldownFor(AbilityType.EmergencyGold) > 0f)
    }

    @Test
    fun tickCooldowns_reducesCooldownWithoutGoingNegative() {
        val spent = AbilitySystem.spendAbility(AbilityState(), AbilityType.MeteorStrike)
        val cooled = AbilitySystem.tickCooldowns(spent, AbilityType.MeteorStrike.cooldownSeconds + 5f)

        assertEquals(0f, cooled.cooldownFor(AbilityType.MeteorStrike), 0.001f)
        assertTrue(cooled.canUse(AbilityType.MeteorStrike))
    }

    @Test
    fun enemiesInRadius_selectsOnlyAffectedEnemies() {
        val enemies = listOf(
            enemy(id = 1, row = 1f, col = 1f),
            enemy(id = 2, row = 4f, col = 4f),
        )

        val affected = AbilitySystem.enemiesInRadius(enemies, centerRow = 1f, centerCol = 1f, radius = 1.2f)

        assertEquals(listOf(1), affected.map { it.id })
    }

    private fun enemy(id: Int, row: Float, col: Float): Enemy {
        return Enemy(
            id = id,
            type = EnemyType.Normal,
            row = row,
            col = col,
            path = listOf(GridCell(0, 0), GridCell(1, 1)),
            pathIndex = 1,
            health = 10f,
            maxHealth = 10f,
            speed = 1f,
            reward = 1,
        )
    }
}
