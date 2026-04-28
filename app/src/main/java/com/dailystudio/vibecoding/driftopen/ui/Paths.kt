package com.dailystudio.vibecoding.driftopen.ui

import androidx.compose.ui.graphics.Path
import com.dailystudio.vibecoding.driftopen.game.models.AlienType

object GamePaths {

    val shipSkins = mapOf(
        "default" to Path().apply {
            moveTo(0f, -0.5f)
            lineTo(-0.2f, -0.1f)
            lineTo(-0.5f, 0.3f)
            lineTo(-0.2f, 0.5f)
            lineTo(0.2f, 0.5f)
            lineTo(0.5f, 0.3f)
            lineTo(0.2f, -0.1f)
            close()
            // Wings
            moveTo(-0.1f, 0.5f); lineTo(-0.3f, 0.7f); lineTo(-0.1f, 0.7f); close()
            moveTo(0.1f, 0.5f); lineTo(0.3f, 0.7f); lineTo(0.1f, 0.7f); close()
        },
        "heavy" to Path().apply {
            moveTo(-0.5f, 0.5f); lineTo(-0.5f, -0.2f); lineTo(-0.2f, -0.5f); lineTo(0.2f, -0.5f); lineTo(0.5f, -0.2f); lineTo(0.5f, 0.5f); close()
            // Massive boosters
            addRect(androidx.compose.ui.geometry.Rect(-0.4f, 0.5f, -0.1f, 0.7f))
            addRect(androidx.compose.ui.geometry.Rect(0.1f, 0.5f, 0.4f, 0.7f))
            // Front cannons
            moveTo(-0.1f, -0.5f); lineTo(-0.1f, -0.7f); lineTo(0.1f, -0.7f); lineTo(0.1f, -0.5f); close()
        },
        "stealth" to Path().apply {
            moveTo(0f, -0.7f)
            lineTo(-0.5f, 0.5f)
            lineTo(0f, 0.2f)
            lineTo(0.5f, 0.5f)
            close()
            // Inner details
            moveTo(0f, -0.3f); lineTo(-0.2f, 0.2f); lineTo(0.2f, 0.2f); close()
        },
        "retro" to Path().apply {
            // Blocky ship
            addRect(androidx.compose.ui.geometry.Rect(-0.1f, -0.5f, 0.1f, 0.5f))
            addRect(androidx.compose.ui.geometry.Rect(-0.3f, 0f, 0.3f, 0.3f))
            addRect(androidx.compose.ui.geometry.Rect(-0.5f, 0.3f, 0.5f, 0.6f))
        }
    )

