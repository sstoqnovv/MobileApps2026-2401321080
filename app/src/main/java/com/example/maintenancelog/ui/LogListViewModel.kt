package com.example.maintenancelog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.data.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LogListViewModel @Inject constructor(
    private val repository: MaintenanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogListUiState(isLoading = true))
    val uiState: StateFlow<LogListUiState> = _uiState.asStateFlow()

    private val allLogs = MutableStateFlow<List<MaintenanceLogEntity>>(emptyList())

    init {
        repository.getAllLogs()
            .onEach { logs ->
                allLogs.value = logs
                val categories = logs
                    .map { it.category }
                    .distinctBy { it.lowercase() }
                    .sortedBy { it.lowercase() }
                _uiState.update { state ->
                    val selectedCategory = state.selectedCategory?.takeIf { selected ->
                        categories.any { it.equals(selected, ignoreCase = true) }
                    }
                    state.copy(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                applyFilters()
            }
            .catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: LOAD_ERROR
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val category = state.selectedCategory
        val filteredLogs = allLogs.value.filter { log ->
            val matchesQuery = query.isEmpty() ||
                log.title.contains(query, ignoreCase = true) ||
                log.description.contains(query, ignoreCase = true)
            val matchesCategory = category == null ||
                log.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
        _uiState.update {
            it.copy(
                logs = filteredLogs,
                totalCost = filteredLogs.sumOf { log -> log.cost }
            )
        }
    }

    fun refreshLogs() {
        if (_uiState.value.isSyncing) {
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSyncing = true, errorMessage = null)
            }
            repository.refreshLogs()
                .onSuccess {
                    _uiState.update { it.copy(isSyncing = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            errorMessage = throwable.message ?: SYNC_ERROR
                        )
                    }
                }
        }
    }

    fun deleteLog(log: MaintenanceLogEntity) {
        viewModelScope.launch {
            runCatching { repository.deleteLog(log) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: DELETE_ERROR)
                    }
                }
        }
    }

    fun deleteLogById(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteLogById(id) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: DELETE_ERROR)
                    }
                }
        }
    }

    private companion object {
        const val LOAD_ERROR = "Failed to load maintenance logs."
        const val SYNC_ERROR = "Failed to sync maintenance logs."
        const val DELETE_ERROR = "Failed to delete the maintenance log."
    }
}
