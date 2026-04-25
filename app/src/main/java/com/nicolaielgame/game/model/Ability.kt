package com.nicolaielgame.game.model

import androidx.compose.ui.graphics.Color

enum class AbilityType(
    val title: String,
    val shortLabel: String,
    val cooldownSeconds: Float,
    val color: Color,
) {
    MeteorStrike(
        title = "Meteor Strike",
        shortLabel = "Meteor",
        cooldownSeconds = 20f,
        color = Color(0xFFFF7A45),
    ),
    FreezePulse(
        title = "Freeze Pulse",
        shortLabel = "Freeze",
        cooldownSeconds = 18f,
        color = Color(0xFFA9F1FF),
    ),
    EmergencyGold(
        title = "Emergency Gold",
        shortLabel = "Gold",
        cooldownSeconds = 24f,
        color = Color(0xFFF6C55D),
    ),
}

data class AbilityState(
    val cooldowns: Map<AbilityType, Float> = AbilityType.entries.associateWith { 0f },
    val emergencyGoldUsesRemaining: Int = 2,
) {
    fun cooldownFor(type: AbilityType): Float {
        return cooldowns[type] ?: 0f
    }

    fun canUse(type: AbilityType): Boolean {
        return cooldownFor(type) <= 0f &&
            (type != AbilityType.EmergencyGold || emergencyGoldUsesRemaining > 0)
    }
}
