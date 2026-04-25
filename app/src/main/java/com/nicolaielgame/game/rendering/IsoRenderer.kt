package com.nicolaielgame.game.rendering

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.GameMap
import com.nicolaielgame.game.model.GameState
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.Projectile
import com.nicolaielgame.game.model.Tower
import kotlin.math.abs
import kotlin.math.min

class IsoRenderer(private val map: GameMap) {
    fun draw(drawScope: DrawScope, state: GameState) = with(drawScope) {
        val layout = createLayout(size)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF071413), Color(0xFF123A35), Color(0xFF1D2334)),
            ),
        )

        drawBoardShadow(layout)
        for (cell in map.cells.sortedWith(compareBy<GridCell> { it.row + it.col }.thenBy { it.row })) {
            drawTile(cell, layout, state)
        }

        state.rangePreview?.let { preview ->
            drawRangePreview(preview.cell, preview.range, preview.color, layout)
        }
        state.towers.sortedBy { it.cell.row + it.cell.col }.forEach { tower ->
            drawTower(tower, layout)
        }
        state.enemies.sortedBy { it.row + it.col }.forEach { enemy ->
            drawEnemy(enemy, layout)
        }
        state.hitEffects.forEach { effect ->
            drawHitEffect(effect, layout)
        }
        state.projectiles.forEach { projectile ->
            drawProjectile(projectile, state.enemies, layout)
        }
        drawEndpointMarkers(layout)
    }

    fun cellForOffset(offset: Offset, canvasSize: Size): GridCell? {
        val layout = createLayout(canvasSize)
        return map.cells.firstOrNull { cell ->
            val center = cellCenter(cell, layout)
            val normalized = abs(offset.x - center.x) / (layout.tileWidth / 2f) +
                abs(offset.y - center.y) / (layout.tileHeight / 2f)
            normalized <= 1f
        }
    }

    private fun DrawScope.drawBoardShadow(layout: IsoLayout) {
        val top = cellCenter(GridCell(0, 0), layout)
        val right = cellCenter(GridCell(0, map.cols - 1), layout)
        val bottom = cellCenter(GridCell(map.rows - 1, map.cols - 1), layout)
        val left = cellCenter(GridCell(map.rows - 1, 0), layout)
        val shadow = Path().apply {
            moveTo(top.x, top.y + layout.tileHeight * 0.6f)
            lineTo(right.x + layout.tileWidth * 0.55f, right.y + layout.tileHeight * 0.6f)
            lineTo(bottom.x, bottom.y + layout.tileHeight * 1.25f)
            lineTo(left.x - layout.tileWidth * 0.55f, left.y + layout.tileHeight * 0.6f)
            close()
        }
        drawPath(shadow, Color.Black.copy(alpha = 0.28f))
    }

    private fun DrawScope.drawTile(
        cell: GridCell,
        layout: IsoLayout,
        state: GameState,
    ) {
        val center = cellCenter(cell, layout)
        val tile = diamondPath(center, layout.tileWidth, layout.tileHeight)
        val isPath = cell in state.pathPreview
        val isLocked = cell in map.buildLockedCells
        val isSelected = state.selectedCell == cell
        val baseColor = when {
            cell == map.spawn -> Color(0xFF1BC7A4)
            cell == map.base -> Color(0xFFF0B84E)
            isLocked -> Color(0xFF314E54)
            isPath -> Color(0xFF6B7D49)
            cell in map.scenicPath -> Color(0xFF415F49)
            else -> Color(0xFF244D47)
        }

        drawPath(tile, baseColor)
        drawPath(
            path = tile,
            color = Color.White.copy(alpha = if (isPath) 0.22f else 0.1f),
            style = Stroke(width = 1.4f),
        )

        if (isSelected) {
            drawPath(
                path = tile,
                color = if (state.placementAccepted) Color(0xFF78F5C8) else Color(0xFFFF5A73),
                style = Stroke(width = 4f),
            )
            drawPath(
                path = tile,
                color = if (state.placementAccepted) {
                    Color(0x3378F5C8)
                } else {
                    Color(0x33FF5A73)
                },
            )
        }
    }

    private fun DrawScope.drawEndpointMarkers(layout: IsoLayout) {
        val spawn = cellCenter(map.spawn, layout)
        val base = cellCenter(map.base, layout)

        drawCircle(
            color = Color(0xAA18E0B5),
            radius = layout.tileHeight * 0.38f,
            center = spawn,
            style = Stroke(width = 5f),
        )
        drawCircle(
            color = Color(0xFFB5FFE9),
            radius = layout.tileHeight * 0.13f,
            center = spawn,
        )

        drawCircle(
            color = Color.Black.copy(alpha = 0.24f),
            radius = layout.tileHeight * 0.42f,
            center = base + Offset(0f, layout.tileHeight * 0.16f),
        )
        drawCircle(
            color = Color(0xFFF6C55D),
            radius = layout.tileHeight * 0.34f,
            center = base,
        )
        drawCircle(
            color = Color(0xFF7C3F1D),
            radius = layout.tileHeight * 0.15f,
            center = base,
        )
    }

    private fun DrawScope.drawRangePreview(
        cell: GridCell,
        range: Float,
        color: Color,
        layout: IsoLayout,
    ) {
        val center = cellCenter(cell, layout)
        val radius = range * layout.tileWidth * 0.48f
        drawCircle(
            color = color.copy(alpha = 0.12f),
            radius = radius,
            center = center,
        )
        drawCircle(
            color = color.copy(alpha = 0.42f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.5f),
        )
    }

    private fun DrawScope.drawTower(tower: Tower, layout: IsoLayout) {
        val center = cellCenter(tower.cell, layout)
        val w = layout.tileWidth
        val h = layout.tileHeight

        drawOval(
            color = Color.Black.copy(alpha = 0.32f),
            topLeft = Offset(center.x - w * 0.25f, center.y - h * 0.02f),
            size = Size(w * 0.5f, h * 0.24f),
        )
        drawRoundRect(
            color = tower.type.primaryColor,
            topLeft = Offset(center.x - w * 0.16f, center.y - h * 0.72f),
            size = Size(w * 0.32f, h * 0.68f),
            cornerRadius = CornerRadius(8f, 8f),
        )
        drawRoundRect(
            color = tower.type.accentColor,
            topLeft = Offset(center.x - w * 0.09f, center.y - h * 0.62f),
            size = Size(w * 0.18f, h * 0.18f),
            cornerRadius = CornerRadius(6f, 6f),
        )
        drawCircle(
            color = Color(0xFFF6C55D),
            radius = h * 0.18f,
            center = Offset(center.x, center.y - h * 0.78f),
        )
        repeat(tower.level.coerceAtMost(4)) { index ->
            drawCircle(
                color = tower.type.accentColor,
                radius = h * 0.045f,
                center = Offset(
                    center.x - h * 0.16f + index * h * 0.105f,
                    center.y - h * 0.18f,
                ),
            )
        }
    }

    private fun DrawScope.drawEnemy(enemy: Enemy, layout: IsoLayout) {
        val center = gridToScreen(enemy.row, enemy.col, layout)
        val radius = layout.tileHeight * 0.23f
        val healthPercent = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)

        drawOval(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(center.x - radius * 1.2f, center.y + radius * 0.45f),
            size = Size(radius * 2.4f, radius * 0.65f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(enemy.type.accentColor, enemy.type.primaryColor),
                center = center - Offset(radius * 0.25f, radius * 0.25f),
                radius = radius * 1.4f,
            ),
            radius = radius,
            center = center - Offset(0f, radius * 0.25f),
        )
        if (enemy.isSlowed) {
            drawCircle(
                color = Color(0x99A9F1FF),
                radius = radius * 1.24f,
                center = center - Offset(0f, radius * 0.25f),
                style = Stroke(width = 3f),
            )
        }
        val barWidth = radius * 2.2f
        val barTopLeft = Offset(center.x - barWidth / 2f, center.y - radius * 1.55f)
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.55f),
            topLeft = barTopLeft,
            size = Size(barWidth, 5f),
            cornerRadius = CornerRadius(3f, 3f),
        )
        drawRoundRect(
            color = Color(0xFF7CFF6B),
            topLeft = barTopLeft,
            size = Size(barWidth * healthPercent, 5f),
            cornerRadius = CornerRadius(3f, 3f),
        )
    }

    private fun DrawScope.drawHitEffect(effect: com.nicolaielgame.game.model.HitEffect, layout: IsoLayout) {
        val center = gridToScreen(effect.row, effect.col, layout)
        val progress = (effect.age / effect.duration).coerceIn(0f, 1f)
        drawCircle(
            color = effect.color.copy(alpha = (1f - progress) * 0.72f),
            radius = layout.tileHeight * (0.18f + progress * 0.42f),
            center = center - Offset(0f, layout.tileHeight * 0.08f),
            style = Stroke(width = 2.8f),
        )
    }

    private fun DrawScope.drawProjectile(
        projectile: Projectile,
        enemies: List<Enemy>,
        layout: IsoLayout,
    ) {
        val target = enemies.firstOrNull { it.id == projectile.targetEnemyId }
        val current = gridToScreen(projectile.row, projectile.col, layout)
        val targetOffset = target?.let { gridToScreen(it.row, it.col, layout) } ?: current

        drawLine(
            color = projectile.towerType.accentColor.copy(alpha = 0.7f),
            start = current,
            end = targetOffset,
            strokeWidth = 3f,
        )
        drawCircle(
            color = projectile.towerType.accentColor,
            radius = layout.tileHeight * 0.11f,
            center = current,
        )
    }

    private fun createLayout(canvasSize: Size): IsoLayout {
        val boardWidthFactor = (map.rows + map.cols) / 2f
        val tileWidthByWidth = canvasSize.width * 0.88f / boardWidthFactor
        val tileWidthByHeight = canvasSize.height * 0.78f / (boardWidthFactor * 0.54f)
        val tileWidth = min(tileWidthByWidth, tileWidthByHeight).coerceAtLeast(36f)
        val tileHeight = tileWidth * 0.54f
        val boardHeight = boardWidthFactor * tileHeight
        val origin = Offset(
            x = canvasSize.width / 2f,
            y = (canvasSize.height - boardHeight).coerceAtLeast(0f) * 0.52f + tileHeight,
        )

        return IsoLayout(
            tileWidth = tileWidth,
            tileHeight = tileHeight,
            origin = origin,
        )
    }

    private fun cellCenter(cell: GridCell, layout: IsoLayout): Offset {
        return gridToScreen(cell.row.toFloat(), cell.col.toFloat(), layout)
    }

    private fun gridToScreen(row: Float, col: Float, layout: IsoLayout): Offset {
        return Offset(
            x = layout.origin.x + (col - row) * layout.tileWidth / 2f,
            y = layout.origin.y + (col + row) * layout.tileHeight / 2f,
        )
    }

    private fun diamondPath(center: Offset, width: Float, height: Float): Path {
        return Path().apply {
            moveTo(center.x, center.y - height / 2f)
            lineTo(center.x + width / 2f, center.y)
            lineTo(center.x, center.y + height / 2f)
            lineTo(center.x - width / 2f, center.y)
            close()
        }
    }

    private data class IsoLayout(
        val tileWidth: Float,
        val tileHeight: Float,
        val origin: Offset,
    )
}
