# Asset Integration

Iterations 7B through 9 use a curated subset of real Kenney CC0 assets while keeping the current Canvas/vector/tone fallbacks active. Missing optional files must never break builds or gameplay.

## Legal Policy

Use only assets with clear CC0, public-domain, or commercial-use terms. Do not use ripped game assets, marketplace-restricted files, unclear AI-generated files, or anything with unknown attribution/license status.

Preferred source: Kenney official assets. Kenney packs listed below are Creative Commons CC0 and can be used commercially without required attribution. Optional credit is recorded in `CREDITS.md`.

## Bundled Packs

1. Kenney Tower Defense
   - URL: https://kenney.nl/assets/tower-defense
   - Used for isometric tiles, tower sprites, crystal/rock enemy stand-ins, and boss/base visuals.
   - Destination: `app/src/main/assets/tiles`, `towers`, `enemies`, `bosses`.

2. Kenney UI Pack - Sci-Fi
   - URL: https://kenney.nl/assets/ui-pack-sci-fi
   - Used for projectile/campaign/daily/leaderboard crosshair accents.
   - Destination: `app/src/main/assets/projectiles_effects`, `ui`, plus lightweight UI copies in `app/src/main/res/drawable/kenney_icon_*.webp`.

3. Kenney Interface Sounds
   - URL: https://kenney.nl/assets/interface-sounds
   - Used for button click, tower placement, tower sell, and wave start.
   - Destination: `app/src/main/assets/audio/sfx`.

4. Kenney Impact Sounds
   - URL: https://kenney.nl/assets/impact-sounds
   - Used for enemy hit, meteor, and base hit.
   - Destination: `app/src/main/assets/audio/sfx`.

5. Kenney Digital Audio
   - URL: https://kenney.nl/assets/digital-audio
   - Used for tower fire, upgrades, boss warning, victory, and game over.
   - Destination: `app/src/main/assets/audio/sfx`.

6. Kenney Sci-fi Sounds
   - URL: https://kenney.nl/assets/sci-fi-sounds
   - Used for enemy kill, boss death, and freeze pulse.
   - Destination: `app/src/main/assets/audio/sfx`.

7. Kenney Music Loops
   - URL: https://kenney.itch.io/kenney-game-assets
   - Verification: Kenney Game Assets All-in-1 lists audio/music loops and Creative Commons Zero licensing.
   - Bundled file: `Space Cadet.ogg`, copied as `app/src/main/assets/audio/music/music_loop.ogg`.
   - Download helper used: https://gamesounds.xyz/?dir=Kenney%27s+Sound+Pack%2FMusic+Loops%2FLoops

## Not Bundled

- Kenney Tower Defense Kit remains an optional future 3D reference pack. Do not include large source files in the APK unless converted and optimized.
- Kenney Music Jingles (`https://kenney.nl/assets/music-jingles`) is verified as Creative Commons CC0 and remains safe as a future source for short stingers.

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

## Bundled File Names

Visual:

- `tiles/tile_grass.webp`
- `tiles/tile_path.webp`
- `tiles/tile_spawn.webp`
- `tiles/tile_base.webp`
- `towers/tower_basic.webp`
- `towers/tower_sniper.webp`
- `towers/tower_frost.webp`
- `enemies/enemy_normal.webp`
- `enemies/enemy_fast.webp`
- `enemies/enemy_tank.webp`
- `enemies/enemy_shielded.webp`
- `enemies/enemy_swarm.webp`
- `bosses/boss_juggernaut.webp`
- `projectiles_effects/effect_projectile.webp`
- `ui/icon_campaign.webp`
- `ui/icon_daily.webp`
- `ui/icon_leaderboard.webp`

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
- `audio/sfx/base_hit.ogg`
- `audio/sfx/victory.ogg`
- `audio/sfx/game_over.ogg`
- `audio/music/music_loop.ogg`

## Optimization Notes

- Bundled sprites are converted to lossless WebP and selected one-by-one from official ZIPs.
- Iteration 9 replaces the incorrect red Frost tower stand-in with a small project-authored blue ice tower WebP.
- Bundled SFX are OGG files copied one-by-one from official ZIPs.
- Bundled music is one OGG loop only, kept under 350 KB.
- Keep only used files in the APK.
- Keep large source files, previews, and unused spritesheets out of `app/src/main/assets`.
- Confirm all added files are listed in `CREDITS.md`.

## Fallback Behavior

- `GameVisualAssets.load` reads assets from `app/src/main/assets` and returns `null` for missing files.
- `IsoRenderer` uses Kenney sprites when present and falls back to project-authored Canvas shapes when absent or when high contrast mode should favor clear generated shapes.
- Iteration 9 reduces Canvas overdraw around sprites: no permanent tower dots, no permanent tower/enemy accent rings, subtler range rings, grounded shadows, and projectile origins aligned with visible tower tops.
- `AndroidGameSoundPlayer` attempts `SoundPool` asset playback first and falls back to Android generated tones if the clip is missing, not yet loaded, or unavailable.
- `AndroidGameMusicPlayer` attempts looping MediaPlayer playback only when `audio/music/music_loop.ogg` exists and the persisted music setting is enabled; otherwise it stays silent.
- `GameAssetCatalog` and `AudioRouting` expose pure logic for fallback and route tests.

## Iteration 9 Visual QA Notes

- Campaign and daily screens should use the same fantasy-tech visual language as gameplay: dark circuit backgrounds, teal/gold/violet accents, route/node depth, and clear locked/current/completed states.
- Daily Challenge should remain a special entry point, not a separate renderer. Actual daily runs go through `GameScreen` and `IsoRenderer`.
- Keep high contrast mode generated-shape-first so enemy/tower distinction remains readable even if decorative sprites are less visible.
- Tower sprites should read as the primary tower visuals. Renderer overlays should be reserved for selection, range preview, firing, slow/hit feedback, and accessibility fallback.
