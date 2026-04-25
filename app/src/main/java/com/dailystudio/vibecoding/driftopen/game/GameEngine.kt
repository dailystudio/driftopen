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
    private val spacing = 60f

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

    private fun createAliens(screenWidth: Float, level: Int): List<Alien> {
        if (level % 5 == 0) {
            // Superboss level
            val health = 30 + level * 2
            return listOf(
                Alien(
                    id = 0,
                    x = screenWidth / 2,
                    y = 150f,
                    gridCol = 0,
                    gridRow = 0,
                    width = 200f,
                    height = 150f,
                    color = Color.White,
                    type = AlienType.SUPERBOSS,
                    health = health,
                    maxHealth = health
                )
            )
        }

        val aliens = mutableListOf<Alien>()
        val rows = 4
        val cols = 8
        formationX = (screenWidth - (cols - 1) * spacing) / 2
        val startY = 150f

        var idCounter = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val finalType = if (level == 1) AlienType.NORMAL
                else if (level == 2) {
                    if (row == 0) AlienType.FAST else AlienType.NORMAL
                } else {
                    if (row == 0) AlienType.BOSS
                    else if (row == 1) AlienType.FAST
                    else AlienType.NORMAL
                }

                val color = when (finalType) {
                    AlienType.BOSS -> Color.Magenta
                    AlienType.FAST -> Color.Yellow
                    AlienType.NORMAL -> Color.Red
                    else -> Color.Red
                }
                val health = if (finalType == AlienType.BOSS) 3 else 1

                aliens.add(
                    Alien(
                        id = idCounter++,
                        x = formationX + col * spacing,
                        y = startY + row * spacing,
                        gridCol = col,
                        gridRow = row,
                        type = finalType,
                        color = color,
                        health = health,
                        maxHealth = health
                    )
                )
            }
        }
        return aliens
    }

    private fun spawnAliens(screenWidth: Float) {
        val level = _gameState.value.level
        val aliens = createAliens(screenWidth, level)
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
                val newBullets = when (s.activePowerUp?.type) {
                    PowerUpType.DOUBLE_FIRE -> {
                        s.bullets + listOf(
                            Bullet(s.ship.x - 15f, bulletY, speed = 25f),
                            Bullet(s.ship.x + 15f, bulletY, speed = 25f)
                        )
                    }
                    PowerUpType.SPREAD_SHOT -> {
                        s.bullets + listOf(
                            Bullet(s.ship.x, bulletY, vx = -8f, speed = 22f, type = BulletType.SPREAD),
                            Bullet(s.ship.x, bulletY, vx = 0f, speed = 25f, type = BulletType.SPREAD),
                            Bullet(s.ship.x, bulletY, vx = 8f, speed = 22f, type = BulletType.SPREAD)
                        )
                    }
                    PowerUpType.LASER_BEAM -> {
                        s.bullets + Bullet(s.ship.x, bulletY, speed = 40f, height = 60f, width = 12f, type = BulletType.LASER, pierceCount = 5)
                    }
                    PowerUpType.HOMING_MISSILES -> {
                        s.bullets + Bullet(s.ship.x, bulletY, speed = 12f, type = BulletType.HOMING)
                    }
                    PowerUpType.EXPLOSIVE_BOMBS -> {
                        s.bullets + Bullet(s.ship.x, bulletY, speed = 15f, width = 20f, height = 20f, type = BulletType.BOMB)
                    }
                    else -> {
                        s.bullets + Bullet(s.ship.x, bulletY, speed = 25f)
                    }
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
                lives = 5,
                score = 0,
                level = 1,
                bullets = emptyList(),
                alienBullets = emptyList(),
                explosions = emptyList(),
                powerUps = emptyList(),
                activePowerUp = null,
                aliens = createAliens(it.screenWidth, 1),
                ship = it.ship.copy(x = it.screenWidth / 2, y = it.screenHeight - 150f, invincibilityFrames = 0)
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

            // Update ship invincibility
            val updatedShip = if (state.ship.invincibilityFrames > 0) {
                state.ship.copy(invincibilityFrames = state.ship.invincibilityFrames - 1)
            } else state.ship

            val newExplosions = state.explosions.map { 
                it.copy(life = it.life - 1, radius = it.radius + 2f, alpha = it.alpha * 0.9f) 
            }.filter { it.life > 0 }

            val newBullets = state.bullets.map { bullet ->
                var nx = bullet.x + bullet.vx
                var ny = bullet.y - bullet.speed
                var nvx = bullet.vx
                var ntargetId = bullet.targetId

                if (bullet.type == BulletType.HOMING) {
                    val target = state.aliens.find { it.id == bullet.targetId } 
                        ?: state.aliens.minByOrNull { kotlin.math.hypot(it.x - bullet.x, it.y - bullet.y) }
                    
                    target?.let {
                        ntargetId = it.id
                        val dx = it.x - bullet.x
                        val dy = it.y - bullet.y
                        val dist = kotlin.math.hypot(dx, dy)
                        if (dist > 0) {
                            nvx = (nvx + (dx / dist) * 2f).coerceIn(-10f, 10f)
                        }
                    }
                }
                bullet.copy(x = nx, y = ny, vx = nvx, targetId = ntargetId)
            }.filter { it.y > -100f && it.x > -100f && it.x < state.screenWidth + 100f }
            
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
            val levelBonus = (state.level - 1) * 0.3f
            formationX += alienMoveDirection * (2f + levelBonus)
            val formationWidth = (6 - 1) * spacing
            if (formationX < 50f || formationX + formationWidth > state.screenWidth - 50f) {
                alienMoveDirection *= -1
                formationX = formationX.coerceIn(50f, state.screenWidth - 50f - formationWidth)
            }

            var nextAliens = state.aliens.map { alien ->
                if (alien.type == AlienType.SUPERBOSS) {
                    var nx = alien.x + alienMoveDirection * 4f
                    if (nx < 150f || nx > state.screenWidth - 150f) {
                        alienMoveDirection *= -1
                        nx = alien.x + alienMoveDirection * 4f
                    }
                    alien.copy(x = nx)
                } else if (alien.isAttacking) {
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
            val attackChance = (2 + state.level * 0.5f).toInt()
            if (Random.nextInt(100) < attackChance && nextAliens.any { it.type != AlienType.SUPERBOSS && !it.isAttacking && it.y >= 150f }) {
                val candidates = nextAliens.indices.filter { i -> nextAliens[i].type != AlienType.SUPERBOSS && !nextAliens[i].isAttacking && nextAliens[i].y >= 150f }
                if (candidates.isNotEmpty()) {
                    val index = candidates.random()
                    nextAliens[index] = nextAliens[index].copy(isAttacking = true)
                }
            }
            
            var updatedAlienBullets = newAlienBullets
            val shootChance = (3 + state.level * 0.5f).toInt()
            if (Random.nextInt(100) < shootChance && nextAliens.isNotEmpty()) {
                val shooter = nextAliens.random()
                if (shooter.y > 0) {
                    if (shooter.type == AlienType.SUPERBOSS) {
                        updatedAlienBullets = updatedAlienBullets + listOf(
                            Bullet(shooter.x - 40f, shooter.y + 40f, speed = 10f),
                            Bullet(shooter.x, shooter.y + 60f, speed = 12f),
                            Bullet(shooter.x + 40f, shooter.y + 40f, speed = 10f)
                        )
                    } else {
                        updatedAlienBullets = updatedAlienBullets + Bullet(shooter.x, shooter.y + shooter.height / 2)
                    }
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

            for (bullet in finalBullets) {
                val hitAlienIndex = nextAliens.indexOfFirst { it.getRect().overlaps(bullet.getRect()) }
                if (hitAlienIndex != -1) {
                    val hitAlien = nextAliens[hitAlienIndex]
                    
                    if (bullet.type == BulletType.BOMB) {
                        bulletsToRemove.add(bullet)
                        currentExplosions.add(Explosion(bullet.x, bullet.y, Color.Red, radius = 50f, life = 30))
                        // AOE Damage
                        nextAliens.forEachIndexed { idx, a ->
                            val dist = kotlin.math.hypot(a.x - bullet.x, a.y - bullet.y)
                            if (dist < 150f) {
                                val updatedA = a.copy(health = a.health - 5)
                                nextAliens[idx] = updatedA
                            }
                        }
                    } else {
                        val updatedHealth = hitAlien.health - 1
                        val newHitAlien = hitAlien.copy(health = updatedHealth)
                        nextAliens[hitAlienIndex] = newHitAlien
                        
                        if (bullet.type == BulletType.LASER) {
                            val newBullet = bullet.copy(pierceCount = bullet.pierceCount - 1)
                            if (newBullet.pierceCount <= 0) bulletsToRemove.add(bullet)
                        } else {
                            bulletsToRemove.add(bullet)
                        }

                        if (updatedHealth <= 0) {
                            scoreGain += when(hitAlien.type) {
                                AlienType.SUPERBOSS -> 5000
                                AlienType.BOSS -> 500
                                else -> 100
                            }
                            currentExplosions.add(Explosion(hitAlien.x, hitAlien.y, hitAlien.color))
                            
                            // Drop powerup
                            if (Random.nextInt(100) < 15) {
                                val types = PowerUpType.values()
                                currentPowerUps.add(PowerUp(hitAlien.x, hitAlien.y, types.random()))
                            }
                        }
                    }
                }
            }

            nextAliens.removeAll { it.health <= 0 }
            finalBullets.removeAll(bulletsToRemove)
            
            // Collect powerups
            val collectedPowerUps = currentPowerUps.filter { it.getRect().overlaps(state.ship.getRect()) }
            if (collectedPowerUps.isNotEmpty()) {
                nextActivePowerUp = ActivePowerUp(collectedPowerUps.last().type)
                currentPowerUps.removeAll(collectedPowerUps)
            }

            // Player hit detection
            val hitByBullet = if (updatedShip.invincibilityFrames == 0) updatedAlienBullets.find { it.getRect().overlaps(updatedShip.getRect()) } else null
            val hitByAlien = if (updatedShip.invincibilityFrames == 0) nextAliens.find { it.getRect().overlaps(updatedShip.getRect()) } else null

            var finalShip = updatedShip
            if (hitByBullet != null || hitByAlien != null) {
                if (nextActivePowerUp?.type == PowerUpType.SHIELD) {
                    nextActivePowerUp = null // Consume shield
                    if (hitByBullet != null) updatedAlienBullets = updatedAlienBullets - hitByBullet
                } else {
                    newLives -= 1
                    currentExplosions.add(Explosion(updatedShip.x, updatedShip.y, Color.White, radius = 20f))
                    updatedAlienBullets = emptyList()
                    finalShip = updatedShip.copy(invincibilityFrames = 60) // 1 second invincibility
                    
                    if (hitByAlien != null && hitByAlien.type != AlienType.SUPERBOSS) {
                        nextAliens.remove(hitByAlien)
                    }
                    
                    if (newLives <= 0) {
                        nextPhase = GamePhase.GAME_OVER
                    }
                }
            }

            if (nextAliens.isEmpty() && state.aliens.isNotEmpty()) {
                nextLevel += 1
                nextAliens = createAliens(state.screenWidth, nextLevel).toMutableList()
            }

            state.copy(
                ship = finalShip,
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
