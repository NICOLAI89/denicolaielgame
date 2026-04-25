# Arcane Circuit Defense

Arcane Circuit Defense is a native Android 2.5D isometric tower defense prototype built with Kotlin, Gradle, Jetpack Compose, and Compose Canvas. Enemies spawn in deterministic waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 6 Features

- Native Android Kotlin app with Jetpack Compose screens and centralized Compose Canvas rendering.
- Profile select, tutorial, main menu, level select, daily challenge, local leaderboard, settings, achievements, pause, victory, and game over flows.
- Seven handcrafted campaign levels with progression unlocks through Level 7.
- Daily challenge mode with a deterministic local date seed, fixed daily difficulty, fixed daily modifiers, generated daily waves, and local best daily score saving.
- Local leaderboard for completed campaign runs, including profile slot, level, difficulty, score, completion time, lives remaining, and completion date/time.
- Dynamic A* rerouting with tower placement validation and wave lifecycle states: `Ready`, `In Progress`, `Cleared`, and `Finished`.
- Three tower types: Basic, Sniper, and Frost, retuned for the longer campaign.
- Tower upgrades, sell/refund controls, tower range preview, selected tower stats, current targeting mode indicator, targeting mode buttons, and firing aim beams.
- Tower targeting modes: First, Last, Strongest, Weakest, and Closest.
- Active abilities: Meteor Strike, Freeze Pulse, and Emergency Gold, each with cooldown state and clearer cooldown labels.
- Enemy types: Normal, Fast, Tank, Shielded, Swarm, Boss, Juggernaut, and Regenerator.
- Shielded enemies reduce incoming damage; Swarm enemies are low-health, fast, high-count pressure units.
- Boss waves with named UI, slow-resistant Juggernauts, and regenerating Regenerators.
- Difficulty modes: Easy, Normal, and Hard. Difficulty affects enemy health, speed, rewards, starting gold/lives, and score.
- Accessibility and feedback toggles persisted with DataStore: sound, music placeholder, grid visibility, screen shake, damage numbers, high contrast mode, FPS counter, and last difficulty.
- Lightweight performance HUD toggle showing FPS, average frame time, and average update time.
- Local run summary tracking for waves completed, towers built/upgraded/sold, abilities used, enemies killed, bosses killed, and run time.
- DataStore persistence for up to three local profiles, per-profile best scores, highest unlocked level, achievements, tutorial completion, leaderboard entries, daily bests, and settings.
- Local achievements: First Victory, No Lives Lost, Tower Specialist, Boss Slayer, and Hard Mode Clear.
- App label confirmed as "Arcane Circuit Defense"; debug builds use versionCode `1` and versionName `0.1.0`.
- Intentional dark splash/theme colors and vector launcher icon.

## Iteration 6 Highlights

- The prototype now has repeatable reasons to return: local leaderboard runs and a deterministic daily challenge.
- Level 6 introduces Shielded and Swarm enemies; Level 7 combines bosses, shields, swarms, abilities, and tighter targeting pressure.
- Selected tower targeting is easier to read, and firing beams make target choice visible during combat.
- High contrast visuals, damage number toggle, screen shake toggle, readable cooldown text, and the optional FPS HUD make playtesting more practical.

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

## Run On Emulator Or Device

1. Open this repository in Android Studio.
2. Let Gradle sync.
3. Select the `app` run configuration.
4. Run on an emulator or Android 7.0/API 24+ device.

## Release Readiness Checklist

- App label: "Arcane Circuit Defense".
- Package id: `com.nicolaielgame`.
- Version: `versionCode = 1`, `versionName = "0.1.0"`.
- Local-only systems: profiles, progress, achievements, leaderboard, and daily challenge bests.
- No backend, login, ads, in-app purchases, or external game engine.
- Splash/theme colors and launcher vector are present.
- Command-line validation target: `testDebugUnitTest`, `assembleDebug`, and `git diff --check`.
- Play Store publishing is intentionally not configured yet.

## Known Limitations

- Visuals are still generated Canvas shapes rather than authored production art.
- Audio uses generated Android tones, and music is a persisted placeholder toggle only.
- Daily challenge is local-only and date-seeded; there is no server authority or anti-cheat.
- Leaderboard is local-only and can be reset with profile data.
- Balance is broader in Iteration 6 but still needs longer real-device playtesting.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Tablet and foldable layouts rely on responsive spacing, not a dedicated large-screen UI.
- Ability targeting is automatic rather than manually aimed.
- Save slots are local profiles without cloud sync.

## Suggested Iteration 7 Roadmap

- Real authored art and sound pack.
- More daily challenge modifiers.
- Local achievements expansion.
- Advanced tower targeting visuals.
- Campaign map.
- Local economy/progression rewards.
- Play Store internal testing track.
- Optional cloud leaderboard later.
