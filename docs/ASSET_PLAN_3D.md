# 3D Asset Plan

Do not import large binary packs yet. Iteration 10 creates the plan and Unity starter structure only. Add real 3D assets in a later iteration after the first Unity project opens cleanly.

## License Rules

- Use CC0, public-domain, or clear commercial-safe assets only.
- Avoid ripped assets, AI-unclear assets, marketplace-restricted assets, and packs with unclear redistribution terms.
- Record source, provider, license, attribution requirement, and local path in `CREDITS.md`.
- Keep downloaded source ZIPs out of the APK and out of the repository unless explicitly needed.
- Prefer optimized FBX/GLB/PNG/OGG files that are actually used by the vertical slice.

## Required Categories

Terrain tiles:

- Grass/buildable tile.
- Path tile.
- Locked/gate tile.
- Raised tile side/wall.
- Floating board underside.

Build tiles:

- Clear buildable marker.
- Hover/selected highlight.
- Invalid placement marker.

Path tiles:

- Straight segment.
- Corner segment.
- Spawn-adjacent variant if needed.
- Base-adjacent variant if needed.

Towers:

- Basic tower.
- Sniper tower.
- Frost tower.
- Upgrade add-ons for levels 2+.
- Range preview material/VFX.

Enemies:

- Normal.
- Fast.
- Tank.
- Shielded.
- Swarm.

Boss:

- Boss.
- Juggernaut.
- Regenerator.

Projectiles:

- Basic bolt.
- Sniper shot.
- Frost shard.
- Impact sprites/particles.

VFX:

- Tower muzzle flash.
- Enemy hit flash.
- Enemy death burst.
- Frost slow pulse.
- Meteor strike.
- Boss warning.
- Victory/game over effects.

UI:

- Mobile HUD panels.
- Tower buttons.
- Ability icons.
- Wave/start/pause/settings icons.
- Campaign map icons.
- Daily challenge portal badge.
- Leaderboard/profile/achievement icons.

SFX/music:

- UI click.
- Tower place/upgrade/sell/fire.
- Enemy hit/death.
- Boss warning/death.
- Meteor/freeze/emergency gold.
- Wave start.
- Victory/game over.
- Subtle looping background music.

## Recommended Packs To Evaluate

Quaternius:

- Fantasy Props MegaKit for magic-tech props and landmarks.
- Medieval Village MegaKit or Stylized Nature MegaKit for terrain and environmental pieces.
- Sci-Fi Essentials Kit or Modular Sci-Fi Megakit for circuit/tech accents.
- 3D Card Kit - Fantasy only if its small fantasy props fit the art direction.
- Individual low-poly weapon/projectile packs for tower barrels, crystals, bolts, or missiles.

Kenney:

- Tower Defense Kit for 3D tower-defense references.
- Prototype Kit or Nature Kit for simple blockout pieces.
- UI Pack - Sci-Fi for 2D UI.
- Interface Sounds, Impact Sounds, Digital Audio, Sci-fi Sounds, and Music Loops for audio.

## Unity Folder Targets

Use this layout:

```text
unity/ArcaneCircuitDefense3D/Assets/
  Art/
    Terrain/
    Towers/
    Enemies/
    Bosses/
    Projectiles/
    VFX/
    UI/
  Audio/
    SFX/
    Music/
  Materials/
  Prefabs/
  Scenes/
  Scripts/
```

## Import Workflow

1. Pick one pack per category and verify license before import.
2. Copy only the needed source files into `Assets/Art` or `Assets/Audio`.
3. Create Unity materials in `Assets/Materials`.
4. Build prefabs in `Assets/Prefabs`.
5. Keep source model names readable.
6. Document each imported file in `CREDITS.md`.
7. Test on Android after every asset batch.

## First Asset Batch

For the first 3D vertical slice, import only:

- 2-3 terrain/path tile models.
- 1 spawn marker.
- 1 base marker.
- 1 Basic tower model.
- 1 Normal enemy model.
- 1 projectile model or VFX.
- 3-5 HUD icons if needed.
- 4-6 SFX clips and optionally one short music loop.

