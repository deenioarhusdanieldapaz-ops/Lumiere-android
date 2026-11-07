package com.lumiere.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SmokeTest {

    @Test
    @DisplayName("Pipeline CI corre JUnit 5")
    fun pipeline_runs_junit5() {
        assertEquals(4, 2 + 2)
    }

    @Test
    @DisplayName("Strings e listas funcionam no CI")
    fun basic_operations_work() {
        val list = listOf(1, 2, 3)
        assertEquals(3, list.size)
        assertEquals("QA", "Q" + "A")
    }
}
