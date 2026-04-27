using System.Collections.Generic;
using System.Linq;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public enum GameRunState3D
    {
        Running,
        Paused,
        Victory,
        GameOver
    }

    public sealed class GameManager : MonoBehaviour
    {
        [SerializeField] private LevelDefinition levelDefinition;
        [SerializeField] private EnemyTypeDefinition normalEnemyDefinition;
        [SerializeField] private TowerTypeDefinition basicTowerDefinition;
        [SerializeField] private GridManager gridManager;
        [SerializeField] private PathfindingService pathfindingService;
        [SerializeField] private WaveManager waveManager;
        [SerializeField] private EconomyManager economyManager;
        [SerializeField] private UIHudController hudController;
        [SerializeField] private Transform enemyRoot;
        [SerializeField] private Transform towerRoot;
        [SerializeField] private Transform projectileRoot;

        private readonly List<Enemy> enemies = new List<Enemy>();
        private readonly List<Tower> towers = new List<Tower>();

        public GameRunState3D RunState { get; private set; } = GameRunState3D.Running;
        public int Gold { get { return economyManager != null ? economyManager.Gold : 0; } }
        public int Lives { get { return economyManager != null ? economyManager.Lives : 0; } }
        public int Score { get { return economyManager != null ? economyManager.Score : 0; } }
        public int CurrentWave { get { return waveManager != null ? waveManager.CurrentWaveNumber : 0; } }
        public int TotalWaves { get { return waveManager != null ? waveManager.TotalWaves : 0; } }
        public WavePhase3D WavePhase { get { return waveManager != null ? waveManager.Phase : WavePhase3D.Ready; } }
        public bool CanStartWave { get { return RunState == GameRunState3D.Running && waveManager != null && waveManager.CanStartNextWave; } }

        private void Awake()
        {
            gridManager = gridManager != null ? gridManager : FindObjectOfType<GridManager>();
            pathfindingService = pathfindingService != null ? pathfindingService : FindObjectOfType<PathfindingService>();
            waveManager = waveManager != null ? waveManager : FindObjectOfType<WaveManager>();
            economyManager = economyManager != null ? economyManager : FindObjectOfType<EconomyManager>();
            hudController = hudController != null ? hudController : FindObjectOfType<UIHudController>();

            if (normalEnemyDefinition == null)
            {
                normalEnemyDefinition = ScriptableObject.CreateInstance<EnemyTypeDefinition>();
            }

            if (basicTowerDefinition == null)
            {
                basicTowerDefinition = ScriptableObject.CreateInstance<TowerTypeDefinition>();
            }

            if (levelDefinition == null)
            {
                levelDefinition = LevelDefinition.CreateRuntimeVerticalSlice(normalEnemyDefinition);
            }
        }

        private void Start()
        {
            gridManager.Build(levelDefinition);
            waveManager.Configure(levelDefinition);
            economyManager.Configure(levelDefinition.startingGold, levelDefinition.startingLives);
            RefreshHud("Build towers, then start the wave.");
        }

        private void Update()
        {
            if (RunState != GameRunState3D.Running)
            {
                return;
            }

            var delta = Mathf.Min(Time.deltaTime, 0.05f);
            foreach (var enemyType in waveManager.Tick(delta))
            {
                SpawnEnemy(enemyType);
            }

            foreach (var enemy in enemies.ToArray())
            {
                if (enemy != null)
                {
                    enemy.Tick(delta);
                }
            }

            enemies.RemoveAll(enemy => enemy == null || enemy.IsDead);

            foreach (var tower in towers)
            {
                tower.Tick(delta, enemies, projectileRoot);
            }

            waveManager.NotifyAliveEnemies(enemies.Count);
            if (economyManager.Lives <= 0)
            {
                RunState = GameRunState3D.GameOver;
                RefreshHud("Game over.");
            }
            else if (waveManager.Phase == WavePhase3D.Finished && enemies.Count == 0)
            {
                RunState = GameRunState3D.Victory;
                economyManager.AddScore(325);
                RefreshHud("Victory.");
            }
            else
            {
                RefreshHud(null);
            }
        }

        public void StartNextWave()
        {
            if (!CanStartWave)
            {
                return;
            }

            if (waveManager.StartNextWave())
            {
                RefreshHud("Wave started.");
            }
        }

        public bool TryBuildAtWorld(Vector3 worldPosition, TowerTypeDefinition selectedTowerType = null)
        {
            GridCell cell;
            if (!gridManager.TryGetCellFromWorld(worldPosition, out cell))
            {
                RefreshHud("No buildable tile selected.");
                return false;
            }

            return TryBuildTower(cell, selectedTowerType != null ? selectedTowerType : basicTowerDefinition);
        }

        public bool TryBuildTower(GridCell cell, TowerTypeDefinition towerType)
        {
            if (RunState != GameRunState3D.Running || towerType == null)
            {
                return false;
            }

            if (!gridManager.CanBuildAt(cell))
            {
                RefreshHud("Cannot build there.");
                return false;
            }

            if (!PathExistsWithExtraBlock(cell))
            {
                RefreshHud("That would block the path.");
                return false;
            }

            if (!economyManager.SpendGold(towerType.baseCost))
            {
                RefreshHud("Not enough gold.");
                return false;
            }

            var tower = InstantiateTower(towerType, cell);
            gridManager.SetTower(cell, tower);
            towers.Add(tower);
            RefreshHud("Tower built.");
            return true;
        }

        private void SpawnEnemy(EnemyTypeDefinition enemyType)
        {
            var path = pathfindingService.FindPath(gridManager, gridManager.SpawnCell, gridManager.BaseCell, CurrentBlockedCells(null));
            if (path == null || path.Count == 0)
            {
                return;
            }

            GameObject enemyObject;
            if (enemyType.prefab != null)
            {
                enemyObject = Instantiate(enemyType.prefab, enemyRoot);
            }
            else
            {
                enemyObject = GameObject.CreatePrimitive(PrimitiveType.Capsule);
                if (enemyRoot != null)
                {
                    enemyObject.transform.SetParent(enemyRoot, false);
                }
            }

            var enemy = enemyObject.GetComponent<Enemy>();
            if (enemy == null)
            {
                enemy = enemyObject.AddComponent<Enemy>();
            }

            enemy.Initialize(enemyType, path, OnEnemyReachedBase, OnEnemyKilled);
            enemies.Add(enemy);
        }

        private Tower InstantiateTower(TowerTypeDefinition towerType, GridCell cell)
        {
            GameObject towerObject;
            if (towerType.towerPrefab != null)
            {
                towerObject = Instantiate(towerType.towerPrefab, towerRoot);
            }
            else
            {
                towerObject = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
                towerObject.transform.localScale = new Vector3(0.5f, 0.65f, 0.5f);
                if (towerRoot != null)
                {
                    towerObject.transform.SetParent(towerRoot, false);
                }
            }

            var tower = towerObject.GetComponent<Tower>();
            if (tower == null)
            {
                tower = towerObject.AddComponent<Tower>();
            }

            tower.Initialize(towerType, cell);
            return tower;
        }

        private bool PathExistsWithExtraBlock(GridCell extraBlockedCell)
        {
            return pathfindingService.FindPath(
                gridManager,
                gridManager.SpawnCell,
                gridManager.BaseCell,
                CurrentBlockedCells(extraBlockedCell)) != null;
        }

        private HashSet<GridCell> CurrentBlockedCells(GridCell extraBlockedCell)
        {
            var blocked = new HashSet<GridCell>(towers.Select(tower => tower.Cell));
            if (extraBlockedCell != null)
            {
                blocked.Add(extraBlockedCell);
            }

            return blocked;
        }

        private void OnEnemyReachedBase(Enemy enemy)
        {
            economyManager.LoseLife();
            Destroy(enemy.gameObject);
        }

        private void OnEnemyKilled(Enemy enemy)
        {
            economyManager.AddGold(enemy.Reward);
            economyManager.AddScore(enemy.ScoreValue);
            Destroy(enemy.gameObject);
        }

        private void RefreshHud(string message)
        {
            if (hudController != null)
            {
                hudController.Refresh(this, message);
            }
        }
    }
}

