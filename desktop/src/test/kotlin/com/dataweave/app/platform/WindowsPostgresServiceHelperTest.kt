package com.dataweave.app.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WindowsPostgresServiceHelperTest {
    @Test
    fun parseScOutputFindsPostgresServices() {
        val lines =
            listOf(
                "SERVICE_NAME: postgresql-x64-16",
                "DISPLAY_NAME: PostgreSQL 16",
                "STATE              : 1  STOPPED",
                "",
                "SERVICE_NAME: Spooler",
                "DISPLAY_NAME: Print Spooler",
            )
        val found = WindowsPostgresServiceHelper.parseScOutput(lines)
        assertEquals(1, found.size)
        assertEquals("postgresql-x64-16", found.first().name)
    }

    @Test
    fun startMessageMentionsUac() {
        assertTrue(WindowsPostgresServiceHelper.startServiceMessage("postgresql-x64-16").contains("Administrator"))
    }
}
