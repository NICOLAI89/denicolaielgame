package com.nicolaielgame.game.model

data class AbilityEffect(
    val id: Int,
    val type: AbilityType,
    val row: Float,
    val col: Float,
    val age: Float = 0f,
    val duration: Float = 0.72f,
) {
    val progress: Float
        get() = (age / duration).coerceIn(0f, 1f)
}
