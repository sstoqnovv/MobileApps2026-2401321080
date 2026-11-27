package com.example.maintenancelog.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MaintenanceLogApiService {

    @GET("maintenance-logs")
    suspend fun getLogs(): List<MaintenanceLogDto>

    @GET("maintenance-logs/{id}")
    suspend fun getLogById(@Path("id") id: Long): MaintenanceLogDto

    @POST("maintenance-logs")
    suspend fun createLog(@Body log: MaintenanceLogDto): MaintenanceLogDto

    @PUT("maintenance-logs/{id}")
    suspend fun updateLog(
        @Path("id") id: Long,
        @Body log: MaintenanceLogDto
    ): MaintenanceLogDto

    @DELETE("maintenance-logs/{id}")
    suspend fun deleteLog(@Path("id") id: Long)
}
