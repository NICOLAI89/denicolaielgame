package com.nicolaielgame.game.model

import com.nicolaielgame.game.systems.WaveDefinition
import com.nicolaielgame.game.systems.WaveEnemyGroup

data class LevelDefinition(
    val id: Int,
    val title: String,
    val description: String,
    val map: GameMap,
    val startingGold: Int,
    val startingLives: Int,
    val waves: List<WaveDefinition>,
) {
    val totalWaves: Int
        get() = waves.size
}

object LevelCatalog {
    val levels: List<LevelDefinition> = listOf(
        levelOne(),
        levelTwo(),
        levelThree(),
        levelFour(),
        levelFive(),
    )

    val firstLevel: LevelDefinition = levels.first()

    fun find(levelId: Int): LevelDefinition {
        return levels.firstOrNull { it.id == levelId } ?: firstLevel
    }

    fun nextLevelId(levelId: Int): Int {
        return (levelId + 1).coerceAtMost(levels.last().id)
    }

    private fun levelOne(): LevelDefinition {
        val spawn = GridCell(0, 4)
        val base = GridCell(8, 4)
        val scenicPath = (0..8).map { row -> GridCell(row, 4) }.toSet()
        val locked = setOf(spawn, base, GridCell(1, 4), GridCell(7, 4))

        return LevelDefinition(
            id = 1,
            title = "Green Run",
            description = "A clean route for learning tower placement.",
            map = GameMap(
                rows = 9,
                cols = 9,
                spawn = spawn,
                base = base,
                buildLockedCells = locked,
                scenicPath = scenicPath,
            ),
            startingGold = 160,
            startingLives = 22,
            waves = listOf(
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 6)), spawnInterval = 0.9f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 7), WaveEnemyGroup(EnemyType.Fast, 3)), spawnInterval = 0.78f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 8), WaveEnemyGroup(EnemyType.Normal, 5)), spawnInterval = 0.72f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 8), WaveEnemyGroup(EnemyType.Tank, 2)), spawnInterval = 0.74f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 8), WaveEnemyGroup(EnemyType.Tank, 3), WaveEnemyGroup(EnemyType.Normal, 6)), spawnInterval = 0.64f),
            ),
        )
    }

    private fun levelTwo(): LevelDefinition {
        val spawn = GridCell(0, 2)
        val base = GridCell(9, 7)
        val scenicPath = listOf(
            GridCell(0, 2), GridCell(1, 2), GridCell(2, 2), GridCell(3, 2),
            GridCell(3, 3), GridCell(3, 4), GridCell(4, 4), GridCell(5, 4),
            GridCell(5, 5), GridCell(6, 5), GridCell(7, 5), GridCell(7, 6),
            GridCell(8, 6), GridCell(9, 6), GridCell(9, 7),
        ).toSet()
        val locked = scenicPath.filter { it.row in setOf(0, 1, 8, 9) || it.col == 2 }.toSet() +
            setOf(base, GridCell(4, 0), GridCell(5, 8))

        return LevelDefinition(
            id = 2,
            title = "Switchback",
            description = "A wider map where reroutes start to matter.",
            map = GameMap(
                rows = 10,
                cols = 10,
                spawn = spawn,
                base = base,
                buildLockedCells = locked,
                scenicPath = scenicPath,
            ),
            startingGold = 170,
            startingLives = 18,
            waves = listOf(
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 8), WaveEnemyGroup(EnemyType.Fast, 4)), spawnInterval = 0.76f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 10), WaveEnemyGroup(EnemyType.Normal, 6)), spawnInterval = 0.66f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Tank, 3), WaveEnemyGroup(EnemyType.Normal, 10)), spawnInterval = 0.72f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 12), WaveEnemyGroup(EnemyType.Tank, 3)), spawnInterval = 0.58f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 10), WaveEnemyGroup(EnemyType.Fast, 8), WaveEnemyGroup(EnemyType.Tank, 5), WaveEnemyGroup(EnemyType.Boss, 1)), spawnInterval = 0.56f),
            ),
        )
    }

    private fun levelThree(): LevelDefinition {
        val spawn = GridCell(1, 0)
        val base = GridCell(8, 10)
        val scenicPath = listOf(
            GridCell(1, 0), GridCell(1, 1), GridCell(1, 2), GridCell(2, 2),
            GridCell(3, 2), GridCell(4, 2), GridCell(4, 3), GridCell(4, 4),
            GridCell(3, 4), GridCell(2, 4), GridCell(2, 5), GridCell(3, 5),
            GridCell(4, 5), GridCell(5, 5), GridCell(6, 5), GridCell(6, 6),
            GridCell(6, 7), GridCell(7, 7), GridCell(8, 7), GridCell(8, 8),
            GridCell(8, 9), GridCell(8, 10),
        ).toSet()
        val locked = setOf(
            spawn, base, GridCell(1, 1), GridCell(8, 9),
            GridCell(0, 5), GridCell(1, 5), GridCell(9, 5), GridCell(10, 5),
            GridCell(5, 1), GridCell(5, 9),
        )

        return LevelDefinition(
            id = 3,
            title = "Crucible",
            description = "Tighter lanes and heavier waves reward smart upgrades.",
            map = GameMap(
                rows = 11,
                cols = 11,
                spawn = spawn,
                base = base,
                buildLockedCells = locked,
                scenicPath = scenicPath,
            ),
            startingGold = 195,
            startingLives = 16,
            waves = listOf(
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 10), WaveEnemyGroup(EnemyType.Normal, 6)), spawnInterval = 0.62f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Tank, 4), WaveEnemyGroup(EnemyType.Fast, 8)), spawnInterval = 0.66f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 12), WaveEnemyGroup(EnemyType.Tank, 5)), spawnInterval = 0.58f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 14), WaveEnemyGroup(EnemyType.Tank, 5)), spawnInterval = 0.5f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 12), WaveEnemyGroup(EnemyType.Fast, 10), WaveEnemyGroup(EnemyType.Tank, 5), WaveEnemyGroup(EnemyType.Juggernaut, 1)), spawnInterval = 0.5f),
            ),
        )
    }

    private fun levelFour(): LevelDefinition {
        val spawn = GridCell(0, 5)
        val base = GridCell(11, 6)
        val scenicPath = listOf(
            GridCell(0, 5), GridCell(1, 5), GridCell(2, 5), GridCell(2, 4),
            GridCell(3, 4), GridCell(4, 4), GridCell(4, 5), GridCell(4, 6),
            GridCell(5, 6), GridCell(6, 6), GridCell(6, 5), GridCell(6, 4),
            GridCell(7, 4), GridCell(8, 4), GridCell(8, 5), GridCell(8, 6),
            GridCell(9, 6), GridCell(10, 6), GridCell(11, 6),
        ).toSet()
        val locked = setOf(
            spawn, base, GridCell(1, 5), GridCell(10, 6),
            GridCell(3, 1), GridCell(3, 10), GridCell(7, 1), GridCell(7, 10),
        )

        return LevelDefinition(
            id = 4,
            title = "Crosswinds",
            description = "Multiple reroute options reward careful targeting and abilities.",
            map = GameMap(
                rows = 12,
                cols = 12,
                spawn = spawn,
                base = base,
                buildLockedCells = locked,
                scenicPath = scenicPath,
            ),
            startingGold = 220,
            startingLives = 15,
            waves = listOf(
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 14), WaveEnemyGroup(EnemyType.Normal, 10)), spawnInterval = 0.52f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Tank, 5), WaveEnemyGroup(EnemyType.Fast, 12)), spawnInterval = 0.58f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 18), WaveEnemyGroup(EnemyType.Tank, 5)), spawnInterval = 0.5f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 18), WaveEnemyGroup(EnemyType.Tank, 6), WaveEnemyGroup(EnemyType.Boss, 1)), spawnInterval = 0.46f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 16), WaveEnemyGroup(EnemyType.Fast, 14), WaveEnemyGroup(EnemyType.Tank, 7), WaveEnemyGroup(EnemyType.Regenerator, 1)), spawnInterval = 0.44f),
            ),
        )
    }

    private fun levelFive(): LevelDefinition {
        val spawn = GridCell(1, 0)
        val base = GridCell(10, 12)
        val scenicPath = listOf(
            GridCell(1, 0), GridCell(1, 1), GridCell(2, 1), GridCell(3, 1),
            GridCell(3, 2), GridCell(3, 3), GridCell(4, 3), GridCell(5, 3),
            GridCell(5, 4), GridCell(5, 5), GridCell(4, 5), GridCell(3, 5),
            GridCell(3, 6), GridCell(4, 6), GridCell(5, 6), GridCell(6, 6),
            GridCell(7, 6), GridCell(7, 7), GridCell(7, 8), GridCell(8, 8),
            GridCell(9, 8), GridCell(9, 9), GridCell(9, 10), GridCell(10, 10),
            GridCell(10, 11), GridCell(10, 12),
        ).toSet()
        val locked = setOf(
            spawn, base, GridCell(1, 1), GridCell(10, 11),
            GridCell(0, 6), GridCell(1, 6), GridCell(11, 6), GridCell(12, 6),
            GridCell(6, 2), GridCell(6, 10), GridCell(2, 9), GridCell(9, 2),
        )

        return LevelDefinition(
            id = 5,
            title = "Last Gate",
            description = "A tight economy and boss mechanics make every tile matter.",
            map = GameMap(
                rows = 13,
                cols = 13,
                spawn = spawn,
                base = base,
                buildLockedCells = locked,
                scenicPath = scenicPath,
            ),
            startingGold = 245,
            startingLives = 15,
            waves = listOf(
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 16), WaveEnemyGroup(EnemyType.Normal, 12)), spawnInterval = 0.5f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Tank, 7), WaveEnemyGroup(EnemyType.Fast, 14)), spawnInterval = 0.54f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 20), WaveEnemyGroup(EnemyType.Tank, 7), WaveEnemyGroup(EnemyType.Boss, 1)), spawnInterval = 0.46f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Fast, 22), WaveEnemyGroup(EnemyType.Tank, 8), WaveEnemyGroup(EnemyType.Juggernaut, 1)), spawnInterval = 0.42f),
                WaveDefinition(groups = listOf(WaveEnemyGroup(EnemyType.Normal, 18), WaveEnemyGroup(EnemyType.Fast, 18), WaveEnemyGroup(EnemyType.Tank, 9), WaveEnemyGroup(EnemyType.Juggernaut, 1), WaveEnemyGroup(EnemyType.Regenerator, 1)), spawnInterval = 0.4f),
            ),
        )
    }
}

