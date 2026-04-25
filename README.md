# Denicolaiel Game

Denicolaiel Game is a native Android 2.5D isometric tower defense game built with Kotlin, Gradle, Jetpack Compose, and Compose Canvas. Enemies spawn in waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 2 Features

- Android Gradle project with a Kotlin `app` module, Compose UI, and Compose Canvas rendering.
- Main menu, level select, game screen, pause overlay, victory screen, and game over screen.
- Three playable levels with distinct grid sizes, spawn/base cells, locked cells, starting gold, lives, and wave definitions.
- Dynamic A* pathfinding across every level.
- Tower placement validation that rejects occupied cells, locked cells, enemy-occupied cells, insufficient gold, and path-blocking placements.
- Tower range previews for selected tower types and existing towers.
- Tower upgrade and sell/refund actions from a compact tower panel.
- Mixed enemy waves with visible health bars, hit feedback, and slow status visuals.
- Manual next-wave button after each cleared wave.
- Score, gold, lives, victory/loss conditions, best score tracking, and level unlock progression.
- Jetpack DataStore persistence for legacy best score, per-level best scores, and highest unlocked level.
- Safe generated Android tone hooks for placement, upgrade, sell, enemy hit, enemy kill, wave start, victory, and game over.
- Fast JVM tests for pathfinding, placement rejection, tower upgrades, wave definitions, and progression rules.

## Tower Types

- **Basic Tower**: balanced damage, range, fire rate, and cost.
- **Sniper Tower**: high damage and long range with a slower fire rate.
- **Frost Tower**: lower damage with a temporary enemy slow effect.

## Enemy Types

- **Normal Enemy**: baseline health, reward, score, and speed.
- **Fast Enemy**: lower health, lower reward, and higher speed.
- **Tank Enemy**: high health, higher reward, and slower movement.

## Level Select And Progression

The game currently includes three handcrafted maps:

- **Level 1: Green Run**: a clean intro lane for learning tower placement.
- **Level 2: Switchback**: wider routing with more meaningful reroutes.
- **Level 3: Crucible**: tighter lanes and heavier mixed waves.

Only Level 1 is unlocked by default. Winning a level unlocks the next level, and best scores are saved per level.

## Open In Android Studio

1. Open Android Studio.
2. Choose **Open**.
3. Select this repository folder.
4. Let Android Studio sync the Gradle project.
5. Use an emulator or physical device running Android 7.0/API 24 or newer.

## Build From Command Line

From the repository root on Windows:

```powershell
.\gradlew.bat assembleDebug
```

Run JVM tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

On macOS or Linux:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Run On Emulator Or Device

1. Start an Android emulator or connect a physical Android device with USB debugging enabled.
2. In Android Studio, select the `app` run configuration.
3. Press **Run**.

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` after a successful command-line build.

## Known Limitations

- Visuals are polished placeholder Canvas shapes, not authored production art.
- Audio uses generated Android tones instead of authored sound assets.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Balance values are first-pass and need more device playtesting.
- Phone layout is the primary target; tablet and foldable layouts need more refinement.
- There are no settings, profiles, achievements, boss waves, or difficulty modes yet.

## Suggested Iteration 3 Roadmap

- Add authored art and sound assets.
- Improve attack, impact, movement, and wave transition animations.
- Add difficulty modes and deeper balancing.
- Add boss waves and optional special enemy behaviors.
- Add save slots or player profiles.
- Refine tablet and foldable layouts.
- Add optional achievements.
- Run a full balancing pass across all tower types, maps, and waves.
