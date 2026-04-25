package com.nicolaielgame.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nicolaielgame.game.model.DifficultyMode
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

    val settings: Flow<GameSettings> = dataStore.data
        .safePreferences()
        .map { preferences ->
            GameSettings(
                soundEnabled = preferences[Keys.SoundEnabled] ?: true,
                musicEnabled = preferences[Keys.MusicEnabled] ?: false,
                showGrid = preferences[Keys.ShowGrid] ?: true,
                lastDifficulty = preferences[Keys.LastDifficulty]
                    ?.let { runCatching { DifficultyMode.valueOf(it) }.getOrNull() }
                    ?: DifficultyMode.Normal,
            )
        }

    val achievements: Flow<Set<Achievement>> = dataStore.data
        .safePreferences()
        .map { preferences ->
            preferences[Keys.UnlockedAchievements].orEmpty().mapNotNull { name ->
                runCatching { Achievement.valueOf(name) }.getOrNull()
            }.toSet()
        }

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

    suspend fun saveSettings(settings: GameSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SoundEnabled] = settings.soundEnabled
            preferences[Keys.MusicEnabled] = settings.musicEnabled
            preferences[Keys.ShowGrid] = settings.showGrid
            preferences[Keys.LastDifficulty] = settings.lastDifficulty.name
        }
    }

    suspend fun saveLastDifficulty(difficulty: DifficultyMode) {
        dataStore.edit { preferences ->
            preferences[Keys.LastDifficulty] = difficulty.name
        }
    }

    suspend fun unlockAchievements(newAchievements: Set<Achievement>) {
        if (newAchievements.isEmpty()) return
        dataStore.edit { preferences ->
            val current = preferences[Keys.UnlockedAchievements].orEmpty()
            preferences[Keys.UnlockedAchievements] = current + newAchievements.map { it.name }
        }
    }

    suspend fun resetProgress() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.BestScore)
            preferences.remove(Keys.HighestUnlockedLevel)
            preferences.remove(Keys.UnlockedAchievements)
            (1..3).forEach { levelId ->
                preferences.remove(Keys.bestScoreForLevel(levelId))
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
        val SoundEnabled = booleanPreferencesKey("sound_enabled")
        val MusicEnabled = booleanPreferencesKey("music_enabled")
        val ShowGrid = booleanPreferencesKey("show_grid")
        val LastDifficulty = stringPreferencesKey("last_difficulty")
        val UnlockedAchievements = stringSetPreferencesKey("unlocked_achievements")

        fun bestScoreForLevel(levelId: Int) = intPreferencesKey("best_score_level_$levelId")
    }
}
