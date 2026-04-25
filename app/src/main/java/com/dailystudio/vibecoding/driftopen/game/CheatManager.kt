package com.dailystudio.vibecoding.driftopen.game

import com.dailystudio.vibecoding.driftopen.game.models.CheatType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CheatManager {
    private var startScreenTapCount = 0
    private val _activatedCheats = MutableStateFlow<Set<CheatType>>(emptySet())
    val activatedCheats = _activatedCheats.asStateFlow()

    fun onStartScreenTap() {
        startScreenTapCount++
        if (startScreenTapCount >= 10) {
            activateCheat(CheatType.INVINCIBILITY)
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
