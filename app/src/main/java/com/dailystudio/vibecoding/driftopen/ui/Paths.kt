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
        "boss_0" to mapOf(
            AlienType.BOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.5f, -0.3f, 0.5f, 0.3f)); moveTo(-0.5f, 0f); lineTo(-0.8f, -0.4f); moveTo(0.5f, 0f); lineTo(0.8f, -0.4f)
            }
        ),
        "boss_1" to mapOf(
            AlienType.BOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.4f, 0f, 0.4f, 0.4f)); addRect(androidx.compose.ui.geometry.Rect(0.2f, -0.4f, 0.4f, 0f))
            }
        ),
        "boss_2" to mapOf(
            AlienType.BOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.3f, -0.5f, 0.3f, 0.5f)); moveTo(0.3f, 0f); lineTo(0.7f, -0.6f)
            }
        ),
        "boss_3" to mapOf(
            AlienType.BOSS to Path().apply {
                moveTo(0f, -0.6f); lineTo(0f, 0.6f); moveTo(-0.4f, -0.4f); lineTo(0f, -0.2f); lineTo(0f, -0.6f); close(); moveTo(0.4f, -0.4f); lineTo(0f, -0.2f); lineTo(0f, -0.6f); close()
            }
        ),
        "boss_4" to mapOf(
            AlienType.BOSS to Path().apply {
                moveTo(0f, -0.6f); lineTo(-0.5f, 0.5f); lineTo(0.5f, 0.5f); close()
            }
        ),
        "boss_5" to mapOf(
            AlienType.BOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.2f, -0.4f, 0.2f, 0.4f)); addOval(androidx.compose.ui.geometry.Rect(-0.6f, -0.2f, -0.2f, 0.2f)); addOval(androidx.compose.ui.geometry.Rect(0.2f, -0.2f, 0.6f, 0.2f))
            }
        ),
        "boss_6" to mapOf(
            AlienType.BOSS to Path().apply {
                moveTo(0f, 0.6f); lineTo(0f, -0.4f); moveTo(-0.5f, -0.4f); lineTo(0.5f, -0.4f); lineTo(0f, -0.2f); close()
            }
        ),
        "boss_7" to mapOf(
            AlienType.BOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.5f, -0.5f, 0f, 0.5f)); addOval(androidx.compose.ui.geometry.Rect(0f, -0.5f, 0.5f, 0.5f))
            }
        ),
        "boss_8" to mapOf(
            AlienType.BOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.6f, -0.3f, -0.1f, 0.3f)); addOval(androidx.compose.ui.geometry.Rect(0.1f, -0.3f, 0.6f, 0.3f))
            }
        ),
        "boss_9" to mapOf(
            AlienType.BOSS to Path().apply {
                moveTo(0f, -0.4f); lineTo(-0.6f, 0.2f); lineTo(-0.2f, 0.4f); lineTo(0f, 0.8f); lineTo(0.2f, 0.4f); lineTo(0.6f, 0.2f); close()
            }
        ),
        "superboss_0" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.6f, -0.6f, 0.6f, 0.6f)); moveTo(-0.8f, -0.2f); lineTo(-0.5f, 0f); moveTo(0.8f, -0.2f); lineTo(0.5f, 0f)
            }
        ),
        "superboss_1" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                moveTo(0f, 0.8f); lineTo(0f, 0f); moveTo(0f, 0f); lineTo(-0.6f, -0.6f); moveTo(0f, 0f); lineTo(-0.2f, -0.8f); moveTo(0f, 0f); lineTo(0.2f, -0.8f); moveTo(0f, 0f); lineTo(0.6f, -0.6f)
            }
        ),
        "superboss_2" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.7f, -0.4f, 0.7f, 0.4f)); moveTo(-0.7f, 0f); lineTo(-0.9f, -0.5f); moveTo(0.7f, 0f); lineTo(0.9f, -0.5f)
            }
        ),
        "superboss_3" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.2f, -0.5f, 0.2f, 0.5f)); moveTo(-0.2f, -0.5f); lineTo(-0.6f, -0.8f); moveTo(0.2f, -0.5f); lineTo(0.6f, -0.8f)
            }
        ),
        "superboss_4" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                moveTo(0f, -0.8f); lineTo(-0.9f, 0.2f); lineTo(-0.3f, 0.2f); lineTo(0f, 0.8f); lineTo(0.3f, 0.2f); lineTo(0.9f, 0.2f); close()
            }
        ),
        "superboss_5" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.4f, -0.4f, 0.4f, 0.4f)); moveTo(-0.4f, -0.4f); lineTo(-0.8f, -0.8f); moveTo(0.4f, -0.4f); lineTo(0.8f, -0.8f)
            }
        ),
        "superboss_6" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.8f, -0.2f, -0.4f, 0.6f)); addRect(androidx.compose.ui.geometry.Rect(-0.2f, -0.2f, 0.2f, 0.6f)); addRect(androidx.compose.ui.geometry.Rect(0.4f, -0.2f, 0.8f, 0.6f))
            }
        ),
        "superboss_7" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.3f, -0.3f, 0.3f, 0.3f)); moveTo(-0.8f, -0.8f); lineTo(0.8f, 0.8f); moveTo(-0.8f, 0.8f); lineTo(0.8f, -0.8f)
            }
        ),
        "superboss_8" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.7f, -0.5f, -0.3f, 0.5f)); addRect(androidx.compose.ui.geometry.Rect(-0.2f, -0.5f, 0.2f, 0.5f)); addRect(androidx.compose.ui.geometry.Rect(0.3f, -0.5f, 0.7f, 0.5f))
            }
        ),
        "superboss_9" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                moveTo(0f, 0.8f); lineTo(-0.6f, 0.4f); lineTo(0.6f, 0f); lineTo(-0.6f, -0.4f); lineTo(0.4f, -0.8f)
            }
        ),
        "superboss_10" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.8f, -0.6f, -0.2f, 0f)); addOval(androidx.compose.ui.geometry.Rect(-0.3f, -0.8f, 0.3f, -0.2f)); addOval(androidx.compose.ui.geometry.Rect(0.2f, -0.6f, 0.8f, 0f))
            }
        ),
        "superboss_11" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.5f, 0f, 0.5f, 0.8f)); addRect(androidx.compose.ui.geometry.Rect(-0.8f, 0.6f, -0.5f, 0.8f)); addRect(androidx.compose.ui.geometry.Rect(0.5f, 0.6f, 0.8f, 0.8f))
            }
        ),
        "superboss_12" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                moveTo(-0.8f, 0.5f); lineTo(0f, -0.8f); lineTo(0.6f, -0.2f); lineTo(0.2f, 0.8f); close()
            }
        ),
        "superboss_13" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                moveTo(0f, 0.5f); lineTo(-0.9f, -0.5f); lineTo(-0.3f, -0.7f); lineTo(0f, -0.9f); lineTo(0.3f, -0.7f); lineTo(0.9f, -0.5f); close()
            }
        ),
        "superboss_14" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.8f, -0.4f, 0.6f, 0.4f)); moveTo(0.6f, 0f); lineTo(0.9f, -0.4f); lineTo(0.9f, 0.4f); close()
            }
        ),
        "superboss_15" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.5f, -0.5f, 0.5f, 0.5f)); moveTo(-0.5f, 0f); lineTo(-0.9f, -0.2f); moveTo(-0.5f, 0.2f); lineTo(-0.9f, 0.4f); moveTo(0.5f, 0f); lineTo(0.9f, -0.2f); moveTo(0.5f, 0.2f); lineTo(0.9f, 0.4f)
            }
        ),
        "superboss_16" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.6f, -0.8f, 0.6f, 0.8f))
            }
        ),
        "superboss_17" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                moveTo(0f, -0.9f); lineTo(-0.5f, 0.9f); lineTo(0.5f, 0.9f); close(); moveTo(-0.8f, -0.2f); lineTo(0.8f, -0.2f)
            }
        ),
        "superboss_18" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(-0.6f, -0.5f, -0.1f, 0.1f)); addOval(androidx.compose.ui.geometry.Rect(0.1f, -0.5f, 0.6f, 0.1f)); addRect(androidx.compose.ui.geometry.Rect(-0.4f, 0.1f, 0.4f, 0.7f))
            }
        ),
        "superboss_19" to mapOf(
            AlienType.SUPERBOSS to Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(-0.3f, -0.8f, 0.3f, 0f)); moveTo(0f, 0f); lineTo(0.6f, 0.4f); lineTo(-0.6f, 0.8f)
            }
        )
    )
}
