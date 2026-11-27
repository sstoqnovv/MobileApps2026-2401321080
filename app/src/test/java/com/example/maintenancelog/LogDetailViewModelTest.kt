package com.example.maintenancelog

import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.fakes.FakeMaintenanceRepository
import com.example.maintenancelog.ui.LogDetailViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LogDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMaintenanceRepository()

    private val newLog = MaintenanceLogEntity(
        id = 0L,
        title = "Oil change",
        description = "Changed engine oil",
        category = "Engine",
        date = 1_000L,
        cost = 45.0
    )

    private val existingLog = newLog.copy(id = 5L)

    @Test
    fun `blank title is rejected and not saved`() = runTest {
        val viewModel = LogDetailViewModel(repository)

        viewModel.saveLog(newLog.copy(title = "   "))

        assertNotNull(viewModel.uiState.value.titleError)
        assertFalse(viewModel.uiState.value.isSaved)
        assertTrue(repository.insertedLogs.isEmpty())
    }

    @Test
    fun `negative or non finite cost is rejected and not saved`() = runTest {
        val negativeCostViewModel = LogDetailViewModel(repository)

        negativeCostViewModel.saveLog(newLog.copy(cost = -1.0))

        assertNotNull(negativeCostViewModel.uiState.value.costError)
        assertFalse(negativeCostViewModel.uiState.value.isSaved)
        assertTrue(repository.insertedLogs.isEmpty())

        val invalidCostViewModel = LogDetailViewModel(repository)

        invalidCostViewModel.saveLog(newLog.copy(cost = Double.NaN))

        assertNotNull(invalidCostViewModel.uiState.value.costError)
        assertTrue(repository.insertedLogs.isEmpty())
    }

    @Test
    fun `new log is inserted`() = runTest {
        repository.nextInsertedId = 9L
        val viewModel = LogDetailViewModel(repository)

        viewModel.saveLog(newLog)

        assertEquals(listOf(newLog), repository.insertedLogs)
        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(9L, viewModel.uiState.value.log?.id)
    }

    @Test
    fun `existing log is loaded`() = runTest {
        repository.storedLogs[existingLog.id] = existingLog
        val viewModel = LogDetailViewModel(repository)

        viewModel.loadLog(existingLog.id)

        assertEquals(existingLog, viewModel.uiState.value.log)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `existing log is updated`() = runTest {
        val editedLog = existingLog.copy(title = "Updated oil change")
        val viewModel = LogDetailViewModel(repository)

        viewModel.saveLog(editedLog)

        assertEquals(listOf(editedLog), repository.updatedLogs)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `existing log is deleted`() = runTest {
        repository.storedLogs[existingLog.id] = existingLog
        val viewModel = LogDetailViewModel(repository)
        viewModel.loadLog(existingLog.id)

        viewModel.deleteLog()

        assertEquals(listOf(existingLog), repository.deletedLogs)
        assertTrue(viewModel.uiState.value.isDeleted)
        assertNull(viewModel.uiState.value.log)
    }
}
