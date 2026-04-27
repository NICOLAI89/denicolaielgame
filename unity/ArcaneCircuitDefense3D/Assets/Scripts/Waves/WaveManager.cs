using System.Collections.Generic;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public enum WavePhase3D
    {
        Ready,
        InProgress,
        Cleared,
        Finished
    }

    public sealed class WaveManager : MonoBehaviour
    {
        private LevelDefinition level;
        private int currentWaveIndex;
        private List<EnemyTypeDefinition> spawnQueue = new List<EnemyTypeDefinition>();
        private int spawnedInWave;
        private float spawnTimer;

        public WavePhase3D Phase { get; private set; } = WavePhase3D.Ready;
        public int CurrentWaveNumber { get { return TotalWaves <= 0 ? 0 : Mathf.Clamp(currentWaveIndex + 1, 1, TotalWaves); } }
        public int TotalWaves { get { return level != null ? level.waves.Count : 0; } }
        public bool CanStartNextWave { get { return Phase == WavePhase3D.Ready || Phase == WavePhase3D.Cleared; } }

        public void Configure(LevelDefinition levelDefinition)
        {
            level = levelDefinition;
            currentWaveIndex = 0;
            spawnQueue.Clear();
            spawnedInWave = 0;
            spawnTimer = 0f;
            Phase = level != null && level.waves.Count > 0 ? WavePhase3D.Ready : WavePhase3D.Finished;
        }

        public bool StartNextWave()
        {
            if (!CanStartNextWave || level == null || currentWaveIndex < 0 || currentWaveIndex >= level.waves.Count)
            {
                return false;
            }

            spawnQueue = level.waves[currentWaveIndex].BuildSpawnQueue();
            spawnedInWave = 0;
            spawnTimer = 0f;
            Phase = WavePhase3D.InProgress;
            return true;
        }

        public List<EnemyTypeDefinition> Tick(float deltaSeconds)
        {
            var spawns = new List<EnemyTypeDefinition>();
            if (Phase != WavePhase3D.InProgress || level == null || currentWaveIndex >= level.waves.Count)
            {
                return spawns;
            }

            var wave = level.waves[currentWaveIndex];
            spawnTimer -= Mathf.Max(0f, deltaSeconds);
            while (spawnTimer <= 0f && spawnedInWave < spawnQueue.Count)
            {
                spawns.Add(spawnQueue[spawnedInWave]);
                spawnedInWave++;
                spawnTimer += Mathf.Max(0.05f, wave.spawnInterval);
            }

            return spawns;
        }

        public void NotifyAliveEnemies(int aliveEnemies)
        {
            if (Phase != WavePhase3D.InProgress || aliveEnemies > 0 || spawnedInWave < spawnQueue.Count)
            {
                return;
            }

            if (level == null || currentWaveIndex >= level.waves.Count - 1)
            {
                Phase = WavePhase3D.Finished;
            }
            else
            {
                currentWaveIndex++;
                spawnQueue.Clear();
                spawnedInWave = 0;
                spawnTimer = 0f;
                Phase = WavePhase3D.Cleared;
            }
        }
    }
}
