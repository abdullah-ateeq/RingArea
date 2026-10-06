package com.example.ringarea.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringarea.data.model.SavedCalculation
import com.example.ringarea.data.repository.HistoryRepository
import com.example.ringarea.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val searchQuery: String = "",
    val deleteConfirmId: String? = null,
    val showClearAllDialog: Boolean = false
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val settings = settingsRepository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.ringarea.data.model.UserSettings()
    )

    val calculations: StateFlow<List<SavedCalculation>> = combine(
        historyRepository.historyFlow,
        _uiState
    ) { list, state ->
        if (state.searchQuery.isBlank()) {
            list
        } else {
            val q = state.searchQuery.trim().lowercase()
            list.filter { item ->
                item.projectName.lowercase().contains(q) ||
                item.calculationMode.label.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(id)
        }
    }

    fun duplicateCalculation(id: String) {
        viewModelScope.launch {
            historyRepository.duplicateCalculation(id)
            _snackbarMessage.emit("Calculation duplicated")
        }
    }

    fun requestDelete(id: String) {
        val confirm = settings.value.confirmDelete
        if (confirm) {
            _uiState.value = _uiState.value.copy(deleteConfirmId = id)
        } else {
            deleteCalculation(id)
        }
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(deleteConfirmId = null)
    }

    fun confirmDelete() {
        val id = _uiState.value.deleteConfirmId ?: return
        deleteCalculation(id)
        _uiState.value = _uiState.value.copy(deleteConfirmId = null)
    }

    private fun deleteCalculation(id: String) {
        viewModelScope.launch {
            historyRepository.deleteCalculation(id)
            _snackbarMessage.emit("Calculation deleted")
        }
    }

    fun requestClearAll() {
        val confirm = settings.value.confirmDelete
        if (confirm) {
            _uiState.value = _uiState.value.copy(showClearAllDialog = true)
        } else {
            clearAll()
        }
    }

    fun dismissClearAllDialog() {
        _uiState.value = _uiState.value.copy(showClearAllDialog = false)
    }

    fun confirmClearAll() {
        clearAll()
        _uiState.value = _uiState.value.copy(showClearAllDialog = false)
    }

    private fun clearAll() {
        viewModelScope.launch {
            historyRepository.clearAll()
            _snackbarMessage.emit("History cleared")
        }
    }
}
