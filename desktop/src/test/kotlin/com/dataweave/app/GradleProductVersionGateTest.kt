package com.dataweave.app

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the v0.3.0 RC branch against accidentally shipping installers built from stale Gradle
 * product version (e.g. main @ 0.2.9). jpackage mapping must stay aligned with [jpackageVersion]
 * in `desktop/build.gradle.kts`.
 */
class GradleProductVersionGateTest {
    private val repoRoot: Path =
        generateSequence(Path.of(System.getProperty("user.dir"))) { it.parent }
            .first { Files.isRegularFile(it.resolve("settings.gradle.kts")) }

    @Test
    fun gradleProductVersion_isV030ReleaseCandidate() {
        val content = repoRoot.resolve("build.gradle.kts").readText()
        val version =
            Regex("""^\s*version\s*=\s*"([^"]+)"""", RegexOption.MULTILINE)
                .find(content)
                ?.groupValues
                ?.get(1)
                ?: error("Could not parse product version from ${repoRoot.resolve("build.gradle.kts")}")
        assertTrue(
            version.startsWith("0.3.0"),
            "Expected v0.3.0 RC Gradle product version, got '$version'. " +
                "Windows MSI on main (0.2.9) is the public release, not this branch.",
        )
        assertEquals("1.3.0", jpackageVersion(version.removeSuffix("-SNAPSHOT")))
    }

    @Test
    fun jpackageVersion_mapsSubOneMajorProductVersions() {
        assertEquals("1.2.9", jpackageVersion("0.2.9"))
        assertEquals("1.3.0", jpackageVersion("0.3.0-SNAPSHOT"))
        assertEquals("2.0.0", jpackageVersion("2.0.0"))
    }

    private fun jpackageVersion(raw: String): String {
        val version = raw.removeSuffix("-SNAPSHOT")
        val parts = version.split(".")
        val major = parts.firstOrNull()?.toIntOrNull() ?: 1
        if (major >= 1) return version
        val rest = parts.drop(1).joinToString(".").ifEmpty { "0" }
        return "1.$rest"
    }
}
