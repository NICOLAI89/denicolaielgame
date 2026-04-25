package com.nicolaielgame.data

object ProgressionRules {
    fun highestUnlockedAfterVictory(
        completedLevelId: Int,
        currentHighestUnlocked: Int,
        maxLevelId: Int,
    ): Int {
        val nextLevel = (completedLevelId + 1).coerceAtMost(maxLevelId)
        return maxOf(currentHighestUnlocked, nextLevel)
    }
}
