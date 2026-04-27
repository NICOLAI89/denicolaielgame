using System.Collections.Generic;
using System.Linq;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public enum TargetingMode
    {
        First,
        Last,
        Strongest,
        Weakest,
        Closest
    }

    public sealed class Tower : MonoBehaviour
    {
        private TowerTypeDefinition definition;
        private GridCell cell;
        private int level = 1;
        private int totalInvested;
        private float cooldown;

        public TargetingMode targetingMode = TargetingMode.First;

        public GridCell Cell { get { return cell; } }
        public int Level { get { return level; } }
        public TowerTypeDefinition Definition { get { return definition; } }

        public void Initialize(TowerTypeDefinition towerType, GridCell occupiedCell)
        {
            definition = towerType;
            cell = occupiedCell;
            level = 1;
            totalInvested = towerType.baseCost;
            cooldown = 0f;
            transform.position = occupiedCell.WorldPosition + Vector3.up * 0.32f;
        }

        public void Tick(float deltaSeconds, IReadOnlyList<Enemy> enemies, Transform projectileRoot)
        {
            if (definition == null)
            {
                return;
            }

            cooldown = Mathf.Max(0f, cooldown - deltaSeconds);
            if (cooldown > 0f)
            {
                return;
            }

            var stats = definition.StatsForLevel(level);
            var target = SelectTarget(enemies, stats.range);
            if (target == null)
            {
                return;
            }

            FireAt(target, stats, projectileRoot);
            cooldown = stats.fireInterval;
        }

        public int UpgradeCost()
        {
            return definition != null ? definition.UpgradeCostForLevel(level) : 0;
        }

        public void Upgrade()
        {
            var cost = UpgradeCost();
            level++;
            totalInvested += cost;
        }

        public int SellRefund()
        {
            return Mathf.Max(1, Mathf.FloorToInt(totalInvested * 0.7f));
        }

        private Enemy SelectTarget(IReadOnlyList<Enemy> enemies, float range)
        {
            var inRange = enemies
                .Where(enemy => enemy != null && !enemy.IsDead)
                .Where(enemy => Vector3.Distance(transform.position, enemy.transform.position) <= range)
                .ToList();

            if (inRange.Count == 0)
            {
                return null;
            }

            switch (targetingMode)
            {
                case TargetingMode.Last:
                    return inRange.OrderBy(enemy => enemy.PathProgress).ThenBy(enemy => enemy.Health).First();
                case TargetingMode.Strongest:
                    return inRange.OrderByDescending(enemy => enemy.Health).ThenByDescending(enemy => enemy.PathProgress).First();
                case TargetingMode.Weakest:
                    return inRange.OrderBy(enemy => enemy.Health).ThenByDescending(enemy => enemy.PathProgress).First();
                case TargetingMode.Closest:
                    return inRange.OrderBy(enemy => Vector3.Distance(transform.position, enemy.transform.position)).First();
                default:
                    return inRange.OrderByDescending(enemy => enemy.PathProgress).ThenByDescending(enemy => enemy.Health).First();
            }
        }

        private void FireAt(Enemy target, TowerStats stats, Transform projectileRoot)
        {
            Projectile projectile;
            if (definition.projectilePrefab != null)
            {
                projectile = Instantiate(definition.projectilePrefab, projectileRoot);
            }
            else
            {
                var fallback = GameObject.CreatePrimitive(PrimitiveType.Sphere);
                fallback.transform.localScale = Vector3.one * 0.14f;
                projectile = fallback.AddComponent<Projectile>();
                if (projectileRoot != null)
                {
                    fallback.transform.SetParent(projectileRoot, false);
                }
            }

            projectile.transform.position = transform.position + Vector3.up * 0.45f;
            projectile.Initialize(target, stats.damage, stats.projectileSpeed, stats.slowMultiplier, stats.slowDuration);
        }
    }
}

