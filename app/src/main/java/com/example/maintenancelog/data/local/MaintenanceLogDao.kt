package com.example.maintenancelog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceLogDao {

    @Query("SELECT * FROM maintenance_logs ORDER BY date DESC")
    fun getAll(): Flow<List<MaintenanceLogEntity>>

    @Query("SELECT * FROM maintenance_logs WHERE id = :id")
    suspend fun getById(id: Long): MaintenanceLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MaintenanceLogEntity): Long

    @Update
    suspend fun update(log: MaintenanceLogEntity)

    @Delete
    suspend fun delete(log: MaintenanceLogEntity)

    @Query("DELETE FROM maintenance_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
