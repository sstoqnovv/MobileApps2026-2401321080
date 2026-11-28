package com.example.maintenancelog.data.preferences

import android.content.Context

class ThemePreferences(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun getSavedDarkMode(): Boolean? =
        if (preferences.contains(KEY_DARK_MODE)) {
            preferences.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }

    fun saveDarkMode(isDark: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "maintenance_log_preferences"
        const val KEY_DARK_MODE = "dark_mode"
    }
}
