package com.dataweave.app

import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class LaunchDiagnosticsInstallRootTest {
    @Test
    fun resolveInstallRoot_finds_linux_deb_layout() {
        val tmp = Files.createTempDirectory("dataweave-layout")
        tmp.toFile().deleteOnExit()
        val root = tmp.resolve("opt/dataweave")
        root.resolve("bin").createDirectories()
        root.resolve("lib/app").createDirectories()
        root.resolve("lib/runtime").createDirectories()
        Files.createFile(root.resolve("bin/DataWeave"))
        val launcher = root.resolve("bin/DataWeave").toString()
        assertEquals(root, LaunchDiagnostics.resolveInstallRoot(launcher))
    }

    @Test
    fun resolveInstallRoot_finds_windows_msi_layout() {
        val tmp = Files.createTempDirectory("dataweave-win")
        tmp.toFile().deleteOnExit()
        val root = tmp.resolve("DataWeave")
        root.resolve("app").createDirectories()
        root.resolve("runtime").createDirectories()
        Files.createFile(root.resolve("DataWeave.exe"))
        assertEquals(root, LaunchDiagnostics.resolveInstallRoot(root.resolve("DataWeave.exe").toString()))
    }

    @Test
    fun collectLines_reports_runtime_modules_on_linux_layout() {
        val tmp = Files.createTempDirectory("dataweave-diag")
        tmp.toFile().deleteOnExit()
        val root = tmp.resolve("prefix")
        root.resolve("bin").createDirectories()
        root.resolve("lib/app").createDirectories()
        root.resolve("lib/runtime").createDirectories()
        Files.createFile(root.resolve("bin/DataWeave"))
        root.resolve("lib/runtime/release").writeText(
            """
            MODULES="java.base java.desktop java.sql java.xml java.naming java.net.http java.prefs jdk.accessibility jdk.crypto.ec jdk.unsupported"
            """.trimIndent(),
        )
        root.resolve("lib/app/DataWeave.cfg").writeText("app.mainclass=com.dataweave.app.DataWeaveBootstrapKt\n")
        val prev = System.getProperty("jpackage.app-path")
        try {
            System.setProperty("jpackage.app-path", root.resolve("bin/DataWeave").toString())
            val lines = LaunchDiagnostics.collectLines(arrayOf("--diagnose"))
            assertEquals(true, lines.any { it.contains("install.root.resolved=") && it.endsWith(root.toString()) })
            assertEquals(true, lines.any { it.contains("install.runtime.missing_required_modules=(none)") })
        } finally {
            if (prev == null) System.clearProperty("jpackage.app-path") else System.setProperty("jpackage.app-path", prev)
        }
    }
}
