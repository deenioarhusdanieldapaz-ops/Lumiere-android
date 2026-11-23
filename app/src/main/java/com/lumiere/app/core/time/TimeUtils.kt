package com.lumiere.app.core.time

import java.util.Calendar
import java.util.TimeZone

object TimeUtils {
    fun dayKey(millis: Long, tz: TimeZone): Long {
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = millis
        val year = cal.get(Calendar.YEAR).toLong()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR).toLong()
        return year * 1000L + dayOfYear
    }

    fun startOfDay(millis: Long, tz: TimeZone): Long {
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
