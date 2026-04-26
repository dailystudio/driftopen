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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dailystudio.vibecoding.driftopen.game.GameEngine
import com.dailystudio.vibecoding.driftopen.game.models.*

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
                        engine.moveShipRelative(dragAmount.x, dragAmount.y)
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

            val shakeX = if (state.screenShakeIntensity > 0) (kotlin.random.Random.nextFloat() - 0.5f) * 2 * state.screenShakeIntensity else 0f
            val shakeY = if (state.screenShakeIntensity > 0) (kotlin.random.Random.nextFloat() - 0.5f) * 2 * state.screenShakeIntensity else 0f

            drawContext.canvas.save()
            drawContext.canvas.translate(shakeX, shakeY)

            // Draw background
            drawRect(Color.Black)
            
            // Draw stars
            state.stars.forEach { star ->
                drawCircle(Color.White.copy(alpha = 0.8f), radius = star.size, center = Offset(star.x, star.y))
            }

            // Draw ship (Offsetting visually so it's above the finger)
            val ship = state.ship
            val visualY = ship.y - 120f // Move visually 120 pixels up from touch point
            
            val shipAlpha = if (ship.invincibilityFrames > 0) {
                if ((ship.invincibilityFrames / 5) % 2 == 0) 0.3f else 0.7f
            } else 1f
            
            val shipPath = GamePaths.shipSkins[ship.skinId] ?: GamePaths.shipSkins["default"]!!

            drawContext.canvas.save()
            drawContext.canvas.translate(ship.x, visualY)
            drawContext.canvas.scale(ship.width, ship.height)
            drawPath(shipPath, Color.Cyan.copy(alpha = shipAlpha))
            drawContext.canvas.restore()
            
            // Cockpit (still drawing circle directly as it's simple)
            drawCircle(Color.White.copy(alpha = 0.8f * shipAlpha), radius = ship.width * 0.1f, center = Offset(ship.x, visualY))

            // Draw shield aura if active (using visualY)
            if (state.activePowerUp?.type == PowerUpType.SHIELD) {
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.4f),
                    radius = ship.width * 0.8f,
                    center = Offset(ship.x, visualY),
                    style = Stroke(width = 4f)
                )
            }

            // Draw aliens
            state.aliens.forEach { alien ->
                val w = alien.width / 2
                val h = alien.height / 2
                val skins = GamePaths.alienSkins[alien.skinId] ?: GamePaths.alienSkins["default"]!!
                val path = skins[alien.type] ?: skins[AlienType.NORMAL]!!
                
                drawContext.canvas.save()
                drawContext.canvas.translate(alien.x, alien.y)
                drawContext.canvas.scale(alien.width, alien.height)
                drawPath(path, color = alien.color)
                
                if (alien.type != AlienType.SUPERBOSS) {
                    drawPath(path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 2f / alien.width))
                } else {
                    drawPath(path, color = Color.Yellow.copy(alpha = 0.5f), style = Stroke(width = 4f / alien.width))
                }
                drawContext.canvas.restore()

                // Eyes (still simple circles)
                val eyeOffset = w * 0.3f
                val eyeSize = w * 0.2f
                drawCircle(Color.White, radius = eyeSize, center = Offset(alien.x - eyeOffset, alien.y - h * 0.2f))
                drawCircle(Color.White, radius = eyeSize, center = Offset(alien.x + eyeOffset, alien.y - h * 0.2f))
                drawCircle(Color.Black, radius = eyeSize * 0.5f, center = Offset(alien.x - eyeOffset, alien.y - h * 0.2f))
                drawCircle(Color.Black, radius = eyeSize * 0.5f, center = Offset(alien.x + eyeOffset, alien.y - h * 0.2f))
                
                // Health bar
                if (alien.health < alien.maxHealth) {
                    val barWidth = alien.width * 0.8f
                    val healthWidth = barWidth * (alien.health.toFloat() / alien.maxHealth)
                    drawRect(
                        color = Color.Gray,
                        topLeft = Offset(alien.x - barWidth / 2, alien.y - alien.height / 2 - 15f),
                        size = Size(barWidth, 6f)
                    )
                    drawRect(
                        color = Color.Green,
                        topLeft = Offset(alien.x - barWidth / 2, alien.y - alien.height / 2 - 15f),
                        size = Size(healthWidth, 6f)
                    )
                }
            }

            // Draw player bullets
            state.bullets.forEach { bullet ->
                when (bullet.type) {
                    BulletType.LASER -> {
                        drawRect(
                            color = Color(0xFF00FFFF),
                            topLeft = Offset(bullet.x - bullet.width / 2, bullet.y - bullet.height / 2),
                            size = Size(bullet.width, bullet.height)
                        )
                    }
                    BulletType.BOMB -> {
                        drawCircle(
                            color = Color(0xFFFF4500),
                            radius = bullet.width / 2,
                            center = Offset(bullet.x, bullet.y)
                        )
                    }
                    BulletType.HOMING -> {
                        val p = Path().apply {
                            moveTo(bullet.x, bullet.y - bullet.height / 2)
                            lineTo(bullet.x - bullet.width / 2, bullet.y + bullet.height / 2)
                            lineTo(bullet.x + bullet.width / 2, bullet.y + bullet.height / 2)
                            close()
                        }
                        drawPath(p, Color.White)
                    }
                    else -> {
                        drawRect(
                            color = Color.Yellow,
                            topLeft = Offset(bullet.x - bullet.width / 2, bullet.y - bullet.height / 2),
                            size = Size(bullet.width, bullet.height)
                        )
                    }
                }
            }

            // Draw alien bullets
            state.alienBullets.forEach { bullet ->
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(bullet.x - 3f, bullet.y),
                    size = Size(6f, 20f)
                )
            }

            // Draw power-ups
            state.powerUps.forEach { powerUp ->
                val color = when (powerUp.type) {
                    PowerUpType.SHIELD -> Color.Cyan
                    PowerUpType.DOUBLE_FIRE -> Color.Green
                    PowerUpType.RAPID_FIRE -> Color.Yellow
                    PowerUpType.SPREAD_SHOT -> Color.Magenta
                    PowerUpType.LASER_BEAM -> Color.Blue
                    PowerUpType.HOMING_MISSILES -> Color.White
                    PowerUpType.EXPLOSIVE_BOMBS -> Color(0xFFFF4500)
                    PowerUpType.INVINCIBILITY -> Color(0xFFFFD700) // Gold
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

            drawContext.canvas.restore()
        }

        // HUD: Score and Lives
        if (state.phase == GamePhase.PLAYING) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SCORE: ${state.score}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "HI-SCORE: ${state.highScore}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = "LVL: ${state.level}",
                        color = Color.Yellow,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.lives <= 5) {
                            repeat(state.lives) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = " x${state.lives}",
                                color = Color.Red,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
                
                // Power-up indicator
                state.activePowerUp?.let { 
                    Text(
                        text = "${it.type.name.replace("_", " ")} ACTIVE: ${it.timeRemaining / 60}s",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Overlays
        when (state.phase) {
            GamePhase.START -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                engine.recordStartScreenTap()
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Game Logo
                    Canvas(modifier = Modifier.size(150.dp)) {
                        val w = size.width
                        val h = size.height
                        val logoPath = Path().apply {
                            // Futuristic G-shape / Wings
                            moveTo(w * 0.1f, h * 0.5f)
                            lineTo(w * 0.3f, h * 0.2f)
                            lineTo(w * 0.7f, h * 0.2f)
                            lineTo(w * 0.9f, h * 0.5f)
                            lineTo(w * 0.7f, h * 0.8f)
                            lineTo(w * 0.3f, h * 0.8f)
                            close()
                            
                            // Core
                            addOval(androidx.compose.ui.geometry.Rect(w * 0.4f, h * 0.4f, w * 0.6f, h * 0.6f))
                        }
                        drawPath(logoPath, Color.Cyan, style = Stroke(width = 4.dp.toPx()))
                        drawPath(logoPath, Color.Cyan.copy(alpha = 0.3f))
                        
                        // Extra wing details
                        drawLine(Color.Cyan, Offset(w * 0.3f, h * 0.2f), Offset(w * 0.1f, h * 0.1f), strokeWidth = 2.dp.toPx())
                        drawLine(Color.Cyan, Offset(w * 0.7f, h * 0.2f), Offset(w * 0.9f, h * 0.1f), strokeWidth = 2.dp.toPx())
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "DRIFTOPEN",
                        color = Color.Cyan,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "HI-SCORE: ${state.highScore}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (state.activeCheats.contains(com.dailystudio.vibecoding.driftopen.game.models.CheatType.INVINCIBILITY)) {
                        Text(
                            text = "CHEATS ACTIVE: INVINCIBILITY",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { engine.startGame() }) {
                        Text("START GAME")
                    }
                }
            }
            GamePhase.WIN -> {
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
                    Text(
                        text = "HI-SCORE: ${state.highScore}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { engine.resetToStart() }) {
                        Text("BACK TO START")
                    }
                }
            }
            GamePhase.GAME_OVER -> {
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
            GamePhase.PAUSED -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PAUSED",
                        color = Color.Yellow,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { engine.resumeGame() }, modifier = Modifier.width(200.dp)) {
                        Text("CONTINUE")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { engine.resetToStart() }, modifier = Modifier.width(200.dp)) {
                        Text("RETURN TO TITLE", color = Color.White)
                    }
                }
            }
            GamePhase.PLAYING -> { /* HUD is drawn above */ }
        }
    }
}
