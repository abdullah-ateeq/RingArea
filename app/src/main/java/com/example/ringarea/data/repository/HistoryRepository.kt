package com.example.ringarea.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.ringarea.data.model.SavedCalculation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HistoryRepository(private val context: Context) {

    private val gson = Gson()
    private val historyKey = stringPreferencesKey("history_calculations_json")
    private val maxItems = 50

    val historyFlow: Flow<List<SavedCalculation>> = context.dataStore.data.map { preferences ->
        val json = preferences[historyKey] ?: ""
        if (json.isBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<SavedCalculation>>() {}.type
                val items: List<SavedCalculation> = gson.fromJson(json, type) ?: emptyList()
                sortCalculations(items)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun sortCalculations(list: List<SavedCalculation>): List<SavedCalculation> {
        // Favorites appear first, then sorted by timestamp descending (newest first)
        return list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        context.dataStore.edit { preferences ->
            val json = preferences[historyKey] ?: ""
            val currentList: MutableList<SavedCalculation> = if (json.isBlank()) {
                mutableListOf()
            } else {
                try {
                    val type = object : TypeToken<List<SavedCalculation>>() {}.type
                    gson.fromJson<List<SavedCalculation>>(json, type)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }
            }

            // Remove if already exists with same id, then add new at front
            currentList.removeAll { it.id == calculation.id }
            currentList.add(0, calculation)

            // Keep max 50 items. If > 50, remove the oldest non-favorite or simply oldest.
            // When record #51 is saved, remove oldest.
            while (currentList.size > maxItems) {
                // Find oldest (lowest timestamp)
                val oldest = currentList.minByOrNull { it.timestamp }
                if (oldest != null) {
                    currentList.remove(oldest)
                } else {
                    currentList.removeAt(currentList.lastIndex)
                }
            }

            preferences[historyKey] = gson.toJson(currentList)
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[historyKey] ?: ""
            if (json.isNotBlank()) {
                try {
                    val type = object : TypeToken<List<SavedCalculation>>() {}.type
                    val list: List<SavedCalculation> = gson.fromJson(json, type) ?: emptyList()
                    val updated = list.map { item ->
                        if (item.id == id) item.copy(isFavorite = !item.isFavorite) else item
                    }
                    preferences[historyKey] = gson.toJson(updated)
                } catch (e: Exception) {
                    // Ignore on error
                }
            }
        }
    }

    suspend fun duplicateCalculation(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[historyKey] ?: ""
            if (json.isNotBlank()) {
                try {
                    val type = object : TypeToken<List<SavedCalculation>>() {}.type
                    val currentList: MutableList<SavedCalculation> =
                        gson.fromJson<List<SavedCalculation>>(json, type)?.toMutableList() ?: mutableListOf()
                    val target = currentList.find { it.id == id }
                    if (target != null) {
                        val duplicate = target.copy(
                            id = UUID.randomUUID().toString(),
                            projectName = "Copy of ${target.projectName}",
                            timestamp = System.currentTimeMillis(),
                            isFavorite = false
                        )
                        currentList.add(0, duplicate)
                        while (currentList.size > maxItems) {
                            val oldest = currentList.minByOrNull { it.timestamp }
                            if (oldest != null) {
                                currentList.remove(oldest)
                            } else {
                                currentList.removeAt(currentList.lastIndex)
                            }
                        }
                        preferences[historyKey] = gson.toJson(currentList)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[historyKey] ?: ""
            if (json.isNotBlank()) {
                try {
                    val type = object : TypeToken<List<SavedCalculation>>() {}.type
                    val list: List<SavedCalculation> = gson.fromJson(json, type) ?: emptyList()
                    val updated = list.filter { it.id != id }
                    preferences[historyKey] = gson.toJson(updated)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.remove(historyKey)
        }
    }
}
