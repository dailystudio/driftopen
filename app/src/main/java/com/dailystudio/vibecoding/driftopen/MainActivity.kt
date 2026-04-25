package com.dailystudio.vibecoding.driftopen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dailystudio.vibecoding.driftopen.game.GameEngine
import com.dailystudio.vibecoding.driftopen.ui.GameScreen

class MainActivity : ComponentActivity() {
    private val engine = GameEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
