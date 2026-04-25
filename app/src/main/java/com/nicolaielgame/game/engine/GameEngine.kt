package com.nicolaielgame.game.engine

import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameMap
import com.nicolaielgame.game.model.GameState
import com.nicolaielgame.game.model.GameStatus
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.HitEffect
import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.game.model.LevelDefinition
import com.nicolaielgame.game.model.Projectile
import com.nicolaielgame.game.model.RangePreview
import com.nicolaielgame.game.model.Tower
import com.nicolaielgame.game.model.TowerType
import com.nicolaielgame.game.model.gridDistance
import com.nicolaielgame.game.pathfinding.PathFinder
import com.nicolaielgame.game.systems.SilentSoundPlayer
import com.nicolaielgame.game.systems.SoundPlayer
import com.nicolaielgame.game.systems.WaveManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class GameEngine(
    initialLevel: LevelDefinition = LevelCatalog.firstLevel,
    initialDifficulty: DifficultyMode = DifficultyMode.Normal,
    private val soundPlayer: SoundPlayer = SilentSoundPlayer,
) {
    private var level = initialLevel
    private var difficulty = initialDifficulty
    private var map = level.map
    private var pathFinder = PathFinder(map)
    private var waveManager = WaveManager(level.waves)
    private val mutableState = MutableStateFlow(createInitialState(bestScore = 0))

    private var nextTowerId = 1
    private var nextEnemyId = 1
    private var nextProjectileId = 1
    private var nextHitEffectId = 1

    val state: StateFlow<GameState> = mutableState.asStateFlow()

    fun reset(
        bestScore: Int = mutableState.value.bestScore,
        levelDefinition: LevelDefinition = level,
        difficultyMode: DifficultyMode = difficulty,
    ) {
        level = levelDefinition
        difficulty = difficultyMode
        map = level.map
        pathFinder = PathFinder(map)
        waveManager = WaveManager(level.waves)
        nextTowerId = 1
        nextEnemyId = 1
        nextProjectileId = 1
        nextHitEffectId = 1
        mutableState.value = createInitialState(bestScore)
    }

    fun setBestScore(bestScore: Int) {
        val current = mutableState.value
        if (current.bestScore != bestScore) {
            mutableState.value = current.copy(bestScore = bestScore)
        }
    }

    fun selectTowerType(type: TowerType) {
        val current = mutableState.value
        val previewCell = current.selectedCell?.takeIf { cell ->
            current.towers.none { it.cell == cell }
        }
        mutableState.value = current.copy(
            selectedTowerType = type,
            selectedTowerId = null,
            rangePreview = previewCell?.let { RangePreview(it, type.statsForLevel(1).range, type.accentColor) },
            placementMessage = "${type.shortLabel} selected. Tap a buildable tile.",
            placementAccepted = true,
        )
    }

    fun handleCellTap(cell: GridCell) {
        val tower = mutableState.value.towers.firstOrNull { it.cell == cell }
        if (tower != null) {
            selectTower(tower.id)
        } else {
            placeTower(cell)
        }
    }

    fun selectTower(towerId: Int) {
        val current = mutableState.value
        val tower = current.towers.firstOrNull { it.id == towerId } ?: return
        mutableState.value = current.copy(
            selectedCell = tower.cell,
            selectedTowerId = tower.id,
            rangePreview = RangePreview(tower.cell, tower.range, tower.type.accentColor),
            placementMessage = "${tower.type.shortLabel} tower selected.",
            placementAccepted = true,
        )
    }

    fun upgradeTower(towerId: Int) {
        val current = mutableState.value
        if (current.status != GameStatus.Running) return
        val tower = current.towers.firstOrNull { it.id == towerId } ?: return
        val cost = tower.upgradeCost

        if (current.gold < cost) {
            mutableState.value = current.copy(
                selectedTowerId = towerId,
                selectedCell = tower.cell,
                rangePreview = RangePreview(tower.cell, tower.range, tower.type.accentColor),
                placementMessage = "Not enough gold to upgrade.",
                placementAccepted = false,
            )
            soundPlayer.placementDenied()
            return
        }

        val upgraded = tower.upgraded()
        mutableState.value = current.copy(
            towers = current.towers.map { if (it.id == towerId) upgraded else it },
            gold = current.gold - cost,
            selectedTowerId = towerId,
            selectedCell = upgraded.cell,
            rangePreview = RangePreview(upgraded.cell, upgraded.range, upgraded.type.accentColor),
            placementMessage = "${upgraded.type.shortLabel} upgraded to level ${upgraded.level}.",
            placementAccepted = true,
        )
        soundPlayer.towerUpgraded()
    }

    fun sellTower(towerId: Int) {
        val current = mutableState.value
        if (current.status != GameStatus.Running) return
        val tower = current.towers.firstOrNull { it.id == towerId } ?: return
        val remainingTowers = current.towers.filterNot { it.id == towerId }
        val blockedCells = remainingTowers.map { it.cell }.toSet()

        mutableState.value = current.copy(
            towers = remainingTowers,
            enemies = recalculateEnemyPaths(current.enemies, blockedCells) ?: current.enemies,
            gold = current.gold + tower.sellRefund(),
            pathPreview = pathFinder.findPath(blockedCells).orEmpty(),
            selectedTowerId = null,
            selectedCell = tower.cell,
            rangePreview = null,
            placementMessage = "Sold ${tower.type.shortLabel} for ${tower.sellRefund()}g.",
            placementAccepted = true,
        )
        soundPlayer.towerSold()
    }

    fun startNextWave() {
        if (mutableState.value.status != GameStatus.Running) return
        if (waveManager.startNextWave()) {
            val snapshot = waveManager.snapshot(mutableState.value.enemies.size)
            mutableState.value = mutableState.value.copy(
                placementMessage = if (snapshot.isBossWave) {
                    "Boss wave ${snapshot.currentWave} started."
                } else {
                    "Wave ${snapshot.currentWave} started."
                },
                placementAccepted = true,
                wave = snapshot,
            )
            soundPlayer.waveStart()
        }
    }

    fun pause() {
        val current = mutableState.value
        if (current.status == GameStatus.Running) {
            mutableState.value = current.copy(status = GameStatus.Paused)
        }
    }

    fun resume() {
        val current = mutableState.value
        if (current.status == GameStatus.Paused) {
            mutableState.value = current.copy(status = GameStatus.Running)
        }
    }

    fun placeTower(cell: GridCell) {
        val current = mutableState.value
        if (current.status != GameStatus.Running) return
        val type = current.selectedTowerType

        fun reject(message: String) {
            mutableState.value = current.copy(
                selectedCell = cell,
                selectedTowerId = null,
                rangePreview = RangePreview(cell, type.statsForLevel(1).range, type.accentColor),
                placementMessage = message,
                placementAccepted = false,
            )
            soundPlayer.placementDenied()
        }

        if (!map.isInside(cell)) {
            reject("That tile is outside the board.")
            return
        }

        if (cell in map.buildLockedCells) {
            reject("Spawn, base, and gate tiles stay open.")
            return
        }

        val occupiedByTowers = current.towers.map { it.cell }.toSet()
        if (cell in occupiedByTowers) {
            reject("A tower is already standing there.")
            return
        }

        if (current.enemies.any { it.currentCell(map) == cell }) {
            reject("An enemy is moving through that tile.")
            return
        }

        if (current.gold < type.baseCost) {
            reject("Not enough gold for ${type.shortLabel}.")
            return
        }

        val blockedCells = occupiedByTowers + cell
        val newSpawnPath = pathFinder.findPath(blockedCells)
        if (newSpawnPath == null) {
            reject("That would completely block the enemy path.")
            return
        }

        val recalculatedEnemies = recalculateEnemyPaths(current.enemies, blockedCells)
        if (recalculatedEnemies == null) {
            reject("That would trap an enemy with no route.")
            return
        }

        val tower = Tower(
            id = nextTowerId++,
            cell = cell,
            type = type,
            totalInvested = type.baseCost,
        )
        mutableState.value = current.copy(
            towers = current.towers + tower,
            enemies = recalculatedEnemies,
            pathPreview = newSpawnPath,
            selectedCell = cell,
            selectedTowerId = tower.id,
            rangePreview = RangePreview(cell, tower.range, tower.type.accentColor),
            placementMessage = "${type.shortLabel} placed. Enemies rerouted.",
            placementAccepted = true,
            gold = current.gold - type.baseCost,
            towersPlacedByType = current.towersPlacedByType + (type to ((current.towersPlacedByType[type] ?: 0) + 1)),
        )
        soundPlayer.towerPlaced()
    }

    fun tick(deltaSeconds: Float) {
        val current = mutableState.value
        if (current.status != GameStatus.Running) return

        val delta = deltaSeconds.coerceIn(0f, 0.05f)
        var lives = current.lives
        var gold = current.gold
        var score = current.score
        var status = current.status
        val blockedCells = current.towers.map { it.cell }.toSet()

        var enemies = current.enemies
            .map { it.copy(slowTimeRemaining = (it.slowTimeRemaining - delta).coerceAtLeast(0f)) }
            .toMutableList()

        val spawnRequests = waveManager.update(delta)
        for (enemyType in spawnRequests) {
            val path = pathFinder.findPath(blockedCells) ?: continue
            enemies += createEnemy(enemyType, path)
        }

        val movedEnemies = mutableListOf<Enemy>()
        for (enemy in enemies) {
            val move = moveEnemy(enemy, delta, blockedCells)
            if (move.reachedBase) {
                lives--
                soundPlayer.baseHit()
            } else {
                movedEnemies += move.enemy
            }
        }
        enemies = movedEnemies

        val projectileResult = updateProjectiles(
            projectiles = current.projectiles,
            enemies = enemies,
            deltaSeconds = delta,
        )
        enemies = applyProjectileHits(enemies, projectileResult.hitsByEnemyId).toMutableList()

        val survivingEnemies = mutableListOf<Enemy>()
        val deathEffects = mutableListOf<HitEffect>()
        var bossesDefeated = current.bossesDefeated
        for (enemy in enemies) {
            if (enemy.health <= 0f) {
                gold += enemy.reward
                score += enemy.scoreValue
                if (enemy.type.isBoss) {
                    bossesDefeated++
                }
                deathEffects += HitEffect(
                    id = nextHitEffectId++,
                    row = enemy.row,
                    col = enemy.col,
                    color = enemy.type.accentColor,
                    label = "+${enemy.reward}",
                    duration = if (enemy.type.isBoss) 0.8f else 0.48f,
                )
                soundPlayer.enemyDown()
            } else {
                survivingEnemies += enemy
            }
        }
        enemies = survivingEnemies

        val towerFireResult = updateTowers(
            towers = current.towers,
            enemies = enemies,
            deltaSeconds = delta,
        )
        waveManager.completeWaveIfCleared(hasAliveEnemies = enemies.isNotEmpty())

        if (lives <= 0) {
            status = GameStatus.GameOver
            soundPlayer.gameOver()
        } else if (waveManager.isFinished && enemies.isEmpty()) {
            status = GameStatus.Victory
            score += difficulty.applyScore(250 + level.id * 75)
            gold += 50
            soundPlayer.victory()
        }

        val agedHitEffects = current.hitEffects
            .map { it.copy(age = it.age + delta) }
            .filter { it.age < it.duration }

        mutableState.value = current.copy(
            status = status,
            towers = towerFireResult.towers,
            enemies = enemies,
            projectiles = projectileResult.projectiles + towerFireResult.projectiles,
            hitEffects = agedHitEffects + projectileResult.hitEffects + deathEffects,
            lives = lives.coerceAtLeast(0),
            gold = gold,
            score = score,
            bossesDefeated = bossesDefeated,
            pathPreview = pathFinder.findPath(blockedCells).orEmpty(),
            wave = waveManager.snapshot(aliveEnemies = enemies.size),
        )
    }

    private fun createInitialState(bestScore: Int): GameState {
        return GameState(
            map = map,
            level = level,
            difficulty = difficulty,
            bestScore = bestScore,
            lives = difficulty.applyStartingLives(level.startingLives),
            gold = difficulty.applyStartingGold(level.startingGold),
            selectedTowerType = TowerType.Basic,
            pathPreview = pathFinder.findPath(emptySet()).orEmpty(),
            wave = waveManager.snapshot(aliveEnemies = 0),
        )
    }

    private fun createEnemy(enemyType: EnemyType, path: List<GridCell>): Enemy {
        return Enemy(
            id = nextEnemyId++,
            type = enemyType,
            row = map.spawn.row.toFloat(),
            col = map.spawn.col.toFloat(),
            path = path,
            pathIndex = nextPathIndex(path),
            health = difficulty.applyEnemyHealth(enemyType.maxHealth),
            maxHealth = difficulty.applyEnemyHealth(enemyType.maxHealth),
            speed = difficulty.applyEnemySpeed(enemyType.speed),
            reward = difficulty.applyReward(enemyType.reward),
            scoreValue = difficulty.applyScore(enemyType.scoreValue),
        )
    }

    private fun recalculateEnemyPaths(
        enemies: List<Enemy>,
        blockedCells: Set<GridCell>,
    ): List<Enemy>? {
        return enemies.map { enemy ->
            val newPath = pathFinder.findPath(
                start = enemy.currentCell(map),
                goal = map.base,
                blockedCells = blockedCells,
            ) ?: return null
            enemy.copy(path = newPath, pathIndex = nextPathIndex(newPath))
        }
    }

    private fun nextPathIndex(path: List<GridCell>): Int {
        return if (path.size > 1) 1 else 0
    }

    private fun moveEnemy(
        enemy: Enemy,
        deltaSeconds: Float,
        blockedCells: Set<GridCell>,
    ): EnemyMoveResult {
        var path = enemy.path
        var pathIndex = enemy.pathIndex
        var row = enemy.row
        var col = enemy.col
        var remaining = enemy.effectiveSpeed * deltaSeconds

        if (path.isEmpty() || pathIndex >= path.size) {
            path = pathFinder.findPath(enemy.currentCell(map), map.base, blockedCells).orEmpty()
            pathIndex = nextPathIndex(path)
        }

        while (remaining > 0f && pathIndex < path.size) {
            val target = path[pathIndex]
            val dRow = target.row - row
            val dCol = target.col - col
            val distance = sqrt(dRow * dRow + dCol * dCol)

            if (distance < 0.001f) {
                if (target == map.base) {
                    return EnemyMoveResult(enemy, reachedBase = true)
                }
                pathIndex++
                continue
            }

            if (remaining >= distance) {
                row = target.row.toFloat()
                col = target.col.toFloat()
                remaining -= distance
                if (target == map.base) {
                    return EnemyMoveResult(enemy, reachedBase = true)
                }
                pathIndex++
            } else {
                row += dRow / distance * remaining
                col += dCol / distance * remaining
                remaining = 0f
            }
        }

        return EnemyMoveResult(
            enemy = enemy.copy(row = row, col = col, path = path, pathIndex = pathIndex),
            reachedBase = false,
        )
    }

    private fun updateProjectiles(
        projectiles: List<Projectile>,
        enemies: List<Enemy>,
        deltaSeconds: Float,
    ): ProjectileUpdateResult {
        val enemiesById = enemies.associateBy { it.id }
        val keptProjectiles = mutableListOf<Projectile>()
        val hits = mutableMapOf<Int, MutableList<ProjectileHit>>()
        val hitEffects = mutableListOf<HitEffect>()

        for (projectile in projectiles) {
            val target = enemiesById[projectile.targetEnemyId] ?: continue
            val dRow = target.row - projectile.row
            val dCol = target.col - projectile.col
            val distance = sqrt(dRow * dRow + dCol * dCol)
            val step = projectile.speed * deltaSeconds

            if (distance <= 0.16f || step >= distance) {
                hits.getOrPut(target.id) { mutableListOf() } += ProjectileHit(
                    damage = projectile.damage,
                    slowMultiplier = projectile.slowMultiplier,
                    slowDuration = projectile.slowDuration,
                )
                hitEffects += HitEffect(
                    id = nextHitEffectId++,
                    row = target.row,
                    col = target.col,
                    color = projectile.towerType.accentColor,
                    label = "-${projectile.damage.toInt()}",
                )
                soundPlayer.enemyHit()
            } else {
                keptProjectiles += projectile.copy(
                    row = projectile.row + dRow / distance * step,
                    col = projectile.col + dCol / distance * step,
                )
            }
        }

        return ProjectileUpdateResult(
            projectiles = keptProjectiles,
            hitsByEnemyId = hits,
            hitEffects = hitEffects,
        )
    }

    private fun applyProjectileHits(
        enemies: List<Enemy>,
        hitsByEnemyId: Map<Int, List<ProjectileHit>>,
    ): List<Enemy> {
        if (hitsByEnemyId.isEmpty()) return enemies
        return enemies.map { enemy ->
            val hits = hitsByEnemyId[enemy.id].orEmpty()
            if (hits.isEmpty()) {
                enemy
            } else {
                val damage = hits.sumOf { it.damage.toDouble() }.toFloat()
                val strongestSlow = hits
                    .filter { it.slowDuration > 0f && it.slowMultiplier < 1f }
                    .minByOrNull { it.slowMultiplier }
                enemy.copy(
                    health = enemy.health - damage,
                    slowMultiplier = strongestSlow?.slowMultiplier ?: enemy.slowMultiplier,
                    slowTimeRemaining = maxOf(enemy.slowTimeRemaining, strongestSlow?.slowDuration ?: 0f),
                )
            }
        }
    }

    private fun updateTowers(
        towers: List<Tower>,
        enemies: List<Enemy>,
        deltaSeconds: Float,
    ): TowerFireResult {
        val updatedTowers = mutableListOf<Tower>()
        val newProjectiles = mutableListOf<Projectile>()

        for (tower in towers) {
            val cooledTower = tower.copy(cooldown = (tower.cooldown - deltaSeconds).coerceAtLeast(0f))
            if (cooledTower.cooldown > 0f) {
                updatedTowers += cooledTower
                continue
            }

            val target = enemies
                .filter { enemy -> gridDistance(enemy.row, enemy.col, cooledTower.cell) <= cooledTower.range }
                .minByOrNull { enemy -> enemy.pathIndex }

            if (target == null) {
                updatedTowers += cooledTower
            } else {
                val stats = cooledTower.stats
                newProjectiles += Projectile(
                    id = nextProjectileId++,
                    towerType = cooledTower.type,
                    row = cooledTower.cell.row.toFloat(),
                    col = cooledTower.cell.col.toFloat(),
                    targetEnemyId = target.id,
                    damage = stats.damage,
                    slowMultiplier = stats.slowMultiplier,
                    slowDuration = stats.slowDuration,
                    speed = stats.projectileSpeed,
                )
                updatedTowers += cooledTower.copy(cooldown = cooledTower.fireInterval)
                soundPlayer.towerShot()
            }
        }

        return TowerFireResult(updatedTowers, newProjectiles)
    }

    private data class EnemyMoveResult(
        val enemy: Enemy,
        val reachedBase: Boolean,
    )

    private data class ProjectileHit(
        val damage: Float,
        val slowMultiplier: Float,
        val slowDuration: Float,
    )

    private data class ProjectileUpdateResult(
        val projectiles: List<Projectile>,
        val hitsByEnemyId: Map<Int, List<ProjectileHit>>,
        val hitEffects: List<HitEffect>,
    )

    private data class TowerFireResult(
        val towers: List<Tower>,
        val projectiles: List<Projectile>,
    )
}

