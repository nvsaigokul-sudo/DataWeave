package com.dataweave.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuntimeModuleRequirementsTest {
    @Test
    fun missingFrom_detects_absent_tokens() {
        val line = "MODULES=java.base java.desktop java.sql"
        val missing = RuntimeModuleRequirements.missingFrom(line)
        assertTrue("java.xml" in missing)
        assertTrue("jdk.accessibility" in missing)
        assertEquals(emptyList(), RuntimeModuleRequirements.missingFrom(line + " java.xml java.naming java.net.http java.prefs jdk.accessibility jdk.crypto.ec jdk.unsupported"))
    }

    @Test
    fun missingFrom_blank_line_requires_all() {
        assertEquals(RuntimeModuleRequirements.requiredTokens, RuntimeModuleRequirements.missingFrom(null))
        assertEquals(RuntimeModuleRequirements.requiredTokens, RuntimeModuleRequirements.missingFrom("  "))
    }
}
