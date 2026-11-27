package com.example.maintenancelog.data.remote

data class MaintenanceLogDto(
    val id: Long? = null,
    val title: String,
    val description: String,
    val category: String,
    val date: Long,
    val cost: Double
)
