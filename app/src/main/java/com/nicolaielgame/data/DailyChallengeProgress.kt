package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult

data class DailyChallengeBest(
    val dateKey: String,
    val profileSlot: Int,
    val score: Int,
    val completionTimeSeconds: Int,
    val livesRemaining: Int,
    val difficulty: DifficultyMode,
    val won: Boolean,
    val completedAtEpochMillis: Long,
) {
    fun encode(): String {
        return listOf(
            dateKey,
            profileSlot,
            score,
            completionTimeSeconds,
            livesRemaining,
            difficulty.name,
            won,
            completedAtEpochMillis,
        ).joinToString(separator = "|")
    }

    companion object {
        fun fromRun(
            dateKey: String,
            profileSlot: Int,
            result: GameRunResult,
            completedAtEpochMillis: Long,
        ): DailyChallengeBest {
            return DailyChallengeBest(
                dateKey = dateKey,
                profileSlot = ProfileRules.sanitizedSlot(profileSlot),
                score = result.score,
                completionTimeSeconds = result.runStats.timeSeconds.toInt().coerceAtLeast(0),
                livesRemaining = result.livesRemaining,
                difficulty = result.difficulty,
                won = result.won,
                completedAtEpochMillis = completedAtEpochMillis,
            )
        }

        fun decode(encoded: String): DailyChallengeBest? {
            val parts = encoded.split("|")
            if (parts.size != 8) return null
            return runCatching {
                DailyChallengeBest(
                    dateKey = parts[0],
                    profileSlot = ProfileRules.sanitizedSlot(parts[1].toInt()),
                    score = parts[2].toInt(),
                    completionTimeSeconds = parts[3].toInt(),
                    livesRemaining = parts[4].toInt(),
                    difficulty = DifficultyMode.valueOf(parts[5]),
                    won = parts[6].toBoolean(),
                    completedAtEpochMillis = parts[7].toLong(),
                )
            }.getOrNull()
        }
    }
}

object DailyChallengeProgress {
    private val Ranking = compareByDescending<DailyChallengeBest> { it.score }
        .thenBy { it.completionTimeSeconds }
        .thenByDescending { it.livesRemaining }
        .thenByDescending { it.completedAtEpochMillis }

    fun bestFor(
        entries: List<DailyChallengeBest>,
        dateKey: String,
        profileSlot: Int,
    ): DailyChallengeBest? {
        val slot = ProfileRules.sanitizedSlot(profileSlot)
        return entries
            .filter { it.dateKey == dateKey && it.profileSlot == slot }
            .minWithOrNull(Ranking)
    }

    fun saveBest(
        currentEntries: List<DailyChallengeBest>,
        entry: DailyChallengeBest,
    ): List<DailyChallengeBest> {
        val remaining = currentEntries.filterNot {
            it.dateKey == entry.dateKey && it.profileSlot == entry.profileSlot
        }
        val currentBest = bestFor(currentEntries, entry.dateKey, entry.profileSlot)
        val winner = listOfNotNull(currentBest, entry).minWithOrNull(Ranking) ?: entry
        return (remaining + winner).sortedWith(compareByDescending<DailyChallengeBest> { it.dateKey }.thenBy { it.profileSlot })
    }
}
