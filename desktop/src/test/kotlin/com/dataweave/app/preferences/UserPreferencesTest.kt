package com.dataweave.app.preferences

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserPreferencesTest {
    private val priorHome = System.getProperty("user.home")

    @AfterTest
    fun restoreHome() {
        if (priorHome != null) {
            System.setProperty("user.home", priorHome)
        }
    }

    @Test
    fun lastConnectionIdRoundTrip() {
        val dir = Files.createTempDirectory("dw-prefs-test")
        System.setProperty("user.home", dir.toString())
        assertNull(UserPreferences.loadLastConnectionId())
        UserPreferences.saveLastConnectionId("550e8400-e29b-41d4-a716-446655440000")
        assertEquals("550e8400-e29b-41d4-a716-446655440000", UserPreferences.loadLastConnectionId())
        UserPreferences.saveLastConnectionId(null)
        assertNull(UserPreferences.loadLastConnectionId())
    }
}
