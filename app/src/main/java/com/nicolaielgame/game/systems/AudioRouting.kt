package com.nicolaielgame.game.systems

enum class SoundEvent {
    ButtonClick,
    TowerPlaced,
    TowerUpgraded,
    TowerSold,
    PlacementDenied,
    TowerShot,
    EnemyHit,
    EnemyDown,
    BossWarning,
    BossDeath,
    Meteor,
    FreezePulse,
    EmergencyGold,
    WaveStart,
    BaseHit,
    Victory,
    GameOver,
}

object AudioRouting {
    fun shouldPlaySound(soundEnabled: Boolean, event: SoundEvent): Boolean {
        return soundEnabled
    }

    fun shouldPlayMusic(musicEnabled: Boolean, hasMusicAsset: Boolean): Boolean {
        return musicEnabled && hasMusicAsset
    }
}
