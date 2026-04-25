package com.nicolaielgame.game.systems

import com.nicolaielgame.game.model.AbilityState
import com.nicolaielgame.game.model.AbilityType
import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.gridDistance

object AbilitySystem {
    const val MeteorDamage = 135f
    const val MeteorRadius = 2.05f
    const val FreezeRadius = 2.8f
    const val FreezeMultiplier = 0.34f
    const val FreezeDuration = 3.45f
    const val EmergencyGoldAmount = 55

    fun tickCooldowns(state: AbilityState, deltaSeconds: Float): AbilityState {
        return state.copy(
            cooldowns = state.cooldowns.mapValues { (_, cooldown) ->
                (cooldown - deltaSeconds).coerceAtLeast(0f)
            },
        )
    }

    fun spendAbility(state: AbilityState, type: AbilityType): AbilityState {
        return state.copy(
            cooldowns = state.cooldowns + (type to type.cooldownSeconds),
            emergencyGoldUsesRemaining = if (type == AbilityType.EmergencyGold) {
                (state.emergencyGoldUsesRemaining - 1).coerceAtLeast(0)
            } else {
                state.emergencyGoldUsesRemaining
            },
        )
    }

    fun enemiesInRadius(enemies: List<Enemy>, centerRow: Float, centerCol: Float, radius: Float): List<Enemy> {
        return enemies.filter { enemy ->
            gridDistance(enemy.row, enemy.col, centerRow, centerCol) <= radius
        }
    }
}
