package com.nicolaielgame.game.assets

enum class GameAssetCategory(val folderName: String) {
    Tiles("tiles"),
    Towers("towers"),
    Enemies("enemies"),
    Bosses("bosses"),
    ProjectilesEffects("projectiles_effects"),
    Ui("ui"),
    AudioSfx("audio/sfx"),
    AudioMusic("audio/music"),
}

enum class AssetFallbackMode {
    CanvasVectorFallback,
    GeneratedToneFallback,
}

data class GameAssetSlot(
    val key: String,
    val category: GameAssetCategory,
    val expectedPath: String,
    val bundledFallback: String,
    val fallbackMode: AssetFallbackMode,
    val sourcePolicy: String = "Use CC0 or public-domain assets only.",
) {
    val isExternalAsset: Boolean
        get() = expectedPath.startsWith("app/src/main/assets")
}

object GameAssetCatalog {
    val visualSlots: List<GameAssetSlot> = listOf(
        visual("tile_grass", GameAssetCategory.Tiles, "tiles/tile_grass.png", "Compose Canvas isometric tile"),
        visual("tile_path", GameAssetCategory.Tiles, "tiles/tile_path.png", "Compose Canvas path tile"),
        visual("tile_spawn", GameAssetCategory.Tiles, "tiles/tile_spawn.png", "Compose Canvas spawn marker"),
        visual("tile_base", GameAssetCategory.Tiles, "tiles/tile_base.png", "Compose Canvas base marker"),
        visual("tower_basic", GameAssetCategory.Towers, "towers/tower_basic.png", "Compose Canvas basic tower"),
        visual("tower_sniper", GameAssetCategory.Towers, "towers/tower_sniper.png", "Compose Canvas sniper tower"),
        visual("tower_frost", GameAssetCategory.Towers, "towers/tower_frost.png", "Compose Canvas frost tower"),
        visual("enemy_normal", GameAssetCategory.Enemies, "enemies/enemy_normal.png", "Compose Canvas normal enemy"),
        visual("enemy_fast", GameAssetCategory.Enemies, "enemies/enemy_fast.png", "Compose Canvas fast enemy"),
        visual("enemy_tank", GameAssetCategory.Enemies, "enemies/enemy_tank.png", "Compose Canvas tank enemy"),
        visual("enemy_shielded", GameAssetCategory.Enemies, "enemies/enemy_shielded.png", "Compose Canvas shielded enemy"),
        visual("enemy_swarm", GameAssetCategory.Enemies, "enemies/enemy_swarm.png", "Compose Canvas swarm enemy"),
        visual("boss_juggernaut", GameAssetCategory.Bosses, "bosses/boss_juggernaut.png", "Compose Canvas boss body"),
        visual("effect_projectile", GameAssetCategory.ProjectilesEffects, "projectiles_effects/effect_projectile.png", "Compose Canvas projectile glow"),
        visual("ui_campaign", GameAssetCategory.Ui, "ui/icon_campaign.png", "Bundled vector drawable campaign icon"),
        visual("ui_daily", GameAssetCategory.Ui, "ui/icon_daily.png", "Bundled vector drawable daily icon"),
        visual("ui_leaderboard", GameAssetCategory.Ui, "ui/icon_leaderboard.png", "Bundled vector drawable leaderboard icon"),
    )

    val audioSlots: List<GameAssetSlot> = listOf(
        audio("button_click", "audio/sfx/button_click.ogg"),
        audio("tower_placed", "audio/sfx/tower_placed.ogg"),
        audio("tower_upgraded", "audio/sfx/tower_upgraded.ogg"),
        audio("tower_sold", "audio/sfx/tower_sold.ogg"),
        audio("tower_fired", "audio/sfx/tower_fired.ogg"),
        audio("enemy_hit", "audio/sfx/enemy_hit.ogg"),
        audio("enemy_killed", "audio/sfx/enemy_killed.ogg"),
        audio("boss_warning", "audio/sfx/boss_warning.ogg"),
        audio("boss_death", "audio/sfx/boss_death.ogg"),
        audio("meteor", "audio/sfx/meteor.ogg"),
        audio("freeze_pulse", "audio/sfx/freeze_pulse.ogg"),
        audio("emergency_gold", "audio/sfx/emergency_gold.ogg"),
        audio("wave_start", "audio/sfx/wave_start.ogg"),
        audio("victory", "audio/sfx/victory.ogg"),
        audio("game_over", "audio/sfx/game_over.ogg"),
        GameAssetSlot(
            key = "music_loop",
            category = GameAssetCategory.AudioMusic,
            expectedPath = "app/src/main/assets/audio/music/music_loop.ogg",
            bundledFallback = "Silence when no CC0 loop is bundled",
            fallbackMode = AssetFallbackMode.GeneratedToneFallback,
        ),
    )

    val allSlots: List<GameAssetSlot> = visualSlots + audioSlots

    fun slotsFor(category: GameAssetCategory): List<GameAssetSlot> {
        return allSlots.filter { it.category == category }
    }

    fun missingFallbacks(availableAssetPaths: Set<String>): List<GameAssetSlot> {
        return allSlots.filter { it.expectedPath !in availableAssetPaths }
    }

    fun hasFallbackCoverage(availableAssetPaths: Set<String> = emptySet()): Boolean {
        return missingFallbacks(availableAssetPaths).all { it.bundledFallback.isNotBlank() }
    }

    private fun visual(
        key: String,
        category: GameAssetCategory,
        relativePath: String,
        fallback: String,
    ): GameAssetSlot {
        return GameAssetSlot(
            key = key,
            category = category,
            expectedPath = "app/src/main/assets/$relativePath",
            bundledFallback = fallback,
            fallbackMode = AssetFallbackMode.CanvasVectorFallback,
        )
    }

    private fun audio(key: String, relativePath: String): GameAssetSlot {
        return GameAssetSlot(
            key = key,
            category = GameAssetCategory.AudioSfx,
            expectedPath = "app/src/main/assets/$relativePath",
            bundledFallback = "Android ToneGenerator fallback",
            fallbackMode = AssetFallbackMode.GeneratedToneFallback,
        )
    }
}
