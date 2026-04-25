package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult

data class LeaderboardEntry(
    val profileSlot: Int,
    val levelId: Int,
    val difficulty: DifficultyMode,
    val score: Int,
    val completionTimeSeconds: Int,
    val livesRemaining: Int,
    val completedAtEpochMillis: Long,
) {
    fun encode(): String {
        return listOf(
            profileSlot,
            levelId,
            difficulty.name,
            score,
            completionTimeSeconds,
            livesRemaining,
            completedAtEpochMillis,
        ).joinToString(separator = "|")
    }

    companion object {
        fun fromRun(
            profileSlot: Int,
            result: GameRunResult,
            completedAtEpochMillis: Long,
        ): LeaderboardEntry? {
            if (!result.won) return null
            return LeaderboardEntry(
                profileSlot = ProfileRules.sanitizedSlot(profileSlot),
                levelId = result.levelId,
                difficulty = result.difficulty,
                score = result.score,
                completionTimeSeconds = result.runStats.timeSeconds.toInt().coerceAtLeast(0),
                livesRemaining = result.livesRemaining,
                completedAtEpochMillis = completedAtEpochMillis,
            )
        }

        fun decode(encoded: String): LeaderboardEntry? {
            val parts = encoded.split("|")
            if (parts.size != 7) return null
            return runCatching {
                LeaderboardEntry(
                    profileSlot = ProfileRules.sanitizedSlot(parts[0].toInt()),
                    levelId = parts[1].toInt(),
                    difficulty = DifficultyMode.valueOf(parts[2]),
                    score = parts[3].toInt(),
                    completionTimeSeconds = parts[4].toInt(),
                    livesRemaining = parts[5].toInt(),
                    completedAtEpochMillis = parts[6].toLong(),
                )
            }.getOrNull()
        }
    }
}

object LocalLeaderboard {
    private val Ranking = compareByDescending<LeaderboardEntry> { it.score }
        .thenBy { it.completionTimeSeconds }
        .thenByDescending { it.livesRemaining }
        .thenByDescending { it.completedAtEpochMillis }

    fun sorted(entries: List<LeaderboardEntry>): List<LeaderboardEntry> {
        return entries.sortedWith(Ranking)
    }

    fun recordCompletedRun(
        currentEntries: List<LeaderboardEntry>,
        entry: LeaderboardEntry,
        maxEntries: Int = 50,
    ): List<LeaderboardEntry> {
        return sorted(currentEntries + entry).take(maxEntries.coerceAtLeast(1))
    }

    fun topRuns(
        entries: List<LeaderboardEntry>,
        levelId: Int? = null,
        difficulty: DifficultyMode? = null,
        limit: Int = 20,
    ): List<LeaderboardEntry> {
        return sorted(
            entries.filter { entry ->
                (levelId == null || entry.levelId == levelId) &&
                    (difficulty == null || entry.difficulty == difficulty)
            },
        ).take(limit.coerceAtLeast(1))
    }
}
