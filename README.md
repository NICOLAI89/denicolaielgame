# Denicolaiel Game

Denicolaiel Game is a native Android 2.5D isometric tower defense game built with Kotlin, Gradle, Jetpack Compose, and Compose Canvas. Enemies spawn in waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 3 Features

- Native Android Kotlin app with Jetpack Compose screens and Compose Canvas game rendering.
- Main menu, level select, difficulty selection, game screen, settings, achievements, pause, victory, and game over flows.
- Three handcrafted isometric levels with dynamic A* rerouting and tower placement validation.
- Deterministic wave lifecycle with explicit `Ready`, `In Progress`, `Cleared`, and `Finished` states.
- Fixed the Next Wave bug so the button only appears when starting the next wave is valid.
- Three tower types: Basic, Sniper, and Frost.
- Tower upgrades, sell/refund controls, tower range preview, and selected tower stats.
- Four enemy types: Normal, Fast, Tank, and Boss.
- Boss wave support, with a Level 3 boss wave and distinct boss visuals/rewards.
- Difficulty modes: Easy, Normal, and Hard.
- Difficulty affects enemy health, enemy speed, rewards, starting gold/lives, and score.
- Improved wave UX with clear status text, enemy counts, and next-wave previews.
- Canvas polish: stronger tower firing flash, projectile hit rings, fading damage/reward numbers, death effects, frost rings, health bars, and boss markers.
- DataStore persistence for best scores, highest unlocked level, last difficulty, settings, and achievements.
- Settings screen for sound, music placeholder, grid visibility, and reset progress.
- Local achievements: First Victory, No Lives Lost, Tower Specialist, Boss Slayer, and Hard Mode Clear.
- JVM unit tests for pathfinding, placement rejection, upgrades, wave lifecycle, final-wave victory, difficulty modifiers, boss waves, settings defaults, achievements, and progression rules.

## Wave Progression Fix

The wave manager now uses an explicit phase model instead of a loose `awaitingNextWave` transition. A wave can only start from `Ready` or `Cleared`, duplicate starts are rejected while `In Progress`, and the final wave moves to `Finished` only after all enemies have spawned and no active enemies remain.

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
- Balance is improved for Iteration 3 but still needs longer playtesting.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Tablet and foldable layouts are improved only through simpler responsive spacing, not a dedicated large-screen UI.
- Achievements are local only.

## Suggested Iteration 4 Roadmap

- Add authored art packs.
- Add real sound and music assets.
- Add richer enemy AI and more special enemy behavior.
- Add tower targeting modes.
- Add special powers or player abilities.
- Add cloud save or leaderboard support.
- Prepare Play Store metadata, signing, and release pipeline.
- Consider monetization only as a future optional phase.
