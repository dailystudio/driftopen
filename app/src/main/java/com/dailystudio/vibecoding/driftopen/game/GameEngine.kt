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
        if (_gameState.value.phase == GamePhase.PLAYING && _gameState.value.aliens.isEmpty()) {
            spawnAliens(_gameState.value.screenWidth)
        }
    }

    private fun createAliens(screenWidth: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val rows = 4
        val cols = 8
        formationX = (screenWidth - (cols - 1) * spacing) / 2
        val startY = 150f

        var idCounter = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val type = when (row) {
                    0 -> AlienType.BOSS
                    1 -> AlienType.FAST
                    else -> AlienType.NORMAL
                }
                val color = when (type) {
                    AlienType.BOSS -> Color.Magenta
                    AlienType.FAST -> Color.Yellow
                    AlienType.NORMAL -> Color.Red
                }
                val health = if (type == AlienType.BOSS) 3 else 1

                aliens.add(
                    Alien(
                        id = idCounter++,
                        x = formationX + col * spacing,
                        y = startY + row * spacing,
                        gridCol = col,
                        gridRow = row,
                        type = type,
                        color = color,
                        health = health
                    )
                )
            }
        }
        return aliens
    }

    private fun spawnAliens(screenWidth: Float) {
        val aliens = createAliens(screenWidth)
        _gameState.update { it.copy(aliens = aliens) }
    }

    fun moveShipRelative(deltaX: Float) {
        _gameState.update { state ->
            val newX = (state.ship.x + deltaX).coerceIn(state.ship.width / 2, state.screenWidth - state.ship.width / 2)
            state.copy(ship = state.ship.copy(x = newX))
        }
    }

    fun shoot() {
        val state = _gameState.value
        val now = System.currentTimeMillis()
        val cooldown = when (state.activePowerUp?.type) {
            PowerUpType.RAPID_FIRE -> 100L
            else -> 250L
        }

        if (now - lastShootTime > cooldown) {
            _gameState.update { s ->
                val bulletY = s.ship.y - s.ship.height / 2
                val newBullets = if (s.activePowerUp?.type == PowerUpType.DOUBLE_FIRE) {
                    s.bullets + listOf(
                        Bullet(s.ship.x - 15f, bulletY, speed = 25f),
                        Bullet(s.ship.x + 15f, bulletY, speed = 25f)
                    )
                } else {
                    s.bullets + Bullet(s.ship.x, bulletY, speed = 25f)
                }
                s.copy(bullets = newBullets)
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
                level = 1,
                bullets = emptyList(),
                alienBullets = emptyList(),
                explosions = emptyList(),
                powerUps = emptyList(),
                activePowerUp = null,
                aliens = createAliens(it.screenWidth),
                ship = it.ship.copy(x = it.screenWidth / 2, y = it.screenHeight - 150f)
            ) 
        }
    }

    fun resetToStart() {
        _gameState.update { it.copy(phase = GamePhase.START) }
    }

    fun update(dtNanos: Long) {
        val currentState = _gameState.value
        if (currentState.phase != GamePhase.PLAYING) return

        // Auto shoot
        val now = System.currentTimeMillis()
        val autoCooldown = if (currentState.activePowerUp?.type == PowerUpType.RAPID_FIRE) 150L else 400L
        if (now - lastShootTime > autoCooldown) {
            shoot()
        }

        _gameState.update { state ->
            // Update PowerUp timer
            val updatedActivePowerUp = state.activePowerUp?.let {
                if (it.timeRemaining > 0) it.copy(timeRemaining = it.timeRemaining - 1) else null
            }

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

            // PowerUps falling
            val newPowerUps = state.powerUps.map { it.copy(y = it.y + it.speed) }
                .filter { it.y < state.screenHeight + 50f }

            // Move formation
            val levelBonus = (state.level - 1) * 0.5f
            formationX += alienMoveDirection * (2f + levelBonus)
            val formationWidth = (8 - 1) * spacing
            if (formationX < 50f || formationX + formationWidth > state.screenWidth - 50f) {
                alienMoveDirection *= -1
                formationX = formationX.coerceIn(50f, state.screenWidth - 50f - formationWidth)
            }

            var nextAliens = state.aliens.map { alien ->
                if (alien.isAttacking) {
                    val speedMultiplier = when(alien.type) {
                        AlienType.FAST -> 1.5f
                        AlienType.BOSS -> 0.8f
                        else -> 1.0f
                    }
                    val phaseInc = 0.05f * (if (alien.type == AlienType.FAST) 1.5f else 1.0f)
                    val phase = alien.attackPhase + phaseInc
                    
                    val freq = if (alien.type == AlienType.FAST) 2f else 1f
                    val amp = if (alien.type == AlienType.BOSS) 20f else 10f
                    
                    val diveX = alien.x + kotlin.math.sin(phase * freq) * amp
                    val diveY = alien.y + 12f * speedMultiplier
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

            // Attack logic
            val attackChance = 2 + state.level
            if (Random.nextInt(100) < attackChance && nextAliens.any { !it.isAttacking && it.y >= 150f }) {
                val index = nextAliens.indices.filter { i -> !nextAliens[i].isAttacking && nextAliens[i].y >= 150f }.random()
                nextAliens[index] = nextAliens[index].copy(isAttacking = true)
            }
            
            var updatedAlienBullets = newAlienBullets
            val shootChance = 3 + state.level
            if (Random.nextInt(100) < shootChance && nextAliens.isNotEmpty()) {
                val shooter = nextAliens.random()
                if (shooter.y > 0) {
                    updatedAlienBullets = updatedAlienBullets + Bullet(shooter.x, shooter.y + shooter.height / 2)
                }
            }

            val finalBullets = newBullets.toMutableList()
            val currentExplosions = newExplosions.toMutableList()
            val currentPowerUps = newPowerUps.toMutableList()
            var scoreGain = 0
            var newLives = state.lives
            var nextPhase = state.phase
            var nextLevel = state.level
            var nextActivePowerUp = updatedActivePowerUp

            val bulletsToRemove = mutableListOf<Bullet>()
            val aliensToRemove = mutableListOf<Alien>()

            for (bullet in finalBullets) {
                val hitAlienIndex = nextAliens.indexOfFirst { it.getRect().overlaps(bullet.getRect()) }
                if (hitAlienIndex != -1) {
                    val hitAlien = nextAliens[hitAlienIndex]
                    bulletsToRemove.add(bullet)
                    val updatedHealth = hitAlien.health - 1
                    if (updatedHealth <= 0) {
                        aliensToRemove.add(hitAlien)
                        scoreGain += if (hitAlien.type == AlienType.BOSS) 500 else 100
                        currentExplosions.add(Explosion(hitAlien.x, hitAlien.y, hitAlien.color))
                        
                        // Drop powerup
                        if (Random.nextInt(100) < 10) {
                            currentPowerUps.add(PowerUp(hitAlien.x, hitAlien.y, PowerUpType.values().random()))
                        }
                    } else {
                        nextAliens[hitAlienIndex] = hitAlien.copy(health = updatedHealth)
                    }
                }
            }

            nextAliens.removeAll(aliensToRemove)
            finalBullets.removeAll(bulletsToRemove)
            
            // Collect powerups
            val collectedPowerUps = currentPowerUps.filter { it.getRect().overlaps(state.ship.getRect()) }
            if (collectedPowerUps.isNotEmpty()) {
                nextActivePowerUp = ActivePowerUp(collectedPowerUps.last().type)
                currentPowerUps.removeAll(collectedPowerUps)
            }

            val hitByBullet = updatedAlienBullets.find { it.getRect().overlaps(state.ship.getRect()) }
            val hitByAlien = nextAliens.find { it.getRect().overlaps(state.ship.getRect()) }

            if (hitByBullet != null || hitByAlien != null) {
                if (nextActivePowerUp?.type == PowerUpType.SHIELD) {
                    nextActivePowerUp = null // Consume shield
                    if (hitByBullet != null) updatedAlienBullets = updatedAlienBullets - hitByBullet
                } else {
                    newLives -= 1
                    currentExplosions.add(Explosion(state.ship.x, state.ship.y, Color.White, radius = 20f))
                    updatedAlienBullets = emptyList()
                    if (newLives <= 0) {
                        nextPhase = GamePhase.GAME_OVER
                    }
                }
            }

            if (nextAliens.isEmpty() && state.aliens.isNotEmpty()) {
                nextLevel += 1
                nextAliens = createAliens(state.screenWidth).toMutableList()
            }

            state.copy(
                bullets = finalBullets,
                alienBullets = updatedAlienBullets,
                aliens = nextAliens,
                stars = newStars,
                explosions = currentExplosions,
                powerUps = currentPowerUps,
                activePowerUp = nextActivePowerUp,
                score = state.score + scoreGain,
                lives = newLives,
                level = nextLevel,
                phase = nextPhase
            )
        }
    }
}
