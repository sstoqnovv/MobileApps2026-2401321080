package com.example.maintenancelog.fakes

import com.example.maintenancelog.data.remote.MaintenanceLogApiService
import com.example.maintenancelog.data.remote.MaintenanceLogDto

class FakeMaintenanceLogApiService : MaintenanceLogApiService {

    var remoteLogs: List<MaintenanceLogDto> = emptyList()
    var getLogsError: Throwable? = null
    var getLogsCallCount: Int = 0
        private set

    override suspend fun getLogs(): List<MaintenanceLogDto> {
        getLogsCallCount++
        getLogsError?.let { throw it }
        return remoteLogs
    }

    override suspend fun getLogById(id: Long): MaintenanceLogDto =
        remoteLogs.first { it.id == id }

    override suspend fun createLog(log: MaintenanceLogDto): MaintenanceLogDto = log

    override suspend fun updateLog(
        id: Long,
        log: MaintenanceLogDto
    ): MaintenanceLogDto = log

    override suspend fun deleteLog(id: Long) = Unit
}
