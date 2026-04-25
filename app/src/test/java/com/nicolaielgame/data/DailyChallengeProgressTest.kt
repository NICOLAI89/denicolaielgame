package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyChallengeProgressTest {
    @Test
    fun saveBest_keepsHighestDailyScoreForProfileAndDate() {
        val first = best(score = 800, time = 90, lives = 6, completedAt = 1L)
        val worse = best(score = 700, time = 60, lives = 12, completedAt = 2L)
        val better = best(score = 950, time = 120, lives = 4, completedAt = 3L)

        val afterWorse = DailyChallengeProgress.saveBest(listOf(first), worse)
        val afterBetter = DailyChallengeProgress.saveBest(afterWorse, better)

        assertEquals(first, DailyChallengeProgress.bestFor(afterWorse, "2026-04-25", 1))
        assertEquals(better, DailyChallengeProgress.bestFor(afterBetter, "2026-04-25", 1))
    }

    private fun best(score: Int, time: Int, lives: Int, completedAt: Long): DailyChallengeBest {
        return DailyChallengeBest(
            dateKey = "2026-04-25",
            profileSlot = 1,
            score = score,
            completionTimeSeconds = time,
            livesRemaining = lives,
            difficulty = DifficultyMode.Normal,
            won = score > 0,
            completedAtEpochMillis = completedAt,
        )
    }
}
