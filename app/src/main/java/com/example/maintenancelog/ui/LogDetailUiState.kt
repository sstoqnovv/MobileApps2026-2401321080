package com.example.maintenancelog.ui

import com.example.maintenancelog.data.local.MaintenanceLogEntity

data class LogDetailUiState(
    val log: MaintenanceLogEntity? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val titleError: String? = null,
    val categoryError: String? = null,
    val costError: String? = null,
    val errorMessage: String? = null
)
