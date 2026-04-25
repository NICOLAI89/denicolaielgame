package com.nicolaielgame.game.systems

import com.nicolaielgame.game.model.EnemyType
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

    fun spawnQueue(): List<EnemyType> {
        return groups.flatMap { group -> List(group.count) { group.type } }
    }
}

class WaveManager(private val waves: List<WaveDefinition>) {
    private var currentWaveIndex = 0
    private var spawnQueue = emptyList<EnemyType>()
    private var spawnedInWave = 0
    private var spawnTimer = 0.8f
    private var waveActive = false
    private var awaitingNextWave = false

    init {
        reset()
    }

    val isFinished: Boolean
        get() = currentWaveIndex >= waves.size

    val canStartNextWave: Boolean
        get() = awaitingNextWave && !isFinished

    fun reset() {
        currentWaveIndex = 0
        spawnQueue = waves.firstOrNull()?.spawnQueue().orEmpty()
        spawnedInWave = 0
        spawnTimer = 0.8f
        waveActive = waves.isNotEmpty()
        awaitingNextWave = false
    }

    fun startNextWave(): Boolean {
        if (!canStartNextWave) return false
        spawnQueue = waves[currentWaveIndex].spawnQueue()
        spawnedInWave = 0
        spawnTimer = 0f
        waveActive = true
        awaitingNextWave = false
        return true
    }

    fun update(deltaSeconds: Float, hasAliveEnemies: Boolean): List<EnemyType> {
        if (isFinished) return emptyList()

        if (!waveActive) {
            if (!hasAliveEnemies && !awaitingNextWave) {
                currentWaveIndex++
                if (currentWaveIndex >= waves.size) return emptyList()
                awaitingNextWave = true
            }
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

        if (spawnedInWave >= spawnQueue.size) {
            waveActive = false
        }

        return spawns
    }

    fun snapshot(aliveEnemies: Int): WaveSnapshot {
        val displayWave = if (waves.isEmpty()) 0 else (currentWaveIndex + 1).coerceAtMost(waves.size)
        val leftToSpawn = if (isFinished || waves.isEmpty()) {
            0
        } else {
            (spawnQueue.size - spawnedInWave).coerceAtLeast(0)
        }

        return WaveSnapshot(
            currentWave = displayWave,
            totalWaves = waves.size,
            enemiesLeftToSpawn = leftToSpawn,
            aliveEnemies = aliveEnemies,
            nextWaveInSeconds = 0f,
            awaitingNextWave = canStartNextWave,
            enemiesRemaining = leftToSpawn + aliveEnemies,
        )
    }
}

