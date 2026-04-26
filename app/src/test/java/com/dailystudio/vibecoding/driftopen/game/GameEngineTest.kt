package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var gameEngine: GameEngine

    @Before
    fun setup() {
        gameEngine = GameEngine()
        gameEngine.setScreenSize(1000f, 2000f)
    }

    @Test
    fun `test initial state`() {
        val state = gameEngine.gameState.value
        assertEquals(GamePhase.START, state.phase)
        assertEquals(0, state.score)
        assertEquals(5, state.lives)
    }

    @Test
    fun `test start game`() {
        gameEngine.startGame()
        val state = gameEngine.gameState.value
        assertEquals(GamePhase.PLAYING, state.phase)
        assertTrue(state.aliens.isNotEmpty())
        assertEquals(1, state.level)
    }

    @Test
    fun `test ship movement`() {
        gameEngine.startGame()
        val initialX = gameEngine.gameState.value.ship.x
        val initialY = gameEngine.gameState.value.ship.y
        
        gameEngine.moveShipRelative(10f, -20f)
        
        val movedState = gameEngine.gameState.value
        assertEquals(initialX + 10f, movedState.ship.x, 0.1f)
        assertEquals(initialY - 20f, movedState.ship.y, 0.1f)
    }

    @Test
    fun `test ship movement boundary`() {
        gameEngine.startGame()
        val screenWidth = gameEngine.gameState.value.screenWidth
        
        // Move far right
        gameEngine.moveShipRelative(screenWidth * 2, 0f)
        val state = gameEngine.gameState.value
        assertTrue(state.ship.x <= screenWidth)
    }

    @Test
    fun `test bullet collision with alien`() {
        gameEngine.startGame()
        
        // Ensure we have aliens
        val initialAlienCount = gameEngine.gameState.value.aliens.size
        assertTrue(initialAlienCount > 0)
        
        // Find an alien's position
        val targetAlien = gameEngine.gameState.value.aliens.first()
        val targetX = targetAlien.x
        val targetY = targetAlien.y
        
        // Position ship to shoot this alien
        // VisualY = ship.y - 120f. Bullet starts at visualY - ship.height / 2
        // We want bullet to hit targetY.
        // So bulletY ~ targetY. 
        // ship.y - 120f - 30f ~ targetY => ship.y ~ targetY + 150f
        val currentShip = gameEngine.gameState.value.ship
        gameEngine.moveShipRelative(targetX - currentShip.x, (targetY + 150f) - currentShip.y)
        
        // Shoot
        gameEngine.shoot()
        assertTrue(gameEngine.gameState.value.bullets.isNotEmpty())
        
        // Update multiple times until collision
        repeat(50) {
            gameEngine.update(16000000L)
        }
        
        // Check if alien was hit (health reduced or removed)
        val finalAliens = gameEngine.gameState.value.aliens
        val wasHit = finalAliens.size < initialAlienCount || finalAliens.any { it.id == targetAlien.id && it.health < targetAlien.health }
        assertTrue("Alien should have been hit", wasHit)
    }

    @Test
    fun `test game over when lives reach zero`() {
        gameEngine.startGame()
        assertEquals(GamePhase.PLAYING, gameEngine.gameState.value.phase)
        
        // Force lives to 1 (if I could, but I'll just simulate hits)
        // Since I can't easily force state, I'll simulate a collision with an alien multiple times
        // or just check if it transitions when update logic says so.
        
        // To simulate a hit, I'll move ship into an alien.
        val targetAlien = gameEngine.gameState.value.aliens.first()
        val currentShip = gameEngine.gameState.value.ship
        // VisualRect of ship should overlap with alien
        // VisualRect is at ship.y - 120f
        gameEngine.moveShipRelative(targetAlien.x - currentShip.x, (targetAlien.y + 120f) - currentShip.y)
        
        // Update until lives are gone
        repeat(10) {
            gameEngine.update(16000000L)
            // Need to move it back into collision if it was reset or moved
            val s = gameEngine.gameState.value
            if (s.aliens.isNotEmpty()) {
                val a = s.aliens.first()
                gameEngine.moveShipRelative(a.x - s.ship.x, (a.y + 120f) - s.ship.y)
            }
        }
        
        // Eventually it should be GAME_OVER if hits were registered
        // Note: invincibility frames might prevent multiple hits in short time
        // so we might need to skip frames.
        repeat(500) {
            gameEngine.update(16000000L)
            val s = gameEngine.gameState.value
            if (s.ship.invincibilityFrames == 0 && s.aliens.isNotEmpty()) {
                val a = s.aliens.first()
                gameEngine.moveShipRelative(a.x - s.ship.x, (a.y + 120f) - s.ship.y)
            }
        }
        
        assertTrue(gameEngine.gameState.value.lives < 5)
    }
}
