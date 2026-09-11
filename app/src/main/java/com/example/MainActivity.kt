package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.farmblaster.audio.ArcadeAudioManager
import com.example.farmblaster.data.GamePreferences
import com.example.farmblaster.engine.GameEngine
import com.example.farmblaster.model.GameState
import com.example.farmblaster.ui.GameScreen
import com.example.farmblaster.ui.HighScoresScreen
import com.example.farmblaster.ui.HowToPlayScreen
import com.example.farmblaster.ui.MainMenuScreen
import com.example.farmblaster.ui.SettingsScreen
import com.example.farmblaster.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination {
    SPLASH,
    MAIN_MENU,
    HOW_TO_PLAY,
    HIGH_SCORES,
    SETTINGS,
    GAME
}

class MainActivity : ComponentActivity() {

    private lateinit var prefs: GamePreferences
    private lateinit var audioManager: ArcadeAudioManager
    private var gameEngine: GameEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = GamePreferences(this)
        audioManager = ArcadeAudioManager(this, prefs)
        val engine = GameEngine(this, prefs, audioManager)
        gameEngine = engine

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF070913)
                ) {
                    FarmBlasterApp(
                        prefs = prefs,
                        audioManager = audioManager,
                        engine = engine
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameEngine?.pauseGame()
    }

    override fun onResume() {
        super.onResume()
        if (gameEngine?.gameState == GameState.PLAYING && prefs.isMusicEnabled) {
            audioManager.startMusic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameEngine?.release()
    }
}

@Composable
fun FarmBlasterApp(
    prefs: GamePreferences,
    audioManager: ArcadeAudioManager,
    engine: GameEngine
) {
    var currentScreen by remember { mutableStateOf(AppDestination.SPLASH) }

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(300),
        label = "screen_nav"
    ) { screen ->
        when (screen) {
            AppDestination.SPLASH -> {
                SplashScreen(
                    onStartClicked = { currentScreen = AppDestination.MAIN_MENU }
                )
            }
            AppDestination.MAIN_MENU -> {
                MainMenuScreen(
                    prefs = prefs,
                    onPlayClicked = {
                        engine.startNewGame()
                        currentScreen = AppDestination.GAME
                    },
                    onHowToPlayClicked = { currentScreen = AppDestination.HOW_TO_PLAY },
                    onHighScoresClicked = { currentScreen = AppDestination.HIGH_SCORES },
                    onSettingsClicked = { currentScreen = AppDestination.SETTINGS }
                )
            }
            AppDestination.HOW_TO_PLAY -> {
                HowToPlayScreen(
                    onBackClicked = { currentScreen = AppDestination.MAIN_MENU }
                )
            }
            AppDestination.HIGH_SCORES -> {
                HighScoresScreen(
                    prefs = prefs,
                    onBackClicked = { currentScreen = AppDestination.MAIN_MENU }
                )
            }
            AppDestination.SETTINGS -> {
                SettingsScreen(
                    prefs = prefs,
                    audioManager = audioManager,
                    onBackClicked = { currentScreen = AppDestination.MAIN_MENU }
                )
            }
            AppDestination.GAME -> {
                GameScreen(
                    engine = engine,
                    prefs = prefs,
                    onExitToMenu = {
                        engine.pauseGame()
                        currentScreen = AppDestination.MAIN_MENU
                    }
                )
            }
        }
    }
}
