package com.nicolaielgame.game

import com.nicolaielgame.game.assets.GameVisualAssets
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.TowerType
import org.junit.Assert.assertNull
import org.junit.Test

class VisualAssetMappingTest {
    @Test
    fun emptyVisualAssetsFallBackForAllTowerAndEnemyTypes() {
        val assets = GameVisualAssets.Empty

        TowerType.entries.forEach { type ->
            assertNull("Expected $type to use renderer fallback when missing", assets.tower(type))
        }
        EnemyType.entries.forEach { type ->
            assertNull("Expected $type to use renderer fallback when missing", assets.enemy(type))
        }
    }
}
