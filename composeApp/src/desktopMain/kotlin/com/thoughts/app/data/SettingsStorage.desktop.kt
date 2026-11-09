package com.thoughts.app.data

import java.util.prefs.Preferences

actual object SettingsStorage {
    private val prefs: Preferences = Preferences.userRoot().node("com/thoughts/app")

    actual fun getString(key: String, default: String): String =
        prefs.get(key, default)

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    actual fun getFloat(key: String, default: Float): Float =
        prefs.getFloat(key, default)

    actual fun putFloat(key: String, value: Float) {
        prefs.putFloat(key, value)
    }

    actual fun getBoolean(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }
}
