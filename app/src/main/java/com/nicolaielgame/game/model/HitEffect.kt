package com.nicolaielgame.game.model

import androidx.compose.ui.graphics.Color

data class HitEffect(
    val id: Int,
    val row: Float,
    val col: Float,
    val color: Color,
    val age: Float = 0f,
    val duration: Float = 0.42f,
)

data class RangePreview(
    val cell: GridCell,
    val range: Float,
    val color: Color,
)

