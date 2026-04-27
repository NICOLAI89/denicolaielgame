using UnityEngine;
using UnityEngine.UI;

namespace ArcaneCircuitDefense3D
{
    public sealed class UIHudController : MonoBehaviour
    {
        [SerializeField] private Text goldText;
        [SerializeField] private Text livesText;
        [SerializeField] private Text scoreText;
        [SerializeField] private Text waveText;
        [SerializeField] private Text messageText;
        [SerializeField] private Button startWaveButton;
        [SerializeField] private GameManager gameManager;

        private void Awake()
        {
            gameManager = gameManager != null ? gameManager : FindObjectOfType<GameManager>();
            if (startWaveButton != null)
            {
                startWaveButton.onClick.AddListener(OnStartWaveClicked);
            }
        }

        public void Refresh(GameManager manager, string message)
        {
            if (manager == null)
            {
                return;
            }

            SetText(goldText, "Gold: " + manager.Gold);
            SetText(livesText, "Lives: " + manager.Lives);
            SetText(scoreText, "Score: " + manager.Score);
            SetText(waveText, "Wave: " + manager.CurrentWave + "/" + manager.TotalWaves + " " + manager.WavePhase);
            if (!string.IsNullOrEmpty(message))
            {
                SetText(messageText, message);
            }

            if (startWaveButton != null)
            {
                startWaveButton.interactable = manager.CanStartWave;
            }
        }

        private void OnStartWaveClicked()
        {
            if (gameManager != null)
            {
                gameManager.StartNextWave();
            }
        }

        private static void SetText(Text target, string value)
        {
            if (target != null)
            {
                target.text = value;
            }
        }
    }
}

