package com.nicolaielgame.game.systems

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator

interface SoundPlayer {
    fun buttonClick()
    fun towerPlaced()
    fun towerUpgraded()
    fun towerSold()
    fun placementDenied()
    fun towerShot()
    fun enemyHit()
    fun enemyDown()
    fun bossWarning()
    fun bossDeath()
    fun meteor()
    fun freezePulse()
    fun emergencyGold()
    fun waveStart()
    fun baseHit()
    fun victory()
    fun gameOver()
}

class AndroidGameSoundPlayer(context: Context) : SoundPlayer {
    var enabled: Boolean = true

    private val appContext = context.applicationContext
    private val toneGenerator: ToneGenerator? = runCatching {
        @Suppress("DEPRECATION")
        ToneGenerator(AudioManager.STREAM_MUSIC, 35)
    }.getOrNull()

    override fun towerPlaced() {
        play(SoundEvent.TowerPlaced, ToneGenerator.TONE_PROP_ACK, 70)
    }

    override fun buttonClick() {
        play(SoundEvent.ButtonClick, ToneGenerator.TONE_PROP_BEEP, 45)
    }

    override fun towerUpgraded() {
        play(SoundEvent.TowerUpgraded, ToneGenerator.TONE_CDMA_CONFIRM, 90)
    }

    override fun towerSold() {
        play(SoundEvent.TowerSold, ToneGenerator.TONE_PROP_PROMPT, 80)
    }

    override fun placementDenied() {
        play(SoundEvent.PlacementDenied, ToneGenerator.TONE_PROP_NACK, 90)
    }

    override fun towerShot() {
        play(SoundEvent.TowerShot, ToneGenerator.TONE_PROP_BEEP, 35)
    }

    override fun enemyHit() {
        play(SoundEvent.EnemyHit, ToneGenerator.TONE_CDMA_PIP, 35)
    }

    override fun enemyDown() {
        play(SoundEvent.EnemyDown, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 55)
    }

    override fun bossWarning() {
        play(SoundEvent.BossWarning, ToneGenerator.TONE_CDMA_ABBR_ALERT, 140)
    }

    override fun bossDeath() {
        play(SoundEvent.BossDeath, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180)
    }

    override fun meteor() {
        play(SoundEvent.Meteor, ToneGenerator.TONE_CDMA_ABBR_ALERT, 100)
    }

    override fun freezePulse() {
        play(SoundEvent.FreezePulse, ToneGenerator.TONE_CDMA_PIP, 90)
    }

    override fun emergencyGold() {
        play(SoundEvent.EmergencyGold, ToneGenerator.TONE_CDMA_CONFIRM, 100)
    }

    override fun waveStart() {
        play(SoundEvent.WaveStart, ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 120)
    }

    override fun baseHit() {
        play(SoundEvent.BaseHit, ToneGenerator.TONE_SUP_ERROR, 120)
    }

    override fun victory() {
        play(SoundEvent.Victory, ToneGenerator.TONE_CDMA_CONFIRM, 180)
    }

    override fun gameOver() {
        play(SoundEvent.GameOver, ToneGenerator.TONE_CDMA_ABBR_ALERT, 220)
    }

    fun release() {
        runCatching { soundPool?.release() }
        runCatching { toneGenerator?.release() }
    }

    private fun play(event: SoundEvent, tone: Int, durationMs: Int) {
        if (!enabled) return
        val soundId = soundIds[event]
        val pool = soundPool
        if (soundId != null && pool != null) {
            val streamId = runCatching {
                pool.play(soundId, 0.82f, 0.82f, 1, 0, 1f)
            }.getOrDefault(0)
            if (streamId != 0) return
        }
        runCatching { toneGenerator?.startTone(tone, durationMs) }
    }

    private val soundPool: SoundPool? = runCatching {
        SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    }.getOrNull()

    private val soundIds: Map<SoundEvent, Int> = loadSoundIds()

    private fun loadSoundIds(): Map<SoundEvent, Int> {
        val pool = soundPool ?: return emptyMap()
        return assetPaths.mapNotNull { (event, path) ->
            val soundId = runCatching {
                appContext.assets.openFd(path).use { descriptor ->
                    pool.load(descriptor, 1)
                }
            }.getOrNull()
            soundId?.takeIf { it != 0 }?.let { event to it }
        }.toMap()
    }

    companion object {
        private val assetPaths = mapOf(
            SoundEvent.ButtonClick to "audio/sfx/button_click.ogg",
            SoundEvent.TowerPlaced to "audio/sfx/tower_placed.ogg",
            SoundEvent.TowerUpgraded to "audio/sfx/tower_upgraded.ogg",
            SoundEvent.TowerSold to "audio/sfx/tower_sold.ogg",
            SoundEvent.TowerShot to "audio/sfx/tower_fired.ogg",
            SoundEvent.EnemyHit to "audio/sfx/enemy_hit.ogg",
            SoundEvent.EnemyDown to "audio/sfx/enemy_killed.ogg",
            SoundEvent.BossWarning to "audio/sfx/boss_warning.ogg",
            SoundEvent.BossDeath to "audio/sfx/boss_death.ogg",
            SoundEvent.Meteor to "audio/sfx/meteor.ogg",
            SoundEvent.FreezePulse to "audio/sfx/freeze_pulse.ogg",
            SoundEvent.EmergencyGold to "audio/sfx/emergency_gold.ogg",
            SoundEvent.WaveStart to "audio/sfx/wave_start.ogg",
            SoundEvent.BaseHit to "audio/sfx/base_hit.ogg",
            SoundEvent.Victory to "audio/sfx/victory.ogg",
            SoundEvent.GameOver to "audio/sfx/game_over.ogg",
        )
    }
}

class AndroidGameMusicPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null

    fun setEnabled(enabled: Boolean) {
        if (AudioRouting.shouldPlayMusic(enabled, hasBundledMusic())) {
            start()
        } else {
            stop()
        }
    }

    fun hasBundledMusic(): Boolean {
        return runCatching {
            appContext.assets.openFd(MusicAssetPath).use { Unit }
        }.isSuccess
    }

    fun release() {
        stop()
    }

    private fun start() {
        val existing = player
        if (existing != null) {
            if (!existing.isPlaying) runCatching { existing.start() }
            return
        }
        val descriptor = runCatching { appContext.assets.openFd(MusicAssetPath) }.getOrNull() ?: return
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                isLooping = true
                setVolume(0.22f, 0.22f)
                prepare()
                start()
            }
        }.getOrNull()
    }

    private fun stop() {
        runCatching {
            player?.stop()
            player?.release()
        }
        player = null
    }

    companion object {
        const val MusicAssetPath = "audio/music/music_loop.ogg"
    }
}

object SilentSoundPlayer : SoundPlayer {
    override fun buttonClick() = Unit
    override fun towerPlaced() = Unit
    override fun towerUpgraded() = Unit
    override fun towerSold() = Unit
    override fun placementDenied() = Unit
    override fun towerShot() = Unit
    override fun enemyHit() = Unit
    override fun enemyDown() = Unit
    override fun bossWarning() = Unit
    override fun bossDeath() = Unit
    override fun meteor() = Unit
    override fun freezePulse() = Unit
    override fun emergencyGold() = Unit
    override fun waveStart() = Unit
    override fun baseHit() = Unit
    override fun victory() = Unit
    override fun gameOver() = Unit
}
