package com.thoughts.app.data

import android.content.Context

/**
 * Set once from MainActivity so platform helpers can reach application context
 * without threading it through every composable.
 */
object AppContextHolder {
    lateinit var context: Context
        private set

    fun install(context: Context) {
        this.context = context.applicationContext
    }
}

actual object SettingsStorage {
    private val prefs
        get() = AppContextHolder.context.getSharedPreferences("thoughts_studio", Context.MODE_PRIVATE)

    actual fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getFloat(key: String, default: Float): Float =
        prefs.getFloat(key, default)

    actual fun putFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    actual fun getBoolean(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
