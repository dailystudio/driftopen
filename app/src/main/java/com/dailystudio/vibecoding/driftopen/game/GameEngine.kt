package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class GameEngine {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private var lastShootTime = 0L
    private var alienMoveDirection = 1f
    private var formationX = 0f
    private val spacing = 80f

    fun setScreenSize(width: Float, height: Float) {
        _gameState.update {
            it.copy(
                screenWidth = width,
                screenHeight = height,
                ship = it.ship.copy(x = width / 2, y = height - 150f),
                stars = List(50) {
                    Star(
                        Random.nextFloat() * width,
                        Random.nextFloat() * height,
                        Random.nextFloat() * 4f + 1f,
                        Random.nextFloat() * 2f + 1f
                    )
                }
            )
        }
        if (_gameState.value.phase == GamePhase.PLAYING) {
            spawnAliens(width)
        }
    }

    private fun spawnAliens(screenWidth: Float) {
        val aliens = mutableListOf<Alien>()
        val rows = 4
        val cols = 8
        formationX = (screenWidth - (cols - 1) * spacing) / 2
        val startY = 150f

        var idCounter = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                aliens.add(
                    Alien(
                        id = idCounter++,
                        x = formationX + col * spacing,
                        y = startY + row * spacing,
                        gridCol = col,
                        gridRow = row
                    )
                )
            }
        }
        _gameState.update { it.copy(aliens = aliens) }
    }

    fun moveShipRelative(deltaX: Float) {
        _gameState.update { state ->
            val newX = (state.ship.x + deltaX).coerceIn(state.ship.width / 2, state.screenWidth - state.ship.width / 2)
            state.copy(ship = state.ship.copy(x = newX))
        }
    }

    fun shoot() {
        val now = System.currentTimeMillis()
        if (now - lastShootTime > 250) {
            _gameState.update { state ->
                val bulletY = state.ship.y - state.ship.height / 2
                state.copy(bullets = state.bullets + Bullet(state.ship.x, bulletY, speed = 25f))
            }
            lastShootTime = now
        }
    }

    fun startGame() {
        _gameState.update { 
            it.copy(
                phase = GamePhase.PLAYING,
                lives = 3,
                score = 0,
                bullets = emptyList(),
                alienBullets = emptyList(),
                explosions = emptyList(),
                aliens = emptyList(),
                ship = it.ship.copy(x = it.screenWidth / 2, y = it.screenHeight - 150f)
            ) 
        }
        spawnAliens(_gameState.value.screenWidth)
    }

    fun resetToStart() {
        _gameState.update { it.copy(phase = GamePhase.START) }
    }

    fun update(dtNanos: Long) {
        val currentState = _gameState.value
        if (currentState.phase != GamePhase.PLAYING) return

        // Auto shoot
        val now = System.currentTimeMillis()
        if (now - lastShootTime > 400) {
            shoot()
        }

        _gameState.update { state ->
            val newExplosions = state.explosions.map { 
                it.copy(life = it.life - 1, radius = it.radius + 2f, alpha = it.alpha * 0.9f) 
            }.filter { it.life > 0 }

            val newBullets = state.bullets.map { it.copy(y = it.y - it.speed) }
                .filter { it.y > -50f }
            
            val newAlienBullets = state.alienBullets.map { it.copy(y = it.y + it.speed * 0.5f) }
                .filter { it.y < state.screenHeight + 50f }

            val newStars = state.stars.map {
                val newY = it.y + it.speed
                if (newY > state.screenHeight) it.copy(y = 0f, x = Random.nextFloat() * state.screenWidth)
                else it.copy(y = newY)
            }

            // Move formation
            formationX += alienMoveDirection * 2f
            val formationWidth = (8 - 1) * spacing
            if (formationX < 50f || formationX + formationWidth > state.screenWidth - 50f) {
                alienMoveDirection *= -1
                formationX = formationX.coerceIn(50f, state.screenWidth - 50f - formationWidth)
            }

            val newAliens = state.aliens.map { alien ->
                if (alien.isAttacking) {
                    val phase = alien.attackPhase + 0.05f
                    val diveX = alien.x + kotlin.math.sin(phase) * 10f
                    val diveY = alien.y + 12f
                    if (diveY > state.screenHeight + 50f) {
                        alien.copy(y = -50f, isAttacking = false, attackPhase = 0f)
                    } else {
                        alien.copy(x = diveX, y = diveY, attackPhase = phase)
                    }
                } else {
                    val targetX = formationX + alien.gridCol * spacing
                    val targetY = 150f + alien.gridRow * spacing
                    
                    val dx = targetX - alien.x
                    val dy = targetY - alien.y
                    
                    val newX = if (kotlin.math.abs(dx) < 2f) targetX else alien.x + dx * 0.1f
                    val newY = if (kotlin.math.abs(dy) < 2f) targetY else alien.y + dy * 0.1f
                    
                    alien.copy(x = newX, y = newY)
                }
            }.toMutableList()

            if (Random.nextInt(100) < 2 && newAliens.any { !it.isAttacking && it.y >= 150f }) {
                val index = newAliens.indices.filter { !newAliens[it].isAttacking && newAliens[it].y >= 150f }.random()
                newAliens[index] = newAliens[index].copy(isAttacking = true)
            }
            
            var updatedAlienBullets = newAlienBullets
            if (Random.nextInt(100) < 3 && newAliens.isNotEmpty()) {
                val shooter = newAliens.random()
                if (shooter.y > 0) {
                    updatedAlienBullets = updatedAlienBullets + Bullet(shooter.x, shooter.y + shooter.height / 2)
                }
            }

            val remainingAliens = newAliens.toMutableList()
            val finalBullets = newBullets.toMutableList()
            val currentExplosions = newExplosions.toMutableList()
            var scoreGain = 0
            var newLives = state.lives
            var nextPhase = state.phase

            val bulletsToRemove = mutableListOf<Bullet>()
            val aliensToRemove = mutableListOf<Alien>()

            for (bullet in finalBullets) {
                val hitAlien = remainingAliens.find { it.getRect().overlaps(bullet.getRect()) }
                if (hitAlien != null) {
                    bulletsToRemove.add(bullet)
                    aliensToRemove.add(hitAlien)
                    scoreGain += 100
                    currentExplosions.add(Explosion(hitAlien.x, hitAlien.y, hitAlien.color))
                }
            }

            remainingAliens.removeAll(aliensToRemove)
            finalBullets.removeAll(bulletsToRemove)
            
            val hitByBullet = updatedAlienBullets.any { it.getRect().overlaps(state.ship.getRect()) }
            val hitByAlien = remainingAliens.any { it.getRect().overlaps(state.ship.getRect()) }

            if (hitByBullet || hitByAlien) {
                newLives -= 1
                currentExplosions.add(Explosion(state.ship.x, state.ship.y, Color.White, radius = 20f))
                updatedAlienBullets = emptyList()
                if (newLives <= 0) {
                    nextPhase = GamePhase.GAME_OVER
                }
            }

            if (remainingAliens.isEmpty() && state.aliens.isNotEmpty()) {
                nextPhase = GamePhase.WIN
            }

            state.copy(
                bullets = finalBullets,
                alienBullets = updatedAlienBullets,
                aliens = remainingAliens,
                stars = newStars,
                explosions = currentExplosions,
                score = state.score + scoreGain,
                lives = newLives,
                phase = nextPhase
            )
        }
    }
}
