package com.dataweave.app

import com.dataweave.backend.DesktopSession
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression: Spring Boot defaults headless=true, which breaks Compose Desktop on Windows GUI launch. */
class SpringHeadlessPropertyTest {
    @Test
    fun springBootStartDefaultsHeadlessTrue() {
        System.clearProperty("java.awt.headless")
        DesktopSession.start("--dataweave.test.mode=true").use {
            assertEquals("true", System.getProperty("java.awt.headless"))
        }
    }

    @Test
    fun startForGuiKeepsNonHeadlessForCompose() {
        System.clearProperty("java.awt.headless")
        DesktopSession.startForGui("--dataweave.test.mode=true").use {
            assertEquals("false", System.getProperty("java.awt.headless"))
        }
    }

}
