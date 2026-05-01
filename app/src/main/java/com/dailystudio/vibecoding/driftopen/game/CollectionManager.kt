package com.dailystudio.vibecoding.driftopen.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.collectionDataStore: DataStore<Preferences> by preferencesDataStore(name = "collection")

class CollectionManager(private val context: Context) {
    companion object {
        private val UNLOCKED_ALIENS_KEY = stringSetPreferencesKey("unlocked_aliens")
    }

    val unlockedAliensFlow: Flow<Set<String>> = context.collectionDataStore.data.map { preferences ->
        preferences[UNLOCKED_ALIENS_KEY] ?: setOf("default") // Default skin is always unlocked
    }

    suspend fun unlockAlien(skinId: String) {
        context.collectionDataStore.edit { preferences ->
            val current = preferences[UNLOCKED_ALIENS_KEY] ?: emptySet()
            if (!current.contains(skinId)) {
                preferences[UNLOCKED_ALIENS_KEY] = current + skinId
            }
        }
    }
    
    suspend fun unlockAliens(skinIds: Collection<String>) {
        context.collectionDataStore.edit { preferences ->
            val current = preferences[UNLOCKED_ALIENS_KEY] ?: emptySet()
            preferences[UNLOCKED_ALIENS_KEY] = current + skinIds
        }
    }
}
