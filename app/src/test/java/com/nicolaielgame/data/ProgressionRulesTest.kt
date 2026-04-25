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
                maxLevelId = 7,
            ),
        )
        assertEquals(
            7,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 1,
                currentHighestUnlocked = 7,
                maxLevelId = 7,
            ),
        )
    }

    @Test
    fun highestUnlockedAfterVictory_clampsAtLastLevel() {
        assertEquals(
            7,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 7,
                currentHighestUnlocked = 7,
                maxLevelId = 7,
            ),
        )
    }

    @Test
    fun highestUnlockedAfterVictory_unlocksThroughLevelSeven() {
        assertEquals(
            7,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 6,
                currentHighestUnlocked = 6,
                maxLevelId = 7,
            ),
        )
    }
}
