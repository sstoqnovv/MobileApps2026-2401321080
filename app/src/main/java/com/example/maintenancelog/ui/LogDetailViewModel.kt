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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    private val repository: MaintenanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogDetailUiState())
    val uiState: StateFlow<LogDetailUiState> = _uiState.asStateFlow()

    fun loadLog(id: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSaved = false,
                    isDeleted = false,
                    errorMessage = null
                )
            }
            runCatching { repository.getLogById(id) }
                .onSuccess { log ->
                    _uiState.update {
                        it.copy(
                            log = log,
                            isLoading = false,
                            errorMessage = if (log == null) NOT_FOUND_ERROR else null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: LOAD_ERROR
                        )
                    }
                }
        }
    }

    fun saveLog(log: MaintenanceLogEntity) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, isSaved = false, errorMessage = null)
            }
            runCatching {
                if (log.id == 0L) {
                    log.copy(id = repository.insertLog(log))
                } else {
                    repository.updateLog(log)
                    log
                }
            }.onSuccess { savedLog ->
                _uiState.update {
                    it.copy(log = savedLog, isSaving = false, isSaved = true)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: SAVE_ERROR
                    )
                }
            }
        }
    }

    fun deleteLog(log: MaintenanceLogEntity) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isDeleting = true, isDeleted = false, errorMessage = null)
            }
            runCatching { repository.deleteLog(log) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            log = null,
                            isDeleting = false,
                            isDeleted = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = throwable.message ?: DELETE_ERROR
                        )
                    }
                }
        }
    }

    fun deleteLogById(id: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isDeleting = true, isDeleted = false, errorMessage = null)
            }
            runCatching { repository.deleteLogById(id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            log = null,
                            isDeleting = false,
                            isDeleted = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = throwable.message ?: DELETE_ERROR
                        )
                    }
                }
        }
    }

    private companion object {
        const val LOAD_ERROR = "Failed to load the maintenance log."
        const val NOT_FOUND_ERROR = "Maintenance log not found."
        const val SAVE_ERROR = "Failed to save the maintenance log."
        const val DELETE_ERROR = "Failed to delete the maintenance log."
    }
}
