using System;
using System.Collections.Generic;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public sealed class Enemy : MonoBehaviour
    {
        private EnemyTypeDefinition definition;
        private List<GridCell> path = new List<GridCell>();
        private int pathIndex = 1;
        private Action<Enemy> reachedBase;
        private Action<Enemy> killed;
        private float slowTimeRemaining;
        private float slowMultiplier = 1f;

        public float Health { get; private set; }
        public float MaxHealth { get; private set; }
        public int Reward { get; private set; }
        public int ScoreValue { get; private set; }
        public bool IsDead { get; private set; }
        public int PathProgress { get { return pathIndex; } }
        public EnemyTypeDefinition Definition { get { return definition; } }

        public void Initialize(
            EnemyTypeDefinition enemyType,
            IList<GridCell> initialPath,
            Action<Enemy> onReachedBase,
            Action<Enemy> onKilled)
        {
            definition = enemyType;
            path = new List<GridCell>(initialPath);
            pathIndex = path.Count > 1 ? 1 : 0;
            reachedBase = onReachedBase;
            killed = onKilled;
            MaxHealth = enemyType.maxHealth;
            Health = MaxHealth;
            Reward = enemyType.reward;
            ScoreValue = enemyType.scoreValue;
            slowMultiplier = 1f;
            slowTimeRemaining = 0f;
            IsDead = false;

            transform.localScale = Vector3.one * Mathf.Max(0.25f, enemyType.sizeScale);
            if (path.Count > 0)
            {
                transform.position = path[0].WorldPosition + Vector3.up * 0.35f;
            }
        }

        public void Tick(float deltaSeconds)
        {
            if (IsDead || definition == null)
            {
                return;
            }

            if (definition.regenPerSecond > 0f)
            {
                Health = Mathf.Min(MaxHealth, Health + definition.regenPerSecond * deltaSeconds);
            }

            slowTimeRemaining = Mathf.Max(0f, slowTimeRemaining - deltaSeconds);
            var speed = definition.speed * (slowTimeRemaining > 0f ? slowMultiplier : 1f);
            var remaining = speed * deltaSeconds;

            while (remaining > 0f && pathIndex < path.Count)
            {
                var target = path[pathIndex].WorldPosition + Vector3.up * 0.35f;
                var toTarget = target - transform.position;
                var distance = toTarget.magnitude;
                if (distance < 0.01f)
                {
                    if (path[pathIndex].Kind == GridCellKind.Base)
                    {
                        ReachBase();
                        return;
                    }

                    pathIndex++;
                    continue;
                }

                var step = Mathf.Min(distance, remaining);
                transform.position += toTarget.normalized * step;
                remaining -= step;
            }
        }

        public void ApplyDamage(float damage, float appliedSlowMultiplier, float slowDuration)
        {
            if (IsDead)
            {
                return;
            }

            Health -= Mathf.Max(0f, damage) * definition.damageTakenMultiplier;
            if (slowDuration > 0f && appliedSlowMultiplier < 1f)
            {
                var resistedSlow = 1f - ((1f - appliedSlowMultiplier) * definition.slowVulnerability);
                slowMultiplier = Mathf.Min(slowMultiplier, resistedSlow);
                slowTimeRemaining = Mathf.Max(slowTimeRemaining, slowDuration * definition.slowVulnerability);
            }

            if (Health <= 0f)
            {
                IsDead = true;
                killed?.Invoke(this);
            }
        }

        private void ReachBase()
        {
            IsDead = true;
            reachedBase?.Invoke(this);
        }
    }
}

