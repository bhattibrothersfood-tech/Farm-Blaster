package com.example.farmblaster.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.farmblaster.data.GamePreferences
import com.example.farmblaster.engine.GameEngine
import com.example.farmblaster.engine.GameSurfaceView
import com.example.farmblaster.model.GameState

@Composable
fun GameScreen(
    engine: GameEngine,
    prefs: GamePreferences,
    onExitToMenu: () -> Unit
) {
    val context = LocalContext.current

    var currentScore by remember { mutableIntStateOf(engine.score) }
    var currentCombo by remember { mutableIntStateOf(engine.combo) }
    var currentWave by remember { mutableIntStateOf(engine.waveManager.currentWave) }
    var currentLives by remember { mutableIntStateOf(engine.player.lives) }
    var currentBombs by remember { mutableIntStateOf(engine.player.bombs) }
    var gameState by remember { mutableStateOf(engine.gameState) }

    // Wave complete state
    var waveBonus by remember { mutableIntStateOf(0) }
    var waveKills by remember { mutableIntStateOf(0) }
    var waveComboBonus by remember { mutableIntStateOf(0) }

    // Game Over state
    var isNewHighScore by remember { mutableStateOf(false) }

    // Hook listeners into engine
    DisposableEffect(engine) {
        val oldScore = prefs.highScore

        engine.onScoreUpdated = { score, combo ->
            currentScore = score
            currentCombo = combo
        }

        engine.onStatsUpdated = { lives, bombs, wave ->
            currentLives = lives
            currentBombs = bombs
            currentWave = wave
        }

        engine.onGameStateChanged = { state ->
            gameState = state
            if (state == GameState.GAME_OVER) {
                isNewHighScore = engine.score > oldScore && engine.score > 0
            }
        }

        engine.onWaveComplete = { wave, bonus, kills, comboBonus ->
            waveBonus = bonus
            waveKills = kills
            waveComboBonus = comboBonus
            gameState = GameState.LEVEL_COMPLETE
        }

        onDispose {
            engine.pauseGame()
        }
    }

    // Handle back button: pause if playing, or confirm exit
    BackHandler {
        if (gameState == GameState.PLAYING) {
            engine.pauseGame()
        } else if (gameState == GameState.PAUSED) {
            onExitToMenu()
        } else {
            onExitToMenu()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Native SurfaceView Game Canvas
        AndroidView(
            factory = {
                GameSurfaceView(context, engine)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay HUD
        InGameHUD(
            score = currentScore,
            combo = currentCombo,
            wave = currentWave,
            lives = currentLives,
            bombs = currentBombs,
            player = engine.player,
            onPauseClicked = { engine.pauseGame() }
        )

        // Modal Dialogs depending on GameState
        when (gameState) {
            GameState.PAUSED -> {
                PauseDialog(
                    onResume = { engine.resumeGame() },
                    onRestart = { engine.startNewGame() },
                    onMainMenu = onExitToMenu
                )
            }
            GameState.LEVEL_COMPLETE -> {
                LevelCompleteDialog(
                    wave = currentWave,
                    bonus = waveBonus,
                    kills = waveKills,
                    comboBonus = waveComboBonus,
                    onContinue = { engine.nextWave() }
                )
            }
            GameState.GAME_OVER -> {
                GameOverDialog(
                    score = currentScore,
                    highScore = prefs.highScore,
                    wave = currentWave,
                    kills = engine.enemiesKilledInGame,
                    bestCombo = prefs.bestCombo,
                    isNewRecord = isNewHighScore,
                    onPlayAgain = { engine.startNewGame() },
                    onMainMenu = onExitToMenu
                )
            }
            else -> {}
        }
    }
}
