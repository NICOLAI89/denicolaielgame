package com.nicolaielgame.game.pathfinding

import com.nicolaielgame.game.model.GameMap
import com.nicolaielgame.game.model.GridCell
import java.util.PriorityQueue

class PathFinder(private val map: GameMap) {
    fun findPath(blockedCells: Set<GridCell>): List<GridCell>? {
        return findPath(
            start = map.spawn,
            goal = map.base,
            blockedCells = blockedCells,
        )
    }

    fun findPath(
        start: GridCell,
        goal: GridCell,
        blockedCells: Set<GridCell>,
    ): List<GridCell>? {
        if (!map.isInside(start) || !map.isInside(goal)) return null
        if (goal in blockedCells) return null

        val open = PriorityQueue<SearchNode>(
            compareBy<SearchNode> { it.fScore }.thenBy { it.gScore },
        )
        val cameFrom = mutableMapOf<GridCell, GridCell>()
        val gScore = mutableMapOf(start to 0f)
        val closed = mutableSetOf<GridCell>()

        open.add(SearchNode(cell = start, gScore = 0f, fScore = heuristic(start, goal)))

        while (open.isNotEmpty()) {
            val current = open.poll().cell
            if (current == goal) {
                return reconstructPath(cameFrom, current)
            }
            if (!closed.add(current)) continue

            for (neighbor in map.neighborsOf(current)) {
                if (neighbor in blockedCells && neighbor != start && neighbor != goal) continue

                val tentativeG = (gScore[current] ?: Float.MAX_VALUE) + movementCost(neighbor)
                if (tentativeG < (gScore[neighbor] ?: Float.MAX_VALUE)) {
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeG
                    open.add(
                        SearchNode(
                            cell = neighbor,
                            gScore = tentativeG,
                            fScore = tentativeG + heuristic(neighbor, goal),
                        ),
                    )
                }
            }
        }

        return null
    }

    private fun movementCost(cell: GridCell): Float {
        return if (cell in map.scenicPath) 0.92f else 1f
    }

    private fun heuristic(from: GridCell, to: GridCell): Float {
        return from.manhattanDistanceTo(to).toFloat()
    }

    private fun reconstructPath(
        cameFrom: Map<GridCell, GridCell>,
        end: GridCell,
    ): List<GridCell> {
        val path = mutableListOf(end)
        var current = end
        while (current in cameFrom) {
            current = cameFrom.getValue(current)
            path += current
        }
        return path.asReversed()
    }

    private data class SearchNode(
        val cell: GridCell,
        val gScore: Float,
        val fScore: Float,
    )
}

