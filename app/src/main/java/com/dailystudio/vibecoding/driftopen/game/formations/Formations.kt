package com.dailystudio.vibecoding.driftopen.game.formations

import com.dailystudio.vibecoding.driftopen.game.models.Alien
import com.dailystudio.vibecoding.driftopen.game.models.AlienType
import com.dailystudio.vibecoding.driftopen.game.models.FormationType
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

object FormationGenerator {

    fun generateFormation(
        type: FormationType,
        level: Int,
        screenWidth: Float,
        spacing: Float,
        startY: Float
    ): List<Alien> {
        return when (type) {
            FormationType.GRID -> generateGrid(level, screenWidth, spacing, startY)
            FormationType.V_SHAPE -> generateVShape(level, screenWidth, spacing, startY)
            FormationType.DIAMOND -> generateDiamond(level, screenWidth, spacing, startY)
            FormationType.CIRCLE -> generateCircle(level, screenWidth, spacing, startY)
            FormationType.HEART -> generateHeart(level, screenWidth, spacing, startY)
            FormationType.RANDOM_SCATTER -> generateRandomScatter(level, screenWidth, spacing, startY)
        }
    }

    private fun getAlienProperties(row: Int, level: Int): Pair<AlienType, Color> {
        val type = if (level == 1) AlienType.NORMAL
        else if (level == 2) {
            if (row == 0) AlienType.FAST else AlienType.NORMAL
        } else {
            if (row == 0) AlienType.BOSS
            else if (row == 1) AlienType.FAST
            else AlienType.NORMAL
        }

        val color = when (type) {
            AlienType.BOSS -> Color.Magenta
            AlienType.FAST -> Color.Yellow
            AlienType.NORMAL -> Color.Red
            else -> Color.Red
        }
        return type to color
    }

    private fun createAlien(
        id: Int,
        x: Float,
        y: Float,
        offsetX: Float,
        offsetY: Float,
        col: Int,
        row: Int,
        type: AlienType,
        color: Color
    ): Alien {
        val health = if (type == AlienType.BOSS) 3 else 1
        return Alien(
            id = id,
            x = x,
            y = y,
            offsetX = offsetX,
            offsetY = offsetY,
            gridCol = col,
            gridRow = row,
            type = type,
            color = color,
            health = health,
            maxHealth = health,
            skinId = "default"
        )
    }

    private fun generateGrid(level: Int, screenWidth: Float, spacing: Float, startY: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val rows = 4 + (level / 2)
        val cols = 6 + (level / 2)
        val formationWidth = (cols - 1) * spacing
        val formationX = (screenWidth - formationWidth) / 2

        var idCounter = 0
        for (row in 0 until rows) {
            val (type, color) = getAlienProperties(row, level)
            for (col in 0 until cols) {
                val ox = col * spacing
                val oy = row * spacing
                aliens.add(createAlien(idCounter++, formationX + ox, startY + oy, ox, oy, col, row, type, color))
            }
        }
        return aliens
    }

    private fun generateVShape(level: Int, screenWidth: Float, spacing: Float, startY: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val wings = 4 + level
        val centerX = screenWidth / 2
        var idCounter = 0

        for (i in 0 until wings) {
            val (type, color) = getAlienProperties(i / 2, level)
            // Left wing
            val lox = -i * (spacing * 0.8f)
            val loy = i * (spacing * 0.8f)
            aliens.add(createAlien(idCounter++, centerX + lox, startY + loy, lox, loy, -i, i, type, color))
            // Right wing
            if (i > 0) {
                val rox = i * (spacing * 0.8f)
                val roy = i * (spacing * 0.8f)
                aliens.add(createAlien(idCounter++, centerX + rox, startY + roy, rox, roy, i, i, type, color))
            }
        }
        return aliens
    }

    private fun generateDiamond(level: Int, screenWidth: Float, spacing: Float, startY: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val size = 3 + (level / 2) // Half height of diamond
        val centerX = screenWidth / 2
        var idCounter = 0

        for (row in -size..size) {
            val absRow = abs(row)
            val colsInRow = size - absRow
            val (type, color) = getAlienProperties(absRow, level)
            for (col in -colsInRow..colsInRow) {
                val ox = col * spacing
                val oy = (row + size) * spacing
                aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, col, row + size, type, color))
            }
        }
        return aliens
    }

    private fun generateCircle(level: Int, screenWidth: Float, spacing: Float, startY: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val count = 12 + level * 2
        val radius = spacing * (2 + level * 0.2f)
        val centerX = screenWidth / 2
        var idCounter = 0

        for (i in 0 until count) {
            val angle = 2 * PI * i / count
            val ox = radius * cos(angle).toFloat()
            val oy = radius + radius * sin(angle).toFloat()
            val (type, color) = getAlienProperties(i % 4, level)
            aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, i, 0, type, color))
        }
        return aliens
    }

    private fun generateHeart(level: Int, screenWidth: Float, spacing: Float, startY: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val count = 15
        val centerX = screenWidth / 2
        val verticalOffset = spacing * 2
        var idCounter = 0

        for (i in 0 until count) {
            val t = 2 * PI * i / count
            val hx = 16 * sin(t).pow(3)
            val hy = -(13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t))
            
            val scale = spacing / 8
            val ox = hx.toFloat() * scale
            val oy = verticalOffset + hy.toFloat() * scale
            val (type, color) = getAlienProperties(i % 3, level)
            aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, i, 0, type, color))
        }
        return aliens
    }

    private fun generateRandomScatter(level: Int, screenWidth: Float, spacing: Float, startY: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val count = 10 + level
        var idCounter = 0
        val centerX = screenWidth / 2
        for (i in 0 until count) {
            val rx = (50f..(screenWidth - 50f)).random()
            val ry = (0f..(spacing * 4)).random()
            val (type, color) = getAlienProperties(i % 3, level)
            // For scatter, we still want them to move with formationX, so we calculate offset from center
            val ox = rx - centerX
            aliens.add(createAlien(idCounter++, rx, startY + ry, ox, ry, i, 0, type, color))
        }
        return aliens
    }

    private fun ClosedRange<Float>.random() =
        Random(System.nanoTime()).nextFloat() * (endInclusive - start) + start
    
    private fun IntRange.random() =
        kotlin.random.Random.nextInt(this.first, this.last + 1)
}
