package com.nicolaielgame.game.assets

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.nicolaielgame.game.model.EnemyType
import com.nicolaielgame.game.model.TowerType

data class GameVisualAssets(
    val tileGrass: ImageBitmap? = null,
    val tilePath: ImageBitmap? = null,
    val tileSpawn: ImageBitmap? = null,
    val tileBase: ImageBitmap? = null,
    val towerBasic: ImageBitmap? = null,
    val towerSniper: ImageBitmap? = null,
    val towerFrost: ImageBitmap? = null,
    val enemyNormal: ImageBitmap? = null,
    val enemyFast: ImageBitmap? = null,
    val enemyTank: ImageBitmap? = null,
    val enemyShielded: ImageBitmap? = null,
    val enemySwarm: ImageBitmap? = null,
    val bossJuggernaut: ImageBitmap? = null,
    val projectileEffect: ImageBitmap? = null,
) {
    fun tower(type: TowerType): ImageBitmap? {
        return when (type) {
            TowerType.Basic -> towerBasic
            TowerType.Sniper -> towerSniper
            TowerType.Frost -> towerFrost
        }
    }

    fun enemy(type: EnemyType): ImageBitmap? {
        return when (type) {
            EnemyType.Normal -> enemyNormal
            EnemyType.Fast -> enemyFast
            EnemyType.Tank -> enemyTank
            EnemyType.Shielded -> enemyShielded
            EnemyType.Swarm -> enemySwarm
            EnemyType.Boss,
            EnemyType.Juggernaut,
            EnemyType.Regenerator -> bossJuggernaut
        }
    }

    companion object {
        val Empty = GameVisualAssets()

        fun load(context: Context): GameVisualAssets {
            fun image(path: String): ImageBitmap? {
                return runCatching {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }

            return GameVisualAssets(
                tileGrass = image("tiles/tile_grass.webp"),
                tilePath = image("tiles/tile_path.webp"),
                tileSpawn = image("tiles/tile_spawn.webp"),
                tileBase = image("tiles/tile_base.webp"),
                towerBasic = image("towers/tower_basic.webp"),
                towerSniper = image("towers/tower_sniper.webp"),
                towerFrost = image("towers/tower_frost.webp"),
                enemyNormal = image("enemies/enemy_normal.webp"),
                enemyFast = image("enemies/enemy_fast.webp"),
                enemyTank = image("enemies/enemy_tank.webp"),
                enemyShielded = image("enemies/enemy_shielded.webp"),
                enemySwarm = image("enemies/enemy_swarm.webp"),
                bossJuggernaut = image("bosses/boss_juggernaut.webp"),
                projectileEffect = image("projectiles_effects/effect_projectile.webp"),
            )
        }
    }
}
