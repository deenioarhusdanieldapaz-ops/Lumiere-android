package com.lumiere.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationsRepository(private val prefs: SharedPreferences) {
    companion object {
        const val KEY_ENABLED = "notifications_enabled"
        const val KEY_SOUND = "notifications_sound"
        const val KEY_VIBRATION = "notifications_vibration"
        const val KEY_VOLUME = "notifications_volume"
        const val KEY_TONE = "notifications_tone"
        const val KEY_SILENCE = "notifications_silence"
        const val KEY_CRITICAL = "notifications_critical"
        const val KEY_M01 = "notif_m01"; const val KEY_M02 = "notif_m02"; const val KEY_M03 = "notif_m03"
        const val KEY_M04 = "notif_m04"; const val KEY_M05 = "notif_m05"
        private const val KEY_MIGRATED = "notif_migrated"

        fun fromContext(context: Context): NotificationsRepository =
            NotificationsRepository(context.getSharedPreferences("lumiere_prefs", Context.MODE_PRIVATE))
    }

    val enabled = MutableStateFlow(readWithMigration())
    val soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val volume = MutableStateFlow(prefs.getInt(KEY_VOLUME, 75))
    val tone = MutableStateFlow(prefs.getString(KEY_TONE, "Lumière Chime") ?: "Lumière Chime")
    val silenceEnabled = MutableStateFlow(prefs.getBoolean(KEY_SILENCE, false))
    val criticalAlerts = MutableStateFlow(prefs.getBoolean(KEY_CRITICAL, true))
    
    val m01 = MutableStateFlow(prefs.getBoolean(KEY_M01, true)); val m02 = MutableStateFlow(prefs.getBoolean(KEY_M02, true))
    val m03 = MutableStateFlow(prefs.getBoolean(KEY_M03, true)); val m04 = MutableStateFlow(prefs.getBoolean(KEY_M04, true))
    val m05 = MutableStateFlow(prefs.getBoolean(KEY_M05, true))

    fun setEnabled(v: Boolean) { prefs.edit().putBoolean(KEY_ENABLED, v).apply(); enabled.value = v }
    fun setSoundEnabled(v: Boolean) { prefs.edit().putBoolean(KEY_SOUND, v).apply(); soundEnabled.value = v }
    fun setVibrationEnabled(v: Boolean) { prefs.edit().putBoolean(KEY_VIBRATION, v).apply(); vibrationEnabled.value = v }
    fun setVolume(v: Int) { prefs.edit().putInt(KEY_VOLUME, v).apply(); volume.value = v }
    fun setTone(v: String) { prefs.edit().putString(KEY_TONE, v).apply(); tone.value = v }
    fun setSilenceEnabled(v: Boolean) { prefs.edit().putBoolean(KEY_SILENCE, v).apply(); silenceEnabled.value = v }
    fun setCriticalAlerts(v: Boolean) { prefs.edit().putBoolean(KEY_CRITICAL, v).apply(); criticalAlerts.value = v }
    
    fun setM01(v: Boolean) { prefs.edit().putBoolean(KEY_M01, v).apply(); m01.value = v }
    fun setM02(v: Boolean) { prefs.edit().putBoolean(KEY_M02, v).apply(); m02.value = v }
    fun setM03(v: Boolean) { prefs.edit().putBoolean(KEY_M03, v).apply(); m03.value = v }
    fun setM04(v: Boolean) { prefs.edit().putBoolean(KEY_M04, v).apply(); m04.value = v }
    fun setM05(v: Boolean) { prefs.edit().putBoolean(KEY_M05, v).apply(); m05.value = v }

    private fun readWithMigration(): Boolean {
        if (prefs.getBoolean(KEY_MIGRATED, false)) return prefs.getBoolean(KEY_ENABLED, false)
        val isExistingUser = prefs.all.keys.any { it != KEY_ENABLED && it != KEY_MIGRATED }
        prefs.edit().putBoolean(KEY_ENABLED, isExistingUser).putBoolean(KEY_MIGRATED, true).apply()
        return isExistingUser
    }
}
