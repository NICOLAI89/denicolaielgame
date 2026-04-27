using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public sealed class BuildController : MonoBehaviour
    {
        [SerializeField] private GameManager gameManager;
        [SerializeField] private Camera worldCamera;
        [SerializeField] private TowerTypeDefinition selectedTowerType;
        [SerializeField] private LayerMask placementMask = ~0;

        private void Awake()
        {
            gameManager = gameManager != null ? gameManager : FindObjectOfType<GameManager>();
            worldCamera = worldCamera != null ? worldCamera : Camera.main;
        }

        private void Update()
        {
            if (!PointerPressedThisFrame())
            {
                return;
            }

            var screenPosition = PointerScreenPosition();
            var ray = worldCamera.ScreenPointToRay(screenPosition);
            RaycastHit hit;
            if (Physics.Raycast(ray, out hit, 200f, placementMask))
            {
                gameManager.TryBuildAtWorld(hit.point, selectedTowerType);
            }
        }

        public void SelectTowerType(TowerTypeDefinition towerType)
        {
            selectedTowerType = selectedTowerType == towerType ? null : towerType;
        }

        private static bool PointerPressedThisFrame()
        {
            return Input.GetMouseButtonDown(0) ||
                (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began);
        }

        private static Vector2 PointerScreenPosition()
        {
            return Input.touchCount > 0 ? Input.GetTouch(0).position : (Vector2)Input.mousePosition;
        }
    }
}

