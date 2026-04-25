package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSettingsTest {
    @Test
    fun defaultsKeepExistingGameFeel() {
        val settings = GameSettings()

        assertTrue(settings.soundEnabled)
        assertFalse(settings.musicEnabled)
        assertTrue(settings.showGrid)
        assertTrue(settings.screenShakeEnabled)
        assertTrue(settings.damageNumbersEnabled)
        assertFalse(settings.highContrastMode)
        assertEquals(DifficultyMode.Normal, settings.lastDifficulty)
    }
}
