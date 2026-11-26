package com.example.maintenancelog.ui

import com.example.maintenancelog.data.local.MaintenanceLogEntity

data class LogListUiState(
    val logs: List<MaintenanceLogEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
