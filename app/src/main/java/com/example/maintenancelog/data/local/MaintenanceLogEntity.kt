package com.example.maintenancelog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_logs")
data class MaintenanceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val category: String,
    val date: Long,
    val cost: Double
)
