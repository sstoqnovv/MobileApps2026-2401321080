package com.example.maintenancelog

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.maintenancelog.data.local.AppDatabase
import com.example.maintenancelog.data.local.MaintenanceLogDao
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaintenanceLogDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: MaintenanceLogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.maintenanceLogDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAll_returnsInsertedLog() = runTest {
        val id = dao.insert(sampleLog(title = "Oil change"))

        val logs = dao.getAll().first()

        assertEquals(1, logs.size)
        assertEquals(id, logs.single().id)
        assertEquals("Oil change", logs.single().title)
        assertEquals("Engine", logs.single().category)
        assertEquals(45.0, logs.single().cost, 0.001)
    }

    @Test
    fun insertAll_upsertsExistingAndAddsNewEntries() = runTest {
        val existingId = dao.insert(sampleLog(title = "Original", cost = 10.0))

        dao.insertAll(
            listOf(
                sampleLog(id = existingId, title = "Updated", cost = 20.0),
                sampleLog(title = "New entry", cost = 30.0)
            )
        )

        val logsByTitle = dao.getAll().first().associateBy { it.title }
        assertEquals(2, logsByTitle.size)
        assertEquals(20.0, logsByTitle.getValue("Updated").cost, 0.001)
        assertEquals(30.0, logsByTitle.getValue("New entry").cost, 0.001)
    }

    @Test
    fun update_changesExistingFields() = runTest {
        val id = dao.insert(sampleLog())

        val updated = sampleLog(
            id = id,
            title = "Brake service",
            description = "Replaced brake pads",
            category = "Brakes",
            date = 2_000L,
            cost = 99.5
        )
        dao.update(updated)

        assertEquals(updated, dao.getById(id))
    }

    @Test
    fun delete_removesEntity() = runTest {
        val id = dao.insert(sampleLog())
        val inserted = dao.getById(id)
        assertNotNull(inserted)

        dao.delete(inserted!!)

        assertNull(dao.getById(id))
        assertTrue(dao.getAll().first().isEmpty())
    }

    @Test
    fun deleteById_removesEntity() = runTest {
        val id = dao.insert(sampleLog())

        dao.deleteById(id)

        assertNull(dao.getById(id))
        assertTrue(dao.getAll().first().isEmpty())
    }

    @Test
    fun getAll_sortsByDateDescending() = runTest {
        dao.insert(sampleLog(title = "Older", date = 1_000L))
        dao.insert(sampleLog(title = "Newer", date = 2_000L))

        val logs = dao.getAll().first()

        assertEquals(listOf("Newer", "Older"), logs.map { it.title })
    }

    @Test
    fun emptyDatabase_returnsEmptyListAndNullForMissingId() = runTest {
        assertTrue(dao.getAll().first().isEmpty())
        assertNull(dao.getById(999L))
    }

    private fun sampleLog(
        id: Long = 0L,
        title: String = "Oil change",
        description: String = "Changed engine oil",
        category: String = "Engine",
        date: Long = 1_000L,
        cost: Double = 45.0
    ): MaintenanceLogEntity =
        MaintenanceLogEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            date = date,
            cost = cost
        )
}
