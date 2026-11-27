package com.example.maintenancelog

import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.fakes.FakeMaintenanceRepository
import com.example.maintenancelog.ui.LogListViewModel
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LogListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMaintenanceRepository()

    private val oilChange = MaintenanceLogEntity(
        id = 1L,
        title = "Oil change",
        description = "Changed engine oil",
        category = "Engine",
        date = 1_000L,
        cost = 50.0
    )

    private val brakePads = MaintenanceLogEntity(
        id = 2L,
        title = "Brake pads",
        description = "Replaced front pads",
        category = "Brakes",
        date = 2_000L,
        cost = 120.0
    )

    private val tireRotation = MaintenanceLogEntity(
        id = 3L,
        title = "Tire rotation",
        description = "Rotated tires",
        category = "Tires",
        date = 3_000L,
        cost = 30.0
    )

    private fun createViewModel(): LogListViewModel {
        repository.logs.value = listOf(oilChange, brakePads, tireRotation)
        return LogListViewModel(repository)
    }

    @Test
    fun `loads logs and calculates summary`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(3, state.logs.size)
        assertEquals(200.0, state.totalCost, 0.001)
        assertEquals(listOf("Brakes", "Engine", "Tires"), state.categories)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `search filters by title and description case-insensitively`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateSearchQuery("OIL")
        assertEquals(listOf(oilChange), viewModel.uiState.value.logs)

        viewModel.updateSearchQuery("rotated")
        assertEquals(listOf(tireRotation), viewModel.uiState.value.logs)
    }

    @Test
    fun `category filter keeps only selected category`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectCategory("Brakes")
        assertEquals(listOf(brakePads), viewModel.uiState.value.logs)
        assertEquals(120.0, viewModel.uiState.value.totalCost, 0.001)

        viewModel.selectCategory(null)
        assertEquals(3, viewModel.uiState.value.logs.size)
    }

    @Test
    fun `search and category filters combine`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateSearchQuery("pads")
        viewModel.selectCategory("Brakes")
        assertEquals(listOf(brakePads), viewModel.uiState.value.logs)

        viewModel.selectCategory("Tires")
        assertTrue(viewModel.uiState.value.logs.isEmpty())

        viewModel.selectCategory(null)
        assertEquals(listOf(brakePads), viewModel.uiState.value.logs)
    }

    @Test
    fun `sync error keeps local logs visible`() = runTest {
        repository.refreshResult = Result.failure(IOException("offline"))
        val viewModel = createViewModel()

        viewModel.refreshLogs()

        val state = viewModel.uiState.value
        assertEquals(3, state.logs.size)
        assertEquals(200.0, state.totalCost, 0.001)
        assertFalse(state.isSyncing)
        assertNotNull(state.errorMessage)
        assertEquals(1, repository.refreshCallCount)
    }
}
