# Asset Integration

Iteration 7 adds a clean asset pipeline and fallback-safe catalog, but keeps the current Canvas/vector/tone fallbacks active until licensed files are dropped into the project. This prevents missing optional assets from breaking builds.

## Legal Policy

Use only assets with clear CC0, public-domain, or commercial-use terms. Do not use ripped game assets, marketplace-restricted files, unclear AI-generated files, or anything with unknown attribution/license status.

Preferred source: Kenney official assets. Kenney packs listed below are Creative Commons CC0 and can be used commercially without required attribution. Optional credit is recorded in `CREDITS.md`.

## Recommended Packs

1. Kenney Tower Defense
   - URL: https://kenney.nl/assets/tower-defense
   - Use for isometric tiles, towers, landscape pieces, projectile/effect references.
   - Destination: `app/src/main/assets/tiles`, `towers`, `projectiles_effects`.

2. Kenney UI Pack - Sci-Fi
   - URL: https://kenney.nl/assets/ui-pack-sci-fi
   - Use for menu panels, campaign buttons, settings controls, and HUD accents.
   - Destination: `app/src/main/assets/ui`.

3. Kenney Interface Sounds
   - URL: https://kenney.nl/assets/interface-sounds
   - Use for button click, confirmation, denial, menu transitions.
   - Destination: `app/src/main/assets/audio/sfx`.

4. Kenney Impact Sounds
   - URL: https://kenney.nl/assets/impact-sounds
   - Use for enemy hit, enemy death, boss death, meteor impact, base hit.
   - Destination: `app/src/main/assets/audio/sfx`.

5. Kenney Tower Defense Kit
   - URL: https://kenney.nl/assets/tower-defense-kit
   - Optional 3D reference/source pack for future authored art. Do not include large source files in the APK unless converted and optimized.

## Folder Layout

```text
app/src/main/assets/
  tiles/
  towers/
  enemies/
  bosses/
  projectiles_effects/
  ui/
  audio/
    sfx/
    music/
```

The pure catalog in `GameAssetCatalog.kt` lists expected slots and fallback behavior. Missing assets should fall back to current Canvas/vector/tone rendering rather than throwing.

## Suggested File Names

Visual:

- `tiles/tile_grass.png`
- `tiles/tile_path.png`
- `tiles/tile_spawn.png`
- `tiles/tile_base.png`
- `towers/tower_basic.png`
- `towers/tower_sniper.png`
- `towers/tower_frost.png`
- `enemies/enemy_normal.png`
- `enemies/enemy_fast.png`
- `enemies/enemy_tank.png`
- `enemies/enemy_shielded.png`
- `enemies/enemy_swarm.png`
- `bosses/boss_juggernaut.png`
- `projectiles_effects/effect_projectile.png`
- `ui/icon_campaign.png`
- `ui/icon_daily.png`
- `ui/icon_leaderboard.png`

Audio:

- `audio/sfx/button_click.ogg`
- `audio/sfx/tower_placed.ogg`
- `audio/sfx/tower_upgraded.ogg`
- `audio/sfx/tower_sold.ogg`
- `audio/sfx/tower_fired.ogg`
- `audio/sfx/enemy_hit.ogg`
- `audio/sfx/enemy_killed.ogg`
- `audio/sfx/boss_warning.ogg`
- `audio/sfx/boss_death.ogg`
- `audio/sfx/meteor.ogg`
- `audio/sfx/freeze_pulse.ogg`
- `audio/sfx/emergency_gold.ogg`
- `audio/sfx/wave_start.ogg`
- `audio/sfx/victory.ogg`
- `audio/sfx/game_over.ogg`
- `audio/music/music_loop.ogg`

## Optimization Notes

- Prefer optimized PNG/WebP for sprites.
- Prefer OGG for SFX/music.
- Keep only used files in the APK.
- Keep large source files, previews, and unused spritesheets out of `app/src/main/assets`.
- Confirm all added files are listed in `CREDITS.md`.

## Sandbox Note

During Iteration 7, official Kenney pages were reachable for license verification, but command-line download failed in this environment with Windows TLS credential errors. The app therefore ships with the existing fallback visuals/audio plus a ready integration structure.
