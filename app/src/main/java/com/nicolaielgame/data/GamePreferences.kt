package com.nicolaielgame.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.gameDataStore by preferencesDataStore(name = "game_progress")

data class ProgressSnapshot(
    val legacyBestScore: Int,
    val highestUnlockedLevel: Int,
    val bestScoresByLevel: Map<Int, Int>,
) {
    fun bestScoreForLevel(levelId: Int): Int {
        return bestScoresByLevel[levelId] ?: 0
    }
}

class GamePreferences(context: Context) {
    private val dataStore = context.applicationContext.gameDataStore

    val progress: Flow<ProgressSnapshot> = dataStore.data
        .safePreferences()
        .map { preferences ->
            val legacyBest = preferences[Keys.BestScore] ?: 0
            val levelScores = (1..3).associateWith { levelId ->
                val perLevel = preferences[Keys.bestScoreForLevel(levelId)] ?: 0
                if (levelId == 1) maxOf(perLevel, legacyBest) else perLevel
            }
            ProgressSnapshot(
                legacyBestScore = legacyBest,
                highestUnlockedLevel = preferences[Keys.HighestUnlockedLevel] ?: 1,
                bestScoresByLevel = levelScores,
            )
        }

    val bestScore: Flow<Int> = progress.map { snapshot ->
        maxOf(snapshot.legacyBestScore, snapshot.bestScoresByLevel.values.maxOrNull() ?: 0)
    }

    val lastUnlockedLevel: Flow<Int> = progress.map { it.highestUnlockedLevel }

    suspend fun saveBestScore(score: Int) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.BestScore] ?: 0
            if (score > current) {
                preferences[Keys.BestScore] = score
            }
        }
    }

    suspend fun saveBestScoreForLevel(levelId: Int, score: Int) {
        dataStore.edit { preferences ->
            val key = Keys.bestScoreForLevel(levelId)
            val current = preferences[key] ?: 0
            if (score > current) {
                preferences[key] = score
            }
            val legacyBest = preferences[Keys.BestScore] ?: 0
            if (score > legacyBest) {
                preferences[Keys.BestScore] = score
            }
        }
    }

    suspend fun saveLastUnlockedLevel(level: Int) {
        unlockLevel(level)
    }

    suspend fun unlockLevel(level: Int) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.HighestUnlockedLevel] ?: 1
            if (level > current) {
                preferences[Keys.HighestUnlockedLevel] = level
            }
        }
    }

    private fun Flow<Preferences>.safePreferences(): Flow<Preferences> {
        return catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
    }

    private object Keys {
        val BestScore = intPreferencesKey("best_score")
        val HighestUnlockedLevel = intPreferencesKey("last_unlocked_level")

        fun bestScoreForLevel(levelId: Int) = intPreferencesKey("best_score_level_$levelId")
    }
}
