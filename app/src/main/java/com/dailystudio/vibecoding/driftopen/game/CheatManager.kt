package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.CheatType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CheatManager {
    private var startScreenTapCount = 0
    private val _activatedCheats = MutableStateFlow<Set<CheatType>>(emptySet())
    val activatedCheats = _activatedCheats.asStateFlow()

    fun onStartScreenTap(): Boolean {
        startScreenTapCount++
        return startScreenTapCount >= 10
    }

    fun validateCode(code: String): Pair<CheatType, Int?>? {
        return when {
            code == "7777" -> CheatType.INVINCIBILITY to null
            code == "9999" -> CheatType.LIVES_99 to null
            // Level Select: X + 2 digits + X (e.g., X05X)
            code.startsWith("X") && code.endsWith("X") && code.length == 4 -> {
                val levelStr = code.substring(1, 3)
                val level = levelStr.toIntOrNull()
                if (level != null && level in 1..99) {
                    CheatType.LEVEL_SELECT to level
                } else null
            }
            else -> null
        }
    }

    fun activateCheat(cheat: CheatType) {
        _activatedCheats.update { it + cheat }
    }

    fun deactivateCheat(cheat: CheatType) {
        _activatedCheats.update { it - cheat }
    }

    fun resetTaps() {
        startScreenTapCount = 0
    }

    fun resetAll() {
        startScreenTapCount = 0
        _activatedCheats.value = emptySet()
    }
}
