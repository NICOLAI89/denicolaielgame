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
                maxLevelId = 3,
            ),
        )
        assertEquals(
            3,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 1,
                currentHighestUnlocked = 3,
                maxLevelId = 3,
            ),
        )
    }

    @Test
    fun highestUnlockedAfterVictory_clampsAtLastLevel() {
        assertEquals(
            3,
            ProgressionRules.highestUnlockedAfterVictory(
                completedLevelId = 3,
                currentHighestUnlocked = 3,
                maxLevelId = 3,
            ),
        )
    }
}
