package com.example.maintenancelog.data.repository

import com.example.maintenancelog.data.local.MaintenanceLogDao
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.data.remote.MaintenanceLogApiService
import com.example.maintenancelog.data.remote.toDto
import com.example.maintenancelog.data.remote.toEntity
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceLogDao: MaintenanceLogDao,
    private val apiService: MaintenanceLogApiService
) : MaintenanceRepository {

    private val remoteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllLogs(): Flow<List<MaintenanceLogEntity>> =
        maintenanceLogDao.getAll()

    override suspend fun getLogById(id: Long): MaintenanceLogEntity? =
        maintenanceLogDao.getById(id)

    override suspend fun insertLog(log: MaintenanceLogEntity): Long {
        val id = maintenanceLogDao.insert(log)
        syncInBackground {
            apiService.createLog(log.copy(id = id).toDto().copy(id = null))
        }
        return id
    }

    override suspend fun updateLog(log: MaintenanceLogEntity) {
        maintenanceLogDao.update(log)
        syncInBackground {
            apiService.updateLog(log.id, log.toDto())
        }
    }

    override suspend fun deleteLog(log: MaintenanceLogEntity) {
        maintenanceLogDao.delete(log)
        syncInBackground {
            apiService.deleteLog(log.id)
        }
    }

    override suspend fun deleteLogById(id: Long) {
        maintenanceLogDao.deleteById(id)
        syncInBackground {
            apiService.deleteLog(id)
        }
    }

    override suspend fun refreshLogs(): Result<Unit> = runCatching {
        val remoteLogs = apiService.getLogs()
        val entities = remoteLogs.mapNotNull { dto ->
            dto.id?.let { dto.toEntity() }
        }
        maintenanceLogDao.insertAll(entities)
    }

    private fun syncInBackground(block: suspend () -> Unit) {
        remoteScope.launch {
            runCatching { block() }
        }
    }
}
