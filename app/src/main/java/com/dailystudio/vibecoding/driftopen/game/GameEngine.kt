package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.*
import com.dailystudio.vibecoding.driftopen.game.formations.FormationGenerator
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.math.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GameEngine(private val soundManager: SoundManager? = null) {
    companion object {
        private const val SCORE_PER_EXTRA_LIFE = 10000
        private const val SCORE_PER_BOMB = 50000
    }

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastAttackTime = 0L

    init {
        scope.launch {
            CheatManager.activatedCheats.collect { cheats ->
                _gameState.update { it.copy(activeCheats = cheats) }
                
                // Handle immediate effects of newly activated cheats
                if (cheats.contains(CheatType.LIVES_99)) {
                    _gameState.update { it.copy(lives = 99) }
                }
                if (cheats.contains(CheatType.BOMBS_99)) {
                    _gameState.update { it.copy(bombs = 99) }
                }
            }
        }
        scope.launch {
            CheatManager.selectedLevel.collect { level ->
                if (level != null) {
                    _gameState.update { it.copy(level = level) }
                }
            }
        }
    }

    private fun getScale(level: Int): Float {
        return (1.0f / sqrt(level.toFloat() / 15f)).coerceAtMost(1.0f).coerceAtLeast(0.4f)
    }

    fun recordStartScreenTap() {
        if (CheatManager.onStartScreenTap()) {
            scope.launch {
                _events.emit(GameEvent.OPEN_CHEAT_CONSOLE)
            }
            CheatManager.resetTaps()
        }
    }

    fun openHelp() {
        scope.launch {
            _events.emit(GameEvent.OPEN_HELP)
        }
    }

    fun openCollection() {
        scope.launch {
            _events.emit(GameEvent.OPEN_COLLECTION)
        }
    }

    fun openSettings() {
        scope.launch {
            _events.emit(GameEvent.OPEN_SETTINGS)
        }
    }

    fun setDifficulty(difficulty: String) {
        _gameState.update { it.copy(difficulty = difficulty) }
    }

    fun setHighScore(score: Int) {
        _gameState.update { it.copy(highScore = score) }
    }

    fun debugSetScore(score: Int) {
        _gameState.update { it.copy(score = score) }
    }

    fun debugAddScore(gain: Int) {
        _gameState.update { state ->
            val newScoreTotal = state.score + gain
            val extraLives = (newScoreTotal / SCORE_PER_EXTRA_LIFE) - (state.score / SCORE_PER_EXTRA_LIFE)
            val extraBombs = (newScoreTotal / SCORE_PER_BOMB) - (state.score / SCORE_PER_BOMB)
            state.copy(
                score = newScoreTotal,
                lives = state.lives + extraLives,
                bombs = state.bombs + extraBombs
            )
        }
    }

    fun debugSetLives(lives: Int) {
        _gameState.update { it.copy(lives = lives) }
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
        val cappedLevel = level.coerceIn(1, 100)
        if (cappedLevel % 5 == 0) {
            // Superboss level
            val health = 30 + cappedLevel * 2
            val superbossIndex = ((cappedLevel - 1) / 5).coerceIn(0, 19)
            val skinId = "superboss_$superbossIndex"
            val patternId = Random.nextInt(5)

            return listOf(
                Alien(
                    id = 0,
                    x = screenWidth / 2,
                    y = 250f,
                    offsetX = 0f,
                    offsetY = 0f,
                    gridCol = 0,
                    gridRow = 0,
                    width = 200f,
                    height = 150f,
                    color = Color.White,
                    type = AlienType.SUPERBOSS,
                    health = health,
                    maxHealth = health,
                    skinId = skinId,
                    patternId = patternId
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
            startY = 250f
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

    fun useBomb() {
        if (_gameState.value.bombs <= 0 || _gameState.value.phase != GamePhase.PLAYING) return

        _gameState.update { state ->
            val nextAliens = state.aliens.map { 
                if (it.isAttacking) it.copy(health = 0) else it 
            }
            state.copy(
                bombs = state.bombs - 1,
                aliens = nextAliens,
                alienBullets = emptyList(),
                screenShakeIntensity = 25f,
                bombEffectFrames = 40
            )
        }
        soundManager?.playSound("explosion")
    }

    fun startGame() {
        _gameState.update { 
            val startLevel = if (it.activeCheats.contains(CheatType.LEVEL_SELECT)) it.level else 1
            val startLives = if (it.activeCheats.contains(CheatType.LIVES_99)) it.lives else 5
            val startBombs = if (it.activeCheats.contains(CheatType.BOMBS_99)) it.bombs else 1
            val initialX = getInitialFormationX(it.screenWidth, startLevel)
            it.copy(
                phase = GamePhase.PLAYING,
                lives = startLives,
                bombs = startBombs,
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
        CheatManager.resetAll()
        _gameState.update { it.copy(phase = GamePhase.START) }
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

    private var lastShipSkin = "default"
    private var lastLevelForSkin = -1

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
            val nextShake = if (state.screenShakeIntensity > 0.1f) state.screenShakeIntensity * 0.9f else 0f
            val nextBombEffectFrames = if (state.bombEffectFrames > 0) state.bombEffectFrames - 1 else 0

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
                        ?: state.aliens.minByOrNull { hypot(it.x - bullet.x, it.y - bullet.y) }
                    
                    target?.let {
                        ntargetId = it.id
                        val dx = it.x - bullet.x
                        val dy = it.y - bullet.y
                        val dist = hypot(dx, dy)
                        if (dist > 0) {
                            nvx = (nvx + (dx / dist) * 2f).coerceIn(-10f, 10f)
                        }
                    }
                }
                bullet.copy(x = nx, y = ny, vx = nvx, targetId = ntargetId)
            }.filter { it.y > -100f && it.x > -100f && it.x < state.screenWidth + 100f }
            
            val newAlienBullets = state.alienBullets.map { bullet ->
                var nx = bullet.x + bullet.vx
                var ny = bullet.y - bullet.speed
                var nvx = bullet.vx

                if (bullet.type == BulletType.HOMING) {
                    val dx = state.ship.x - bullet.x
                    val dy = (state.ship.y - 120f) - bullet.y
                    val dist = hypot(dx, dy)
                    if (dist > 0) {
                        nvx = (nvx + (dx / dist) * 0.5f).coerceIn(-5f, 5f)
                    }
                }
                bullet.copy(x = nx, y = ny, vx = nvx)
            }.filter { it.y < state.screenHeight + 50f && it.y > -100f && it.x > -100f && it.x < state.screenWidth + 100f }

            val newStars = state.stars.map {
                val newY = it.y + it.speed
                if (newY > state.screenHeight) it.copy(y = 0f, x = Random.nextFloat() * state.screenWidth)
                else it.copy(y = newY)
            }

            // PowerUps falling
            val newPowerUps = state.powerUps.map { it.copy(y = it.y + it.speed) }
                .filter { it.y < state.screenHeight + 50f }

            // Move formation
            val formationSpeed = 4f
            var nextMoveDirection = state.alienMoveDirection
            var nextFormationX = state.formationX + nextMoveDirection * formationSpeed
            var nextSuperbossDirection = state.superbossDirection
            
            // Boundary check for formation
            var minX = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var hasFormationAliens = false

            for (alien in state.aliens) {
                if (!alien.isAttacking && alien.type != AlienType.SUPERBOSS) {
                    if (alien.x < minX) minX = alien.x
                    if (alien.x > maxX) maxX = alien.x
                    hasFormationAliens = true
                }
            }

            if (hasFormationAliens) {
                val centerX = (minX + maxX) / 2
                if (centerX < 0f && nextMoveDirection < 0) {
                    nextMoveDirection = 1f
                } else if (centerX > state.screenWidth && nextMoveDirection > 0) {
                    nextMoveDirection = -1f
                }
            }
            
            // Determine ship skin (cached)
            if (state.level != lastLevelForSkin) {
                lastShipSkin = when (((state.level - 1) / 2) % 4) {
                    0 -> "default"
                    1 -> "heavy"
                    2 -> "stealth"
                    3 -> "retro"
                    else -> "default"
                }
                lastLevelForSkin = state.level
            }
            val baseShip = updatedShip.copy(skinId = lastShipSkin)
            val shipRect = baseShip.getVisualRect()

            val nextAliens = mutableListOf<Alien>()
            var attackersCount = 0
            for (alien in state.aliens) {
                val updatedAlien = if (alien.type == AlienType.SUPERBOSS) {
                    val healthPct = alien.health.toFloat() / alien.maxHealth
                    val speedScale = 1f + (1f - healthPct) * 1.5f
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
                    attackersCount++
                    val speedMultiplier = when(alien.type) {
                        AlienType.FAST -> 1.5f
                        AlienType.BOSS -> 0.8f
                        else -> 1.0f
                    }
                    val phaseInc = 0.05f * (if (alien.type == AlienType.FAST) 1.5f else 1.0f)
                    val phase = alien.attackPhase + phaseInc
                    val freq = if (alien.type == AlienType.FAST) 2f else 1f
                    val amp = if (alien.type == AlienType.BOSS) 20f else 10f
                    val diveX = alien.x + sin(phase * freq) * amp
                    val diveY = alien.y + 12f * speedMultiplier
                    if (diveY > state.screenHeight + 50f) {
                        alien.copy(y = -50f, isAttacking = false, attackPhase = 0f, readyTime = System.currentTimeMillis() + 1000L)
                    } else {
                        alien.copy(x = diveX, y = diveY, attackPhase = phase)
                    }
                } else {
                    val targetX = nextFormationX + alien.offsetX
                    val targetY = 250f + alien.offsetY
                    val dx = targetX - alien.x
                    val dy = targetY - alien.y
                    val newX = if (abs(dx) < 2f) targetX else alien.x + dx * 0.1f
                    val newY = if (abs(dy) < 2f) targetY else alien.y + dy * 0.1f
                    alien.copy(x = newX, y = newY)
                }
                nextAliens.add(updatedAlien)
            }

            // Attack logic
            val nowMillis = System.currentTimeMillis()
            val alienCount = state.aliens.size
            val baseInterval = (1500L - (state.level / 5) * 200L).coerceAtLeast(600L)
            
            // Scale interval based on population: fewer aliens = longer wait between dives
            val populationMultiplier = when {
                alienCount <= 1 -> 4.0f
                alienCount <= 3 -> 2.5f
                alienCount <= 6 -> 1.5f
                else -> 1.0f
            }
            val attackInterval = (baseInterval * populationMultiplier).toLong()
            
            // Scale max attackers: ensure not everyone is diving at once when count is low
            val baseMaxAttackers = (2 + state.level / 3).coerceAtMost(8)
            val maxConcurrentAttackers = if (alienCount <= 4) 1 else baseMaxAttackers

            if (nowMillis - lastAttackTime > attackInterval && attackersCount < maxConcurrentAttackers) {
                val candidates = mutableListOf<Int>()
                nextAliens.forEachIndexed { i, a ->
                    if (a.type != AlienType.SUPERBOSS && !a.isAttacking && a.y >= 250f && nowMillis >= a.readyTime) {
                        candidates.add(i)
                    }
                }
                if (candidates.isNotEmpty()) {
                    val index = candidates.random()
                    nextAliens[index] = nextAliens[index].copy(isAttacking = true)
                    lastAttackTime = nowMillis
                } else if (attackersCount == 0) {
                    // If no one is attacking and no one is ready (e.g. they are still returning to formation),
                    // reset the timer so they don't dive instantly the moment they arrive.
                    lastAttackTime = nowMillis
                }
            }
            
            var updatedAlienBullets = newAlienBullets
            
            // Difficulty factors relative to "Original"
            val (speedMult, densityMult, scoreMult) = when(state.difficulty) {
                "normal" -> Triple(0.95f, 0.95f, 1.2f)
                "hard" -> Triple(1.05f, 1.05f, 1.5f)
                else -> Triple(0.85f, 0.8f, 1.0f) // easy
            }

            val shootChance = ((3 + state.level * 0.5f) * densityMult).toInt()
            if (Random.nextInt(100) < shootChance && nextAliens.isNotEmpty()) {
                val shooterIndex = nextAliens.indices.random()
                val shooter = nextAliens[shooterIndex]
                if (shooter.y > 0) {
                    if (shooter.type == AlienType.SUPERBOSS && Random.nextInt(100) < 15) {
                        nextAliens[shooterIndex] = shooter.copy(patternId = Random.nextInt(5))
                    }
                    val shooterNewBullets = when (shooter.type) {
                        AlienType.SUPERBOSS -> {
                            val healthPct = shooter.health.toFloat() / shooter.maxHealth
                            when (shooter.patternId) {
                                1 -> List(8) { i ->
                                    val angle = (i * 45f) * (PI / 180f).toFloat()
                                    Bullet(shooter.x, shooter.y + 40f, vx = (8f * speedMult) * cos(angle.toDouble()).toFloat(), speed = -(8f * speedMult) * sin(angle.toDouble()).toFloat(), type = BulletType.CIRCLE)
                                }
                                2 -> List(7) { i -> Bullet(shooter.x, shooter.y + 40f, vx = (i - 3) * (3f * speedMult), speed = -(12f * speedMult)) }
                                3 -> {
                                    val dx = state.ship.x - shooter.x
                                    val dy = (state.ship.y - 120f) - shooter.y
                                    val dist = hypot(dx, dy)
                                    if (dist > 0) {
                                        listOf(
                                            Bullet(shooter.x - 20f, shooter.y + 40f, vx = (dx / dist) * (15f * speedMult), speed = -(dy / dist) * (15f * speedMult)),
                                            Bullet(shooter.x + 20f, shooter.y + 40f, vx = (dx / dist) * (15f * speedMult), speed = -(dy / dist) * (15f * speedMult))
                                        )
                                    } else emptyList()
                                }
                                4 -> List(3) { i -> Bullet(shooter.x + (i - 1) * 40f, shooter.y + 40f, speed = -(8f * speedMult), type = BulletType.HOMING) }
                                else -> if (healthPct < 0.5f) {
                                    listOf(
                                        Bullet(shooter.x - 60f, shooter.y + 40f, vx = -(4f * speedMult), speed = -(8f * speedMult)),
                                        Bullet(shooter.x - 30f, shooter.y + 60f, vx = -(2f * speedMult), speed = -(10f * speedMult)),
                                        Bullet(shooter.x, shooter.y + 80f, vx = 0f, speed = -(12f * speedMult)),
                                        Bullet(shooter.x + 30f, shooter.y + 60f, vx = (2f * speedMult), speed = -(10f * speedMult)),
                                        Bullet(shooter.x + 60f, shooter.y + 40f, vx = (4f * speedMult), speed = -(8f * speedMult))
                                    )
                                } else {
                                    listOf(
                                        Bullet(shooter.x - 40f, shooter.y + 40f, speed = -(10f * speedMult)),
                                        Bullet(shooter.x, shooter.y + 60f, speed = -(12f * speedMult)),
                                        Bullet(shooter.x + 40f, shooter.y + 40f, speed = -(10f * speedMult))
                                    )
                                }
                            }
                        }
                        AlienType.BOSS -> when (shooter.patternId) {
                            1 -> listOf(Bullet(shooter.x, shooter.y + 20f, vx = -(3f * speedMult), speed = -(8f * speedMult)), Bullet(shooter.x, shooter.y + 20f, vx = 0f, speed = -(10f * speedMult)), Bullet(shooter.x, shooter.y + 20f, vx = (3f * speedMult), speed = -(8f * speedMult)))
                            2 -> listOf(Bullet(shooter.x, shooter.y + 20f, speed = -(12f * speedMult)), Bullet(shooter.x, shooter.y + 50f, speed = -(12f * speedMult)))
                            else -> listOf(Bullet(shooter.x, shooter.y + shooter.height / 2, speed = -(10f * speedMult)))
                        }
                        else -> listOf(Bullet(shooter.x, shooter.y + shooter.height / 2, speed = -(10f * speedMult)))
                    }
                    updatedAlienBullets = updatedAlienBullets + shooterNewBullets
                }
            }

            val finalBullets = mutableListOf<Bullet>()
            val currentExplosions = newExplosions.toMutableList()
            val currentPowerUps = newPowerUps.toMutableList()
            var scoreGain = 0
            var newLives = state.lives
            var newBombs = state.bombs
            var nextPhase = state.phase
            var nextLevel = state.level
            var nextActivePowerUp = updatedActivePowerUp

            // Spatial bucketing for optimized collision
            val alienBuckets = mutableMapOf<Int, MutableList<Int>>()
            nextAliens.forEachIndexed { index, alien ->
                if (alien.health > 0) {
                    val bucket = (alien.y / 100f).toInt()
                    alienBuckets.getOrPut(bucket) { mutableListOf() }.add(index)
                }
            }

            // Optimized Collision: Bullets vs Aliens
            for (bullet in newBullets) {
                val bulletRect = bullet.getRect()
                var hit = false
                val bucket = (bullet.y / 100f).toInt()
                
                outer@for (b in bucket - 1..bucket + 1) {
                    val indices = alienBuckets[b] ?: continue
                    for (i in indices) {
                        val alien = nextAliens[i]
                        if (alien.health > 0 && alien.getRect().overlaps(bulletRect)) {
                            hit = true
                            if (bullet.type == BulletType.BOMB) {
                                currentExplosions.add(Explosion(bullet.x, bullet.y, Color.Red, radius = 50f, life = 30))
                                // AOE Damage optimized
                                for (eb in bucket - 2..bucket + 2) {
                                    alienBuckets[eb]?.forEach { j ->
                                        val a = nextAliens[j]
                                        if (hypot(a.x - bullet.x, a.y - bullet.y) < 150f) {
                                            nextAliens[j] = a.copy(health = a.health - 5)
                                        }
                                    }
                                }
                            } else {
                                nextAliens[i] = alien.copy(health = alien.health - 1)
                                if (bullet.type == BulletType.LASER && bullet.pierceCount > 1) {
                                    finalBullets.add(bullet.copy(pierceCount = bullet.pierceCount - 1))
                                }
                            }
                            break@outer
                        }
                    }
                }
                if (!hit) finalBullets.add(bullet)
            }

            // Cleanup dead aliens and handle points/drops
            val aliveAliens = mutableListOf<Alien>()
            for (alien in nextAliens) {
                if (alien.health <= 0) {
                    soundManager?.playSound("explosion")
                    val basePoints = when(alien.type) {
                        AlienType.SUPERBOSS -> 10000
                        AlienType.BOSS -> 500
                        else -> 100
                    }
                    scoreGain += (basePoints * scoreMult).toInt()
                    currentExplosions.add(Explosion(alien.x, alien.y, alien.color))
                    if (Random.nextInt(100) < 15) {
                        currentPowerUps.add(PowerUp(alien.x, alien.y, PowerUpType.values().random()))
                    }
                } else {
                    if (alien.type == AlienType.SUPERBOSS && Random.nextInt(1000) < 5) { // Occasional mid-hit drop
                         currentPowerUps.add(PowerUp(alien.x, alien.y, PowerUpType.values().random()))
                    }
                    aliveAliens.add(alien)
                }
            }

            val isCheated = state.activeCheats.isNotEmpty()
            val scoreGainFinal = if (isCheated) 0 else scoreGain
            val newScoreTotal = state.score + scoreGainFinal
            if (!isCheated) {
                newLives += (newScoreTotal / SCORE_PER_EXTRA_LIFE) - (state.score / SCORE_PER_EXTRA_LIFE)
                newBombs += (newScoreTotal / SCORE_PER_BOMB) - (state.score / SCORE_PER_BOMB)
            }
            // Powerups collection
            val remainingPowerUps = mutableListOf<PowerUp>()
            for (pu in currentPowerUps) {
                if (pu.getRect().overlaps(shipRect)) {
                    soundManager?.playSound("powerup")
                    nextActivePowerUp = ActivePowerUp(pu.type)
                } else {
                    remainingPowerUps.add(pu)
                }
            }

            // Player hit detection optimized
            var finalShip = baseShip
            var finalShake = nextShake
            if (baseShip.invincibilityFrames == 0) {
                var playerHit = false
                val bulletHit = updatedAlienBullets.find { it.getRect().overlaps(shipRect) }
                if (bulletHit != null) {
                    playerHit = true
                    updatedAlienBullets = updatedAlienBullets - bulletHit
                } else {
                    val shipBucket = (shipRect.center.y / 100f).toInt()
                    outer@for (sb in shipBucket - 1..shipBucket + 1) {
                        val indices = alienBuckets[sb] ?: continue
                        for (i in indices) {
                            if (nextAliens[i].getRect().overlaps(shipRect)) {
                                playerHit = true
                                break@outer
                            }
                        }
                    }
                }

                if (playerHit) {
                    if (nextActivePowerUp?.type == PowerUpType.SHIELD) {
                        nextActivePowerUp = null
                        soundManager?.playSound("hit")
                    } else if (state.activeCheats.contains(CheatType.INVINCIBILITY) || nextActivePowerUp?.type == PowerUpType.INVINCIBILITY) {
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
                        finalShip = baseShip.copy(invincibilityFrames = 60)
                        if (newLives <= 0) nextPhase = GamePhase.GAME_OVER
                    }
                }
            }

            if (aliveAliens.isEmpty() && state.aliens.isNotEmpty()) {
                nextLevel = (nextLevel + 1).coerceAtMost(100)
                val newLevelAliens = createAliens(state.screenWidth, nextLevel)
                aliveAliens.addAll(newLevelAliens)
                nextFormationX = getInitialFormationX(state.screenWidth, nextLevel)
                nextMoveDirection = 1f
                nextSuperbossDirection = 1f
            }

            val nextState = state.copy(
                ship = finalShip,
                bullets = finalBullets,
                alienBullets = updatedAlienBullets,
                aliens = aliveAliens,
                stars = newStars,
                explosions = currentExplosions,
                powerUps = remainingPowerUps,
                activePowerUp = nextActivePowerUp,
                score = newScoreTotal,
                lives = newLives,
                bombs = newBombs,
                level = nextLevel,
                formationX = nextFormationX,
                alienMoveDirection = nextMoveDirection,
                superbossDirection = nextSuperbossDirection,
                screenShakeIntensity = finalShake,
                bombEffectFrames = nextBombEffectFrames,
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
