package com.dailystudio.vibecoding.driftopen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dailystudio.vibecoding.driftopen.game.GameEngine
import com.dailystudio.vibecoding.driftopen.game.ScoreManager
import com.dailystudio.vibecoding.driftopen.game.SoundManager
import com.dailystudio.vibecoding.driftopen.game.models.GamePhase
import com.dailystudio.vibecoding.driftopen.ui.GameScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val soundManager = remember { SoundManager(context) }
            val scoreManager = remember { ScoreManager(context) }
            val engine = remember { GameEngine(soundManager) }

            val gameState by engine.gameState.collectAsState()
            val highScore by scoreManager.highScoreFlow.collectAsState(initial = 0)

            BackHandler(enabled = gameState.phase == GamePhase.PLAYING) {
                engine.pauseGame()
            }

            // Sync high score to engine
            LaunchedEffect(highScore) {
                engine.setHighScore(highScore)
            }

            // Auto-save high score
            LaunchedEffect(gameState.phase, gameState.score) {
                if (gameState.phase == GamePhase.GAME_OVER || gameState.phase == GamePhase.WIN) {
                    scoreManager.saveHighScore(gameState.score)
                }
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            soundManager.resumeMusic()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            engine.pauseGame()
                            soundManager.pauseMusic()
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                soundManager.startMusic()

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    soundManager.release()
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(engine)
                }
            }
        }
    }
}
