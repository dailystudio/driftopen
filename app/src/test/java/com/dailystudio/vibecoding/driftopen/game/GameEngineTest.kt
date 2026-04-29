package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class GameEngineTest {

    private lateinit var gameEngine: GameEngine
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gameEngine = GameEngine()
        gameEngine.setScreenSize(1000f, 2000f)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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

        // Force lives to 1
        gameEngine.debugSetLives(1)

        // Move ship into an alien to trigger hit (hitbox is at y)
        val targetAlien = gameEngine.gameState.value.aliens.first()
        val currentShip = gameEngine.gameState.value.ship
        gameEngine.moveShipRelative(targetAlien.x - currentShip.x, targetAlien.y - currentShip.y)

        // Update to trigger hit
        repeat(10) {
            gameEngine.update(16000000L)
        }

        val state = gameEngine.gameState.value
        assertEquals(0, state.lives)
        assertEquals(GamePhase.GAME_OVER, state.phase)
    }

    @Test
    fun `test extra life awarded at 10000 points`() {
        gameEngine.startGame()
        val initialLives = gameEngine.gameState.value.lives
        
        // Set score to just below 10000
        gameEngine.debugSetScore(9900)
        
        // Add 100 points
        gameEngine.debugAddScore(100)
        
        // Score should be 10000 and lives should be initialLives + 1
        val state = gameEngine.gameState.value
        assertEquals(10000, state.score)
        assertEquals(initialLives + 1, state.lives)
        
        // Test another 10000 points
        gameEngine.debugAddScore(10000)
        
        val state2 = gameEngine.gameState.value
        assertEquals(20000, state2.score)
        assertEquals(initialLives + 2, state2.lives)

        // Verify it doesn't award at 5000 anymore
        gameEngine.debugSetScore(4900)
        val livesBefore = gameEngine.gameState.value.lives
        gameEngine.debugAddScore(200)
        assertEquals(5100, gameEngine.gameState.value.score)
        assertEquals(livesBefore, gameEngine.gameState.value.lives)
    }
}
