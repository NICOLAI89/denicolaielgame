# Localization Plan

Target languages:

- English.
- Spanish.
- Portuguese.
- Chinese.
- Russian.

## Current Android-Native Approach

The current prototype is mostly hardcoded English strings in Kotlin/Compose screens and model titles. It does not yet use Android resource-string localization comprehensively.

Because the strategic direction is Unity 3D migration, do not spend a large iteration localizing the Kotlin prototype. Keep small corrections safe, but prioritize Unity-ready string structure.

## Recommended Unity Approach

Use Unity Localization package or a simple table-backed localization service early in the Unity project. The preferred structure:

- String tables by key.
- One table per language.
- A runtime language selector in Settings.
- Main menu language selector for first-run access.
- Fallback to English for missing strings.
- Avoid embedding player-facing strings in gameplay scripts.

## Locale Codes

| Language | Suggested locale |
| --- | --- |
| English | `en` |
| Spanish | `es` |
| Portuguese | `pt` |
| Chinese | `zh-Hans` first, `zh-Hant` later if needed |
| Russian | `ru` |

## Key UI Strings

Main menu:

- Play.
- Campaign.
- Daily Challenge.
- Leaderboard.
- Achievements.
- Profiles.
- Settings.
- Tutorial.
- Quit.

Gameplay HUD:

- Gold.
- Lives.
- Score.
- Wave.
- Start Wave.
- Next Wave.
- Pause.
- Resume.
- Victory.
- Game Over.
- Retry.
- Exit.

Tower panel:

- Build.
- Upgrade.
- Sell.
- Range.
- Damage.
- Fire Rate.
- Targeting.
- First.
- Last.
- Strongest.
- Weakest.
- Closest.
- Not enough gold.
- Path blocked.

Abilities:

- Meteor Strike.
- Freeze Pulse.
- Emergency Gold.
- Recharging.
- Ready.

Settings:

- Sound.
- Music.
- Screen Shake.
- Damage Numbers.
- High Contrast.
- FPS Counter.
- Auto-start Waves.
- Language.

Progression:

- Locked.
- Unlocked.
- Completed.
- Best Score.
- New Record.
- Profile Slot.

Daily challenge:

- Today's Challenge.
- Modifiers.
- Best Daily Score.
- Local only.

## Implementation Plan

1. Define English keys while building the Unity HUD.
2. Keep UI scripts referencing keys, not literal strings.
3. Add Spanish and Portuguese first for Latin-language layout checks.
4. Add Chinese and Russian to catch font, width, and line-height issues.
5. Include a language selector in Settings.
6. Add first-run language selection only after the main menu stabilizes.
7. Keep Android prototype localization deferred unless release plans return to native Android.

## Font And Layout Notes

- Choose fonts that support Latin, Chinese, and Cyrillic scripts.
- Avoid narrow fixed-width buttons.
- Test Russian and Portuguese strings for length.
- Test Chinese with larger minimum line heights.
- Do not use all-caps for every label because localization length varies heavily.

