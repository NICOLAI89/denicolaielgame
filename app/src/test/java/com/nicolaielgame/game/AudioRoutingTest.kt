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
}
