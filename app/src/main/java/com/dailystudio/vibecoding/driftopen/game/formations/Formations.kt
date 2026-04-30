package com.dailystudio.vibecoding.driftopen.game.formations

import com.dailystudio.vibecoding.driftopen.game.models.Alien
import com.dailystudio.vibecoding.driftopen.game.models.AlienType
import com.dailystudio.vibecoding.driftopen.game.models.FormationType
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

object FormationGenerator {

    private fun getScale(level: Int): Float {
        return (1.0f / sqrt(level.toFloat() / 15f)).coerceAtMost(1.0f).coerceAtLeast(0.4f)
    }

    fun generateFormation(
        type: FormationType,
        level: Int,
        screenWidth: Float,
        spacing: Float,
        startY: Float
    ): List<Alien> {
        val cappedLevelForCount = level.coerceAtMost(49)
        val baseScale = getScale(cappedLevelForCount)
        
        // Calculate max columns/width for the given level and type
        val maxCols = when (type) {
            FormationType.GRID -> (6 + cappedLevelForCount / 2).toFloat()
            FormationType.V_SHAPE -> ((4 + cappedLevelForCount / 2) * 2 - 1).toFloat()
            FormationType.DIAMOND -> ((3 + cappedLevelForCount / 2) * 2 + 1).toFloat()
            FormationType.CIRCLE -> (2 + cappedLevelForCount * 0.4f) * 2f
            FormationType.HEART -> {
                val heartScale = (spacing / 8) * (1f + cappedLevelForCount * 0.05f).coerceAtMost(2.5f)
                // Heart width is roughly 32 * heartScale
                (32 * heartScale) / spacing
            }
            FormationType.RANDOM_SCATTER -> screenWidth / spacing
        }
        
        // Ensure formation width doesn't exceed 90% of screen
        val predictedWidth = maxCols * spacing * baseScale
        val widthScale = if (predictedWidth > screenWidth * 0.9f) {
            (screenWidth * 0.9f) / predictedWidth
        } else 1.0f
        
        val finalScale = baseScale * widthScale
        val scaledSpacing = spacing * finalScale
        
        return when (type) {
            FormationType.GRID -> generateGrid(level, screenWidth, scaledSpacing, startY, finalScale)
            FormationType.V_SHAPE -> generateVShape(level, screenWidth, scaledSpacing, startY, finalScale)
            FormationType.DIAMOND -> generateDiamond(level, screenWidth, scaledSpacing, startY, finalScale)
            FormationType.CIRCLE -> generateCircle(level, screenWidth, scaledSpacing, startY, finalScale)
            FormationType.HEART -> generateHeart(level, screenWidth, scaledSpacing, startY, finalScale)
            FormationType.RANDOM_SCATTER -> generateRandomScatter(level, screenWidth, scaledSpacing, startY, finalScale)
        }
    }

    private fun getAlienProperties(row: Int, col: Int, level: Int): Pair<AlienType, Color> {
        val type = if (level <= 49) {
            if (level == 1) AlienType.NORMAL
            else if (level == 2) {
                if (row == 0) AlienType.FAST else AlienType.NORMAL
            } else {
                if (row == 0) AlienType.BOSS
                else if (row == 1) AlienType.FAST
                else AlienType.NORMAL
            }
        } else {
            // Difficulty scaling for level > 49: Interleaved patterns
            val progress = (level - 50) / 49f // 0.0 to 1.0
            
            // Budget for a cycle of 12 aliens
            val numBoss = (1 + (5 * progress)).toInt()
            val numFast = (1 + (5 * progress)).toInt()
            val numNormal = (12 - numBoss - numFast).coerceAtLeast(0)
            
            // Create an interleaved pattern
            val pattern = mutableListOf<AlienType>()
            val max = maxOf(numBoss, numFast, numNormal)
            for (i in 0 until max) {
                if (i < numBoss) pattern.add(AlienType.BOSS)
                if (i < numFast) pattern.add(AlienType.FAST)
                if (i < numNormal) pattern.add(AlienType.NORMAL)
            }
            
            // Use both row and col to ensure aliens of same type don't sit together
            pattern[abs(row + col) % pattern.size]
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
        color: Color,
        scale: Float
    ): Alien {
        val health = if (type == AlienType.BOSS) 3 else 1
        val skinId = if (type == AlienType.BOSS) {
            listOf("default", "boss_a", "boss_b").random()
        } else "default"
        val patternId = if (type == AlienType.BOSS) {
            Random.nextInt(3) // 0: single, 1: circular, 2: burst
        } else 0

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
            width = 50f * scale,
            height = 50f * scale,
            health = health,
            maxHealth = health,
            skinId = skinId,
            patternId = patternId
        )
    }

