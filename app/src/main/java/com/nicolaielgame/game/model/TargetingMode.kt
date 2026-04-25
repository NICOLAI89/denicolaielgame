package com.nicolaielgame.game.model

enum class TargetingMode(
    val title: String,
    val shortLabel: String,
) {
    First("First", "1st"),
    Last("Last", "Last"),
    Strongest("Strongest", "High"),
    Weakest("Weakest", "Low"),
    Closest("Closest", "Near"),
}
