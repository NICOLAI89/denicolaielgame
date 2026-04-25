package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult
import com.nicolaielgame.game.model.RunStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalLeaderboardTest {
    @Test
    fun recordCompletedRun_sortsByScoreThenTimeThenLives() {
        val slowHighScore = entry(score = 900, time = 180, lives = 5)
        val fastHighScore = entry(score = 900, time = 120, lives = 3)
        val lowerScore = entry(score = 700, time = 60, lives = 20)

        val saved = LocalLeaderboard.recordCompletedRun(
            currentEntries = listOf(slowHighScore, lowerScore),
            entry = fastHighScore,
        )

        assertEquals(listOf(fastHighScore, slowHighScore, lowerScore), saved)
    }

    @Test
    fun fromRun_onlySavesCompletedRuns() {
        val lostRun = GameRunResult(
            levelId = 2,
            score = 500,
            won = false,
            livesRemaining = 0,
            startingLives = 18,
            difficulty = DifficultyMode.Normal,
            bossesDefeated = 0,
            towersPlacedByType = emptyMap(),
            runStats = RunStats(timeSeconds = 60f),
        )

        assertNull(LeaderboardEntry.fromRun(profileSlot = 1, result = lostRun, completedAtEpochMillis = 1L))
    }

    @Test
    fun encodeDecode_roundTripsSavedEntry() {
        val original = entry(score = 1200, time = 88, lives = 12)

        assertEquals(original, LeaderboardEntry.decode(original.encode()))
    }

    private fun entry(score: Int, time: Int, lives: Int): LeaderboardEntry {
        return LeaderboardEntry(
            profileSlot = 1,
            levelId = 3,
            difficulty = DifficultyMode.Hard,
            score = score,
            completionTimeSeconds = time,
            livesRemaining = lives,
            completedAtEpochMillis = score.toLong(),
        )
    }
}
