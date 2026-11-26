package com.example.maintenancelog.data.repository

import com.example.maintenancelog.data.local.MaintenanceLogEntity
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {

    fun getAllLogs(): Flow<List<MaintenanceLogEntity>>

    suspend fun getLogById(id: Long): MaintenanceLogEntity?

    suspend fun insertLog(log: MaintenanceLogEntity): Long

    suspend fun updateLog(log: MaintenanceLogEntity)

    suspend fun deleteLog(log: MaintenanceLogEntity)

    suspend fun deleteLogById(id: Long)
}
