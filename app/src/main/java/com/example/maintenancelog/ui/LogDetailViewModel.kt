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
                    titleError = null,
                    categoryError = null,
                    costError = null,
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
        val titleError = if (log.title.isBlank()) TITLE_REQUIRED else null
        val categoryError = if (log.category.isBlank()) CATEGORY_REQUIRED else null
        val costError = if (!log.cost.isFinite() || log.cost < 0.0) COST_INVALID else null

        if (titleError != null || categoryError != null || costError != null) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    categoryError = categoryError,
                    costError = costError,
                    isSaved = false,
                    errorMessage = null
                )
            }
            return
        }

        val logToSave = log.copy(
            title = log.title.trim(),
            description = log.description.trim(),
            category = log.category.trim(),
            date = if (log.date > 0L) log.date else System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    isSaved = false,
                    titleError = null,
                    categoryError = null,
                    costError = null,
                    errorMessage = null
                )
            }
            runCatching {
                if (logToSave.id == 0L) {
                    logToSave.copy(id = repository.insertLog(logToSave))
                } else {
                    repository.updateLog(logToSave)
                    logToSave
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

    fun deleteLog() {
        val log = _uiState.value.log ?: return
        deleteLog(log)
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
        const val TITLE_REQUIRED = "Title is required."
        const val CATEGORY_REQUIRED = "Category is required."
        const val COST_INVALID = "Price must be a non-negative number."
    }
}
