package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult
import com.nicolaielgame.game.model.TowerType
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementRulesTest {
    @Test
    fun hardBossPerfectWinUnlocksExpectedAchievements() {
        val unlocked = AchievementRules.unlockedBy(
            GameRunResult(
                levelId = 3,
                score = 1000,
                won = true,
                livesRemaining = 20,
                startingLives = 20,
                difficulty = DifficultyMode.Hard,
                bossesDefeated = 1,
                towersPlacedByType = mapOf(TowerType.Sniper to 3),
            ),
        )

        assertTrue(Achievement.FirstVictory in unlocked)
        assertTrue(Achievement.NoLivesLost in unlocked)
        assertTrue(Achievement.TowerSpecialist in unlocked)
        assertTrue(Achievement.BossSlayer in unlocked)
        assertTrue(Achievement.HardModeClear in unlocked)
    }
}
