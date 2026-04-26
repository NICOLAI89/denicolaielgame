package com.nicolaielgame.data

import com.nicolaielgame.game.model.LevelCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class CampaignProgressionTest {
    @Test
    fun nodesReflectLockedUnlockedAndCompletedStates() {
        val nodes = CampaignProgression.nodes(
            levels = LevelCatalog.levels,
            highestUnlockedLevel = 3,
            bestScoresByLevel = mapOf(1 to 1000, 2 to 800),
        )

        assertEquals(CampaignNodeState.Completed, nodes[0].state)
        assertEquals(CampaignNodeState.Completed, nodes[1].state)
        assertEquals(CampaignNodeState.Unlocked, nodes[2].state)
        assertEquals(CampaignNodeState.Locked, nodes[3].state)
    }

    @Test
    fun nextPlayablePrefersFirstUnlockedThenLastCompleted() {
        val mixed = CampaignProgression.nodes(
            levels = LevelCatalog.levels,
            highestUnlockedLevel = 4,
            bestScoresByLevel = mapOf(1 to 1000, 2 to 900, 3 to 800),
        )
        val allCompleted = CampaignProgression.nodes(
            levels = LevelCatalog.levels,
            highestUnlockedLevel = 7,
            bestScoresByLevel = LevelCatalog.levels.associate { it.id to 1000 },
        )

        assertEquals(4, CampaignProgression.nextPlayableLevelId(mixed))
        assertEquals(7, CampaignProgression.nextPlayableLevelId(allCompleted))
    }
}
