package com.nicolaielgame.game.model

import androidx.compose.ui.graphics.Color

enum class EnemyType(
    val title: String,
    val maxHealth: Float,
    val speed: Float,
    val reward: Int,
    val scoreValue: Int,
    val primaryColor: Color,
    val accentColor: Color,
) {
    Normal(
        title = "Normal",
        maxHealth = 44f,
        speed = 0.98f,
        reward = 9,
        scoreValue = 18,
        primaryColor = Color(0xFFC6284E),
        accentColor = Color(0xFFFF8A72),
    ),
    Fast(
        title = "Fast",
        maxHealth = 30f,
        speed = 1.42f,
        reward = 8,
        scoreValue = 16,
        primaryColor = Color(0xFFD26A1E),
        accentColor = Color(0xFFFFD166),
    ),
    Tank(
        title = "Tank",
        maxHealth = 118f,
        speed = 0.62f,
        reward = 18,
        scoreValue = 34,
        primaryColor = Color(0xFF5E4A8A),
        accentColor = Color(0xFFBCA7FF),
    ),
}

