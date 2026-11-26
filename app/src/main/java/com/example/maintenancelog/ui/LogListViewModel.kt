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

    init {
        repository.getAllLogs()
            .onEach { logs ->
                _uiState.update {
                    it.copy(logs = logs, isLoading = false, errorMessage = null)
                }
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
        const val DELETE_ERROR = "Failed to delete the maintenance log."
    }
}
