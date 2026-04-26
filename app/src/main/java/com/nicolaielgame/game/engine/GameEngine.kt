package com.nicolaielgame.game.engine

import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.AbilityEffect
import com.nicolaielgame.game.model.AbilityType
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
import com.nicolaielgame.game.model.RunStats
import com.nicolaielgame.game.model.TargetingMode
import com.nicolaielgame.game.model.Tower
import com.nicolaielgame.game.model.TowerType
import com.nicolaielgame.game.pathfinding.PathFinder
import com.nicolaielgame.game.systems.AbilitySystem
import com.nicolaielgame.game.systems.SilentSoundPlayer
import com.nicolaielgame.game.systems.SoundPlayer
import com.nicolaielgame.game.systems.TargetingSelector
import com.nicolaielgame.game.systems.WaveManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min
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
    private var nextAbilityEffectId = 1

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
        nextAbilityEffectId = 1
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

    fun setTowerTargetingMode(towerId: Int, mode: TargetingMode) {
        val current = mutableState.value
        val tower = current.towers.firstOrNull { it.id == towerId } ?: return
        mutableState.value = current.copy(
            towers = current.towers.map { existing ->
                if (existing.id == towerId) existing.copy(targetingMode = mode) else existing
            },
            selectedTowerId = towerId,
            selectedCell = tower.cell,
            rangePreview = RangePreview(tower.cell, tower.range, tower.type.accentColor),
            placementMessage = "${tower.type.shortLabel} targeting ${mode.title}.",
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
            runStats = current.runStats.copy(towersUpgraded = current.runStats.towersUpgraded + 1),
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
            runStats = current.runStats.copy(towersSold = current.runStats.towersSold + 1),
        )
        soundPlayer.towerSold()
    }

    fun startNextWave() {
        if (mutableState.value.status != GameStatus.Running) return
        if (waveManager.startNextWave()) {
            val snapshot = waveManager.snapshot(mutableState.value.enemies.size)
            val banner = if (snapshot.isBossWave) {
                "BOSS WAVE ${snapshot.currentWave}"
            } else if (snapshot.currentWave == snapshot.totalWaves) {
                "FINAL WAVE"
            } else {
                "WAVE ${snapshot.currentWave}"
            }
            mutableState.value = mutableState.value.copy(
                placementMessage = if (snapshot.isBossWave) {
                    "Boss wave ${snapshot.currentWave}: ${snapshot.nextWavePreview}."
                } else {
                    "Wave ${snapshot.currentWave} started."
                },
                placementAccepted = true,
                wave = snapshot,
                waveBanner = banner,
                waveBannerTimeRemaining = if (snapshot.isBossWave) 2.2f else 1.25f,
            )
            soundPlayer.waveStart()
            if (snapshot.isBossWave) {
                soundPlayer.bossWarning()
            }
        }
    }

    fun activateAbility(type: AbilityType) {
        val current = mutableState.value
        if (current.status != GameStatus.Running) return

        if (!current.abilities.canUse(type)) {
            mutableState.value = current.copy(
                placementMessage = "${type.shortLabel} is recharging.",
                placementAccepted = false,
            )
            soundPlayer.placementDenied()
            return
        }

        var enemies = current.enemies
        var gold = current.gold
        var score = current.score
        var bossesDefeated = current.bossesDefeated
        var runStats = current.runStats.withAbilityUsed(type)
        var shakeTime = current.shakeTimeRemaining
        var shakeIntensity = current.shakeIntensity
        val hitEffects = mutableListOf<HitEffect>()
        val abilityEffects = mutableListOf<AbilityEffect>()

        when (type) {
            AbilityType.MeteorStrike -> {
                val center = abilityCenterEnemy(enemies)
                if (center == null) {
                    mutableState.value = current.copy(
                        placementMessage = "Meteor needs enemies on the board.",
                        placementAccepted = false,
                    )
                    soundPlayer.placementDenied()
                    return
                }
                val affectedIds = AbilitySystem
                    .enemiesInRadius(enemies, center.row, center.col, AbilitySystem.MeteorRadius)
                    .map { it.id }
                    .toSet()
                enemies = enemies.map { enemy ->
                    if (enemy.id in affectedIds) {
                        val damage = effectiveDamage(AbilitySystem.MeteorDamage, enemy)
                        hitEffects += HitEffect(
                            id = nextHitEffectId++,
                            row = enemy.row,
                            col = enemy.col,
                            color = type.color,
                            label = "-${damage.toInt()}",
                            duration = 0.58f,
                        )
                        enemy.copy(health = enemy.health - damage, hitFlash = 0.2f)
                    } else {
                        enemy
                    }
                }
                abilityEffects += AbilityEffect(
                    id = nextAbilityEffectId++,
                    type = type,
                    row = center.row,
                    col = center.col,
                    duration = 1.05f,
                )
                shakeTime = 0.32f
                shakeIntensity = 5.2f
                soundPlayer.meteor()
                soundPlayer.enemyHit()
            }

            AbilityType.FreezePulse -> {
                val center = abilityCenterEnemy(enemies)
                if (center == null) {
                    mutableState.value = current.copy(
                        placementMessage = "Freeze needs enemies on the board.",
                        placementAccepted = false,
                    )
                    soundPlayer.placementDenied()
                    return
                }
                val affectedIds = AbilitySystem
                    .enemiesInRadius(enemies, center.row, center.col, AbilitySystem.FreezeRadius)
                    .map { it.id }
                    .toSet()
                enemies = enemies.map { enemy ->
                    if (enemy.id in affectedIds) {
                        val slowStrength = (1f - AbilitySystem.FreezeMultiplier) * enemy.type.slowVulnerability
                        enemy.copy(
                            slowMultiplier = min(enemy.slowMultiplier, 1f - slowStrength),
                            slowTimeRemaining = maxOf(
                                enemy.slowTimeRemaining,
                                AbilitySystem.FreezeDuration * enemy.type.slowVulnerability,
                            ),
                        )
                    } else {
                        enemy
                    }
                }
                hitEffects += HitEffect(
                    id = nextHitEffectId++,
                    row = center.row,
                    col = center.col,
                    color = type.color,
                    label = "Slow",
                    duration = 0.68f,
                )
                abilityEffects += AbilityEffect(
                    id = nextAbilityEffectId++,
                    type = type,
                    row = center.row,
                    col = center.col,
                    duration = 0.9f,
                )
                soundPlayer.freezePulse()
                soundPlayer.enemyHit()
            }

            AbilityType.EmergencyGold -> {
                gold += AbilitySystem.EmergencyGoldAmount
                hitEffects += HitEffect(
                    id = nextHitEffectId++,
                    row = map.base.row.toFloat(),
                    col = map.base.col.toFloat(),
                    color = type.color,
                    label = "+${AbilitySystem.EmergencyGoldAmount}g",
                    duration = 0.72f,
                )
                abilityEffects += AbilityEffect(
                    id = nextAbilityEffectId++,
                    type = type,
                    row = map.base.row.toFloat(),
                    col = map.base.col.toFloat(),
                    duration = 0.65f,
                )
                soundPlayer.emergencyGold()
                soundPlayer.towerSold()
            }
        }

        if (enemies.any { it.health <= 0f }) {
            val survivingEnemies = mutableListOf<Enemy>()
            for (enemy in enemies) {
                if (enemy.health <= 0f) {
                    gold += enemy.reward
                    score += enemy.scoreValue
                    runStats = runStats.withKill(enemy.type)
                    if (enemy.type.isBoss) {
                        bossesDefeated++
                        shakeTime = maxOf(shakeTime, 0.38f)
                        shakeIntensity = maxOf(shakeIntensity, 6.4f)
                        soundPlayer.bossDeath()
                    }
                    hitEffects += HitEffect(
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
        }

        mutableState.value = current.copy(
            enemies = enemies,
            gold = gold,
            score = score,
            bossesDefeated = bossesDefeated,
            hitEffects = current.hitEffects + hitEffects,
            abilityEffects = current.abilityEffects + abilityEffects,
            abilities = AbilitySystem.spendAbility(current.abilities, type),
            runStats = runStats,
            shakeTimeRemaining = shakeTime,
            shakeIntensity = shakeIntensity,
            placementMessage = "${type.title} activated.",
            placementAccepted = true,
        )
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
            runStats = current.runStats.copy(towersBuilt = current.runStats.towersBuilt + 1),
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
        var shakeTime = (current.shakeTimeRemaining - delta).coerceAtLeast(0f)
        var shakeIntensity = if (shakeTime > 0f) current.shakeIntensity else 0f
        var runStats = current.runStats.copy(timeSeconds = current.runStats.timeSeconds + delta)
        val blockedCells = current.towers.map { it.cell }.toSet()

        val abilities = AbilitySystem.tickCooldowns(current.abilities, delta)
        var enemies = current.enemies
            .map { enemy ->
                val regeneratedHealth = if (enemy.health > 0f && enemy.type.regenPerSecond > 0f) {
                    min(enemy.maxHealth, enemy.health + enemy.type.regenPerSecond * delta)
                } else {
                    enemy.health
                }
                enemy.copy(
                    health = regeneratedHealth,
                    slowTimeRemaining = (enemy.slowTimeRemaining - delta).coerceAtLeast(0f),
                    hitFlash = (enemy.hitFlash - delta).coerceAtLeast(0f),
                )
            }
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
                shakeTime = maxOf(shakeTime, 0.28f)
                shakeIntensity = maxOf(shakeIntensity, 4.5f)
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
                    shakeTime = maxOf(shakeTime, 0.42f)
                    shakeIntensity = maxOf(shakeIntensity, 6.8f)
                    soundPlayer.bossDeath()
                }
                runStats = runStats.withKill(enemy.type)
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
        val waveSnapshot = waveManager.snapshot(aliveEnemies = enemies.size)
        runStats = runStats.copy(wavesCompleted = maxOf(runStats.wavesCompleted, waveSnapshot.wavesCompleted))

        if (lives <= 0) {
            status = GameStatus.GameOver
            soundPlayer.gameOver()
        } else if (waveManager.isFinished && enemies.isEmpty()) {
            status = GameStatus.Victory
            score += difficulty.applyScore(250 + level.id * 75)
            gold += 50
            shakeTime = maxOf(shakeTime, 0.35f)
            shakeIntensity = maxOf(shakeIntensity, 4.2f)
            soundPlayer.victory()
        }

        val agedHitEffects = current.hitEffects
            .map { it.copy(age = it.age + delta) }
            .filter { it.age < it.duration }
        val agedAbilityEffects = current.abilityEffects
            .map { it.copy(age = it.age + delta) }
            .filter { it.age < it.duration }
        val bannerTime = (current.waveBannerTimeRemaining - delta).coerceAtLeast(0f)

        mutableState.value = current.copy(
            status = status,
            towers = towerFireResult.towers,
            enemies = enemies,
            projectiles = projectileResult.projectiles + towerFireResult.projectiles,
            hitEffects = agedHitEffects + projectileResult.hitEffects + deathEffects,
            abilityEffects = agedAbilityEffects,
            abilities = abilities,
            runStats = runStats,
            shakeTimeRemaining = shakeTime,
            shakeIntensity = shakeIntensity,
            waveBannerTimeRemaining = bannerTime,
            lives = lives.coerceAtLeast(0),
            gold = gold,
            score = score,
            bossesDefeated = bossesDefeated,
            pathPreview = current.pathPreview,
            wave = waveSnapshot,
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
            runStats = RunStats(levelId = level.id, difficulty = difficulty),
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

    private fun abilityCenterEnemy(enemies: List<Enemy>): Enemy? {
        return enemies.maxWithOrNull(
            compareBy<Enemy> { if (it.type.isBoss) 1 else 0 }
                .thenBy { it.pathIndex }
                .thenBy { it.health },
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
                    label = "-${effectiveDamage(projectile.damage, target).toInt()}",
                    duration = if (target.type.isBoss) 0.72f else 0.46f,
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
                val damage = effectiveDamage(hits.sumOf { it.damage.toDouble() }.toFloat(), enemy)
                val strongestSlow = hits
                    .filter { it.slowDuration > 0f && it.slowMultiplier < 1f }
                    .minByOrNull { it.slowMultiplier }
                val resistedSlow = strongestSlow?.let { slow ->
                    1f - ((1f - slow.slowMultiplier) * enemy.type.slowVulnerability)
                }
                val resistedDuration = strongestSlow?.slowDuration?.times(enemy.type.slowVulnerability) ?: 0f
                enemy.copy(
                    health = enemy.health - damage,
                    slowMultiplier = resistedSlow?.let { min(enemy.slowMultiplier, it) } ?: enemy.slowMultiplier,
                    slowTimeRemaining = maxOf(enemy.slowTimeRemaining, resistedDuration),
                    hitFlash = if (damage > 0f) 0.18f else enemy.hitFlash,
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
                .copy(aimBeamTimeRemaining = (tower.aimBeamTimeRemaining - deltaSeconds).coerceAtLeast(0f))
            if (cooledTower.cooldown > 0f) {
                updatedTowers += cooledTower
                continue
            }

            val target = TargetingSelector.selectTarget(cooledTower, enemies)

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
                updatedTowers += cooledTower.copy(
                    cooldown = cooledTower.fireInterval,
                    lastTargetEnemyId = target.id,
                    aimBeamTimeRemaining = 0.18f,
                )
                soundPlayer.towerShot()
            }
        }

        return TowerFireResult(updatedTowers, newProjectiles)
    }

    private fun effectiveDamage(baseDamage: Float, enemy: Enemy): Float {
        return baseDamage * enemy.type.damageTakenMultiplier
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

