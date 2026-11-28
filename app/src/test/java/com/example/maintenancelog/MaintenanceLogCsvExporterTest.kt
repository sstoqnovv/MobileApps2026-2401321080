package com.example.maintenancelog

import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.ui.MaintenanceLogCsvExporter
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceLogCsvExporterTest {

    @Test
    fun `builds header and formatted columns`() {
        val log = MaintenanceLogEntity(
            id = 7L,
            title = "Oil change",
            description = "Changed engine oil",
            category = "Engine",
            date = date(2024, 5, 15),
            cost = 49.99
        )

        val csv = MaintenanceLogCsvExporter.buildCsv(listOf(log))
        val lines = csvLines(csv)

        assertTrue(csv.startsWith(MaintenanceLogCsvExporter.UTF8_BOM))
        assertEquals("ID,Title,Category,Date,Cost,Description", lines[0])
        assertEquals("7,Oil change,Engine,2024-05-15,49.99,Changed engine oil", lines[1])
    }

    @Test
    fun `escapes commas quotes and line breaks`() {
        val log = MaintenanceLogEntity(
            id = 2L,
            title = "Oil, filter",
            description = "Replaced \"old\" filter\nand gasket",
            category = "Engine",
            date = date(2024, 1, 2),
            cost = 12.5
        )

        val csv = MaintenanceLogCsvExporter.buildCsv(listOf(log))
        val lines = csvLines(csv)

        assertEquals(
            "2,\"Oil, filter\",Engine,2024-01-02,12.50,\"Replaced \"\"old\"\" filter\nand gasket\"",
            lines[1]
        )
    }

    @Test
    fun `empty list still contains header and zero summary`() {
        val lines = csvLines(MaintenanceLogCsvExporter.buildCsv(emptyList()))

        assertEquals(2, lines.size)
        assertEquals("ID,Title,Category,Date,Cost,Description", lines[0])
        assertEquals("Summary,0 entries,,,0.00,", lines[1])
    }

    @Test
    fun `summary row contains entry count and total cost`() {
        val logs = listOf(
            MaintenanceLogEntity(
                id = 1L,
                title = "First",
                description = "First log",
                category = "Engine",
                date = date(2024, 3, 1),
                cost = 10.5
            ),
            MaintenanceLogEntity(
                id = 2L,
                title = "Second",
                description = "Second log",
                category = "Brakes",
                date = date(2024, 3, 2),
                cost = 20.25
            )
        )

        val lines = csvLines(MaintenanceLogCsvExporter.buildCsv(logs))

        assertEquals(4, lines.size)
        assertEquals("Summary,2 entries,,,30.75,", lines[3])
    }

    private fun csvLines(csv: String): List<String> =
        csv.removePrefix(MaintenanceLogCsvExporter.UTF8_BOM)
            .trimEnd('\r', '\n')
            .split("\r\n")

    private fun date(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month - 1, day)
        return calendar.timeInMillis
    }
}
