package com.nicolaielgame.data

import com.nicolaielgame.game.model.DifficultyMode

data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val showGrid: Boolean = true,
    val lastDifficulty: DifficultyMode = DifficultyMode.Normal,
)
