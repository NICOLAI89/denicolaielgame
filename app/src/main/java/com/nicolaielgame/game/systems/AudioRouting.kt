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
    private val soundAssetPaths = mapOf(
        SoundEvent.ButtonClick to "app/src/main/assets/audio/sfx/button_click.ogg",
        SoundEvent.TowerPlaced to "app/src/main/assets/audio/sfx/tower_placed.ogg",
        SoundEvent.TowerUpgraded to "app/src/main/assets/audio/sfx/tower_upgraded.ogg",
        SoundEvent.TowerSold to "app/src/main/assets/audio/sfx/tower_sold.ogg",
        SoundEvent.TowerShot to "app/src/main/assets/audio/sfx/tower_fired.ogg",
        SoundEvent.EnemyHit to "app/src/main/assets/audio/sfx/enemy_hit.ogg",
        SoundEvent.EnemyDown to "app/src/main/assets/audio/sfx/enemy_killed.ogg",
        SoundEvent.BossWarning to "app/src/main/assets/audio/sfx/boss_warning.ogg",
        SoundEvent.BossDeath to "app/src/main/assets/audio/sfx/boss_death.ogg",
        SoundEvent.Meteor to "app/src/main/assets/audio/sfx/meteor.ogg",
        SoundEvent.FreezePulse to "app/src/main/assets/audio/sfx/freeze_pulse.ogg",
        SoundEvent.EmergencyGold to "app/src/main/assets/audio/sfx/emergency_gold.ogg",
        SoundEvent.WaveStart to "app/src/main/assets/audio/sfx/wave_start.ogg",
        SoundEvent.BaseHit to "app/src/main/assets/audio/sfx/base_hit.ogg",
        SoundEvent.Victory to "app/src/main/assets/audio/sfx/victory.ogg",
        SoundEvent.GameOver to "app/src/main/assets/audio/sfx/game_over.ogg",
    )

    fun shouldPlaySound(soundEnabled: Boolean, event: SoundEvent): Boolean {
        assetPathFor(event)
        return soundEnabled
    }

    fun shouldPlayMusic(musicEnabled: Boolean, hasMusicAsset: Boolean): Boolean {
        return musicEnabled && hasMusicAsset
    }

    fun assetPathFor(event: SoundEvent): String? {
        return soundAssetPaths[event]
    }

    fun eventsWithBundledAssets(availableAssetPaths: Set<String>): Set<SoundEvent> {
        return soundAssetPaths
            .filterValues { path -> path in availableAssetPaths }
            .keys
    }
}
