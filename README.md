# Denicolaiel Game

Denicolaiel Game is a native Android 2.5D isometric tower defense game built with Kotlin, Gradle, Jetpack Compose, and Compose Canvas. Enemies spawn in waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 4 Features

- Native Android Kotlin app with Jetpack Compose screens and Compose Canvas game rendering.
- Profile select, tutorial, main menu, level select, difficulty selection, game screen, settings, achievements, pause, victory, and game over flows.
- Five handcrafted isometric levels with dynamic A* rerouting and tower placement validation.
- Deterministic wave lifecycle with explicit `Ready`, `In Progress`, `Cleared`, and `Finished` states.
- Fixed the Next Wave bug so the button only appears when starting the next wave is valid.
- Three tower types: Basic, Sniper, and Frost.
- Tower upgrades, sell/refund controls, tower range preview, selected tower stats, and current-level targeting modes.
- Tower targeting modes: First, Last, Strongest, Weakest, and Closest.
- Active player abilities: Meteor Strike, Freeze Pulse, and Emergency Gold, each with cooldown state and Canvas feedback.
- Enemy types: Normal, Fast, Tank, Boss, Juggernaut, and Regenerator.
- Boss wave support with named boss wave UI, slow-resistant Juggernauts, and regenerating Regenerators.
- Difficulty modes: Easy, Normal, and Hard.
- Difficulty affects enemy health, enemy speed, rewards, starting gold/lives, and score.
- Improved wave UX with clear status text, enemy counts, and next-wave previews.
- Canvas polish: stronger tower firing flash, projectile hit rings, fading damage/reward numbers, death effects, frost rings, health bars, and boss markers.
- DataStore persistence for up to three local profiles, per-profile best scores, highest unlocked level, achievements, and tutorial completion.
- Global settings persistence for sound, music placeholder, grid visibility, and last difficulty.
- Settings screen for sound, music placeholder, grid visibility, and active-profile reset.
- Local achievements: First Victory, No Lives Lost, Tower Specialist, Boss Slayer, and Hard Mode Clear.
- JVM unit tests for pathfinding, placement rejection, upgrades, wave lifecycle, final-wave victory, difficulty modifiers, boss waves, targeting selection, ability cooldown/effects, profile slot rules, settings defaults, achievements, level catalog validity, and progression through Level 5.

## Wave Progression Fix

The wave manager now uses an explicit phase model instead of a loose `awaitingNextWave` transition. A wave can only start from `Ready` or `Cleared`, duplicate starts are rejected while `In Progress`, and the final wave moves to `Finished` only after all enemies have spawned and no active enemies remain.

## Iteration 4 Highlights

- Targeting modes are stored on each placed tower for the current run and can be changed from the tower panel.
- Meteor Strike auto-targets a high-priority enemy cluster and deals area damage.
- Freeze Pulse slows enemies in an area, with bosses applying their slow resistance.
- Emergency Gold grants a limited-use cash boost with its own cooldown.
- Level 4 focuses on reroute choices, while Level 5 adds tighter economy and stacked boss pressure.
- A first-launch tutorial explains tower placement, path blocking, upgrades/sell, wave control, and abilities.

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

## Run On Emulator Or Device

1. Open this repository in Android Studio.
2. Let Gradle sync.
3. Select the `app` run configuration.
4. Run on an emulator or Android 7.0/API 24+ device.

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` after a successful command-line build.

## Known Limitations

- Visuals are still generated Canvas shapes rather than authored production art.
- Audio uses generated Android tones, and music is a persisted placeholder toggle only.
- Balance is improved for Iteration 4 but still needs longer playtesting on real devices.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Tablet and foldable layouts are improved only through simpler responsive spacing, not a dedicated large-screen UI.
- Achievements are local only.
- Ability targeting is automatic for Iteration 4 rather than manually aimed.
- Save slots are local profiles without cloud sync.

## Suggested Iteration 5 Roadmap

- Add authored art and sound packs.
- Add tower targeting visual indicators.
- Add more enemy archetypes.
- Add daily challenge mode.
- Add a local leaderboard.
- Add a cloud leaderboard as an optional later feature.
- Prepare Play Store metadata, signing, and release pipeline.
- Profile performance on emulator and physical devices.
- Add accessibility improvements.
