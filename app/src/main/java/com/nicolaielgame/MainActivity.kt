package com.nicolaielgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nicolaielgame.data.AchievementRules
import com.nicolaielgame.data.DailyChallengeBest
import com.nicolaielgame.data.DailyChallengeProgress
import com.nicolaielgame.data.GameSettings
import com.nicolaielgame.data.GamePreferences
import com.nicolaielgame.data.LeaderboardEntry
import com.nicolaielgame.data.ProfileSummary
import com.nicolaielgame.data.ProgressSnapshot
import com.nicolaielgame.data.ProgressionRules
import com.nicolaielgame.game.model.DailyChallengeRules
import com.nicolaielgame.game.model.DifficultyMode
import com.nicolaielgame.game.model.GameRunResult
import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.ui.menu.DailyChallengeScreen
import com.nicolaielgame.ui.game.GameScreen
import com.nicolaielgame.ui.menu.AchievementsScreen
import com.nicolaielgame.ui.menu.LeaderboardScreen
import com.nicolaielgame.ui.menu.LevelSelectScreen
import com.nicolaielgame.ui.menu.MainMenuScreen
import com.nicolaielgame.ui.menu.ProfileSelectScreen
import com.nicolaielgame.ui.menu.SettingsScreen
import com.nicolaielgame.ui.menu.TutorialScreen
import com.nicolaielgame.ui.theme.DenicolaielTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = GamePreferences(applicationContext)

        setContent {
            DenicolaielTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DenicolaielApp(preferences = preferences)
                }
            }
        }
    }
}

private enum class RootScreen {
    ProfileSelect,
    Tutorial,
    Menu,
    LevelSelect,
    Game,
    DailyChallenge,
    Leaderboard,
    Settings,
    Achievements,
}