    val alienSkins = mapOf(
        "default" to mapOf(
            AlienType.NORMAL to Path().apply {
                moveTo(0f, 0.5f); lineTo(-0.5f, -0.25f); lineTo(-0.25f, -0.5f); lineTo(0.25f, -0.5f); lineTo(0.5f, -0.25f); close()
                moveTo(-0.25f, 0.5f); lineTo(-0.4f, 0.65f); moveTo(0.25f, 0.5f); lineTo(0.4f, 0.65f)
            },
            AlienType.FAST to Path().apply {
                moveTo(0f, -0.5f); lineTo(0.5f, 0f); lineTo(0f, 0.5f); lineTo(-0.5f, 0f); close()
                moveTo(-0.5f, 0f); lineTo(-0.75f, 0.25f); moveTo(0.5f, 0f); lineTo(0.75f, 0.25f)
            },
            AlienType.BOSS to Path().apply {
                moveTo(0f, -0.5f); lineTo(0.5f, -0.25f); lineTo(0.5f, 0.25f); lineTo(0.25f, 0.5f); lineTo(-0.25f, 0.5f); lineTo(-0.5f, 0.25f); lineTo(-0.5f, -0.25f); close()
                moveTo(-0.15f, -0.5f); lineTo(-0.25f, -0.75f); moveTo(0.15f, -0.5f); lineTo(0.25f, -0.75f)
            },
            AlienType.SUPERBOSS to Path().apply {
                moveTo(0f, -0.5f); lineTo(0.5f, -0.25f); lineTo(0.5f, 0.25f); lineTo(0.25f, 0.5f); lineTo(-0.25f, 0.5f); lineTo(-0.5f, 0.25f); lineTo(-0.5f, -0.25f); close()
                moveTo(-0.5f, 0f); lineTo(-1.0f, -0.5f); lineTo(-0.5f, 0.1f); moveTo(0.5f, 0f); lineTo(1.0f, -0.5f); lineTo(0.5f, 0.1f)
            }
        ),
        "boss_a" to mapOf(
            AlienType.BOSS to Path().apply {
                // Spiky boss
                moveTo(0f, -0.7f)
                lineTo(0.2f, -0.2f); lineTo(0.7f, -0.2f); lineTo(0.3f, 0.1f); lineTo(0.5f, 0.6f)
                lineTo(0f, 0.3f); lineTo(-0.5f, 0.6f); lineTo(-0.3f, 0.1f); lineTo(-0.7f, -0.2f)
                lineTo(-0.2f, -0.2f); close()
            }
        ),
        "boss_b" to mapOf(
            AlienType.BOSS to Path().apply {
                // Shield-like boss
                moveTo(-0.5f, -0.5f); lineTo(0.5f, -0.5f); lineTo(0.5f, 0.2f); lineTo(0f, 0.7f); lineTo(-0.5f, 0.2f); close()
                moveTo(-0.2f, -0.5f); lineTo(-0.2f, -0.7f); lineTo(0.2f, -0.7f); lineTo(0.2f, -0.5f)
            }
        ),
        "superboss_0" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Classic Hex
                moveTo(0f, -0.5f); lineTo(0.5f, -0.25f); lineTo(0.5f, 0.25f); lineTo(0.25f, 0.5f); lineTo(-0.25f, 0.5f); lineTo(-0.5f, 0.25f); lineTo(-0.5f, -0.25f); close()
                moveTo(-0.5f, 0f); lineTo(-1.0f, -0.5f); lineTo(-0.5f, 0.1f); moveTo(0.5f, 0f); lineTo(1.0f, -0.5f); lineTo(0.5f, 0.1f)
            }
        ),
        "superboss_1" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Crab-like
                moveTo(-0.3f, -0.5f); lineTo(0.3f, -0.5f); lineTo(0.6f, 0f); lineTo(0.3f, 0.5f); lineTo(-0.3f, 0.5f); lineTo(-0.6f, 0f); close()
                moveTo(-0.6f, 0f); lineTo(-1.0f, -0.4f); lineTo(-0.8f, 0.2f); close()
                moveTo(0.6f, 0f); lineTo(1.0f, -0.4f); lineTo(0.8f, 0.2f); close()
            }
        ),
        "superboss_2" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Bat-like
                moveTo(0f, 0.2f); lineTo(0.3f, -0.1f); lineTo(1.0f, -0.6f); lineTo(0.5f, 0.5f); lineTo(0f, 0.2f)
                lineTo(-0.5f, 0.5f); lineTo(-1.0f, -0.6f); lineTo(-0.3f, -0.1f); close()
                addOval(androidx.compose.ui.geometry.Rect(-0.2f, -0.5f, 0.2f, -0.1f))
            }
        ),
        "superboss_3" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Spider-like
                addOval(androidx.compose.ui.geometry.Rect(-0.4f, -0.4f, 0.4f, 0.4f))
                for (i in 0..3) {
                    val angle = (i * 45f) * (Math.PI / 180f).toFloat()
                    moveTo(0.4f * Math.cos(angle.toDouble()).toFloat(), 0.4f * Math.sin(angle.toDouble()).toFloat())
                    lineTo(0.8f * Math.cos(angle.toDouble()).toFloat(), 0.8f * Math.sin(angle.toDouble()).toFloat())
                }
                for (i in 0..3) {
                    val angle = (180f + i * 45f) * (Math.PI / 180f).toFloat()
                    moveTo(0.4f * Math.cos(angle.toDouble()).toFloat(), 0.4f * Math.sin(angle.toDouble()).toFloat())
                    lineTo(0.8f * Math.cos(angle.toDouble()).toFloat(), 0.8f * Math.sin(angle.toDouble()).toFloat())
                }
            }
        ),
        "superboss_4" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Star/Nova
                for (i in 0 until 8) {
                    val angle = (i * 45f) * (Math.PI / 180f).toFloat()
                    val r = if (i % 2 == 0) 0.8f else 0.3f
                    val px = r * Math.cos(angle.toDouble()).toFloat()
                    val py = r * Math.sin(angle.toDouble()).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
        ),
        "superboss_5" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Tank/Blocky
                addRect(androidx.compose.ui.geometry.Rect(-0.6f, -0.4f, 0.6f, 0.4f))
                addRect(androidx.compose.ui.geometry.Rect(-0.2f, -0.7f, 0.2f, -0.4f))
                addRect(androidx.compose.ui.geometry.Rect(-0.8f, -0.2f, -0.6f, 0.2f))
                addRect(androidx.compose.ui.geometry.Rect(0.6f, -0.2f, 0.8f, 0.2f))
            }
        ),
        "superboss_6" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Scythe/Bird
                moveTo(0f, -0.5f); lineTo(0.2f, 0f); lineTo(1.0f, 0.5f); lineTo(0.2f, 0.3f); lineTo(0f, 0.8f)
                lineTo(-0.2f, 0.3f); lineTo(-1.0f, 0.5f); lineTo(-0.2f, 0f); close()
            }
        ),
        "superboss_7" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Orbital
                addOval(androidx.compose.ui.geometry.Rect(-0.3f, -0.3f, 0.3f, 0.3f))
                addOval(androidx.compose.ui.geometry.Rect(-0.8f, -0.1f, -0.5f, 0.1f))
                addOval(androidx.compose.ui.geometry.Rect(0.5f, -0.1f, 0.8f, 0.1f))
                addOval(androidx.compose.ui.geometry.Rect(-0.1f, -0.8f, 0.1f, -0.5f))
            }
        ),
        "superboss_8" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Trident
                moveTo(-0.5f, 0.5f); lineTo(-0.5f, -0.2f); lineTo(-0.7f, -0.5f); lineTo(-0.3f, -0.5f); lineTo(-0.3f, -0.2f); lineTo(0f, -0.4f)
                lineTo(0.3f, -0.2f); lineTo(0.3f, -0.5f); lineTo(0.7f, -0.5f); lineTo(0.5f, -0.2f); lineTo(0.5f, 0.5f); close()
            }
        ),
        "superboss_9" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                // Hammerhead
                moveTo(-0.8f, -0.5f); lineTo(0.8f, -0.5f); lineTo(0.8f, -0.2f); lineTo(0.2f, 0f); lineTo(0.2f, 0.6f); lineTo(-0.2f, 0.6f); lineTo(-0.2f, 0f); lineTo(-0.8f, -0.2f); close()
            }
        )
    )
}
