package com.nicolaielgame.game

import com.nicolaielgame.game.assets.GameAssetCatalog
import com.nicolaielgame.game.assets.GameAssetCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetCatalogTest {
    @Test
    fun everyAssetSlotHasFallbackCoverageWhenExternalAssetsAreMissing() {
        assertTrue(GameAssetCatalog.hasFallbackCoverage())
        assertTrue(GameAssetCatalog.missingFallbacks(emptySet()).all { it.bundledFallback.isNotBlank() })
    }

    @Test
    fun catalogContainsVisualAndAudioBuckets() {
        assertFalse(GameAssetCatalog.slotsFor(GameAssetCategory.Tiles).isEmpty())
        assertFalse(GameAssetCatalog.slotsFor(GameAssetCategory.Towers).isEmpty())
        assertFalse(GameAssetCatalog.slotsFor(GameAssetCategory.Enemies).isEmpty())
        assertFalse(GameAssetCatalog.slotsFor(GameAssetCategory.Ui).isEmpty())
        assertFalse(GameAssetCatalog.slotsFor(GameAssetCategory.AudioSfx).isEmpty())
    }

    @Test
    fun bundledIteration7BAssetsCoverGameplaySlotsExceptOptionalMusic() {
        val bundledAssetPaths = GameAssetCatalog.allSlots
            .map { it.expectedPath }
            .filterNot { it.contains("/audio/music/") }
            .toSet()

        val missing = GameAssetCatalog.missingFallbacks(bundledAssetPaths)

        assertTrue(missing.all { it.key == "music_loop" })
        assertTrue(GameAssetCatalog.visualSlots.all { it.expectedPath.endsWith(".webp") })
        assertTrue(GameAssetCatalog.audioSlots.any { it.key == "base_hit" })
    }
}
