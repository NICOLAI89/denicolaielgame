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
private const val MaxPersistedLevels = 7

data class ProgressSnapshot(
    val legacyBestScore: Int,
    val highestUnlockedLevel: Int,
    val bestScoresByLevel: Map<Int, Int>,
    val activeProfile: Int = 1,
    val tutorialCompleted: Boolean = false,
) {
    fun bestScoreForLevel(levelId: Int): Int {
        return bestScoresByLevel[levelId] ?: 0
    }
}

data class ProfileSummary(
    val slot: Int,
    val highestUnlockedLevel: Int,
    val bestScore: Int,
)

object ProfileRules {
    const val MaxProfiles = 3

    fun sanitizedSlot(slot: Int): Int {
        return slot.coerceIn(1, MaxProfiles)
    }
}

class GamePreferences(context: Context) {
    private val dataStore = context.applicationContext.gameDataStore

    val progress: Flow<ProgressSnapshot> = dataStore.data
        .safePreferences()
        .map { preferences ->
            val activeProfile = activeProfile(preferences)
            val legacyBest = if (activeProfile == 1) preferences[Keys.BestScore] ?: 0 else 0
            val profileBest = preferences[Keys.profileBestScore(activeProfile)] ?: 0
            val bestScore = maxOf(legacyBest, profileBest)
            val levelScores = (1..MaxPersistedLevels).associateWith { levelId ->
                val profileLevel = preferences[Keys.profileBestScoreForLevel(activeProfile, levelId)] ?: 0
                val legacyLevel = if (activeProfile == 1) preferences[Keys.bestScoreForLevel(levelId)] ?: 0 else 0
                if (levelId == 1) maxOf(profileLevel, legacyLevel, legacyBest) else maxOf(profileLevel, legacyLevel)
            }
            val unlocked = preferences[Keys.profileHighestUnlockedLevel(activeProfile)]
                ?: if (activeProfile == 1) preferences[Keys.HighestUnlockedLevel] ?: 1 else 1
            val tutorialCompleted = preferences[Keys.profileTutorialCompleted(activeProfile)]
                ?: if (activeProfile == 1) preferences[Keys.TutorialCompleted] ?: false else false
            ProgressSnapshot(
                legacyBestScore = bestScore,
                highestUnlockedLevel = unlocked,
                bestScoresByLevel = levelScores,
                activeProfile = activeProfile,
                tutorialCompleted = tutorialCompleted,
            )
        }

    val bestScore: Flow<Int> = progress.map { snapshot ->
        maxOf(snapshot.legacyBestScore, snapshot.bestScoresByLevel.values.maxOrNull() ?: 0)
    }

    val lastUnlockedLevel: Flow<Int> = progress.map { it.highestUnlockedLevel }

    val profileSummaries: Flow<List<ProfileSummary>> = dataStore.data
        .safePreferences()
        .map { preferences ->
            (1..ProfileRules.MaxProfiles).map { slot ->
                val legacyBest = if (slot == 1) preferences[Keys.BestScore] ?: 0 else 0
                val profileBest = preferences[Keys.profileBestScore(slot)] ?: 0
                val bestLevelScore = (1..MaxPersistedLevels).maxOf { levelId ->
                    val profileLevel = preferences[Keys.profileBestScoreForLevel(slot, levelId)] ?: 0
                    val legacyLevel = if (slot == 1) preferences[Keys.bestScoreForLevel(levelId)] ?: 0 else 0
                    maxOf(profileLevel, legacyLevel)
                }
                val unlocked = preferences[Keys.profileHighestUnlockedLevel(slot)]
                    ?: (if (slot == 1) preferences[Keys.HighestUnlockedLevel] ?: 1 else 1)
                ProfileSummary(
                    slot = slot,
                    highestUnlockedLevel = unlocked,
                    bestScore = maxOf(legacyBest, profileBest, bestLevelScore),
                )
            }
        }

    val settings: Flow<GameSettings> = dataStore.data
        .safePreferences()
        .map { preferences ->
            GameSettings(
                soundEnabled = preferences[Keys.SoundEnabled] ?: true,
                musicEnabled = preferences[Keys.MusicEnabled] ?: false,
                showGrid = preferences[Keys.ShowGrid] ?: true,
                screenShakeEnabled = preferences[Keys.ScreenShakeEnabled] ?: true,
                damageNumbersEnabled = preferences[Keys.DamageNumbersEnabled] ?: true,
                highContrastMode = preferences[Keys.HighContrastMode] ?: false,
                fpsCounterEnabled = preferences[Keys.FpsCounterEnabled] ?: false,
                lastDifficulty = preferences[Keys.LastDifficulty]
                    ?.let { runCatching { DifficultyMode.valueOf(it) }.getOrNull() }
                    ?: DifficultyMode.Normal,
            )
        }

