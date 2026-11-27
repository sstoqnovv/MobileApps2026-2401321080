package com.example.maintenancelog.data.remote

import com.example.maintenancelog.data.local.MaintenanceLogEntity

fun MaintenanceLogDto.toEntity(): MaintenanceLogEntity =
    MaintenanceLogEntity(
        id = id ?: 0L,
        title = title,
        description = description,
        category = category,
        date = date,
        cost = cost
    )

fun MaintenanceLogEntity.toDto(): MaintenanceLogDto =
    MaintenanceLogDto(
        id = id,
        title = title,
        description = description,
        category = category,
        date = date,
        cost = cost
    )
