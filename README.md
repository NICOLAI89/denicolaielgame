# Arcane Circuit Defense

Arcane Circuit Defense is a native Android 2.5D isometric tower defense prototype built with Kotlin, Gradle, Jetpack Compose, and Compose Canvas. Enemies spawn in deterministic waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 7B Features

- Native Android Kotlin app with Jetpack Compose screens and centralized Compose Canvas rendering.
- Profile select, tutorial, main menu, campaign map, daily challenge, local leaderboard, settings, achievements, pause, victory, and game over flows.
- Seven handcrafted campaign levels with progression unlocks through Level 7.
- Visual campaign map with nodes for Levels 1-7, locked/unlocked/completed states, route lines, daily challenge shortcut, and leaderboard shortcut.
- Daily challenge mode with a deterministic local date seed, fixed daily difficulty, fixed daily modifiers, generated daily waves, and local best daily score saving.
- Local leaderboard for completed campaign runs, including profile slot, level, difficulty, score, completion time, lives remaining, and completion date/time.
- Dynamic A* rerouting with tower placement validation and wave lifecycle states: `Ready`, `In Progress`, `Cleared`, and `Finished`.
- Three tower types: Basic, Sniper, and Frost, retuned for the longer campaign.
- Tower upgrades, sell/refund controls, tower range preview, selected tower stats, current targeting mode indicator, targeting mode buttons, and firing aim beams.
- Enemy types: Normal, Fast, Tank, Shielded, Swarm, Boss, Juggernaut, and Regenerator.
- Accessibility and feedback toggles persisted with DataStore: sound, music, grid visibility, screen shake, damage numbers, high contrast mode, FPS counter, and last difficulty.
- Lightweight performance HUD toggle showing FPS, average frame time, and average update time.
- Bundled real CC0 Kenney assets for tiles, towers, enemy/boss stand-ins, projectile accents, UI accents, and gameplay SFX.
- Fallback-safe asset catalog and loaders so missing optional assets keep using Canvas/vector/tone fallbacks.
- SoundPool SFX playback for bundled OGG clips with Android generated-tone fallback.
- Release-oriented vector launcher icon, splash polish, and version `0.2.0` / versionCode `2`.
- CREDITS and asset-integration documentation with CC0 Kenney pack recommendations.

## Asset And Audio Integration

Iteration 7B bundles a small curated set of official Kenney CC0 assets while keeping the build safe if optional files are missing.

- Bundled visuals include selected WebP files from Kenney Tower Defense and UI Pack - Sci-Fi.
- Bundled audio includes selected OGG files from Kenney Interface Sounds, Impact Sounds, Digital Audio, and Sci-fi Sounds.
- Project-authored Canvas/vector/tone fallbacks remain active for high contrast mode, missing files, and placement denial.
- Manual integration and future asset guidance live in `docs/ASSET_INTEGRATION.md`.
- Credits and license notes live in `CREDITS.md`.

No unclear-license assets were added. Music is still not bundled; the music setting remains persisted for future use once a lightweight CC0 loop is selected.

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

## Release Build Notes

Debug validation:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
git diff --check
```

Release APK/AAB generation can be added once signing config is decided. Do not commit signing keys or local keystores. Keep `local.properties` local.

## Play Store Internal Testing Prep

- App label: "Arcane Circuit Defense".
- Package id: `com.nicolaielgame`.
- Version: `versionCode = 2`, `versionName = "0.2.0"`.
- Prepare release signing outside the repository.
- Run debug unit tests and assemble before creating an internal testing artifact.
- Perform real-device smoke tests for campaign map, daily challenge, leaderboard, settings persistence, sound/music toggles, profile reset, and pause/victory/game-over flows.
- Review `CREDITS.md` before uploading an artifact so bundled Kenney CC0 sources stay visible.
- Publishing automation is intentionally not configured yet.

## Privacy Note

Arcane Circuit Defense is local-only in Iteration 7.

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

- The bundled sprites are a curated Kenney prototype pass, not final authored art for every enemy family.
- Some enemies use Kenney crystal/rock/tower-defense props as readable stand-ins while keeping color-coded Canvas fallback in high contrast mode.
- Music remains a persisted placeholder until a safe lightweight CC0 loop is bundled.
- Daily challenge is local-only and date-seeded; there is no server authority or anti-cheat.
- Leaderboard is local-only and can be reset with profile data.
- Balance is broader in Iteration 7 but still needs longer real-device playtesting.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Tablet and foldable layouts are acceptable but not fully custom.
- Ability targeting is automatic rather than manually aimed.
- Save slots are local profiles without cloud sync.

## Suggested Iteration 8 Roadmap

- Play Store internal testing.
- Real device QA checklist.
- Balancing from playtest data.
- Onboarding polish.
- Campaign reward economy.
- More authored level art.
- Optional cloud leaderboard later.
- Optional monetization later.
