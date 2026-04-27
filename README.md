# Arcane Circuit Defense

Arcane Circuit Defense is a tower defense game currently split into two tracks:

- The existing Kotlin/Jetpack Compose Android app is the validated gameplay prototype.
- The next major direction is a Unity 3D migration for real low-poly 3D visuals on Android.

The Android prototype remains preserved and buildable as the reference implementation for rules, balance, progression, persistence, and UX. Enemies spawn in deterministic waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 10 Direction

Iteration 10 starts the migration from Compose Canvas 2.5D into a Unity-based low-poly 3D tower defense implementation. The goal is not to replace the Android app immediately; the goal is to capture the game specification, preserve the proven prototype, and add a serious Unity starter structure for the first 3D vertical slice.

New migration documents:

- `docs/MIGRATION_TO_UNITY.md`
- `docs/GAMEPLAY_SPEC.md`
- `docs/VISUAL_DIRECTION_3D.md`
- `docs/ASSET_PLAN_3D.md`
- `docs/LOCALIZATION_PLAN.md`

Unity starter:

- `unity/ArcaneCircuitDefense3D/`
- Starter C# scripts for game management, grid, pathfinding, towers, enemies, waves, economy, camera, build controls, and HUD.
- A documented vertical-slice target: one 3D map, dynamic A*, buildable tiles, spawn/base, one tower, one enemy, five waves, gold/lives/score, and Start Wave HUD.

## Android Prototype Features

- Native Android Kotlin app with Jetpack Compose screens and centralized Compose Canvas rendering.
- Profile select, tutorial, main menu, campaign map, daily challenge, local leaderboard, settings, achievements, pause, victory, and game over flows.
- Seven handcrafted campaign levels with progression unlocks through Level 7.
- Iteration 9 visual integration pass: tower sprite artifacts removed, range rings reduced, level dots moved out of board rendering, better sprite grounding, clearer projectile/impact alignment, and less Canvas overdraw on top of real assets.
- Frost tower now uses a blue ice-themed replacement sprite instead of the incorrect red tile stand-in.
- Daily Challenge now uses the same upgraded 2.5D visual direction as campaign play, plus a polished daily preview panel and daily in-run badge.
- Rebuilt campaign map presentation with a larger fantasy-tech map panel, route path, completed/locked/current node styling, boss markers, daily portal, and records landmark.
- Daily challenge mode with a deterministic local date seed, fixed daily difficulty, fixed daily modifiers, generated daily waves, and local best daily score saving.
- Local leaderboard for completed campaign runs, including profile slot, level, difficulty, score, completion time, lives remaining, and completion date/time.
- Dynamic A* rerouting with tower placement validation and wave lifecycle states: `Ready`, `In Progress`, `Cleared`, and `Finished`.
- Three tower types: Basic, Sniper, and Frost, retuned for the longer campaign.
- Tower placement mode can now be toggled off by tapping the selected tower button again. Tapping an existing tower prioritizes inspection over accidental placement.
- Tower upgrades, sell/refund controls, tower range preview, selected tower stats, current targeting mode indicator, targeting mode buttons, and firing aim beams.
- Enemy types: Normal, Fast, Tank, Shielded, Swarm, Boss, Juggernaut, and Regenerator.
- Accessibility and feedback toggles persisted with DataStore: sound, music, grid visibility, screen shake, damage numbers, high contrast mode, FPS counter, auto-start waves, and last difficulty.
- Pause menu now includes sound on/off, auto-start waves, and a settings overlay that returns cleanly to the paused run.
- Auto-start waves can be enabled locally. After a cleared non-final wave, the next wave starts after a short delay; it does not run while paused or after victory.
- Lightweight performance HUD toggle showing FPS, average frame time, and average update time.
- Bundled real CC0 Kenney assets for tiles, towers, enemy/boss stand-ins, projectile accents, UI accents, and gameplay SFX.
- Fallback-safe asset catalog and loaders so missing optional assets keep using Canvas/vector/tone fallbacks.
- SoundPool SFX playback for bundled OGG clips with Android generated-tone fallback.
- A lightweight CC0 Kenney music loop is bundled at `app/src/main/assets/audio/music/music_loop.ogg`; music playback respects the persisted music setting and uses subtle volume.
- Release-oriented vector launcher icon, splash polish, and version `0.2.0` / versionCode `2`.
- CREDITS and asset-integration documentation with CC0 Kenney pack recommendations.

