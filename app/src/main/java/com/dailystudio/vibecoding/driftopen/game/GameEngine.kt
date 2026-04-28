package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.*
import com.dailystudio.vibecoding.driftopen.game.formations.FormationGenerator
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.math.sqrt

class GameEngine(private val soundManager: SoundManager? = null) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val cheatManager = CheatManager()
    private var lastAttackTime = 0L

    init {
    }

    private fun getScale(level: Int): Float {
        return (1.0f / sqrt(level.toFloat() / 15f)).coerceAtMost(1.0f).coerceAtLeast(0.4f)
    }

    fun recordStartScreenTap() {
        if (cheatManager.onStartScreenTap()) {
            _gameState.update { it.copy(phase = GamePhase.CHEAT, cheatCodeInput = "", cheatMessage = "ENTER CODE") }
            cheatManager.resetTaps()
        }
    }

    fun handleCheatInput(input: String) {
        _gameState.update { state ->
            val newInput = state.cheatCodeInput + input
            val validation = cheatManager.validateCode(newInput)
            
            if (validation != null) {
                val (cheatType, extra) = validation
                var nextState = state.copy(
                    activeCheats = state.activeCheats + cheatType,
                    cheatCodeInput = "",
                    cheatMessage = "ACCESS GRANTED"
                )
                
                // Apply immediate effects
                nextState = when (cheatType) {
                    CheatType.LIVES_99 -> nextState.copy(lives = 99)
                    CheatType.LEVEL_SELECT -> {
                        val newLevel = extra ?: state.level
                        nextState.copy(level = newLevel)
                    }
                    else -> nextState
                }
                
                cheatManager.activateCheat(cheatType)
                nextState
            } else {
                // Specialized feedback for level select attempts
                val message = when {
                    // Only show invalid if it's a completed pattern that failed validation
                    newInput.startsWith("X") && newInput.endsWith("X") && newInput.length > 1 -> {
                        if (newInput.length == 4) "INVALID LEVEL" else "INVALID CODE"
                    }
                    newInput.length >= 5 && newInput.startsWith("X") -> "INVALID CODE"
                    newInput.length >= 5 -> "INVALID CODE"
                    else -> "TYPING..."
                }
                
                // If it was an invalid level/code, clear input after showing message
                if (message != "TYPING...") {
                    state.copy(cheatCodeInput = "", cheatMessage = message)
                } else {
                    val limitedInput = if (newInput.length > 10) newInput.takeLast(10) else newInput
                    state.copy(cheatCodeInput = limitedInput, cheatMessage = message)
                }
            }
        }
    }

    fun clearCheatInput() {
        _gameState.update { it.copy(cheatCodeInput = "", cheatMessage = "CLEARED") }
    }

    fun closeCheatConsole() {
        _gameState.update { it.copy(phase = GamePhase.START, cheatCodeInput = "", cheatMessage = "") }
    }

    fun setHighScore(score: Int) {
        _gameState.update { it.copy(highScore = score) }
    }

    private var lastShootTime = 0L
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

    private fun getInitialFormationX(screenWidth: Float, level: Int): Float {
        val formationType = when (level % 5) {
            1 -> FormationType.GRID
            2 -> FormationType.V_SHAPE
            3 -> FormationType.DIAMOND
            4 -> FormationType.CIRCLE
            0 -> FormationType.HEART
            else -> FormationType.GRID
        }

        val baseScale = getScale(level)
        val maxCols = when (formationType) {
            FormationType.GRID -> (6 + level / 2).toFloat()
            FormationType.V_SHAPE -> ((4 + level / 2) * 2 - 1).toFloat()
            FormationType.DIAMOND -> ((3 + level / 2) * 2 + 1).toFloat()
            FormationType.CIRCLE -> (2 + level * 0.4f) * 2f
            FormationType.HEART -> {
                val heartScale = (spacing / 8) * (1f + level * 0.05f).coerceAtMost(2.5f)
                (32 * heartScale) / spacing
            }
            FormationType.RANDOM_SCATTER -> screenWidth / spacing
        }
        
        val predictedWidth = maxCols * spacing * baseScale
        val widthScale = if (predictedWidth > screenWidth * 0.9f) {
            (screenWidth * 0.9f) / predictedWidth
        } else 1.0f
        
        val finalScale = baseScale * widthScale
        val currentSpacing = spacing * finalScale

        return when (formationType) {
            FormationType.GRID -> (screenWidth - (6 + (level / 2) - 1) * currentSpacing) / 2
            else -> screenWidth / 2
        }
    }

    private fun createAliens(screenWidth: Float, level: Int): List<Alien> {
        val cappedLevel = level.coerceIn(1, 99)
        if (cappedLevel % 5 == 0) {
            // Superboss level
            val health = 30 + cappedLevel * 2
            return listOf(
                Alien(
                    id = 0,
                    x = screenWidth / 2,
                    y = 150f,
                    offsetX = 0f,
                    offsetY = 0f,
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

        val formationType = when (cappedLevel % 5) {
            1 -> FormationType.GRID
            2 -> FormationType.V_SHAPE
            3 -> FormationType.DIAMOND
            4 -> FormationType.CIRCLE
            0 -> FormationType.HEART
            else -> FormationType.GRID
        }

        return FormationGenerator.generateFormation(
            type = formationType,
            level = cappedLevel,
            screenWidth = screenWidth,
            spacing = spacing,
            startY = 150f
        )
    }

    private fun spawnAliens(screenWidth: Float) {
        val level = _gameState.value.level
        val aliens = createAliens(screenWidth, level)
        val initialX = getInitialFormationX(screenWidth, level)
        _gameState.update { it.copy(aliens = aliens, formationX = initialX, alienMoveDirection = 1f, superbossDirection = 1f) }
    }

    fun moveShipRelative(deltaX: Float, deltaY: Float) {
        _gameState.update { state ->
            val newX = (state.ship.x + deltaX).coerceIn(state.ship.width / 2, state.screenWidth - state.ship.width / 2)
            val newY = (state.ship.y + deltaY).coerceIn(state.screenHeight * 0.4f, state.screenHeight - state.ship.height / 2)
            state.copy(ship = state.ship.copy(x = newX, y = newY))
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
                val visualY = s.ship.y - 120f
                val bulletY = visualY - s.ship.height / 2
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
            soundManager?.playSound("shoot")
            lastShootTime = now
        }
    }

    fun startGame() {
        _gameState.update { 
            val startLevel = if (it.activeCheats.contains(CheatType.LEVEL_SELECT)) it.level else 1
            val startLives = if (it.activeCheats.contains(CheatType.LIVES_99)) it.lives else 5
            val initialX = getInitialFormationX(it.screenWidth, startLevel)
            it.copy(
                phase = GamePhase.PLAYING,
                lives = startLives,
                score = 0,
                level = startLevel,
                formationX = initialX,
                alienMoveDirection = 1f,
                superbossDirection = 1f,
                bullets = emptyList(),
                alienBullets = emptyList(),
                explosions = emptyList(),
                powerUps = emptyList(),
                activePowerUp = null,
                aliens = createAliens(it.screenWidth, startLevel),
                ship = it.ship.copy(x = it.screenWidth / 2, y = it.screenHeight - 150f, invincibilityFrames = 0)
            ) 
        }
    }

    fun resetToStart() {
        cheatManager.resetAll()
        _gameState.update { it.copy(phase = GamePhase.START, activeCheats = emptySet()) }
    }

    fun pauseGame() {
        _gameState.update { 
            if (it.phase == GamePhase.PLAYING) it.copy(phase = GamePhase.PAUSED) else it
        }
    }

    fun resumeGame() {
        _gameState.update { 
            if (it.phase == GamePhase.PAUSED) it.copy(phase = GamePhase.PLAYING) else it
        }
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
            // Decay screen shake
            val nextShake = (state.screenShakeIntensity * 0.9f).let { if (it < 0.1f) 0f else it }

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
            val formationSpeed = 4f // Static speed as requested
            var nextMoveDirection = state.alienMoveDirection
            var nextFormationX = state.formationX + nextMoveDirection * formationSpeed
            var nextSuperbossDirection = state.superbossDirection
            
            // Boundary check for formation
            val formationAliens = state.aliens.filter { !it.isAttacking && it.type != AlienType.SUPERBOSS }
            if (formationAliens.isNotEmpty()) {
                val minX = formationAliens.minOf { it.x }
                val maxX = formationAliens.maxOf { it.x }
                val centerX = (minX + maxX) / 2
                
                // Allow the formation to move until its center hits the screen edge
                // This allows wide formations to move partially off-screen for a better sweep
                if (centerX < 0f && nextMoveDirection < 0) {
                    nextMoveDirection = 1f
                } else if (centerX > state.screenWidth && nextMoveDirection > 0) {
                    nextMoveDirection = -1f
                }
            }
            
            // Determine ship skin
            val shipSkin = when (((state.level - 1) / 2) % 4) {
                0 -> "default"
                1 -> "heavy"
                2 -> "stealth"
                3 -> "retro"
                else -> "default"
            }
            val baseShip = updatedShip.copy(skinId = shipSkin)

            var nextAliens = state.aliens.map { alien ->
                if (alien.type == AlienType.SUPERBOSS) {
                    val healthPct = alien.health.toFloat() / alien.maxHealth
                    val speedScale = 1f + (1f - healthPct) * 1.5f
                    // Superboss moves independently
                    var nx = alien.x + nextSuperbossDirection * 4f * speedScale
                    if (nx < 150f && nextSuperbossDirection < 0) {
                        nextSuperbossDirection = 1f
                        nx = 150f
                    } else if (nx > state.screenWidth - 150f && nextSuperbossDirection > 0) {
                        nextSuperbossDirection = -1f
                        nx = state.screenWidth - 150f
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
                        alien.copy(y = -50f, isAttacking = false, attackPhase = 0f, readyTime = System.currentTimeMillis() + 1000L)
                    } else {
                        alien.copy(x = diveX, y = diveY, attackPhase = phase)
                    }
                } else {
                    val targetX = nextFormationX + alien.offsetX
                    val targetY = 150f + alien.offsetY
                    
                    val dx = targetX - alien.x
                    val dy = targetY - alien.y
                    
                    val newX = if (kotlin.math.abs(dx) < 2f) targetX else alien.x + dx * 0.1f
                    val newY = if (kotlin.math.abs(dy) < 2f) targetY else alien.y + dy * 0.1f
                    
                    alien.copy(x = newX, y = newY)
                }
            }.toMutableList()

            // Attack logic
            val nowMillis = System.currentTimeMillis()
            val attackInterval = (1500L - (state.level / 5) * 200L).coerceAtLeast(600L)
            val maxConcurrentAttackers = (2 + state.level / 3).coerceAtMost(8)
            val currentAttackers = nextAliens.count { it.isAttacking }

            if (nowMillis - lastAttackTime > attackInterval && 
                currentAttackers < maxConcurrentAttackers &&
                nextAliens.any { it.type != AlienType.SUPERBOSS && !it.isAttacking && it.y >= 150f && nowMillis >= it.readyTime }) {
                
                val candidates = nextAliens.indices.filter { i -> 
                    val a = nextAliens[i]
                    a.type != AlienType.SUPERBOSS && !a.isAttacking && a.y >= 150f && nowMillis >= a.readyTime 
                }
                if (candidates.isNotEmpty()) {
                    val index = candidates.random()
                    nextAliens[index] = nextAliens[index].copy(isAttacking = true)
                    lastAttackTime = nowMillis
                }
            }
            
            var updatedAlienBullets = newAlienBullets
            val shootChance = (3 + state.level * 0.5f).toInt()
            if (Random.nextInt(100) < shootChance && nextAliens.isNotEmpty()) {
                val shooter = nextAliens.random()
                if (shooter.y > 0) {
                    if (shooter.type == AlienType.SUPERBOSS) {
                        val healthPct = shooter.health.toFloat() / shooter.maxHealth
                        if (healthPct < 0.5f) {
                            // 5-way spread
                            updatedAlienBullets = updatedAlienBullets + listOf(
                                Bullet(shooter.x - 60f, shooter.y + 40f, vx = -4f, speed = 8f),
                                Bullet(shooter.x - 30f, shooter.y + 60f, vx = -2f, speed = 10f),
                                Bullet(shooter.x, shooter.y + 80f, vx = 0f, speed = 12f),
                                Bullet(shooter.x + 30f, shooter.y + 60f, vx = 2f, speed = 10f),
                                Bullet(shooter.x + 60f, shooter.y + 40f, vx = 4f, speed = 8f)
                            )
                        } else {
                            // 3-way spread
                            updatedAlienBullets = updatedAlienBullets + listOf(
                                Bullet(shooter.x - 40f, shooter.y + 40f, speed = 10f),
                                Bullet(shooter.x, shooter.y + 60f, speed = 12f),
                                Bullet(shooter.x + 40f, shooter.y + 40f, speed = 10f)
                            )
                        }
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
                            soundManager?.playSound("explosion")
                            scoreGain += when(hitAlien.type) {
                                AlienType.SUPERBOSS -> 5000
                                AlienType.BOSS -> 500
                                else -> 100
                            }
                            currentExplosions.add(Explosion(hitAlien.x, hitAlien.y, hitAlien.color))
                            
                            // Death drop
                            if (Random.nextInt(100) < 15) {
                                currentPowerUps.add(PowerUp(hitAlien.x, hitAlien.y, PowerUpType.values().random()))
                            }
                        } else if (hitAlien.type == AlienType.SUPERBOSS && Random.nextInt(100) < 30) {
                            // Mid-hit drop for Superboss
                            currentPowerUps.add(PowerUp(hitAlien.x, hitAlien.y, PowerUpType.values().random()))
                        }
                    }
                }
            }

            nextAliens.removeAll { it.health <= 0 }
            finalBullets.removeAll(bulletsToRemove)
            
            // Add lives for score
            val oldScore = state.score
            val isCheated = state.activeCheats.isNotEmpty()
            val scoreGainFinal = if (isCheated) 0 else scoreGain
            val newScoreTotal = oldScore + scoreGainFinal
            
            if (!isCheated) {
                val livesFromScore = (newScoreTotal / 5000) - (oldScore / 5000)
                newLives += livesFromScore
            }
            
            // Collect powerups
            val collectedPowerUps = currentPowerUps.filter { it.getRect().overlaps(baseShip.getVisualRect()) }
            if (collectedPowerUps.isNotEmpty()) {
                soundManager?.playSound("powerup")
                nextActivePowerUp = ActivePowerUp(collectedPowerUps.last().type)
                currentPowerUps.removeAll(collectedPowerUps)
            }

            // Player hit detection
            val hitByBullet = if (baseShip.invincibilityFrames == 0) updatedAlienBullets.find { it.getRect().overlaps(baseShip.getVisualRect()) } else null
            val hitByAlien = if (baseShip.invincibilityFrames == 0) nextAliens.find { it.getRect().overlaps(baseShip.getVisualRect()) } else null

            var finalShip = baseShip
            var finalShake = nextShake
            if (hitByBullet != null || hitByAlien != null) {
                if (nextActivePowerUp?.type == PowerUpType.SHIELD) {
                    nextActivePowerUp = null // Consume shield
                    soundManager?.playSound("hit")
                    if (hitByBullet != null) updatedAlienBullets = updatedAlienBullets - hitByBullet
                } else if (state.activeCheats.contains(CheatType.INVINCIBILITY) || nextActivePowerUp?.type == PowerUpType.INVINCIBILITY) {
                    // Cheat or Power-up active: don't lose lives, but still show feedback
                    finalShake = 10f
                    soundManager?.playSound("hit")
                    currentExplosions.add(Explosion(baseShip.x, baseShip.y - 120f, Color.White, radius = 20f))
                    finalShip = baseShip.copy(invincibilityFrames = 40)
                } else {
                    newLives -= 1
                    finalShake = 15f
                    soundManager?.playSound("hit")
                    currentExplosions.add(Explosion(baseShip.x, baseShip.y - 120f, Color.White, radius = 20f))
                    updatedAlienBullets = emptyList()
                    finalShip = baseShip.copy(invincibilityFrames = 60) // 1 second invincibility
                    
                    if (hitByAlien != null && hitByAlien.type != AlienType.SUPERBOSS) {
                        nextAliens.remove(hitByAlien)
                    }
                    
                    if (newLives <= 0) {
                        nextPhase = GamePhase.GAME_OVER
                    }
                }
            }

            if (nextAliens.isEmpty() && state.aliens.isNotEmpty()) {
                nextLevel = (nextLevel + 1).coerceAtMost(99)
                nextAliens = createAliens(state.screenWidth, nextLevel).toMutableList()
                nextFormationX = getInitialFormationX(state.screenWidth, nextLevel)
                nextMoveDirection = 1f
                nextSuperbossDirection = 1f
            }

            val nextState = state.copy(
                ship = finalShip,
                bullets = finalBullets,
                alienBullets = updatedAlienBullets,
                aliens = nextAliens,
                stars = newStars,
                explosions = currentExplosions,
                powerUps = currentPowerUps,
                activePowerUp = nextActivePowerUp,
                score = state.score + scoreGainFinal,
                lives = newLives,
                level = nextLevel,
                formationX = nextFormationX,
                alienMoveDirection = nextMoveDirection,
                superbossDirection = nextSuperbossDirection,
                screenShakeIntensity = finalShake,
                phase = nextPhase
            )
            
            if (!isCheated && nextState.score > nextState.highScore) {
                nextState.copy(highScore = nextState.score)
            } else {
                nextState
            }
        }
    }
}
