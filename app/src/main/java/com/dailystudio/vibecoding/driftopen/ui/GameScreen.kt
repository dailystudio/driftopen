package com.dailystudio.vibecoding.driftopen.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dailystudio.vibecoding.driftopen.game.GameEngine
import com.dailystudio.vibecoding.driftopen.game.models.GameState
import com.dailystudio.vibecoding.driftopen.game.models.PowerUpType

@Composable
fun GameScreen(engine: GameEngine) {
    val state by engine.gameState.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                engine.update(frameTimeNanos)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        engine.moveShipRelative(dragAmount.x)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        engine.shoot()
                    }
                }
        ) {
            if (state.screenWidth == 0f) {
                engine.setScreenSize(size.width, size.height)
            }

            // Draw background
            drawRect(Color.Black)

            // Draw stars
            state.stars.forEach { star ->
                drawCircle(Color.White.copy(alpha = 0.8f), radius = star.size, center = Offset(star.x, star.y))
            }

            // Draw ship
            val ship = state.ship
            val shipPath = Path().apply {
                moveTo(ship.x, ship.y - ship.height / 2)
                lineTo(ship.x - ship.width / 2, ship.y + ship.height / 2)
                lineTo(ship.x + ship.width / 2, ship.y + ship.height / 2)
                close()
            }
            drawPath(shipPath, Color.Cyan)

            // Draw shield aura if active
            if (state.activePowerUp?.type == PowerUpType.SHIELD) {
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.4f),
                    radius = ship.width * 0.8f,
                    center = Offset(ship.x, ship.y),
                    style = Stroke(width = 4f)
                )
            }

            // Draw aliens
            state.aliens.forEach { alien ->
                drawRect(
                    color = alien.color,
                    topLeft = Offset(alien.x - alien.width / 2, alien.y - alien.height / 2),
                    size = androidx.compose.ui.geometry.Size(alien.width, alien.height)
                )
                // Boss health indicator
                if (alien.type == com.dailystudio.vibecoding.driftopen.game.models.AlienType.BOSS && alien.health > 0) {
                    drawRect(
                        color = Color.Green,
                        topLeft = Offset(alien.x - alien.width / 2, alien.y - alien.height / 2 - 10f),
                        size = androidx.compose.ui.geometry.Size(alien.width * (alien.health / 3f), 4f)
                    )
                }
            }

            // Draw player bullets
            state.bullets.forEach { bullet ->
                drawRect(
                    color = Color.Yellow,
                    topLeft = Offset(bullet.x - 4f, bullet.y - 15f),
                    size = androidx.compose.ui.geometry.Size(8f, 30f)
                )
            }

            // Draw alien bullets
            state.alienBullets.forEach { bullet ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(bullet.x - 4f, bullet.y),
                    size = androidx.compose.ui.geometry.Size(8f, 30f)
                )
            }

            // Draw power-ups
            state.powerUps.forEach { powerUp ->
                val color = when (powerUp.type) {
                    PowerUpType.SHIELD -> Color.Cyan
                    PowerUpType.DOUBLE_FIRE -> Color.Green
                    PowerUpType.RAPID_FIRE -> Color.Yellow
                }
                drawCircle(color, radius = powerUp.radius, center = Offset(powerUp.x, powerUp.y))
                drawCircle(Color.White, radius = powerUp.radius * 0.7f, center = Offset(powerUp.x, powerUp.y), style = Stroke(width = 2f))
            }

            // Draw explosions
            state.explosions.forEach { explosion ->
                drawCircle(
                    color = explosion.color.copy(alpha = explosion.alpha),
                    radius = explosion.radius,
                    center = Offset(explosion.x, explosion.y)
                )
            }
        }

        // HUD: Score and Lives
        if (state.phase == com.dailystudio.vibecoding.driftopen.game.models.GamePhase.PLAYING) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SCORE: ${state.score}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "LEVEL: ${state.level}",
                        color = Color.Yellow,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row {
                        repeat(state.lives) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                // Power-up indicator
                state.activePowerUp?.let { 
                    Text(
                        text = "${it.type} ACTIVE: ${it.timeRemaining / 60}s",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Overlays
        when (state.phase) {
            com.dailystudio.vibecoding.driftopen.game.models.GamePhase.START -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DRIFTOPEN",
                        color = Color.Cyan,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { engine.startGame() }) {
                        Text("START GAME")
                    }
                }
            }
            com.dailystudio.vibecoding.driftopen.game.models.GamePhase.WIN -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VICTORY!",
                        color = Color.Green,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "FINAL SCORE: ${state.score}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { engine.resetToStart() }) {
                        Text("BACK TO START")
                    }
                }
            }
            com.dailystudio.vibecoding.driftopen.game.models.GamePhase.GAME_OVER -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GAME OVER",
                        color = Color.Red,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { engine.resetToStart() }) {
                        Text("BACK TO START")
                    }
                }
            }
            com.dailystudio.vibecoding.driftopen.game.models.GamePhase.PLAYING -> { /* HUD is drawn above */ }
        }
    }
}
