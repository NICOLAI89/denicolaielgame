using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public struct TowerStats
    {
        public float damage;
        public float range;
        public float fireInterval;
        public float projectileSpeed;
        public float slowMultiplier;
        public float slowDuration;
    }

    [CreateAssetMenu(menuName = "Arcane Circuit Defense/Tower Type")]
    public sealed class TowerTypeDefinition : ScriptableObject
    {
        public string displayName = "Basic Tower";
        public int baseCost = 30;
        public float baseDamage = 32f;
        public float baseRange = 2.75f;
        public float baseFireInterval = 0.62f;
        public float projectileSpeed = 7.1f;
        public float slowMultiplier = 1f;
        public float slowDuration;
        public GameObject towerPrefab;
        public Projectile projectilePrefab;

        public TowerStats StatsForLevel(int level)
        {
            var levelIndex = Mathf.Max(0, level - 1);
            return new TowerStats
            {
                damage = baseDamage * (1f + levelIndex * 0.34f),
                range = baseRange + levelIndex * 0.24f,
                fireInterval = Mathf.Max(0.3f, baseFireInterval * (1f - levelIndex * 0.085f)),
                projectileSpeed = projectileSpeed + levelIndex * 0.45f,
                slowMultiplier = slowMultiplier,
                slowDuration = slowDuration > 0f ? slowDuration + levelIndex * 0.22f : 0f
            };
        }

        public int UpgradeCostForLevel(int level)
        {
            return Mathf.Max(1, Mathf.RoundToInt(baseCost * (0.62f + level * 0.5f)));
        }
    }
}

