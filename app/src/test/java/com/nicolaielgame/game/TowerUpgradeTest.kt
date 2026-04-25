package com.nicolaielgame.game

import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.Tower
import com.nicolaielgame.game.model.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TowerUpgradeTest {
    @Test
    fun upgraded_increasesLevelStatsAndInvestedGold() {
        val tower = Tower(
            id = 1,
            cell = GridCell(2, 2),
            type = TowerType.Sniper,
        )
        val upgradeCost = tower.upgradeCost

        val upgraded = tower.upgraded()

        assertEquals(2, upgraded.level)
        assertEquals(TowerType.Sniper.baseCost + upgradeCost, upgraded.totalInvested)
        assertTrue(upgraded.damage > tower.damage)
        assertTrue(upgraded.range > tower.range)
        assertTrue(upgraded.fireInterval < tower.fireInterval)
    }
}
