package com.nicolaielgame.game

import com.nicolaielgame.game.systems.AudioRouting
import com.nicolaielgame.game.systems.SoundEvent
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
}
