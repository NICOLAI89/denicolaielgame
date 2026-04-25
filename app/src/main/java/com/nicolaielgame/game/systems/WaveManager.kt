package com.nicolaielgame.game.systems

import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.WavePhase
import com.nicolaielgame.game.model.WaveSnapshot

data class WaveEnemyGroup(
    val type: EnemyType,
    val count: Int,
)

data class WaveDefinition(
    val groups: List<WaveEnemyGroup>,
    val spawnInterval: Float,
) {
    val totalEnemies: Int
        get() = groups.sumOf { it.count }

    val isBossWave: Boolean
        get() = groups.any { it.type.isBoss }

    fun spawnQueue(): List<EnemyType> {
        return groups.flatMap { group -> List(group.count) { group.type } }
    }

    fun previewText(): String {
        return groups.joinToString("  ") { group ->
            "${group.count} ${group.type.title}"
        }
    }

    fun bossPreviewText(): String {
        return groups
            .filter { it.type.isBoss }
            .joinToString(" + ") { it.type.title }
    }
}

class WaveManager(private val waves: List<WaveDefinition>) {
    private var currentWaveIndex = 0
    private var spawnQueue = emptyList<EnemyType>()
    private var spawnedInWave = 0
    private var spawnTimer = 0f
    private var phase = WavePhase.Ready

    init {
        reset()
    }

    val isFinished: Boolean
        get() = phase == WavePhase.Finished

    val canStartNextWave: Boolean
        get() = phase == WavePhase.Ready || phase == WavePhase.Cleared

    fun reset() {
        currentWaveIndex = 0
        spawnQueue = emptyList()
        spawnedInWave = 0
        spawnTimer = 0f
        phase = if (waves.isEmpty()) WavePhase.Finished else WavePhase.Ready
    }

    fun startNextWave(): Boolean {
        if (!canStartNextWave || currentWaveIndex !in waves.indices) return false
        spawnQueue = waves[currentWaveIndex].spawnQueue()
        spawnedInWave = 0
        spawnTimer = 0f
        phase = WavePhase.InProgress
        return true
    }

    fun update(deltaSeconds: Float): List<EnemyType> {
        if (phase != WavePhase.InProgress || currentWaveIndex !in waves.indices) {
            return emptyList()
        }

        val currentWave = waves[currentWaveIndex]
        spawnTimer -= deltaSeconds
        val spawns = mutableListOf<EnemyType>()

        while (spawnTimer <= 0f && spawnedInWave < spawnQueue.size) {
            spawns += spawnQueue[spawnedInWave]
            spawnedInWave++
            spawnTimer += currentWave.spawnInterval
        }

        return spawns
    }

    fun completeWaveIfCleared(hasAliveEnemies: Boolean) {
        if (phase != WavePhase.InProgress) return
        if (spawnedInWave < spawnQueue.size || hasAliveEnemies) return

        if (currentWaveIndex >= waves.lastIndex) {
            phase = WavePhase.Finished
        } else {
            currentWaveIndex++
            phase = WavePhase.Cleared
            spawnQueue = emptyList()
            spawnedInWave = 0
            spawnTimer = 0f
        }
    }

    fun snapshot(aliveEnemies: Int): WaveSnapshot {
        val currentWave = when {
            waves.isEmpty() -> 0
            phase == WavePhase.Finished -> waves.size
            else -> (currentWaveIndex + 1).coerceAtMost(waves.size)
        }
        val activeWave = waves.getOrNull(currentWaveIndex)
        val leftToSpawn = if (phase == WavePhase.InProgress) {
            (spawnQueue.size - spawnedInWave).coerceAtLeast(0)
        } else {
            0
        }
        val statusText = when (phase) {
            WavePhase.Ready -> if (currentWaveIndex == waves.lastIndex) "Final Wave Ready" else "Wave Ready"
            WavePhase.InProgress -> when {
                activeWave?.isBossWave == true -> "Boss Wave: ${activeWave.bossPreviewText()}"
                currentWaveIndex == waves.lastIndex -> "Final Wave"
                else -> "Wave In Progress"
            }
            WavePhase.Cleared -> if (currentWaveIndex == waves.lastIndex) "Final Wave Ready" else "Wave Cleared"
            WavePhase.Finished -> "Final Wave Cleared"
        }

        return WaveSnapshot(
            currentWave = currentWave,
            totalWaves = waves.size,
            enemiesLeftToSpawn = leftToSpawn,
            aliveEnemies = aliveEnemies,
            nextWaveInSeconds = 0f,
            phase = phase,
            awaitingNextWave = canStartNextWave,
            enemiesRemaining = leftToSpawn + aliveEnemies,
            isBossWave = activeWave?.isBossWave == true,
            nextWavePreview = activeWave?.previewText().orEmpty(),
            statusText = statusText,
        )
    }
}
