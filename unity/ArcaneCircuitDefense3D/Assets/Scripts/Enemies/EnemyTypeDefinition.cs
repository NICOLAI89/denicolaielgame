using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    [CreateAssetMenu(menuName = "Arcane Circuit Defense/Enemy Type")]
    public sealed class EnemyTypeDefinition : ScriptableObject
    {
        public string displayName = "Normal";
        public float maxHealth = 40f;
        public float speed = 0.92f;
        public int reward = 11;
        public int scoreValue = 18;
        public float sizeScale = 1f;
        public bool isBoss;
        public float regenPerSecond;
        public float slowVulnerability = 1f;
        public float damageTakenMultiplier = 1f;
        public GameObject prefab;
    }
}