    private fun generateGrid(level: Int, screenWidth: Float, spacing: Float, startY: Float, scale: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val cappedLevel = level.coerceAtMost(49)
        val rows = 4 + (cappedLevel / 2)
        val cols = 6 + (cappedLevel / 2)
        val formationWidth = (cols - 1) * spacing
        val formationX = (screenWidth - formationWidth) / 2

        var idCounter = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val (type, color) = getAlienProperties(row, col, level)
                val ox = col * spacing
                val oy = row * spacing
                aliens.add(createAlien(idCounter++, formationX + ox, startY + oy, ox, oy, col, row, type, color, scale))
            }
        }
        return aliens
    }

    private fun generateVShape(level: Int, screenWidth: Float, spacing: Float, startY: Float, scale: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val cappedLevel = level.coerceAtMost(49)
        val rows = 4 + (cappedLevel / 2)
        val centerX = screenWidth / 2
        var idCounter = 0

        for (row in 0 until rows) {
            for (col in -row..row) {
                val (type, color) = getAlienProperties(row, col, level)
                val ox = col * spacing
                val oy = row * spacing
                aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, col, row, type, color, scale))
            }
        }
        return aliens
    }

    private fun generateDiamond(level: Int, screenWidth: Float, spacing: Float, startY: Float, scale: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val cappedLevel = level.coerceAtMost(49)
        val size = 3 + (cappedLevel / 2) // Half height of diamond
        val centerX = screenWidth / 2
        var idCounter = 0

        for (row in -size..size) {
            val absRow = abs(row)
            val colsInRow = size - absRow
            for (col in -colsInRow..colsInRow) {
                val (type, color) = getAlienProperties(absRow + size, col, level)
                val ox = col * spacing
                val oy = (row + size) * spacing
                aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, col, row + size, type, color, scale))
            }
        }
        return aliens
    }

    private fun generateCircle(level: Int, screenWidth: Float, spacing: Float, startY: Float, scale: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val cappedLevel = level.coerceAtMost(49)
        val maxRadius = spacing * (2 + cappedLevel * 0.4f)
        val centerX = screenWidth / 2
        var idCounter = 0
        
        val ringCount = (maxRadius / spacing).toInt() + 1
        for (ring in 0 until ringCount) {
            val radius = ring * spacing
            if (radius == 0f) {
                val (type, color) = getAlienProperties(0, 0, level)
                aliens.add(createAlien(idCounter++, centerX, startY + maxRadius, 0f, maxRadius, 0, 0, type, color, scale))
                continue
            }
            
            val circumference = 2 * PI * radius
            val count = (circumference / spacing).toInt().coerceAtLeast(1)
            for (i in 0 until count) {
                val angle = 2 * PI * i / count
                val ox = radius * cos(angle).toFloat()
                val oy = maxRadius + radius * sin(angle).toFloat()
                val (type, color) = getAlienProperties(ring, i, level)
                aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, i, ring, type, color, scale))
            }
        }
        return aliens
    }

    private fun generateHeart(level: Int, screenWidth: Float, spacing: Float, startY: Float, scale: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val cappedLevel = level.coerceAtMost(49)
        val centerX = screenWidth / 2
        val heartScale = (spacing / 8) * (1f + cappedLevel * 0.05f).coerceAtMost(2.5f)
        val verticalOffset = spacing * 4
        var idCounter = 0

        val ringCount = 5
        for (ring in 1..ringCount) {
            val ringScale = heartScale * (ring.toFloat() / ringCount)
            val count = 8 + ring * 4
            for (i in 0 until count) {
                val t = 2 * PI * i / count
                val hx = 16 * sin(t).pow(3)
                val hy = -(13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t))
                
                val ox = hx.toFloat() * ringScale
                val oy = verticalOffset + hy.toFloat() * ringScale
                val (type, color) = getAlienProperties(ring, i, level)
                aliens.add(createAlien(idCounter++, centerX + ox, startY + oy, ox, oy, i, ring, type, color, scale))
            }
        }
        // Center alien
        val (type, color) = getAlienProperties(0, 0, level)
        aliens.add(createAlien(idCounter++, centerX, startY + verticalOffset, 0f, verticalOffset, 0, 0, type, color, scale))
        
        return aliens
    }

    private fun generateRandomScatter(level: Int, screenWidth: Float, spacing: Float, startY: Float, scale: Float): List<Alien> {
        val aliens = mutableListOf<Alien>()
        val cappedLevel = level.coerceAtMost(49)
        val count = 10 + cappedLevel
        var idCounter = 0
        val centerX = screenWidth / 2
        for (i in 0 until count) {
            val rx = (50f..(screenWidth - 50f)).random()
            val ry = (0f..(spacing * 4)).random()
            val (type, color) = getAlienProperties(i, 0, level)
            // For scatter, we still want them to move with formationX, so we calculate offset from center
            val ox = rx - centerX
            aliens.add(createAlien(idCounter++, rx, startY + ry, ox, ry, i, 0, type, color, scale))
        }
        return aliens
    }

    private fun ClosedRange<Float>.random() =
        Random(System.nanoTime()).nextFloat() * (endInclusive - start) + start
    
    private fun IntRange.random() =
        kotlin.random.Random.nextInt(this.first, this.last + 1)
}
