package com.lumiere.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsRepository(private val prefs: SharedPreferences) {
    companion object {
        const val PREFS_NAME = "lumiere_prefs"
        const val KEY_ENABLED = "notifications_enabled"
        const val KEY_SOUND = "notifications_sound"
        const val KEY_VIBRATION = "notifications_vibration"
        private const val KEY_MIGRATED = "notifications_enabled_migrated"

        fun fromContext(context: Context): NotificationsRepository =
            NotificationsRepository(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }

    private val _enabled = MutableStateFlow(readWithMigration())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, value).apply()
        _enabled.value = value
    }

    fun setSoundEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, value).apply()
        _soundEnabled.value = value
    }

    fun setVibrationEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, value).apply()
        _vibrationEnabled.value = value
    }

    private fun readWithMigration(): Boolean {
        if (prefs.getBoolean(KEY_MIGRATED, false)) return prefs.getBoolean(KEY_ENABLED, false)
        val isExistingUser = prefs.all.keys.any { it != KEY_ENABLED && it != KEY_MIGRATED }
        prefs.edit().putBoolean(KEY_ENABLED, isExistingUser).putBoolean(KEY_MIGRATED, true).apply()
        return isExistingUser
    }
}
