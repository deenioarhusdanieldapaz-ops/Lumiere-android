package com.lumiere.app.core.time

/**
 * Contrato de tempo injetável (C9).
 * Permite testar regras STRICT (prazos, conflitos) de forma determinística.
 */
interface TimeProvider {
    fun currentTimeMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
