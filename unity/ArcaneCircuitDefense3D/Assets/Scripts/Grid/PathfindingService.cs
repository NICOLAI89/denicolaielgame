using System.Collections.Generic;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public sealed class PathfindingService : MonoBehaviour
    {
        public List<GridCell> FindPath(GridManager grid, GridCell start, GridCell goal, ISet<GridCell> blockedCells)
        {
            if (grid == null || start == null || goal == null)
            {
                return null;
            }

            var open = new List<GridCell> { start };
            var cameFrom = new Dictionary<GridCell, GridCell>();
            var gScore = new Dictionary<GridCell, float> { { start, 0f } };
            var fScore = new Dictionary<GridCell, float> { { start, Heuristic(start, goal) } };
            var closed = new HashSet<GridCell>();

            while (open.Count > 0)
            {
                var current = LowestScore(open, fScore);
                if (current == goal)
                {
                    return Reconstruct(cameFrom, current);
                }

                open.Remove(current);
                closed.Add(current);

                foreach (var neighbor in grid.GetNeighbors(current))
                {
                    if (closed.Contains(neighbor) || grid.IsBlockedForPath(neighbor, blockedCells))
                    {
                        continue;
                    }

                    var tentative = GetScore(gScore, current) + MovementCost(neighbor);
                    if (!open.Contains(neighbor))
                    {
                        open.Add(neighbor);
                    }
                    else if (tentative >= GetScore(gScore, neighbor))
                    {
                        continue;
                    }

                    cameFrom[neighbor] = current;
                    gScore[neighbor] = tentative;
                    fScore[neighbor] = tentative + Heuristic(neighbor, goal);
                }
            }

            return null;
        }

        private static GridCell LowestScore(IEnumerable<GridCell> open, IReadOnlyDictionary<GridCell, float> scores)
        {
            GridCell best = null;
            var bestScore = float.MaxValue;
            foreach (var cell in open)
            {
                var score = scores.ContainsKey(cell) ? scores[cell] : float.MaxValue;
                if (score < bestScore)
                {
                    best = cell;
                    bestScore = score;
                }
            }

            return best;
        }

        private static float GetScore(IReadOnlyDictionary<GridCell, float> scores, GridCell cell)
        {
            return scores.ContainsKey(cell) ? scores[cell] : float.MaxValue;
        }

        private static float MovementCost(GridCell cell)
        {
            return cell.Kind == GridCellKind.Path ? 0.92f : 1f;
        }

        private static float Heuristic(GridCell from, GridCell to)
        {
            return Mathf.Abs(from.Row - to.Row) + Mathf.Abs(from.Column - to.Column);
        }

        private static List<GridCell> Reconstruct(IReadOnlyDictionary<GridCell, GridCell> cameFrom, GridCell end)
        {
            var path = new List<GridCell> { end };
            var current = end;
            while (cameFrom.ContainsKey(current))
            {
                current = cameFrom[current];
                path.Add(current);
            }

            path.Reverse();
            return path;
        }
    }
}

