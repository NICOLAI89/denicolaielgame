using System;
using UnityEngine;

namespace ArcaneCircuitDefense3D
{
    public sealed class EconomyManager : MonoBehaviour
    {
        public event Action Changed;

        public int Gold { get; private set; }
        public int Lives { get; private set; }
        public int Score { get; private set; }

        public void Configure(int startingGold, int startingLives)
        {
            Gold = Mathf.Max(0, startingGold);
            Lives = Mathf.Max(1, startingLives);
            Score = 0;
            Changed?.Invoke();
        }

        public bool SpendGold(int amount)
        {
            if (amount < 0 || Gold < amount)
            {
                return false;
            }

            Gold -= amount;
            Changed?.Invoke();
            return true;
        }

        public void AddGold(int amount)
        {
            Gold += Mathf.Max(0, amount);
            Changed?.Invoke();
        }

        public void AddScore(int amount)
        {
            Score += Mathf.Max(0, amount);
            Changed?.Invoke();
        }

        public void LoseLife(int amount = 1)
        {
            Lives = Mathf.Max(0, Lives - Mathf.Max(1, amount));
            Changed?.Invoke();
        }
    }
}

