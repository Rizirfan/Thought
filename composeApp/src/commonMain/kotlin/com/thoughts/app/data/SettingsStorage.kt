package com.thoughts.app.data

/**
 * Tiny platform key/value store for user preferences.
 * Android uses SharedPreferences; desktop uses java.util.prefs.
 */
expect object SettingsStorage {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun getFloat(key: String, default: Float): Float
    fun putFloat(key: String, value: Float)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}
