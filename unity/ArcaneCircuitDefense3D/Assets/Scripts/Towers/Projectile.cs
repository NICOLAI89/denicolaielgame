using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public sealed class Projectile : MonoBehaviour
    {
        private Enemy target;
        private float damage;
        private float speed;
        private float slowMultiplier = 1f;
        private float slowDuration;

        public void Initialize(
            Enemy targetEnemy,
            float projectileDamage,
            float projectileSpeed,
            float appliedSlowMultiplier,
            float appliedSlowDuration)
        {
            target = targetEnemy;
            damage = projectileDamage;
            speed = projectileSpeed;
            slowMultiplier = appliedSlowMultiplier;
            slowDuration = appliedSlowDuration;
        }

        private void Update()
        {
            if (target == null || target.IsDead)
            {
                Destroy(gameObject);
                return;
            }

            var targetPosition = target.transform.position + Vector3.up * 0.25f;
            var toTarget = targetPosition - transform.position;
            var step = speed * Time.deltaTime;
            if (toTarget.magnitude <= step || toTarget.magnitude <= 0.08f)
            {
                target.ApplyDamage(damage, slowMultiplier, slowDuration);
                Destroy(gameObject);
                return;
            }

            transform.position += toTarget.normalized * step;
            transform.forward = toTarget.normalized;
        }
    }
}

