package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult

enum class Achievement(
    val title: String,
    val description: String,
) {
    FirstVictory(
        title = "First Victory",
        description = "Win any level.",
    ),
    NoLivesLost(
        title = "No Lives Lost",
        description = "Win a level without losing a life.",
    ),
    TowerSpecialist(
        title = "Tower Specialist",
        description = "Win using one tower type only.",
    ),
    BossSlayer(
        title = "Boss Slayer",
        description = "Defeat a boss enemy.",
    ),
    HardModeClear(
        title = "Hard Mode Clear",
        description = "Win a level on Hard.",
    ),
}

object AchievementRules {
    fun unlockedBy(result: GameRunResult): Set<Achievement> {
        if (!result.won && result.bossesDefeated == 0) return emptySet()

        return buildSet {
            if (result.won) add(Achievement.FirstVictory)
            if (result.won && result.livesRemaining >= result.startingLives) add(Achievement.NoLivesLost)
            if (result.won && result.towersPlacedByType.keys.size == 1 && result.towersPlacedByType.values.sum() >= 3) {
                add(Achievement.TowerSpecialist)
            }
            if (result.bossesDefeated > 0) add(Achievement.BossSlayer)
            if (result.won && result.difficulty == DifficultyMode.Hard) add(Achievement.HardModeClear)
        }
    }
}
