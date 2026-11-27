package com.example.maintenancelog.fakes

import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.data.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMaintenanceRepository : MaintenanceRepository {

    val logs = MutableStateFlow<List<MaintenanceLogEntity>>(emptyList())
    val storedLogs = mutableMapOf<Long, MaintenanceLogEntity>()
    val insertedLogs = mutableListOf<MaintenanceLogEntity>()
    val updatedLogs = mutableListOf<MaintenanceLogEntity>()
    val deletedLogs = mutableListOf<MaintenanceLogEntity>()
    val deletedIds = mutableListOf<Long>()

    var nextInsertedId: Long = 1L
    var refreshResult: Result<Unit> = Result.success(Unit)
    var refreshCallCount: Int = 0
        private set

    override fun getAllLogs(): Flow<List<MaintenanceLogEntity>> = logs

    override suspend fun getLogById(id: Long): MaintenanceLogEntity? =
        storedLogs[id]

    override suspend fun insertLog(log: MaintenanceLogEntity): Long {
        insertedLogs += log
        val id = if (log.id == 0L) nextInsertedId else log.id
        storedLogs[id] = log.copy(id = id)
        return id
    }

    override suspend fun updateLog(log: MaintenanceLogEntity) {
        updatedLogs += log
        storedLogs[log.id] = log
    }

    override suspend fun deleteLog(log: MaintenanceLogEntity) {
        deletedLogs += log
        storedLogs.remove(log.id)
    }

    override suspend fun deleteLogById(id: Long) {
        deletedIds += id
        storedLogs.remove(id)
    }

    override suspend fun refreshLogs(): Result<Unit> {
        refreshCallCount++
        return refreshResult
    }
}
