package com.nicolaielgame.game.model

data class GameMap(
    val rows: Int,
    val cols: Int,
    val spawn: GridCell,
    val base: GridCell,
    val buildLockedCells: Set<GridCell>,
    val scenicPath: Set<GridCell>,
) {
    val cells: List<GridCell> = buildList {
        repeat(rows) { row ->
            repeat(cols) { col ->
                add(GridCell(row, col))
            }
        }
    }

    fun isInside(cell: GridCell): Boolean {
        return cell.row in 0 until rows && cell.col in 0 until cols
    }

    fun neighborsOf(cell: GridCell): List<GridCell> {
        return listOf(
            GridCell(cell.row - 1, cell.col),
            GridCell(cell.row + 1, cell.col),
            GridCell(cell.row, cell.col - 1),
            GridCell(cell.row, cell.col + 1),
        ).filter(::isInside)
    }

    companion object {
        fun default(): GameMap {
            val spawn = GridCell(0, 4)
            val base = GridCell(8, 4)
            val scenicPath = (0..8).map { row -> GridCell(row, 4) }.toSet()
            val locked = setOf(
                spawn,
                base,
                GridCell(1, 4),
                GridCell(7, 4),
            )

            return GameMap(
                rows = 9,
                cols = 9,
                spawn = spawn,
                base = base,
                buildLockedCells = locked,
                scenicPath = scenicPath,
            )
        }
    }
}

