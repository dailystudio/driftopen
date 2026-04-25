package com.dailystudio.vibecoding.driftopen.game.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

enum class GamePhase {
    START, PLAYING, GAME_OVER, WIN
}

data class GameState(
    val ship: PlayerShip = PlayerShip(),
    val aliens: List<Alien> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val alienBullets: List<Bullet> = emptyList(),
    val stars: List<Star> = emptyList(),
    val explosions: List<Explosion> = emptyList(),
    val powerUps: List<PowerUp> = emptyList(),
    val activePowerUp: ActivePowerUp? = null,
    val score: Int = 0,
    val lives: Int = 5,
    val level: Int = 1,
    val phase: GamePhase = GamePhase.START,
    val screenWidth: Float = 0f,
    val screenHeight: Float = 0f
)

data class Explosion(
    val x: Float,
    val y: Float,
    val color: Color,
    val radius: Float = 5f,
    val alpha: Float = 1f,
    val life: Int = 20 // Frames to live
)

data class PlayerShip(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 60f,
    val height: Float = 60f,
    val color: Color = Color.Cyan,
    val invincibilityFrames: Int = 0
) {
    fun getRect() = Rect(Offset(x - width / 2, y - height / 2), Offset(x + width / 2, y + height / 2))
}

data class Alien(
    val id: Int,
    val x: Float,
    val y: Float,
    val gridCol: Int,
    val gridRow: Int,
    val width: Float = 50f,
    val height: Float = 50f,
    val color: Color = Color.Red,
    val type: AlienType = AlienType.NORMAL,
    val health: Int = 1,
    val maxHealth: Int = 1,
    val isAttacking: Boolean = false,
    val attackPhase: Float = 0f
) {
    fun getRect() = Rect(Offset(x - width / 2, y - height / 2), Offset(x + width / 2, y + height / 2))
}

enum class AlienType {
    NORMAL, FAST, BOSS, SUPERBOSS
}

enum class BulletType {
    NORMAL, SPREAD, LASER, HOMING, BOMB
}

data class Bullet(
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val width: Float = 6f,
    val height: Float = 15f,
    val speed: Float = 15f,
    val type: BulletType = BulletType.NORMAL,
    val pierceCount: Int = 1,
    val targetId: Int? = null
) {
    fun getRect() = Rect(Offset(x - width / 2, y - height / 2), Offset(x + width / 2, y + height / 2))
}

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float
)

enum class PowerUpType {
    SHIELD, DOUBLE_FIRE, RAPID_FIRE, SPREAD_SHOT, LASER_BEAM, HOMING_MISSILES, EXPLOSIVE_BOMBS
}

data class PowerUp(
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    val radius: Float = 20f,
    val speed: Float = 5f
) {
    fun getRect() = Rect(Offset(x - radius, y - radius), Offset(x + radius, y + radius))
}

data class ActivePowerUp(
    val type: PowerUpType,
    val timeRemaining: Int = 600 // ~10 seconds at 60fps
)
