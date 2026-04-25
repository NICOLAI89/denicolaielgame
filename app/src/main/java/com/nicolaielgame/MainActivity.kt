package com.nicolaielgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nicolaielgame.data.GamePreferences
import com.nicolaielgame.data.ProgressSnapshot
import com.nicolaielgame.data.ProgressionRules
import com.nicolaielgame.game.model.LevelCatalog
import com.nicolaielgame.ui.game.GameScreen
import com.nicolaielgame.ui.menu.LevelSelectScreen
import com.nicolaielgame.ui.menu.MainMenuScreen
import com.nicolaielgame.ui.theme.DenicolaielTheme

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
    Menu,
    LevelSelect,
    Game,
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
    var screen by rememberSaveable { mutableStateOf(RootScreen.Menu) }
    var selectedLevelId by rememberSaveable { mutableStateOf(LevelCatalog.firstLevel.id) }
    val selectedLevel = LevelCatalog.find(selectedLevelId)

    when (screen) {
        RootScreen.Menu -> MainMenuScreen(
            bestScore = progress.bestScoresByLevel.values.maxOrNull() ?: progress.legacyBestScore,
            lastUnlockedLevel = progress.highestUnlockedLevel,
            onStartGame = { screen = RootScreen.LevelSelect },
        )

        RootScreen.LevelSelect -> LevelSelectScreen(
            highestUnlockedLevel = progress.highestUnlockedLevel,
            bestScoresByLevel = progress.bestScoresByLevel,
            onLevelSelected = { level ->
                selectedLevelId = level.id
                screen = RootScreen.Game
            },
            onBack = { screen = RootScreen.Menu },
        )

        RootScreen.Game -> {
            val scoreSaver = remember(preferences, progress.highestUnlockedLevel) {
                ScoreSaver(
                    preferences = preferences,
                    currentHighestUnlocked = progress.highestUnlockedLevel,
                )
            }
            GameScreen(
                level = selectedLevel,
                bestScore = progress.bestScoreForLevel(selectedLevel.id),
                onBackToMenu = { screen = RootScreen.LevelSelect },
                onScoreFinalized = scoreSaver::saveFinalScore,
            )
        }
    }
}

private class ScoreSaver(
    private val preferences: GamePreferences,
    private val currentHighestUnlocked: Int,
) {
    suspend fun saveFinalScore(levelId: Int, score: Int, won: Boolean) {
        preferences.saveBestScoreForLevel(levelId, score)
        if (won) {
            preferences.unlockLevel(
                ProgressionRules.highestUnlockedAfterVictory(
                    completedLevelId = levelId,
                    currentHighestUnlocked = currentHighestUnlocked,
                    maxLevelId = LevelCatalog.levels.last().id,
                ),
            )
        }
    }
}
