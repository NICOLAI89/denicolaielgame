package com.nicolaielgame.game

import com.nicolaielgame.game.model.DifficultyMode
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyModeTest {
    @Test
    fun difficultyModifiersApplyInExpectedDirections() {
        val baseHealth = 100f
        val baseGold = 100

        assertTrue(DifficultyMode.Easy.applyEnemyHealth(baseHealth) < baseHealth)
        assertTrue(DifficultyMode.Hard.applyEnemyHealth(baseHealth) > baseHealth)
        assertTrue(DifficultyMode.Easy.applyStartingGold(baseGold) > baseGold)
        assertTrue(DifficultyMode.Hard.applyStartingGold(baseGold) < baseGold)
    }
}