@Composable
private fun DenicolaielApp(preferences: GamePreferences) {
    val progress by preferences.progress.collectAsState(
        initial = ProgressSnapshot(
            legacyBestScore = 0,
            highestUnlockedLevel = 1,
            bestScoresByLevel = emptyMap(),
        ),
    )
    val settings by preferences.settings.collectAsState(initial = GameSettings())
    val unlockedAchievements by preferences.achievements.collectAsState(initial = emptySet())
    val leaderboardEntries by preferences.leaderboard.collectAsState(initial = emptyList())
    val dailyBests by preferences.dailyBests.collectAsState(initial = emptyList())
    val profileSummaries by preferences.profileSummaries.collectAsState(
        initial = (1..3).map { slot -> ProfileSummary(slot, highestUnlockedLevel = 1, bestScore = 0) },
    )
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(RootScreen.ProfileSelect) }
    var selectedLevelId by rememberSaveable { mutableStateOf(LevelCatalog.firstLevel.id) }
    var selectedDifficulty by rememberSaveable { mutableStateOf(settings.lastDifficulty) }
    var activeDailyDateKey by rememberSaveable { mutableStateOf<String?>(null) }
    var tutorialAcknowledgedProfile by rememberSaveable { mutableStateOf<Int?>(null) }
    val todayDateKey = remember { DailyChallengeRules.todayDateKey() }
    val todayChallenge = remember(todayDateKey) { DailyChallengeRules.generate(todayDateKey) }
    val activeDailyChallenge = activeDailyDateKey?.let { DailyChallengeRules.generate(it) }
    val selectedLevel = activeDailyChallenge?.level ?: LevelCatalog.find(selectedLevelId)
    val activeDifficulty = activeDailyChallenge?.difficulty ?: selectedDifficulty

    LaunchedEffect(settings.lastDifficulty) {
        selectedDifficulty = settings.lastDifficulty
    }

    LaunchedEffect(screen, progress.activeProfile, progress.tutorialCompleted) {
        if (
            screen == RootScreen.Menu &&
            !progress.tutorialCompleted &&
            tutorialAcknowledgedProfile != progress.activeProfile
        ) {
            screen = RootScreen.Tutorial
        }
    }

    when (screen) {
        RootScreen.ProfileSelect -> ProfileSelectScreen(
            activeProfile = progress.activeProfile,
            profileSummaries = profileSummaries,
            onProfileSelected = { slot ->
                scope.launch {
                    preferences.selectProfile(slot)
                    screen = RootScreen.Menu
                }
            },
            onResetProfile = { slot ->
                if (slot == progress.activeProfile) {
                    tutorialAcknowledgedProfile = null
                }
                scope.launch { preferences.resetProfile(slot) }
            },
            onContinue = { screen = RootScreen.Menu },
        )

        RootScreen.Tutorial -> TutorialScreen(
            onComplete = {
                tutorialAcknowledgedProfile = progress.activeProfile
                scope.launch {
                    preferences.saveTutorialCompleted(true)
                    screen = RootScreen.Menu
                }
            },
            onSkip = {
                tutorialAcknowledgedProfile = progress.activeProfile
                scope.launch {
                    preferences.saveTutorialCompleted(true)
                    screen = RootScreen.Menu
                }
            },
        )

        RootScreen.Menu -> MainMenuScreen(
            bestScore = progress.bestScoresByLevel.values.maxOrNull() ?: progress.legacyBestScore,
            lastUnlockedLevel = progress.highestUnlockedLevel,
            unlockedAchievements = unlockedAchievements.size,
            activeProfile = progress.activeProfile,
            onStartGame = { screen = RootScreen.LevelSelect },
            onDailyChallenge = { screen = RootScreen.DailyChallenge },
            onLeaderboard = { screen = RootScreen.Leaderboard },
            onSettings = { screen = RootScreen.Settings },
            onAchievements = { screen = RootScreen.Achievements },
            onProfiles = { screen = RootScreen.ProfileSelect },
            onTutorial = { screen = RootScreen.Tutorial },
        )

        RootScreen.LevelSelect -> LevelSelectScreen(
            highestUnlockedLevel = progress.highestUnlockedLevel,
            bestScoresByLevel = progress.bestScoresByLevel,
            selectedDifficulty = selectedDifficulty,
            onDifficultySelected = { difficulty ->
                selectedDifficulty = difficulty
                scope.launch { preferences.saveLastDifficulty(difficulty) }
            },
            onLevelSelected = { level ->
                selectedLevelId = level.id
                activeDailyDateKey = null
                screen = RootScreen.Game
            },
            onBack = { screen = RootScreen.Menu },
        )

        RootScreen.DailyChallenge -> DailyChallengeScreen(
            challenge = todayChallenge,
            best = DailyChallengeProgress.bestFor(
                entries = dailyBests,
                dateKey = todayChallenge.dateKey,
                profileSlot = progress.activeProfile,
            ),
            onStart = {
                activeDailyDateKey = todayChallenge.dateKey
                screen = RootScreen.Game
            },
            onBack = { screen = RootScreen.Menu },
        )

        RootScreen.Game -> {
            val scoreSaver = remember(
                preferences,
                progress.highestUnlockedLevel,
                progress.activeProfile,
                activeDailyDateKey,
            ) {
                ScoreSaver(
                    preferences = preferences,
                    currentHighestUnlocked = progress.highestUnlockedLevel,
                    profileSlot = progress.activeProfile,
                    dailyDateKey = activeDailyDateKey,
                )
            }
            GameScreen(
                level = selectedLevel,
                difficulty = activeDifficulty,
                bestScore = activeDailyDateKey?.let { dateKey ->
                    DailyChallengeProgress.bestFor(dailyBests, dateKey, progress.activeProfile)?.score ?: 0
                } ?: progress.bestScoreForLevel(selectedLevel.id),
                soundEnabled = settings.soundEnabled,
                showGrid = settings.showGrid,
                screenShakeEnabled = settings.screenShakeEnabled,
                damageNumbersEnabled = settings.damageNumbersEnabled,
                highContrastMode = settings.highContrastMode,
                fpsCounterEnabled = settings.fpsCounterEnabled,
                onBackToMenu = {
                    screen = if (activeDailyDateKey == null) RootScreen.LevelSelect else RootScreen.DailyChallenge
                    activeDailyDateKey = null
                },
                onRunFinalized = scoreSaver::saveFinalResult,
            )
        }

        RootScreen.Leaderboard -> LeaderboardScreen(
            entries = leaderboardEntries,
            onBack = { screen = RootScreen.Menu },
        )

        RootScreen.Settings -> SettingsScreen(
            settings = settings,
            onSettingsChanged = { nextSettings ->
                scope.launch { preferences.saveSettings(nextSettings) }
            },
            onResetProgress = {
                tutorialAcknowledgedProfile = null
                scope.launch { preferences.resetProgress() }
            },
            onBack = { screen = RootScreen.Menu },
        )

        RootScreen.Achievements -> AchievementsScreen(
            unlockedAchievements = unlockedAchievements,
            onBack = { screen = RootScreen.Menu },
        )
    }
}

private class ScoreSaver(
    private val preferences: GamePreferences,
    private val currentHighestUnlocked: Int,
    private val profileSlot: Int,
    private val dailyDateKey: String?,
) {
    suspend fun saveFinalResult(result: GameRunResult) {
        val completedAt = System.currentTimeMillis()
        if (dailyDateKey != null) {
            preferences.saveDailyBest(
                DailyChallengeBest.fromRun(
                    dateKey = dailyDateKey,
                    profileSlot = profileSlot,
                    result = result,
                    completedAtEpochMillis = completedAt,
                ),
            )
            if (result.won) {
                preferences.unlockAchievements(AchievementRules.unlockedBy(result))
            }
            return
        }

        preferences.saveBestScoreForLevel(result.levelId, result.score)
        preferences.unlockAchievements(AchievementRules.unlockedBy(result))
        if (result.won) {
            LeaderboardEntry.fromRun(
                profileSlot = profileSlot,
                result = result,
                completedAtEpochMillis = completedAt,
            )?.let { preferences.saveLeaderboardEntry(it) }
            preferences.unlockLevel(
                ProgressionRules.highestUnlockedAfterVictory(
                    completedLevelId = result.levelId,
                    currentHighestUnlocked = currentHighestUnlocked,
                    maxLevelId = LevelCatalog.levels.last().id,
                ),
            )
        }
    }
}
