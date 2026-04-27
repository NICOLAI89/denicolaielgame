# Migration To Unity

Iteration 10 changes the project direction from polishing the current Compose Canvas renderer to starting a real 3D implementation. The Kotlin Android app remains the validated gameplay prototype and should continue to build, run, and act as the reference for rules, balance, progression, and UX behavior.

## Why The Kotlin App Becomes The Prototype

The native Android project proved the core game:

- Dynamic A* rerouting when towers are placed, upgraded, or sold.
- Seven campaign levels, daily challenge generation, difficulty scaling, boss waves, abilities, local profiles, local leaderboard, settings, and achievements.
- A playable mobile UI with pause, settings, target selection, auto-start waves, and accessibility toggles.
- Tests around the rule systems that matter most.

Its limitation is visual rather than systemic. Compose Canvas is excellent for custom UI and lightweight 2D/2.5D presentation, but the target has moved to real low-poly 3D with grounded models, actual depth, lighting, camera composition, animation, particles, and mobile rendering controls. Continuing to force the Canvas layer to look like 3D would add complexity without reaching the desired result.

## Why Unity Is The Primary Target

Unity is the recommended migration target for this game because it provides:

- Mature Android build tooling and profiling for mobile 3D.
- A fast path to fixed/isometric 3D camera work.
- Prefab, material, particle, animation, audio, and UI workflows that match the new visual goal.
- Straightforward integration of CC0 low-poly FBX/OBJ/GLB assets from Quaternius and Kenney.
- A large pool of mobile tower-defense patterns without introducing a custom engine.

Godot remains a reasonable alternative, especially for a lighter open-source workflow, but Unity should be treated as the main branch of the migration unless licensing, team workflow, or device performance testing proves otherwise.

## Preserve The Android Prototype

Do not delete or flatten the existing Android project. Keep these directories intact:

- `app/`
- `gradle/`
- root Gradle files
- Android tests
- current assets, credits, and docs

The Kotlin implementation is the executable design document for the Unity version. If a Unity behavior is unclear, the Android `GameEngine`, model classes, and JVM tests should be treated as the source of truth.

## Systems To Port First

Port these in order:

1. Grid and pathfinding: grid cells, spawn/base, build locks, buildable cells, A* path search, route validation after tower placement.
2. Economy and wave lifecycle: gold, lives, score, wave start, spawn queues, wave cleared, victory, game over.
3. One vertical-slice tower and enemy: Basic tower, Normal enemy, projectile hit, reward.
4. Tower placement UX: tap buildable tile, deny path-blocking placement, select existing tower.
5. Campaign level data: start with one level, then convert Levels 1-7 into Unity `ScriptableObject` data.
6. Difficulty scaling: Easy, Normal, Hard multipliers.
7. Remaining tower/enemy families and abilities.
8. Local persistence: profiles, settings, leaderboard, daily challenge bests.

## Systems To Defer

Defer these until the first 3D vertical slice feels good on device:

- Full profile migration and DataStore parity.
- Achievements UI.
- Complete campaign map UX.
- Daily challenge UI polish.
- All audio routing details.
- Advanced targeting visuals.
- Release signing and Play Store internal testing.
- Cloud services, login, ads, IAP, or backend features.

## Migration Risks

| Risk | Mitigation |
| --- | --- |
| Visual scope grows too quickly | Build one polished map, one tower, one enemy, and five waves first. |
| Unity data diverges from Kotlin rules | Keep `docs/GAMEPLAY_SPEC.md` and Android tests as the reference. |
| Mobile performance suffers | Use low-poly assets, baked/simple lighting, pooled enemies/projectiles, and early Android profiling. |
| Asset style becomes mixed | Choose one primary low-poly family and keep Kenney UI/audio as support. |
| Persistence is reimplemented too early | Use in-memory data until the vertical slice is playable. |
| Existing Android app breaks | Keep Unity under `unity/ArcaneCircuitDefense3D/` and avoid Gradle changes. |

## Iteration 10 Vertical Slice

The starter Unity folder scaffolds:

- One generated 3D grid map.
- Dynamic A* pathfinding.
- Buildable tiles, spawn tile, and base tile.
- One tower type and one enemy type via data assets.
- Five wave definitions.
- Gold, lives, score.
- Basic HUD and Start Wave button.

It is intentionally a starter skeleton, not a full Unity project export. Create the Unity project through Unity Hub, then copy or keep this `Assets/` structure inside that project.

