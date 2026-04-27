package com.nicolaielgame.game

import com.nicolaielgame.game.systems.AudioRouting
import com.nicolaielgame.game.systems.SoundEvent
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioRoutingTest {
    @Test
    fun soundEventsRespectSoundToggle() {
        assertTrue(AudioRouting.shouldPlaySound(soundEnabled = true, event = SoundEvent.TowerPlaced))
        assertFalse(AudioRouting.shouldPlaySound(soundEnabled = false, event = SoundEvent.TowerPlaced))
        assertFalse(AudioRouting.shouldPlaySound(soundEnabled = false, event = SoundEvent.ButtonClick))
    }

    @Test
    fun musicRequiresToggleAndAvailableAsset() {
        assertTrue(AudioRouting.shouldPlayMusic(musicEnabled = true, hasMusicAsset = true))
        assertFalse(AudioRouting.shouldPlayMusic(musicEnabled = true, hasMusicAsset = false))
        assertFalse(AudioRouting.shouldPlayMusic(musicEnabled = false, hasMusicAsset = true))
        assertTrue(AudioRouting.musicAssetPath().endsWith("audio/music/music_loop.ogg"))
    }

    @Test
    fun bundledMusicAssetIsDocumented() {
        val docs = repoFile("docs/ASSET_INTEGRATION.md").readText()
        val credits = repoFile("CREDITS.md").readText()

        assertTrue("Expected bundled music file to exist", repoFile(AudioRouting.musicAssetPath()).exists())
        assertTrue("Expected docs to describe bundled music", "Space Cadet" in docs)
        assertTrue("Expected credits to document bundled music", "Space Cadet" in credits)
    }

    @Test
    fun bundledSoundRoutesCoverGameplayEventsAndLeaveDeniedToneAsFallback() {
        val availablePaths = SoundEvent.entries
            .mapNotNull(AudioRouting::assetPathFor)
            .toSet()
        val routedEvents = AudioRouting.eventsWithBundledAssets(availablePaths)

        assertTrue(SoundEvent.TowerPlaced in routedEvents)
        assertTrue(SoundEvent.TowerShot in routedEvents)
        assertTrue(SoundEvent.EnemyHit in routedEvents)
        assertTrue(SoundEvent.BossDeath in routedEvents)
        assertTrue(SoundEvent.GameOver in routedEvents)
        assertFalse(SoundEvent.PlacementDenied in routedEvents)
    }

    private fun repoFile(path: String): File {
        return sequenceOf(File(path), File("..", path))
            .firstOrNull { it.exists() }
            ?: error("Could not find $path from ${File(".").absolutePath}")
    }
}
