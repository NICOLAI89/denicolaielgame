# Gameplay Specification

This document captures the current Kotlin Android prototype so the Unity 3D version can rebuild the same game before expanding it.

## Core Loop

Arcane Circuit Defense is a grid-based tower defense game. Enemies spawn from a spawn tile, follow an A* path to the base, and remove one life when they reach it. The player earns gold by defeating enemies, spends gold on towers and upgrades, uses cooldown abilities during waves, and wins by clearing all waves with at least one life remaining.

The loop:

1. Select a campaign level or daily challenge.
2. Choose difficulty.
3. Place towers on buildable tiles without fully blocking the route.
4. Start a wave.
5. Enemies move through the current route while towers fire automatically.
6. Player upgrades/sells towers, changes targeting modes, and uses abilities.
7. A cleared wave unlocks the next wave or triggers victory after the final wave.
8. Completed campaign runs update profile progress, achievements, and local leaderboard.

## Dynamic Pathfinding

The prototype uses A* on a rectangular grid:

- Orthogonal movement only.
- Spawn and base must remain reachable.
- Towers are blocked cells.
- `GameMap.scenicPath` cells have slightly cheaper movement cost, so enemies prefer authored paths when not rerouted.
- A tower placement is denied if it blocks the spawn-to-base path or traps an enemy.
- Selling a tower recalculates enemy paths.
- Existing enemies recalculate from their current cell to the base after placement/sell events.

Unity should preserve this mechanic because it is the signature gameplay system.

## Tower Placement Rules

- Towers can be placed only while the run status is `Running`.
- Spawn, base, locked gate cells, occupied tower cells, and cells occupied by current enemies are invalid.
- The player must have enough gold for the selected tower type.
- Placement must leave a valid path for future spawns.
- Placement must leave valid routes for enemies already on the board.
- Tapping an existing tower selects it for inspection, upgrades, selling, and targeting.
- Tapping the selected tower button again exits placement mode.

## Towers

| Tower | Cost | Damage | Range | Fire interval | Projectile speed | Role |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Basic | 30 | 32 | 2.75 | 0.62s | 7.1 | Reliable early damage. |
| Sniper | 68 | 96 | 4.45 | 1.74s | 10.2 | Long-range burst damage. |
| Frost | 50 | 17 | 3.05 | 0.92s | 6.4 | Low damage plus slow. |

Tower upgrades:

- Start at level 1.
- Each level adds 34% damage, +0.24 range, +0.45 projectile speed.
- Fire interval is reduced by 8.5% per level and clamps at 0.3s.
- Frost slow duration increases by 0.22s per level.
- Upgrade cost is based on tower base cost and target level.
- Sell refund is 70% of total invested gold, minimum 1.

Targeting modes:

- First: highest path progress, then higher health.
- Last: lowest path progress, then lower health.
- Strongest: highest health, then path progress.
- Weakest: lowest health, then later path progress.
- Closest: shortest grid distance to the tower.

## Enemies

| Enemy | Health | Speed | Reward | Score | Notes |
| --- | ---: | ---: | ---: | ---: | --- |
| Normal | 40 | 0.92 | 11 | 18 | Baseline enemy. |
| Fast | 30 | 1.34 | 9 | 16 | Faster, lower health. |
| Tank | 122 | 0.60 | 22 | 38 | Slow, durable. |
| Shielded | 92 | 0.72 | 24 | 46 | Takes 58% incoming damage. |
| Swarm | 18 | 1.55 | 6 | 12 | Small, fast, high-count pressure. |
| Boss | 540 | 0.45 | 90 | 190 | Large boss enemy. |
| Juggernaut | 700 | 0.40 | 120 | 255 | Boss with strong slow resistance. |
| Regenerator | 610 | 0.48 | 130 | 275 | Boss that regenerates 6.5 health/sec. |

Boss behavior:

- Boss types are larger and trigger boss-wave presentation.
- Defeating a boss increments boss defeat stats, plays boss death audio, and triggers stronger feedback.
- Juggernaut resists slow heavily.
- Regenerator heals while alive, up to max health.

## Abilities

Meteor Strike:

- Cooldown: 20s.
- Targets the most relevant enemy cluster by choosing a boss first, otherwise high-progress enemies.
- Deals 135 base damage in a 2.05 grid radius.
- Uses enemy damage multipliers.

Freeze Pulse:

- Cooldown: 18s.
- Targets the same automatic enemy center rule.
- Applies slow in a 2.8 grid radius.
- Slow multiplier is 0.34 before enemy vulnerability/resistance.
- Base duration is 3.45s.

Emergency Gold:

- Cooldown: 24s.
- Grants 55 gold.
- Limited to 2 uses per run.

## Wave Lifecycle

Wave phases:

- `Ready`: first wave can start.
- `InProgress`: spawn queue is active and enemies may be alive.
- `Cleared`: a non-final wave is complete and the next wave can start.
- `Finished`: final wave spawn queue is complete.

Victory occurs when the wave manager is finished and no enemies remain. Game over occurs when lives reach zero.

