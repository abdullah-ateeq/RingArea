package com.example.ringarea.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.ThemeMode
import com.example.ringarea.data.model.UserSettings
import com.example.ringarea.data.repository.HistoryRepository
import com.example.ringarea.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val historyRepository = HistoryRepository(application)

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserSettings()
    )

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.updateThemeMode(mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateDynamicColor(enabled) }
    }

    fun updateDefaultMode(mode: RingCalculationMode) {
        viewModelScope.launch { settingsRepository.updateDefaultMode(mode) }
    }

    fun updateDefaultUnit(unit: MeasurementUnit) {
        viewModelScope.launch { settingsRepository.updateDefaultUnit(unit) }
    }

    fun updateDecimalPlaces(places: Int) {
        viewModelScope.launch { settingsRepository.updateDecimalPlaces(places) }
    }

    fun updateShowDiagram(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowDiagram(enabled) }
    }

    fun updateShowFormula(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowFormula(enabled) }
    }

    fun updateShowSecondaryResults(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowSecondaryResults(enabled) }
    }

    fun updateShowCircleAreas(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowCircleAreas(enabled) }
    }

    fun updateShowCircumferences(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowCircumferences(enabled) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateHapticFeedback(enabled) }
    }

    fun updateConfirmDelete(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateConfirmDelete(enabled) }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetSettings()
            _snackbarMessage.emit("Settings restored to default")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
            _snackbarMessage.emit("Calculation history cleared")
        }
    }
}
