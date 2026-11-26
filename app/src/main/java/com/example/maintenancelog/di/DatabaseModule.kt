package com.example.maintenancelog.di

import android.content.Context
import com.example.maintenancelog.data.local.AppDatabase
import com.example.maintenancelog.data.local.MaintenanceLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideMaintenanceLogDao(database: AppDatabase): MaintenanceLogDao =
        database.maintenanceLogDao()
}
