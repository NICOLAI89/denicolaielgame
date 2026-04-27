using System.Collections.Generic;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public sealed class GridManager : MonoBehaviour
    {
        [SerializeField] private float tileSize = 1f;
        [SerializeField] private float tileHeight = 0.16f;
        [SerializeField] private GameObject tilePrefab;
        [SerializeField] private Material buildableMaterial;
        [SerializeField] private Material pathMaterial;
        [SerializeField] private Material spawnMaterial;
        [SerializeField] private Material baseMaterial;
        [SerializeField] private Material lockedMaterial;

        private readonly Dictionary<Vector2Int, GridCell> cells = new Dictionary<Vector2Int, GridCell>();

        public LevelDefinition Level { get; private set; }
        public GridCell SpawnCell { get; private set; }
        public GridCell BaseCell { get; private set; }
        public float TileSize { get { return tileSize; } }

        public IEnumerable<GridCell> Cells
        {
            get { return cells.Values; }
        }

        public void Build(LevelDefinition level)
        {
            Level = level;
            cells.Clear();
            ClearTiles();

            for (var row = 0; row < level.rows; row++)
            {
                for (var column = 0; column < level.columns; column++)
                {
                    var kind = ResolveKind(level, row, column);
                    var worldPosition = CellToWorld(row, column);
                    var cell = new GridCell(row, column, kind, worldPosition);
                    cells[cell.Coordinate] = cell;

                    if (kind == GridCellKind.Spawn)
                    {
                        SpawnCell = cell;
                    }
                    else if (kind == GridCellKind.Base)
                    {
                        BaseCell = cell;
                    }

                    CreateTileObject(cell);
                }
            }
        }

        public Vector3 CellToWorld(int row, int column)
        {
            return new Vector3(column * tileSize, 0f, -row * tileSize);
        }

        public bool TryGetCell(int row, int column, out GridCell cell)
        {
            return cells.TryGetValue(new Vector2Int(column, row), out cell);
        }

        public bool TryGetCellFromWorld(Vector3 worldPosition, out GridCell cell)
        {
            var column = Mathf.RoundToInt(worldPosition.x / tileSize);
            var row = Mathf.RoundToInt(-worldPosition.z / tileSize);
            return TryGetCell(row, column, out cell);
        }

        public List<GridCell> GetNeighbors(GridCell cell)
        {
            var result = new List<GridCell>(4);
            AddNeighbor(cell.Row - 1, cell.Column, result);
            AddNeighbor(cell.Row + 1, cell.Column, result);
            AddNeighbor(cell.Row, cell.Column - 1, result);
            AddNeighbor(cell.Row, cell.Column + 1, result);
            return result;
        }

        public bool CanBuildAt(GridCell cell)
        {
            return cell != null && cell.IsBuildable;
        }

        public bool IsBlockedForPath(GridCell cell, ISet<GridCell> extraBlockedCells)
        {
            if (cell == null || cell.IsEndpoint)
            {
                return false;
            }

            return cell.HasTower || (extraBlockedCells != null && extraBlockedCells.Contains(cell));
        }

        public void SetTower(GridCell cell, Tower tower)
        {
            if (cell == null)
            {
                return;
            }

            cell.OccupyingTower = tower;
        }

        public void ClearTower(GridCell cell)
        {
            if (cell == null)
            {
                return;
            }

            cell.OccupyingTower = null;
        }

        private GridCellKind ResolveKind(LevelDefinition level, int row, int column)
        {
            if (level.spawn.row == row && level.spawn.column == column)
            {
                return GridCellKind.Spawn;
            }

            if (level.baseTile.row == row && level.baseTile.column == column)
            {
                return GridCellKind.Base;
            }

            if (level.IsLocked(row, column))
            {
                return GridCellKind.Locked;
            }

            return level.IsScenicPath(row, column) ? GridCellKind.Path : GridCellKind.Buildable;
        }

        private void AddNeighbor(int row, int column, ICollection<GridCell> result)
        {
            GridCell neighbor;
            if (TryGetCell(row, column, out neighbor))
            {
                result.Add(neighbor);
            }
        }

        private void ClearTiles()
        {
            for (var i = transform.childCount - 1; i >= 0; i--)
            {
                var child = transform.GetChild(i).gameObject;
                if (Application.isPlaying)
                {
                    Destroy(child);
                }
                else
                {
                    DestroyImmediate(child);
                }
            }
        }

        private void CreateTileObject(GridCell cell)
        {
            var instance = tilePrefab != null
                ? Instantiate(tilePrefab, transform)
                : GameObject.CreatePrimitive(PrimitiveType.Cube);

            instance.name = string.Format("Tile_{0}_{1}_{2}", cell.Row, cell.Column, cell.Kind);
            instance.transform.SetParent(transform, false);
            instance.transform.position = cell.WorldPosition - Vector3.up * (tileHeight * 0.5f);
            instance.transform.localScale = new Vector3(tileSize * 0.94f, tileHeight, tileSize * 0.94f);

            var renderer = instance.GetComponent<Renderer>();
            if (renderer != null)
            {
                renderer.sharedMaterial = MaterialFor(cell.Kind);
            }
        }

        private Material MaterialFor(GridCellKind kind)
        {
            switch (kind)
            {
                case GridCellKind.Path:
                    return pathMaterial != null ? pathMaterial : buildableMaterial;
                case GridCellKind.Spawn:
                    return spawnMaterial != null ? spawnMaterial : pathMaterial;
                case GridCellKind.Base:
                    return baseMaterial != null ? baseMaterial : pathMaterial;
                case GridCellKind.Locked:
                    return lockedMaterial != null ? lockedMaterial : buildableMaterial;
                default:
                    return buildableMaterial;
            }
        }
    }
}
