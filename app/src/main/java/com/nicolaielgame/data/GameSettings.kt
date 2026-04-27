package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode

data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val showGrid: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val damageNumbersEnabled: Boolean = true,
    val highContrastMode: Boolean = false,
    val fpsCounterEnabled: Boolean = false,
    val autoStartWavesEnabled: Boolean = false,
    val lastDifficulty: DifficultyMode = DifficultyMode.Normal,
)
