using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public enum GridCellKind
    {
        Buildable,
        Path,
        Spawn,
        Base,
        Locked
    }

    public sealed class GridCell
    {
        public GridCell(int row, int column, GridCellKind kind, Vector3 worldPosition)
        {
            Row = row;
            Column = column;
            Kind = kind;
            WorldPosition = worldPosition;
        }

        public int Row { get; }
        public int Column { get; }
        public GridCellKind Kind { get; set; }
        public Vector3 WorldPosition { get; }
        public Tower OccupyingTower { get; set; }

        public Vector2Int Coordinate
        {
            get { return new Vector2Int(Column, Row); }
        }

        public bool HasTower
        {
            get { return OccupyingTower != null; }
        }

        public bool IsBuildable
        {
            get { return Kind == GridCellKind.Buildable && !HasTower; }
        }

        public bool IsEndpoint
        {
            get { return Kind == GridCellKind.Spawn || Kind == GridCellKind.Base; }
        }
    }
}

