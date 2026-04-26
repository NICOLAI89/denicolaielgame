package com.nicolaielgame.game.systems

import android.content.Context
import android.media.AudioManager
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

class AndroidToneSoundPlayer(context: Context) : SoundPlayer {
    var enabled: Boolean = true

    private val toneGenerator: ToneGenerator? = runCatching {
        @Suppress("DEPRECATION")
        ToneGenerator(AudioManager.STREAM_MUSIC, 35)
    }.getOrNull()

    init {
        context.applicationContext
    }

    override fun towerPlaced() {
        play(ToneGenerator.TONE_PROP_ACK, 70)
    }

    override fun buttonClick() {
        play(ToneGenerator.TONE_PROP_BEEP, 45)
    }

    override fun towerUpgraded() {
        play(ToneGenerator.TONE_CDMA_CONFIRM, 90)
    }

    override fun towerSold() {
        play(ToneGenerator.TONE_PROP_PROMPT, 80)
    }

    override fun placementDenied() {
        play(ToneGenerator.TONE_PROP_NACK, 90)
    }

    override fun towerShot() {
        play(ToneGenerator.TONE_PROP_BEEP, 35)
    }

    override fun enemyHit() {
        play(ToneGenerator.TONE_CDMA_PIP, 35)
    }

    override fun enemyDown() {
        play(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 55)
    }

    override fun bossWarning() {
        play(ToneGenerator.TONE_CDMA_ABBR_ALERT, 140)
    }

    override fun bossDeath() {
        play(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180)
    }

    override fun meteor() {
        play(ToneGenerator.TONE_CDMA_ABBR_ALERT, 100)
    }

    override fun freezePulse() {
        play(ToneGenerator.TONE_CDMA_PIP, 90)
    }

    override fun emergencyGold() {
        play(ToneGenerator.TONE_CDMA_CONFIRM, 100)
    }

    override fun waveStart() {
        play(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 120)
    }

    override fun baseHit() {
        play(ToneGenerator.TONE_SUP_ERROR, 120)
    }

    override fun victory() {
        play(ToneGenerator.TONE_CDMA_CONFIRM, 180)
    }

    override fun gameOver() {
        play(ToneGenerator.TONE_CDMA_ABBR_ALERT, 220)
    }

    fun release() {
        runCatching { toneGenerator?.release() }
    }

    private fun play(tone: Int, durationMs: Int) {
        if (!enabled) return
        runCatching { toneGenerator?.startTone(tone, durationMs) }
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