Auto-start waves, when enabled, waits 1.35 seconds after a cleared non-final wave, does not run while paused, and never starts after final victory.

## Difficulty Modes

| Difficulty | Enemy health | Enemy speed | Rewards | Score | Starting gold | Lives |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Easy | 0.78x | 0.88x | 1.20x | 0.85x | 1.28x | +6 |
| Normal | 1.00x | 1.00x | 1.00x | 1.00x | 1.00x | +0 |
| Hard | 1.22x | 1.10x | 0.90x | 1.35x | 0.90x | -3 |

## Campaign Levels

There are seven campaign levels:

1. Green Run: simple straight route, 9x9, 5 waves.
2. Switchback: wider reroute map, 10x10, first boss appears.
3. Crucible: tighter 11x11 lanes and heavier waves.
4. Crosswinds: multiple reroute options, Regenerator boss.
5. Last Gate: tight economy and boss mechanics.
6. Mirror Swarm: introduces Swarm and Shielded pressure.
7. Arcane Apex: final prototype challenge with bosses, shields, swarms, and ability timing.

Unity should convert these into data assets after the first vertical slice is playable. Exact map coordinates currently live in `LevelDefinition.kt`; port them directly into Unity `LevelDefinition` assets so spawn, base, scenic path, and build-locked cells match the prototype.

| Level | Size | Spawn | Base | Gold | Lives | Wave compositions |
| --- | --- | --- | --- | ---: | ---: | --- |
| 1 Green Run | 9x9 | 0,4 | 8,4 | 160 | 22 | N6; N7 F3; F8 N5; N8 T2; F8 T3 N6 |
| 2 Switchback | 10x10 | 0,2 | 9,7 | 170 | 18 | N8 F4; F10 N6; T3 N10; F12 T3; N10 F8 T5 B1 |
| 3 Crucible | 11x11 | 1,0 | 8,10 | 195 | 16 | F10 N6; T4 F8; N12 T5; F14 T5; N12 F10 T5 J1 |
| 4 Crosswinds | 12x12 | 0,5 | 11,6 | 220 | 15 | F14 N10; T5 F12; N18 T5; F18 T6 B1; N16 F14 T7 R1 |
| 5 Last Gate | 13x13 | 1,0 | 10,12 | 245 | 15 | F16 N12; T7 F14; N20 T7 B1; F22 T8 J1; N18 F18 T9 J1 R1 |
| 6 Mirror Swarm | 13x13 | 0,3 | 12,10 | 278 | 14 | S18 N12; Sh5 F12; S24 T5; Sh8 F16 B1; S26 Sh8 T7 R1 |
| 7 Arcane Apex | 14x14 | 1,0 | 13,13 | 318 | 13 | F16 S18 N10; Sh8 T6 S16; F22 Sh9 B1; S30 T8 J1; Sh10 F18 R1 B1; S34 Sh12 T9 J1 R1 |

Legend: N = Normal, F = Fast, T = Tank, Sh = Shielded, S = Swarm, B = Boss, J = Juggernaut, R = Regenerator.

## Daily Challenge

Daily challenge is local-only:

- Date key format: `yyyy-MM-dd`.
- Stable deterministic seed from date string.
- Base level selected from the campaign catalog.
- Difficulty selected deterministically.
- Two daily modifiers are selected from Shielded Vanguard, Swarm Surge, Lean Economy, and Boss Pressure.
- Daily level id is `100 + baseLevelId`.
- Daily waves are generated with Normal, Fast, Tank, Shielded, Swarm, Boss, and Juggernaut mixes.
- Best daily result is saved per date/profile slot.

## Progression And Persistence

Profiles:

- Three local profile slots.
- Each profile stores highest unlocked level, best score, level scores, tutorial completion, achievements, daily bests, and leaderboard entries.

Campaign unlock:

- Level 1 starts unlocked.
- Winning a level unlocks the next level up to Level 7.
- Completed levels are determined by having a positive best score.

Leaderboard:

- Local-only.
- Completed campaign runs only.
- Stores profile slot, level, difficulty, score, completion time, lives remaining, and completion timestamp.
- Ranking is score descending, completion time ascending, lives descending, date descending.

Settings:

- Sound enabled.
- Music enabled.
- Show grid.
- Screen shake enabled.
- Damage numbers enabled.
- High contrast mode.
- FPS counter enabled.
- Auto-start waves enabled.
- Last difficulty.

Achievements:

- First Victory.
- No Lives Lost.
- Tower Specialist.
- Boss Slayer.
- Hard Mode Clear.

## Economy And Scoring

- Starting gold/lives come from level data and difficulty modifiers.
- Enemy rewards add gold on death.
- Enemy score values add score on death.
- Victory adds a difficulty-scaled completion bonus of `250 + levelId * 75`.
- Victory grants 50 bonus gold.
- Emergency Gold adds 55 gold without score.

## Win/Loss Conditions

- Win: final wave finished, no enemies alive, lives greater than zero.
- Loss: lives reach zero.
- Paused games do not tick enemy movement, waves, towers, abilities, or auto-start timers.
