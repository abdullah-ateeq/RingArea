package com.example.ringarea.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.ThemeMode
import com.example.ringarea.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ring_area_prefs")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val SHOW_DIAGRAM = booleanPreferencesKey("show_diagram")
        val SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val SHOW_SECONDARY_RESULTS = booleanPreferencesKey("show_secondary_results")
        val SHOW_CIRCLE_AREAS = booleanPreferencesKey("show_circle_areas")
        val SHOW_CIRCUMFERENCES = booleanPreferencesKey("show_circumferences")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (e: Exception) { ThemeMode.SYSTEM }

        val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true

        val defaultModeStr = preferences[PreferencesKeys.DEFAULT_MODE] ?: RingCalculationMode.CALCULATE.name
        val defaultMode = try { RingCalculationMode.valueOf(defaultModeStr) } catch (e: Exception) { RingCalculationMode.CALCULATE }

        val defaultUnitStr = preferences[PreferencesKeys.DEFAULT_UNIT] ?: MeasurementUnit.CENTIMETER.name
        val defaultUnit = try { MeasurementUnit.valueOf(defaultUnitStr) } catch (e: Exception) { MeasurementUnit.CENTIMETER }

        val decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: 2
        val showDiagram = preferences[PreferencesKeys.SHOW_DIAGRAM] ?: true
        val showFormula = preferences[PreferencesKeys.SHOW_FORMULA] ?: true
        val showSecondaryResults = preferences[PreferencesKeys.SHOW_SECONDARY_RESULTS] ?: true
        val showCircleAreas = preferences[PreferencesKeys.SHOW_CIRCLE_AREAS] ?: true
        val showCircumferences = preferences[PreferencesKeys.SHOW_CIRCUMFERENCES] ?: true
        val hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
        val confirmDelete = preferences[PreferencesKeys.CONFIRM_DELETE] ?: true

        UserSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            defaultMode = defaultMode,
            defaultUnit = defaultUnit,
            decimalPlaces = decimalPlaces,
            showDiagram = showDiagram,
            showFormula = showFormula,
            showSecondaryResults = showSecondaryResults,
            showCircleAreas = showCircleAreas,
            showCircumferences = showCircumferences,
            hapticFeedback = hapticFeedback,
            confirmDelete = confirmDelete
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateDefaultMode(mode: RingCalculationMode) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_MODE] = mode.name }
    }

    suspend fun updateDefaultUnit(unit: MeasurementUnit) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_UNIT] = unit.name }
    }

    suspend fun updateDecimalPlaces(places: Int) {
        context.dataStore.edit { it[PreferencesKeys.DECIMAL_PLACES] = places.coerceIn(0, 4) }
    }

    suspend fun updateShowDiagram(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_DIAGRAM] = enabled }
    }

    suspend fun updateShowFormula(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_FORMULA] = enabled }
    }

    suspend fun updateShowSecondaryResults(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_SECONDARY_RESULTS] = enabled }
    }

    suspend fun updateShowCircleAreas(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_CIRCLE_AREAS] = enabled }
    }

    suspend fun updateShowCircumferences(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_CIRCUMFERENCES] = enabled }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun updateConfirmDelete(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONFIRM_DELETE] = enabled }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.THEME_MODE)
            prefs.remove(PreferencesKeys.DYNAMIC_COLOR)
            prefs.remove(PreferencesKeys.DEFAULT_MODE)
            prefs.remove(PreferencesKeys.DEFAULT_UNIT)
            prefs.remove(PreferencesKeys.DECIMAL_PLACES)
            prefs.remove(PreferencesKeys.SHOW_DIAGRAM)
            prefs.remove(PreferencesKeys.SHOW_FORMULA)
            prefs.remove(PreferencesKeys.SHOW_SECONDARY_RESULTS)
            prefs.remove(PreferencesKeys.SHOW_CIRCLE_AREAS)
            prefs.remove(PreferencesKeys.SHOW_CIRCUMFERENCES)
            prefs.remove(PreferencesKeys.HAPTIC_FEEDBACK)
            prefs.remove(PreferencesKeys.CONFIRM_DELETE)
        }
    }
}
