package com.dataweave.app

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * File-only startup failure logging before AWT/Compose (compose-multiplatform#4233).
 * Used when the native launcher shows "Failed to launch JVM" for Java-side failures.
 */
object EarlyLaunchFailure {
    fun log(phase: String, throwable: Throwable) {
        try {
            val path = errorLogPath()
            Files.createDirectories(path.parent)
            val text =
                buildString {
                    appendLine("timestamp=${java.time.Instant.now()}")
                    appendLine("phase=$phase")
                    appendLine("message=${throwable.message}")
                    appendLine(throwable.stackTraceToString())
                }
            Files.writeString(
                path,
                text,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )
            System.err.println("DataWeave startup failed ($phase). See $path")
            throwable.printStackTrace(System.err)
        } catch (_: Exception) {
            throwable.printStackTrace(System.err)
        }
    }

    private fun errorLogPath(): Path {
        val base =
            System.getenv("LOCALAPPDATA")?.let { Path.of(it, "DataWeave") }
                ?: Path.of(System.getProperty("user.home"), ".dataweave", "logs")
        return base.resolve("launch-error.log")
    }
}