## Asset And Audio Integration

Iterations 7B through 9 bundle a small curated set of Kenney CC0 assets while keeping the build safe if optional files are missing.

- Bundled visuals include selected WebP files from Kenney Tower Defense and UI Pack - Sci-Fi.
- The Frost tower sprite is project-authored to replace a visually incorrect red placeholder while staying within the existing asset pipeline.
- Bundled audio includes selected OGG files from Kenney Interface Sounds, Impact Sounds, Digital Audio, and Sci-fi Sounds.
- Bundled music uses Kenney Music Loops `Space Cadet.ogg`, copied as `audio/music/music_loop.ogg`.
- Project-authored Canvas/vector/tone fallbacks remain active for high contrast mode, missing files, and placement denial.
- Manual integration and future asset guidance live in `docs/ASSET_INTEGRATION.md`.
- Credits and license notes live in `CREDITS.md`.

No unclear-license assets were added.

## Build From Command Line

From the repository root on Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` after a successful command-line build.

## Validation

Recommended local validation before sharing a build:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
git diff --check
```

Keep `local.properties` local. Do not commit signing keys or local keystores.

## Release Status

Iteration 10 is a migration-planning and Unity-starter pass, not a Play Store release preparation pass.

- App label: "Arcane Circuit Defense".
- Package id: `com.nicolaielgame`.
- Version: `versionCode = 2`, `versionName = "0.2.0"`.
- Publishing automation, signing configuration, Play Store metadata, and internal testing setup are intentionally not configured yet.

## Privacy Note

Arcane Circuit Defense is local-only in Iteration 9.

- No backend.
- No account or login.
- No ads.
- No in-app purchases.
- No cloud leaderboard.
- Progress, settings, profiles, achievements, local leaderboard, and daily challenge bests are stored locally with Android DataStore.

## Run On Emulator Or Device

1. Open this repository in Android Studio.
2. Let Gradle sync.
3. Select the `app` run configuration.
4. Run on an emulator or Android 7.0/API 24+ device.

## Known Limitations

- The production visual target has moved to Unity 3D; Compose Canvas should now be treated as the gameplay prototype renderer.
- The Unity folder is a starter skeleton, not a complete Unity project export with scenes, prefabs, imported 3D assets, or generated Unity metadata.
- The bundled sprites are a curated Kenney prototype pass, not final authored art for every enemy family.
- Some enemies use Kenney crystal/rock/tower-defense props as readable stand-ins while keeping color-coded Canvas fallback in high contrast mode.
- Bundled music is a prototype loop and may need replacement during final audio direction.
- Daily challenge is local-only and date-seeded; there is no server authority or anti-cheat.
- Leaderboard is local-only and can be reset with profile data.
- Balance still needs longer real-device playtesting.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Tablet and foldable layouts are acceptable but not fully custom.
- Ability targeting is automatic rather than manually aimed.
- Save slots are local profiles without cloud sync.

## Suggested Iteration 11 Roadmap

- Open the Unity starter in Unity Hub and commit generated-safe project metadata.
- Build the first real 3D scene using the starter scripts.
- Import a tiny CC0 low-poly asset batch for one map, one tower, one enemy, and one projectile.
- Port Level 1 data into Unity `ScriptableObject` assets.
- Add object pooling for enemies, projectiles, and VFX.
- Profile the vertical slice on Android hardware.
- Keep the Android prototype as the reference until Unity reaches gameplay parity.
