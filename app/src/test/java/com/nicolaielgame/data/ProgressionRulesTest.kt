package com.nicolaielgame.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionRulesTest {
    @Test
    fun highestUnlockedAfterVictory_unlocksNextLevelWithoutRegressing() {
        assertEquals(
            2,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 1,
                currentHighestUnlocked = 1,
                maxLevelId = 5,
            ),
        )
        assertEquals(
            5,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 1,
                currentHighestUnlocked = 5,
                maxLevelId = 5,
            ),
        )
    }

    @Test
    fun highestUnlockedAfterVictory_clampsAtLastLevel() {
        assertEquals(
            5,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 5,
                currentHighestUnlocked = 5,
                maxLevelId = 5,
            ),
        )
    }

    @Test
    fun highestUnlockedAfterVictory_unlocksThroughLevelFive() {
        assertEquals(
            5,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 4,
                currentHighestUnlocked = 4,
                maxLevelId = 5,
            ),
        )
    }
}