    val leaderboard: Flow<List<LeaderboardEntry>> = dataStore.data
        .safePreferences()
        .map { preferences ->
            LocalLeaderboard.sorted(
                preferences[Keys.LocalLeaderboardEntries]
                    .orEmpty()
                    .mapNotNull(LeaderboardEntry::decode),
            )
        }

    val dailyBests: Flow<List<DailyChallengeBest>> = dataStore.data
        .safePreferences()
        .map { preferences ->
            preferences[Keys.DailyChallengeBests]
                .orEmpty()
                .mapNotNull(DailyChallengeBest::decode)
        }

    val achievements: Flow<Set<Achievement>> = dataStore.data
        .safePreferences()
        .map { preferences ->
            val activeProfile = activeProfile(preferences)
            val names = preferences[Keys.profileUnlockedAchievements(activeProfile)]
                ?: (if (activeProfile == 1) preferences[Keys.UnlockedAchievements].orEmpty() else emptySet())
            names.mapNotNull { name ->
                runCatching { Achievement.valueOf(name) }.getOrNull()
            }.toSet()
        }

    suspend fun saveBestScore(score: Int) {
        dataStore.edit { preferences ->
            val activeProfile = activeProfile(preferences)
            val key = Keys.profileBestScore(activeProfile)
            val current = preferences[key] ?: 0
            if (score > current) {
                preferences[key] = score
            }
            if (activeProfile == 1) {
                val legacyCurrent = preferences[Keys.BestScore] ?: 0
                if (score > legacyCurrent) preferences[Keys.BestScore] = score
            }
        }
    }

    suspend fun saveBestScoreForLevel(levelId: Int, score: Int) {
        dataStore.edit { preferences ->
            val activeProfile = activeProfile(preferences)
            val key = Keys.profileBestScoreForLevel(activeProfile, levelId)
            val current = preferences[key] ?: 0
            if (score > current) {
                preferences[key] = score
            }
            val profileBestKey = Keys.profileBestScore(activeProfile)
            val profileBest = preferences[profileBestKey] ?: 0
            if (score > profileBest) {
                preferences[profileBestKey] = score
            }
            if (activeProfile == 1) {
                val legacyLevelKey = Keys.bestScoreForLevel(levelId)
                val legacyLevel = preferences[legacyLevelKey] ?: 0
                if (score > legacyLevel) preferences[legacyLevelKey] = score
                val legacyBest = preferences[Keys.BestScore] ?: 0
                if (score > legacyBest) preferences[Keys.BestScore] = score
            }
        }
    }

    suspend fun saveLastUnlockedLevel(level: Int) {
        unlockLevel(level)
    }

    suspend fun unlockLevel(level: Int) {
        dataStore.edit { preferences ->
            val activeProfile = activeProfile(preferences)
            val key = Keys.profileHighestUnlockedLevel(activeProfile)
            val current = preferences[key] ?: (if (activeProfile == 1) preferences[Keys.HighestUnlockedLevel] ?: 1 else 1)
            if (level > current) {
                preferences[key] = level
                if (activeProfile == 1) preferences[Keys.HighestUnlockedLevel] = level
            }
        }
    }

