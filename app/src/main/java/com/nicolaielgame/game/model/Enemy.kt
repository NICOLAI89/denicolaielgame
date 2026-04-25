package com.nicolaielgame.game.model

data class Enemy(
    val id: Int,
    val type: EnemyType = EnemyType.Normal,
    val row: Float,
    val col: Float,
    val path: List<GridCell>,
    val pathIndex: Int,
    val health: Float,
    val maxHealth: Float,
    val speed: Float,
    val reward: Int,
    val scoreValue: Int = reward + 10,
    val slowTimeRemaining: Float = 0f,
    val slowMultiplier: Float = 1f,
) {
    val isSlowed: Boolean
        get() = slowTimeRemaining > 0f && slowMultiplier < 1f

    val effectiveSpeed: Float
        get() = speed * if (isSlowed) slowMultiplier else 1f

    fun currentCell(map: GameMap): GridCell {
        return GridCell(
            row = row.toCellIndex(map.rows),
            col = col.toCellIndex(map.cols),
        )
    }
}
