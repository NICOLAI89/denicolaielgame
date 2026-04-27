# Visual Direction 3D

## Target Style

Arcane Circuit Defense should become a low-poly 3D fantasy-tech tower defense game. The world should feel like arcane machinery built on floating tactical terrain: readable, bright, geometric, and mobile-friendly.

Avoid photorealism, noisy textures, tiny details, and mixed art styles. The first 3D slice should look cohesive before it becomes content-heavy.

## Camera

- Fixed isometric 3D camera.
- Orthographic projection by default for tower-defense readability.
- Camera angle around 45 degrees yaw and 45-60 degrees pitch.
- Board should fit a phone viewport with safe margins for HUD.
- Allow light pan/zoom later, but keep the first slice fixed.

## Lighting

- Soft directional key light.
- Ambient fill so enemy silhouettes are readable.
- Slight rim/glow accents for magic-tech identity.
- Prefer baked/simple lighting and mobile-friendly materials.
- Avoid heavy realtime shadows until device profiling proves safe.

## World

- Floating tile board or raised fantasy-tech terrain.
- Tiles should have thickness, bevels, and soft contact shadows.
- Spawn and base should be distinct 3D landmarks.
- Path tiles should read clearly from a mobile distance.
- Build tiles should be obvious without requiring text labels.

## Towers

- Strong silhouettes from the isometric camera.
- Basic: compact cannon or turret.
- Sniper: tall, precise, long-barrel silhouette.
- Frost: blue/cyan crystal, rune, or ice emitter.
- Upgrade states should add visible height, accent parts, or glow, not just numbers.
- Tower bases should sit flush on tiles.

## Enemies

- Readable families at mobile distance.
- Normal: standard small unit.
- Fast: smaller and sharper silhouette.
- Tank: wider, heavier silhouette.
- Shielded: visible shield shell or frontal plate.
- Swarm: tiny units in higher counts.
- Bosses: larger, grounded, and distinct from regular enemies.
- Do not rely only on color; use shape language.

## Projectiles And VFX

- Projectiles should originate from visible tower muzzles/crystals.
- Hits should land on enemy bodies, not grid centers.
- Use short readable trails and impact flashes.
- Frost slow should show a cold ring or particle burst.
- Meteor should be chunky, brief, and performant.
- VFX must be pooled in Unity.

## UI

- Mobile-first.
- Large enough tap targets.
- HUD should show lives, gold, score, wave, and start wave.
- Tower panel should show cost, upgrade, sell, range, targeting mode.
- Pause/settings should remain accessible during play.
- Keep campaign and daily challenge entry points visually tied to the 3D world.

## Asset Sources To Evaluate

Primary:

- Quaternius CC0 low-poly packs: especially fantasy, sci-fi, nature, modular environment, enemies, props, and weapons.
- Kenney CC0 3D packs, UI packs, and audio packs.

Rules:

- Use CC0/public-domain or explicit commercial-safe licenses only.
- Document every imported pack in `CREDITS.md`.
- Import only the specific models, textures, and audio clips needed.
- Avoid giant unused source packs in the repository.
- Keep one dominant art family per category.

## Quality Bar For The First 3D Slice

The first Unity slice should show:

- A grounded low-poly board with depth.
- Spawn and base landmarks.
- One tower that reads as a tower.
- One enemy that reads from a phone distance.
- A projectile that visually connects tower and enemy.
- A clear win/loss loop over five waves.
- No placeholder primitives in final screenshots unless clearly marked as temporary.

