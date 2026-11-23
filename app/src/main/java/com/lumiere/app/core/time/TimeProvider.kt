package com.lumiere.app.core.time

import java.util.TimeZone

interface TimeProvider {
    fun currentTimeMillis(): Long
    fun timeZone(): TimeZone = TimeZone.getDefault()
}

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun timeZone(): TimeZone = TimeZone.getDefault()
}

class FixedTimeProvider(
    private val fixedMillis: Long,
    private val tz: TimeZone = TimeZone.getTimeZone("UTC")
) : TimeProvider {
    override fun currentTimeMillis(): Long = fixedMillis
    override fun timeZone(): TimeZone = tz
}
