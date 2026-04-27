# Credits

## Bundled Project Assets

| Asset | Provider | License | Attribution required | Local location |
| --- | --- | --- | --- | --- |
| Arcane Circuit Defense vector launcher icon | Project-authored for this repository | Repository license | No | `app/src/main/res/drawable/ic_launcher.xml` |
| Campaign, daily, leaderboard, settings, achievement, profile, and tutorial vector UI icons | Project-authored for this repository | Repository license | No | `app/src/main/res/drawable/ic_ui_*.xml` |
| Canvas tower, enemy, tile, projectile, campaign map, daily preview, and effect fallbacks | Project-authored for this repository | Repository license | No | `app/src/main/java/com/nicolaielgame/game/rendering/IsoRenderer.kt`, `app/src/main/java/com/nicolaielgame/ui/menu` |
| Generated tone audio fallback | Android platform `ToneGenerator` | Android platform API | No | `app/src/main/java/com/nicolaielgame/game/systems/SoundPlayer.kt` |
| Optional music playback routing | Project-authored for this repository | Repository license | No | `app/src/main/java/com/nicolaielgame/game/systems/SoundPlayer.kt` |

## Bundled Kenney CC0 Assets

These assets are bundled in Iteration 7B. Kenney assets are Creative Commons CC0 and can be used commercially without required attribution. Optional credit is appreciated.

| Asset pack | Provider | Source | License | Attribution required | Local location |
| --- | --- | --- | --- | --- | --- |
| Tower Defense | Kenney | https://kenney.nl/assets/tower-defense | Creative Commons CC0 | No, credit appreciated | `app/src/main/assets/tiles`, `towers`, `enemies`, `bosses` |
| UI Pack - Sci-Fi | Kenney | https://kenney.nl/assets/ui-pack-sci-fi | Creative Commons CC0 | No, credit appreciated | `app/src/main/assets/projectiles_effects`, `ui`; `app/src/main/res/drawable/kenney_icon_*.webp` |
| Interface Sounds | Kenney | https://kenney.nl/assets/interface-sounds | Creative Commons CC0 | No, credit appreciated | `app/src/main/assets/audio/sfx/button_click.ogg`, tower place/sell, wave start |
| Impact Sounds | Kenney | https://kenney.nl/assets/impact-sounds | Creative Commons CC0 | No, credit appreciated | `app/src/main/assets/audio/sfx/enemy_hit.ogg`, `meteor.ogg`, `base_hit.ogg` |
| Digital Audio | Kenney | https://kenney.nl/assets/digital-audio | Creative Commons CC0 | No, credit appreciated | `app/src/main/assets/audio/sfx/tower_fired.ogg`, upgrade, boss warning, victory, game over |
| Sci-fi Sounds | Kenney | https://kenney.nl/assets/sci-fi-sounds | Creative Commons CC0 | No, credit appreciated | `app/src/main/assets/audio/sfx/enemy_killed.ogg`, boss death, freeze pulse |

## Not Bundled

| Asset pack | Provider | Source | License | Reason |
| --- | --- | --- | --- | --- |
| Tower Defense Kit | Kenney | https://kenney.nl/assets/tower-defense-kit | Creative Commons CC0 | Optional 3D source/reference pack; not needed for the current 2.5D Canvas renderer and would increase repository/APK size. |
| Music Jingles | Kenney | https://kenney.nl/assets/music-jingles | Creative Commons CC0 | Official CC0 page verified, but no loop-sized background track was downloaded into the repo in Iteration 8. |
| Music Loops | Kenney | Kenney All-in-1 / Music Loops listing | Creative Commons CC0 | Recommended future source for `app/src/main/assets/audio/music/music_loop.ogg`; direct local download was blocked in this sandbox. |

Kenney credit line, optional: "Assets by Kenney (https://kenney.nl), CC0."
