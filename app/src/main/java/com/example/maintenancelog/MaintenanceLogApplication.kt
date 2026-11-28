package com.example.maintenancelog

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.maintenancelog.data.preferences.ThemePreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MaintenanceLogApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemePreferences(this).getSavedDarkMode()?.let { isDark ->
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }
}
