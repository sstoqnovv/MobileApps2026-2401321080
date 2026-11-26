package com.example.maintenancelog.data.repository

import com.example.maintenancelog.data.local.MaintenanceLogDao
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceLogDao: MaintenanceLogDao
) : MaintenanceRepository {

    override fun getAllLogs(): Flow<List<MaintenanceLogEntity>> =
        maintenanceLogDao.getAll()

    override suspend fun getLogById(id: Long): MaintenanceLogEntity? =
        maintenanceLogDao.getById(id)

    override suspend fun insertLog(log: MaintenanceLogEntity): Long =
        maintenanceLogDao.insert(log)

    override suspend fun updateLog(log: MaintenanceLogEntity) =
        maintenanceLogDao.update(log)

    override suspend fun deleteLog(log: MaintenanceLogEntity) =
        maintenanceLogDao.delete(log)

    override suspend fun deleteLogById(id: Long) =
        maintenanceLogDao.deleteById(id)
}