    suspend fun saveSettings(settings: GameSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SoundEnabled] = settings.soundEnabled
            preferences[Keys.MusicEnabled] = settings.musicEnabled
            preferences[Keys.ShowGrid] = settings.showGrid
            preferences[Keys.ScreenShakeEnabled] = settings.screenShakeEnabled
            preferences[Keys.DamageNumbersEnabled] = settings.damageNumbersEnabled
            preferences[Keys.HighContrastMode] = settings.highContrastMode
            preferences[Keys.FpsCounterEnabled] = settings.fpsCounterEnabled
            preferences[Keys.LastDifficulty] = settings.lastDifficulty.name
        }
    }

    suspend fun saveLeaderboardEntry(entry: LeaderboardEntry) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.LocalLeaderboardEntries]
                .orEmpty()
                .mapNotNull(LeaderboardEntry::decode)
            preferences[Keys.LocalLeaderboardEntries] = LocalLeaderboard
                .recordCompletedRun(current, entry)
                .map { it.encode() }
                .toSet()
        }
    }

    suspend fun saveDailyBest(entry: DailyChallengeBest) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.DailyChallengeBests]
                .orEmpty()
                .mapNotNull(DailyChallengeBest::decode)
            preferences[Keys.DailyChallengeBests] = DailyChallengeProgress
                .saveBest(current, entry)
                .map { it.encode() }
                .toSet()
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
            val activeProfile = activeProfile(preferences)
            val key = Keys.profileUnlockedAchievements(activeProfile)
            val current = preferences[key]
                ?: (if (activeProfile == 1) preferences[Keys.UnlockedAchievements].orEmpty() else emptySet())
            val next = current + newAchievements.map { it.name }
            preferences[key] = next
            if (activeProfile == 1) preferences[Keys.UnlockedAchievements] = next
        }
    }

    suspend fun resetProgress() {
        dataStore.edit { preferences ->
            val activeProfile = activeProfile(preferences)
            resetProfileKeys(preferences, activeProfile)
        }
    }

    suspend fun selectProfile(slot: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.ActiveProfile] = ProfileRules.sanitizedSlot(slot)
        }
    }

    suspend fun resetProfile(slot: Int) {
        dataStore.edit { preferences ->
            resetProfileKeys(preferences, ProfileRules.sanitizedSlot(slot))
        }
    }

    suspend fun saveTutorialCompleted(completed: Boolean = true) {
        dataStore.edit { preferences ->
            val activeProfile = activeProfile(preferences)
            preferences[Keys.profileTutorialCompleted(activeProfile)] = completed
            if (activeProfile == 1) preferences[Keys.TutorialCompleted] = completed
        }
    }

    private fun resetProfileKeys(preferences: androidx.datastore.preferences.core.MutablePreferences, profile: Int) {
        preferences.remove(Keys.profileBestScore(profile))
        preferences.remove(Keys.profileHighestUnlockedLevel(profile))
        preferences.remove(Keys.profileUnlockedAchievements(profile))
        preferences.remove(Keys.profileTutorialCompleted(profile))
        preferences[Keys.LocalLeaderboardEntries] = preferences[Keys.LocalLeaderboardEntries]
            .orEmpty()
            .mapNotNull(LeaderboardEntry::decode)
            .filterNot { it.profileSlot == profile }
            .map { it.encode() }
            .toSet()
        preferences[Keys.DailyChallengeBests] = preferences[Keys.DailyChallengeBests]
            .orEmpty()
            .mapNotNull(DailyChallengeBest::decode)
            .filterNot { it.profileSlot == profile }
            .map { it.encode() }
            .toSet()
        (1..MaxPersistedLevels).forEach { levelId ->
            preferences.remove(Keys.profileBestScoreForLevel(profile, levelId))
        }
        if (profile == 1) {
            preferences.remove(Keys.BestScore)
            preferences.remove(Keys.HighestUnlockedLevel)
            preferences.remove(Keys.UnlockedAchievements)
            preferences.remove(Keys.TutorialCompleted)
            (1..MaxPersistedLevels).forEach { levelId ->
                preferences.remove(Keys.bestScoreForLevel(levelId))
            }
        }
    }

    private fun activeProfile(preferences: Preferences): Int {
        return ProfileRules.sanitizedSlot(preferences[Keys.ActiveProfile] ?: 1)
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
        val ActiveProfile = intPreferencesKey("active_profile")
        val SoundEnabled = booleanPreferencesKey("sound_enabled")
        val MusicEnabled = booleanPreferencesKey("music_enabled")
        val ShowGrid = booleanPreferencesKey("show_grid")
        val ScreenShakeEnabled = booleanPreferencesKey("screen_shake_enabled")
        val DamageNumbersEnabled = booleanPreferencesKey("damage_numbers_enabled")
        val HighContrastMode = booleanPreferencesKey("high_contrast_mode")
        val FpsCounterEnabled = booleanPreferencesKey("fps_counter_enabled")
        val LastDifficulty = stringPreferencesKey("last_difficulty")
        val UnlockedAchievements = stringSetPreferencesKey("unlocked_achievements")
        val TutorialCompleted = booleanPreferencesKey("tutorial_completed")
        val LocalLeaderboardEntries = stringSetPreferencesKey("local_leaderboard_entries")
        val DailyChallengeBests = stringSetPreferencesKey("daily_challenge_bests")

        fun bestScoreForLevel(levelId: Int) = intPreferencesKey("best_score_level_$levelId")
        fun profileBestScore(profile: Int) = intPreferencesKey("profile_${profile}_best_score")
        fun profileHighestUnlockedLevel(profile: Int) = intPreferencesKey("profile_${profile}_highest_unlocked_level")
        fun profileUnlockedAchievements(profile: Int) = stringSetPreferencesKey("profile_${profile}_unlocked_achievements")
        fun profileTutorialCompleted(profile: Int) = booleanPreferencesKey("profile_${profile}_tutorial_completed")
        fun profileBestScoreForLevel(profile: Int, levelId: Int) = intPreferencesKey("profile_${profile}_best_score_level_$levelId")
    }
}
