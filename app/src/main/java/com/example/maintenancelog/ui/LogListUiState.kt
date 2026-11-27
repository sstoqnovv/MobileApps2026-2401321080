package com.example.maintenancelog.ui

import com.example.maintenancelog.data.local.MaintenanceLogEntity

data class LogListUiState(
    val logs: List<MaintenanceLogEntity> = emptyList(),
    val totalCost: Double = 0.0,
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)
