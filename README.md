# Arcane Circuit Defense

Arcane Circuit Defense is a native Android 2.5D isometric tower defense game built with Kotlin, Gradle, Jetpack Compose, and Compose Canvas. Enemies spawn in waves and dynamically reroute with A* pathfinding when the player places, upgrades, or sells towers.

## Current Iteration 5 Features

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
- Arcane Circuit Defense visual identity with neon fantasy-tech board ambience, deeper isometric tiles, stronger tower/enemy silhouettes, projectile glow, impact bursts, and more readable boss visuals.
- Game-feel pass with tower muzzle pulses, enemy damage blink, heavier boss hit/death feedback, meteor/freeze ability bursts, subtle board-only screen shake, and wave start banners.
- Difficulty modes: Easy, Normal, and Hard.
- Difficulty affects enemy health, enemy speed, rewards, starting gold/lives, and score; Iteration 5 retunes towers, enemies, abilities, bosses, and level starts.
- Improved wave UX with clear status text, enemy counts, next-wave previews, boss card markers, and wave start transitions.
- Local run summary tracking for waves completed, towers built/upgraded/sold, abilities used, enemies killed, bosses killed, and run time.
- DataStore persistence for up to three local profiles, per-profile best scores, highest unlocked level, achievements, and tutorial completion.
- Global settings persistence for sound, music placeholder, grid visibility, screen shake, damage numbers, high contrast mode, and last difficulty.
- Settings screen for feedback/accessibility toggles and active-profile reset.
- Local achievements: First Victory, No Lives Lost, Tower Specialist, Boss Slayer, and Hard Mode Clear.
- JVM unit tests for pathfinding, placement rejection, upgrades, wave lifecycle, final-wave victory, difficulty modifiers, boss waves, targeting selection, ability cooldown/effects, profile slot rules, run stats, balance sanity, settings defaults, achievements, level catalog validity, and progression through Level 5.

## Wave Progression Fix

The wave manager now uses an explicit phase model instead of a loose `awaitingNextWave` transition. A wave can only start from `Ready` or `Cleared`, duplicate starts are rejected while `In Progress`, and the final wave moves to `Finished` only after all enemies have spawned and no active enemies remain.

## Iteration 5 Highlights

- The board now reads as floating arcane circuitry rather than plain placeholder tiles.
- Combat feedback is clearer: damage flashes, projectile glow, stronger frost visuals, impact rings, boss warnings, and subtle board shake.
- Ability buttons show cooldown progress bars and terminal screens summarize the completed run.
- Balance is smoother for first-time play on Easy, tighter on Normal, and fairer on Hard without removing the importance of dynamic path blocking.
- The game loop avoids recalculating the display path every frame; path preview updates when blocking changes.

## Performance Notes

- Game logic remains in `GameEngine` and pure model/system classes rather than composables.
- Compose Canvas rendering is centralized in `IsoRenderer`.
- The renderer caches sorted map cells per renderer instance.
- A* path preview is no longer recalculated every tick; tower placement/sell still refreshes reroutes.
- Screen shake is applied only inside the Canvas board transform, so HUD text and buttons stay stable.

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
- Balance is improved for Iteration 5 but still needs longer playtesting on real devices.
- Enemy movement uses grid-space interpolation rather than advanced steering.
- Tablet and foldable layouts are improved only through simpler responsive spacing, not a dedicated large-screen UI.
- Achievements are local only.
- Ability targeting is automatic for Iteration 5 rather than manually aimed.
- Save slots are local profiles without cloud sync.
- The run summary is local and per-run; it is not yet a persistent leaderboard.

## Suggested Iteration 6 Roadmap

- Real authored art pack.
- Real authored sound and music pack.
- Tower targeting visual indicators.
- More levels and enemy families.
- Daily challenge mode.
- Local leaderboard.
- Accessibility refinement.
- Play Store release prep.
- Optional cloud leaderboard later.
- Optional monetization later.
