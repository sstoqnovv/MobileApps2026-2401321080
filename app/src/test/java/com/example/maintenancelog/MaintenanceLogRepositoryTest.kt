package com.example.maintenancelog

import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.data.remote.MaintenanceLogDto
import com.example.maintenancelog.data.remote.toEntity
import com.example.maintenancelog.data.repository.MaintenanceRepositoryImpl
import com.example.maintenancelog.fakes.FakeMaintenanceLogApiService
import com.example.maintenancelog.fakes.FakeMaintenanceLogDao
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceLogRepositoryTest {

    private val dao = FakeMaintenanceLogDao()
    private val api = FakeMaintenanceLogApiService()
    private val repository = MaintenanceRepositoryImpl(dao, api)

    private val localLog = MaintenanceLogEntity(
        id = 1L,
        title = "Local log",
        description = "Local description",
        category = "Engine",
        date = 1_000L,
        cost = 10.0
    )

    private val remoteLog = MaintenanceLogDto(
        id = 1L,
        title = "Remote log",
        description = "Remote description",
        category = "Brakes",
        date = 2_000L,
        cost = 20.0
    )

    @Test
    fun `getAllLogs reads from local dao`() = runTest {
        dao.seed(localLog)

        val logs = repository.getAllLogs().first()

        assertEquals(listOf(localLog), logs)
        assertEquals(0, api.getLogsCallCount)
    }

    @Test
    fun `refreshLogs updates cache on success`() = runTest {
        dao.seed(localLog)
        api.remoteLogs = listOf(remoteLog)

        val result = repository.refreshLogs()

        assertTrue(result.isSuccess)
        assertEquals(1, api.getLogsCallCount)
        assertEquals(listOf(remoteLog.toEntity()), dao.snapshot())
    }

    @Test
    fun `refreshLogs keeps local data on network error`() = runTest {
        dao.seed(localLog)
        api.getLogsError = IOException("offline")

        val result = repository.refreshLogs()

        assertTrue(result.isFailure)
        assertEquals(listOf(localLog), dao.snapshot())
        assertEquals(listOf(localLog), repository.getAllLogs().first())
    }
}
