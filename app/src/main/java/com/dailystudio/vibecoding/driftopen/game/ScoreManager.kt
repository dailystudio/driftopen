package com.dailystudio.vibecoding.driftopen.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ScoreManager(private val context: Context) {
    companion object {
        private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
        private val DIFFICULTY_KEY = stringPreferencesKey("difficulty")
    }

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HIGH_SCORE_KEY] ?: 0
    }

    val difficultyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DIFFICULTY_KEY] ?: "easy"
    }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentHighScore = preferences[HIGH_SCORE_KEY] ?: 0
            if (score > currentHighScore) {
                preferences[HIGH_SCORE_KEY] = score
            }
        }
    }

    suspend fun saveDifficulty(difficulty: String) {
        context.dataStore.edit { preferences ->
            preferences[DIFFICULTY_KEY] = difficulty
        }
    }
}
