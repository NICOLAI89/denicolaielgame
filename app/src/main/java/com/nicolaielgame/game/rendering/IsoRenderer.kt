package com.nicolaielgame.game.rendering

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.nicolaielgame.game.model.AbilityType
import com.nicolaielgame.game.model.Enemy
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.GameMap
import com.nicolaielgame.game.model.GameState
import com.nicolaielgame.game.model.GridCell
import com.nicolaielgame.game.model.Projectile
import com.nicolaielgame.game.model.Tower
import com.nicolaielgame.game.model.TowerType
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class IsoRenderer(private val map: GameMap) {
    private val drawCells = map.cells.sortedWith(compareBy<GridCell> { it.row + it.col }.thenBy { it.row })

    fun draw(
        drawScope: DrawScope,
        state: GameState,
        showGrid: Boolean = true,
        screenShakeEnabled: Boolean = true,
        showDamageNumbers: Boolean = true,
        highContrast: Boolean = false,
    ) = with(drawScope) {
        val layout = createLayout(size)
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (highContrast) {
                    listOf(Color.Black, Color(0xFF08202A), Color.Black)
                } else {
                    listOf(ArcanePalette.Void, ArcanePalette.Deep, Color(0xFF14102B))
                },
            ),
        )

        drawAmbience(layout)
        val shake = shakeOffset(state, screenShakeEnabled)
        translate(shake.x, shake.y) {
            drawBoardShadow(layout)
            for (cell in drawCells) {
                drawTile(cell, layout, state, showGrid, highContrast)
            }

            state.rangePreview?.let { preview ->
                drawRangePreview(preview.cell, preview.range, preview.color, layout)
            }
            state.towers.sortedBy { it.cell.row + it.cell.col }.forEach { tower ->
                drawTower(tower, layout)
            }
            drawAimBeams(state, layout)
            state.enemies.sortedBy { it.row + it.col }.forEach { enemy ->
                drawEnemy(enemy, layout, highContrast)
            }
            state.hitEffects.forEach { effect ->
                drawHitEffect(effect, layout, showDamageNumbers)
            }
            state.abilityEffects.forEach { effect ->
                drawAbilityEffect(effect, layout)
            }
            state.projectiles.forEach { projectile ->
                drawProjectile(projectile, state.enemies, layout)
            }
            drawEndpointMarkers(layout)
        }
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

    private fun shakeOffset(state: GameState, enabled: Boolean): Offset {
        if (!enabled || state.shakeTimeRemaining <= 0f || state.shakeIntensity <= 0f) return Offset.Zero
        val pulse = (state.shakeTimeRemaining * 80f).roundToInt()
        val xSign = if (pulse % 2 == 0) 1f else -1f
        val ySign = if (pulse % 3 == 0) -1f else 1f
        val fade = state.shakeTimeRemaining.coerceIn(0f, 0.45f) / 0.45f
        return Offset(xSign * state.shakeIntensity * fade, ySign * state.shakeIntensity * 0.55f * fade)
    }

    private fun DrawScope.drawAmbience(layout: IsoLayout) {
        drawCircle(
            color = ArcanePalette.CircuitTeal.copy(alpha = 0.08f),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.22f, size.height * 0.24f),
        )
        drawCircle(
            color = ArcanePalette.ArcaneViolet.copy(alpha = 0.08f),
            radius = size.minDimension * 0.38f,
            center = Offset(size.width * 0.78f, size.height * 0.72f),
        )
        repeat(5) { index ->
            val y = layout.origin.y + index * layout.tileHeight * 2.15f
            drawLine(
                color = ArcanePalette.CircuitBlue.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y + layout.tileHeight * 0.8f),
                strokeWidth = 1.4f,
            )
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
        showGrid: Boolean,
        highContrast: Boolean,
    ) {
        val center = cellCenter(cell, layout)
        val tile = diamondPath(center, layout.tileWidth, layout.tileHeight)
        val side = diamondPath(center + Offset(0f, layout.tileHeight * 0.18f), layout.tileWidth, layout.tileHeight)
        val isPath = cell in state.pathPreview
        val isLocked = cell in map.buildLockedCells
        val isSelected = state.selectedCell == cell
        val baseColor = when {
            cell == map.spawn -> ArcanePalette.CircuitTeal
            cell == map.base -> ArcanePalette.WarningGold
            isLocked -> if (highContrast) Color(0xFF45515F) else ArcanePalette.TileLocked
            isPath -> if (highContrast) Color(0xFF67B88D) else ArcanePalette.TilePath
            cell in map.scenicPath -> Color(0xFF315B50)
            else -> if (highContrast) Color(0xFF244E5E) else ArcanePalette.TileTop
        }

        drawPath(side, ArcanePalette.TileSide.copy(alpha = 0.92f))
        drawPath(tile, baseColor)
        if (isPath) {
            drawPath(tile, ArcanePalette.CircuitTeal.copy(alpha = if (highContrast) 0.22f else 0.12f))
        }
        if (showGrid || isPath || isSelected) {
            drawPath(
                path = tile,
                color = if (highContrast) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = if (isPath) 0.22f else 0.1f),
                style = Stroke(width = 1.4f),
            )
        }

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
            color = ArcanePalette.CircuitTeal.copy(alpha = 0.16f),
            radius = layout.tileHeight * 0.86f,
            center = spawn,
        )
        drawCircle(
            color = ArcanePalette.CircuitTeal.copy(alpha = 0.84f),
            radius = layout.tileHeight * 0.45f,
            center = spawn,
            style = Stroke(width = 5f),
        )
        drawLine(
            color = Color.White.copy(alpha = 0.72f),
            start = spawn - Offset(layout.tileHeight * 0.18f, 0f),
            end = spawn + Offset(layout.tileHeight * 0.18f, 0f),
            strokeWidth = 3f,
        )
        drawCircle(
            color = ArcanePalette.TextSoft,
            radius = layout.tileHeight * 0.13f,
            center = spawn,
        )

        drawCircle(
            color = Color.Black.copy(alpha = 0.24f),
            radius = layout.tileHeight * 0.42f,
            center = base + Offset(0f, layout.tileHeight * 0.16f),
        )
        drawCircle(
            color = ArcanePalette.WarningGold.copy(alpha = 0.22f),
            radius = layout.tileHeight * 0.68f,
            center = base,
        )
        drawCircle(
            color = ArcanePalette.WarningGold,
            radius = layout.tileHeight * 0.38f,
            center = base,
        )
        drawCircle(
            color = ArcanePalette.ArcaneViolet,
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
        val fireFlash = (tower.cooldown / tower.fireInterval).coerceIn(0f, 1f)

        drawOval(
            color = Color.Black.copy(alpha = 0.32f),
            topLeft = Offset(center.x - w * 0.25f, center.y - h * 0.02f),
            size = Size(w * 0.5f, h * 0.24f),
        )

        when (tower.type) {
            TowerType.Basic -> {
                drawRoundRect(
                    color = tower.type.primaryColor,
                    topLeft = Offset(center.x - w * 0.16f, center.y - h * 0.72f),
                    size = Size(w * 0.32f, h * 0.68f),
                    cornerRadius = CornerRadius(8f, 8f),
                )
                drawCircle(
                    color = tower.type.accentColor,
                    radius = h * 0.16f,
                    center = Offset(center.x, center.y - h * 0.66f),
                )
            }

            TowerType.Sniper -> {
                drawRoundRect(
                    color = tower.type.primaryColor,
                    topLeft = Offset(center.x - w * 0.11f, center.y - h * 0.92f),
                    size = Size(w * 0.22f, h * 0.86f),
                    cornerRadius = CornerRadius(7f, 7f),
                )
                drawLine(
                    color = tower.type.accentColor,
                    start = Offset(center.x, center.y - h * 1.02f),
                    end = Offset(center.x, center.y - h * 0.52f),
                    strokeWidth = 5f,
                )
            }

            TowerType.Frost -> {
                drawRoundRect(
                    color = tower.type.primaryColor,
                    topLeft = Offset(center.x - w * 0.15f, center.y - h * 0.62f),
                    size = Size(w * 0.3f, h * 0.58f),
                    cornerRadius = CornerRadius(9f, 9f),
                )
                drawCircle(
                    color = tower.type.accentColor.copy(alpha = 0.88f),
                    radius = h * 0.22f,
                    center = Offset(center.x, center.y - h * 0.78f),
                    style = Stroke(width = 4f),
                )
            }
        }
        drawCircle(
            color = ArcanePalette.WarningGold,
            radius = h * 0.13f,
            center = Offset(center.x, center.y - h * 0.84f),
        )
        if (fireFlash > 0.72f) {
            drawCircle(
                color = tower.type.accentColor.copy(alpha = (fireFlash - 0.72f) * 2.8f),
                radius = h * (0.22f + fireFlash * 0.24f),
                center = Offset(center.x, center.y - h * 0.82f),
                style = Stroke(width = 3f),
            )
            drawCircle(
                color = Color.White.copy(alpha = (fireFlash - 0.72f) * 1.8f),
                radius = h * 0.08f,
                center = Offset(center.x, center.y - h * 0.88f),
            )
        }
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

    private fun DrawScope.drawEnemy(enemy: Enemy, layout: IsoLayout, highContrast: Boolean) {
        val center = gridToScreen(enemy.row, enemy.col, layout)
        val radius = layout.tileHeight * 0.23f * enemy.type.sizeScale
        val healthPercent = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
        val bodyCenter = center - Offset(0f, radius * 0.25f)

        drawOval(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(center.x - radius * 1.2f, center.y + radius * 0.45f),
            size = Size(radius * 2.4f, radius * 0.65f),
        )
        drawEnemyBody(enemy.type, bodyCenter, radius, highContrast)
        if (enemy.hitFlash > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = (enemy.hitFlash / 0.18f).coerceIn(0f, 1f) * 0.72f),
                radius = radius * if (enemy.type.isBoss) 1.35f else 1.12f,
                center = bodyCenter,
                style = Stroke(width = if (enemy.type.isBoss) 5f else 3f),
            )
        }
        if (enemy.isSlowed) {
            drawCircle(
                color = ArcanePalette.Frost.copy(alpha = 0.32f),
                radius = radius * 1.45f,
                center = bodyCenter,
            )
            drawCircle(
                color = ArcanePalette.Frost.copy(alpha = 0.9f),
                radius = radius * 1.38f,
                center = bodyCenter,
                style = Stroke(width = if (enemy.type.isBoss) 5f else 3f),
            )
        }
        val barWidth = radius * if (enemy.type.isBoss) 2.8f else 2.2f
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
        if (enemy.type.isBoss) {
            drawCircle(
                color = enemy.type.accentColor.copy(alpha = 0.85f),
                radius = radius * 0.24f,
                center = center - Offset(0f, radius * 1.08f),
            )
            drawCircle(
                color = enemy.type.accentColor.copy(alpha = 0.3f),
                radius = radius * 1.62f,
                center = bodyCenter,
                style = Stroke(width = 4f),
            )
        }
    }

    private fun DrawScope.drawEnemyBody(enemyType: EnemyType, center: Offset, radius: Float, highContrast: Boolean) {
        val primary = if (highContrast) enemyType.primaryColor.copy(alpha = 1f) else enemyType.primaryColor
        when (enemyType) {
            EnemyType.Swarm -> {
                val body = Path().apply {
                    moveTo(center.x, center.y - radius * 1.25f)
                    lineTo(center.x + radius * 0.9f, center.y)
                    lineTo(center.x, center.y + radius * 1.05f)
                    lineTo(center.x - radius * 0.9f, center.y)
                    close()
                }
                drawPath(body, primary)
                drawCircle(
                    color = enemyType.accentColor.copy(alpha = 0.88f),
                    radius = radius * 0.38f,
                    center = center,
                )
                drawPath(body, Color.White.copy(alpha = if (highContrast) 0.9f else 0.3f), style = Stroke(width = 2.4f))
            }

            EnemyType.Fast -> {
                val body = Path().apply {
                    moveTo(center.x, center.y - radius * 1.15f)
                    lineTo(center.x + radius * 1.15f, center.y + radius * 0.78f)
                    lineTo(center.x - radius * 1.05f, center.y + radius * 0.62f)
                    close()
                }
                drawPath(body, primary)
                drawPath(body, enemyType.accentColor.copy(alpha = 0.75f), style = Stroke(width = 2.5f))
            }

            EnemyType.Tank -> {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(enemyType.accentColor, primary),
                        center = center - Offset(radius * 0.3f, radius * 0.25f),
                        radius = radius * 1.5f,
                    ),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 1.85f),
                    cornerRadius = CornerRadius(radius * 0.42f, radius * 0.42f),
                )
            }

            EnemyType.Shielded -> {
                val body = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius * 0.9f, center.y - radius * 0.42f)
                    lineTo(center.x + radius * 0.72f, center.y + radius * 0.72f)
                    lineTo(center.x, center.y + radius)
                    lineTo(center.x - radius * 0.72f, center.y + radius * 0.72f)
                    lineTo(center.x - radius * 0.9f, center.y - radius * 0.42f)
                    close()
                }
                drawPath(
                    body,
                    Brush.radialGradient(
                        colors = listOf(enemyType.accentColor, primary),
                        center = center - Offset(radius * 0.25f, radius * 0.3f),
                        radius = radius * 1.55f,
                    ),
                )
                drawPath(body, enemyType.accentColor.copy(alpha = 0.96f), style = Stroke(width = 4.2f))
                drawCircle(
                    color = enemyType.accentColor.copy(alpha = if (highContrast) 0.36f else 0.22f),
                    radius = radius * 1.45f,
                    center = center,
                    style = Stroke(width = 3.4f),
                )
            }

            EnemyType.Boss, EnemyType.Juggernaut, EnemyType.Regenerator -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(enemyType.accentColor, primary, Color.Black.copy(alpha = 0.48f)),
                        center = center - Offset(radius * 0.25f, radius * 0.25f),
                        radius = radius * 1.65f,
                    ),
                    radius = radius,
                    center = center,
                )
                drawCircle(
                    color = enemyType.accentColor.copy(alpha = 0.95f),
                    radius = radius * 0.55f,
                    center = center,
                    style = Stroke(width = 4f),
                )
            }

            EnemyType.Normal -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(enemyType.accentColor, primary),
                        center = center - Offset(radius * 0.25f, radius * 0.25f),
                        radius = radius * 1.4f,
                    ),
                    radius = radius,
                    center = center,
                )
            }
        }
    }

    private fun DrawScope.drawAimBeams(state: GameState, layout: IsoLayout) {
        val enemiesById = state.enemies.associateBy { it.id }
        state.towers.forEach { tower ->
            val beamTarget = tower.lastTargetEnemyId?.let { enemiesById[it] } ?: return@forEach
            if (tower.aimBeamTimeRemaining <= 0f && tower.id != state.selectedTowerId) return@forEach

            val start = cellCenter(tower.cell, layout) - Offset(0f, layout.tileHeight * 0.78f)
            val end = gridToScreen(beamTarget.row, beamTarget.col, layout) - Offset(0f, layout.tileHeight * 0.24f)
            val selectedBoost = if (tower.id == state.selectedTowerId) 1.25f else 1f
            val pulse = (tower.aimBeamTimeRemaining / 0.18f).coerceIn(0.35f, 1f)

            drawLine(
                color = tower.type.accentColor.copy(alpha = 0.16f * selectedBoost * pulse),
                start = start,
                end = end,
                strokeWidth = 7f,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.32f * selectedBoost * pulse),
                start = start,
                end = end,
                strokeWidth = 2.2f,
            )
        }
    }

    private fun DrawScope.drawHitEffect(
        effect: com.nicolaielgame.game.model.HitEffect,
        layout: IsoLayout,
        showDamageNumbers: Boolean,
    ) {
        val center = gridToScreen(effect.row, effect.col, layout)
        val progress = (effect.age / effect.duration).coerceIn(0f, 1f)
        val alpha = (1f - progress).coerceIn(0f, 1f)
        drawCircle(
            color = effect.color.copy(alpha = alpha * 0.72f),
            radius = layout.tileHeight * (0.18f + progress * 0.42f),
            center = center - Offset(0f, layout.tileHeight * 0.08f),
            style = Stroke(width = 2.8f),
        )
        if (showDamageNumbers && effect.label.isNotBlank()) {
            val textOffset = center - Offset(0f, layout.tileHeight * (0.68f + progress * 0.42f))
            drawIntoCanvas { canvas ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = effect.color.copy(alpha = alpha).toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = layout.tileHeight * 0.42f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.nativeCanvas.drawText(effect.label, textOffset.x, textOffset.y, paint)
            }
        }
    }

    private fun DrawScope.drawAbilityEffect(effect: com.nicolaielgame.game.model.AbilityEffect, layout: IsoLayout) {
        val center = gridToScreen(effect.row, effect.col, layout)
        val progress = effect.progress
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val radius = when (effect.type) {
            AbilityType.MeteorStrike -> layout.tileWidth * (0.28f + progress * 1.75f)
            AbilityType.FreezePulse -> layout.tileWidth * (0.42f + progress * 2.05f)
            AbilityType.EmergencyGold -> layout.tileWidth * (0.22f + progress * 0.9f)
        }

        if (effect.type == AbilityType.MeteorStrike && progress < 0.35f) {
            drawCircle(
                color = Color.White.copy(alpha = (1f - progress / 0.35f) * 0.52f),
                radius = layout.tileWidth * (0.18f + progress),
                center = center,
            )
        }
        drawCircle(
            color = effect.type.color.copy(alpha = alpha * 0.22f),
            radius = radius,
            center = center,
        )
        drawCircle(
            color = effect.type.color.copy(alpha = alpha * 0.75f),
            radius = radius,
            center = center,
            style = Stroke(width = 4f),
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
        val glowWidth = when (projectile.towerType) {
            TowerType.Basic -> 3.2f
            TowerType.Sniper -> 5.2f
            TowerType.Frost -> 4.4f
        }

        drawLine(
            color = projectile.towerType.accentColor.copy(alpha = 0.22f),
            start = current,
            end = targetOffset,
            strokeWidth = glowWidth * 2.4f,
        )
        drawLine(
            color = projectile.towerType.accentColor.copy(alpha = 0.82f),
            start = current,
            end = targetOffset,
            strokeWidth = glowWidth,
        )
        drawCircle(
            color = projectile.towerType.accentColor,
            radius = layout.tileHeight * if (projectile.towerType == TowerType.Sniper) 0.08f else 0.12f,
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
