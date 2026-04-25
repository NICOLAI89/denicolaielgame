package com.nicolaielgame.game.model

import kotlin.math.abs
import kotlin.math.roundToInt

data class GridCell(
    val row: Int,
    val col: Int,
) {
    fun manhattanDistanceTo(other: GridCell): Int {
        return abs(row - other.row) + abs(col - other.col)
    }
}

fun gridDistance(row: Float, col: Float, cell: GridCell): Float {
    val dRow = row - cell.row
    val dCol = col - cell.col
    return kotlin.math.sqrt(dRow * dRow + dCol * dCol)
}

fun gridDistance(row: Float, col: Float, targetRow: Float, targetCol: Float): Float {
    val dRow = row - targetRow
    val dCol = col - targetCol
    return kotlin.math.sqrt(dRow * dRow + dCol * dCol)
}

fun Float.toCellIndex(maxExclusive: Int): Int {
    return roundToInt().coerceIn(0, maxExclusive - 1)
}

