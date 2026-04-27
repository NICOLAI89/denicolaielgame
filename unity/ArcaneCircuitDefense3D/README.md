# Arcane Circuit Defense 3D Starter

This folder is an Iteration 10 Unity migration starter. It is not intended to replace the Android/Kotlin prototype yet.

## How To Use

1. Create a new Unity 3D project through Unity Hub.
2. Use a mobile-friendly Unity version already approved for your Android toolchain.
3. Copy or keep this `Assets/` folder inside the Unity project.
4. Open a new scene and add empty GameObjects for:
   - `GameManager`
   - `GridManager`
   - `PathfindingService`
   - `WaveManager`
   - `EconomyManager`
   - `BuildController`
   - `UIHudController`
   - `CameraController`
5. Attach the matching scripts.
6. Create `EnemyTypeDefinition`, `TowerTypeDefinition`, and `LevelDefinition` assets from the Unity asset menu, or let the runtime fallback definitions create a simple vertical slice.

## Vertical Slice Goal

- One low-poly 3D map.
- One spawn tile and one base tile.
- Dynamic A* pathfinding.
- Buildable tiles.
- One tower.
- One enemy.
- Five waves.
- Gold, lives, score, Start Wave button, and basic HUD.

The existing Android project remains the gameplay reference.

