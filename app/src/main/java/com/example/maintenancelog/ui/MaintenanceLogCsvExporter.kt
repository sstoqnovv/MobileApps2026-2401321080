package com.example.maintenancelog.ui

import android.content.Context
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MaintenanceLogCsvExporter {

    const val UTF8_BOM = "\uFEFF"
    const val EXPORT_DIRECTORY = "exports"
    const val HEADER = "ID,Title,Category,Date,Cost,Description"

    private const val LINE_SEPARATOR = "\r\n"
    private const val DATE_PATTERN = "yyyy-MM-dd"
    private const val FILE_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss_SSS"
    private const val FILE_NAME_PREFIX = "maintenance_logs_"
    private const val FILE_EXTENSION = ".csv"

    fun buildCsv(logs: List<MaintenanceLogEntity>): String = buildString {
        append(UTF8_BOM)
        append(HEADER)
        append(LINE_SEPARATOR)
        logs.forEach { log ->
            append(
                toCsvRow(
                    log.id.toString(),
                    log.title,
                    log.category,
                    formatDate(log.date),
                    formatCost(log.cost),
                    log.description
                )
            )
            append(LINE_SEPARATOR)
        }
        append(
            toCsvRow(
                "Summary",
                "${logs.size} entries",
                "",
                "",
                formatCost(logs.sumOf { log -> log.cost }),
                ""
            )
        )
        append(LINE_SEPARATOR)
    }

    fun exportToCache(context: Context, logs: List<MaintenanceLogEntity>): File {
        val exportDirectory = File(context.cacheDir, EXPORT_DIRECTORY)
        if (!exportDirectory.isDirectory && !exportDirectory.mkdirs()) {
            throw IOException("Unable to create export directory: ${exportDirectory.absolutePath}")
        }
        val fileName = FILE_NAME_PREFIX + formatTimestamp(System.currentTimeMillis()) + FILE_EXTENSION
        return File(exportDirectory, fileName).apply {
            writeText(buildCsv(logs), Charsets.UTF_8)
        }
    }

    private fun toCsvRow(vararg fields: String): String =
        fields.joinToString(separator = ",") { field -> escape(field) }

    private fun escape(value: String): String {
        val requiresQuotes = value.any { character ->
            character == ',' || character == '"' || character == '\n' || character == '\r'
        }
        return if (requiresQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun formatDate(date: Long): String =
        SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date(date))

    private fun formatCost(cost: Double): String =
        BigDecimal.valueOf(cost)
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()

    private fun formatTimestamp(timestamp: Long): String =
        SimpleDateFormat(FILE_TIMESTAMP_PATTERN, Locale.US).format(Date(timestamp))
}
