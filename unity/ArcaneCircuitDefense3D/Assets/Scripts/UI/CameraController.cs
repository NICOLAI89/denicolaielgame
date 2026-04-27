using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    [RequireComponent(typeof(Camera))]
    public sealed class CameraController : MonoBehaviour
    {
        [SerializeField] private Vector3 focusPoint = new Vector3(4f, 0f, -4f);
        [SerializeField] private float distance = 10f;
        [SerializeField] private float orthographicSize = 6.5f;
        [SerializeField] private float yaw = 45f;
        [SerializeField] private float pitch = 55f;

        private void Start()
        {
            ApplyView();
        }

        private void OnValidate()
        {
            if (Application.isPlaying)
            {
                ApplyView();
            }
        }

        public void ApplyView()
        {
            var cameraComponent = GetComponent<Camera>();
            cameraComponent.orthographic = true;
            cameraComponent.orthographicSize = orthographicSize;

            var rotation = Quaternion.Euler(pitch, yaw, 0f);
            transform.rotation = rotation;
            transform.position = focusPoint - rotation * Vector3.forward * distance;
        }
    }
}

