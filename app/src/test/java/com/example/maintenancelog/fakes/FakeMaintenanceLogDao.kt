package com.example.maintenancelog.fakes

import com.example.maintenancelog.data.local.MaintenanceLogDao
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMaintenanceLogDao : MaintenanceLogDao {

    private val items = MutableStateFlow<List<MaintenanceLogEntity>>(emptyList())

    override fun getAll(): Flow<List<MaintenanceLogEntity>> = items

    override suspend fun getById(id: Long): MaintenanceLogEntity? =
        items.value.firstOrNull { it.id == id }

    override suspend fun insert(log: MaintenanceLogEntity): Long {
        val id = if (log.id == 0L) {
            (items.value.maxOfOrNull { it.id } ?: 0L) + 1L
        } else {
            log.id
        }
        upsert(log.copy(id = id))
        return id
    }

    override suspend fun insertAll(logs: List<MaintenanceLogEntity>) {
        logs.forEach { upsert(it) }
    }

    override suspend fun update(log: MaintenanceLogEntity) {
        upsert(log)
    }

    override suspend fun delete(log: MaintenanceLogEntity) {
        items.value = items.value.filterNot { it.id == log.id }
    }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    fun seed(vararg logs: MaintenanceLogEntity) {
        items.value = logs.toList()
    }

    fun snapshot(): List<MaintenanceLogEntity> = items.value

    private fun upsert(log: MaintenanceLogEntity) {
        items.value = items.value.filterNot { it.id == log.id } + log
    }
}
