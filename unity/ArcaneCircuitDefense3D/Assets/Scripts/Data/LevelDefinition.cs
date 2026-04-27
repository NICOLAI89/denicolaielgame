using System;
using System.Collections.Generic;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    [Serializable]
    public struct GridCoordinate
    {
        public int row;
        public int column;

        public GridCoordinate(int row, int column)
        {
            this.row = row;
            this.column = column;
        }

        public Vector2Int ToVector2Int()
        {
            return new Vector2Int(column, row);
        }
    }

    [Serializable]
    public sealed class WaveEnemyGroup
    {
        public EnemyTypeDefinition enemyType;
        public int count = 1;
    }

    [Serializable]
    public sealed class WaveDefinition
    {
        public List<WaveEnemyGroup> groups = new List<WaveEnemyGroup>();
        public float spawnInterval = 0.9f;

        public int TotalEnemies
        {
            get
            {
                var total = 0;
                foreach (var group in groups)
                {
                    total += Mathf.Max(0, group.count);
                }

                return total;
            }
        }

        public List<EnemyTypeDefinition> BuildSpawnQueue()
        {
            var queue = new List<EnemyTypeDefinition>();
            foreach (var group in groups)
            {
                if (group.enemyType == null)
                {
                    continue;
                }

                for (var i = 0; i < group.count; i++)
                {
                    queue.Add(group.enemyType);
                }
            }

            return queue;
        }
    }

    [CreateAssetMenu(menuName = "Arcane Circuit Defense/Level Definition")]
    public sealed class LevelDefinition : ScriptableObject
    {
        public int levelId = 1;
        public string title = "Unity Vertical Slice";
        [TextArea] public string description = "One 3D map, one tower, one enemy, five waves.";
        public int rows = 9;
        public int columns = 9;
        public GridCoordinate spawn = new GridCoordinate(0, 4);
        public GridCoordinate baseTile = new GridCoordinate(8, 4);
        public List<GridCoordinate> lockedCells = new List<GridCoordinate>();
        public List<GridCoordinate> scenicPath = new List<GridCoordinate>();
        public int startingGold = 160;
        public int startingLives = 20;
        public List<WaveDefinition> waves = new List<WaveDefinition>();

        public bool Contains(GridCoordinate coordinate)
        {
            return coordinate.row >= 0 &&
                coordinate.row < rows &&
                coordinate.column >= 0 &&
                coordinate.column < columns;
        }

        public bool IsLocked(int row, int column)
        {
            return ContainsCoordinate(lockedCells, row, column);
        }

        public bool IsScenicPath(int row, int column)
        {
            return ContainsCoordinate(scenicPath, row, column);
        }

        public static LevelDefinition CreateRuntimeVerticalSlice(EnemyTypeDefinition normalEnemy)
        {
            var level = CreateInstance<LevelDefinition>();
            level.title = "Unity Vertical Slice";
            level.description = "Runtime fallback map for the first Unity tower-defense slice.";
            level.rows = 9;
            level.columns = 9;
            level.spawn = new GridCoordinate(0, 4);
            level.baseTile = new GridCoordinate(8, 4);
            level.startingGold = 160;
            level.startingLives = 20;

            for (var row = 0; row < 9; row++)
            {
                level.scenicPath.Add(new GridCoordinate(row, 4));
            }

            level.lockedCells.Add(new GridCoordinate(0, 4));
            level.lockedCells.Add(new GridCoordinate(1, 4));
            level.lockedCells.Add(new GridCoordinate(7, 4));
            level.lockedCells.Add(new GridCoordinate(8, 4));

            var counts = new[] { 6, 7, 8, 9, 10 };
            for (var i = 0; i < counts.Length; i++)
            {
                level.waves.Add(new WaveDefinition
                {
                    spawnInterval = Mathf.Max(0.45f, 0.9f - i * 0.08f),
                    groups = new List<WaveEnemyGroup>
                    {
                        new WaveEnemyGroup
                        {
                            enemyType = normalEnemy,
                            count = counts[i]
                        }
                    }
                });
            }

            return level;
        }

        private static bool ContainsCoordinate(IEnumerable<GridCoordinate> coordinates, int row, int column)
        {
            foreach (var coordinate in coordinates)
            {
                if (coordinate.row == row && coordinate.column == column)
                {
                    return true;
                }
            }

            return false;
        }
    }
}

