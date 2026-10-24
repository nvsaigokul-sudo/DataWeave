package com.dataweave.app

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

/** Shared fields for {@code --diagnose} and {@code scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1}. */
object LaunchDiagnostics {
    const val MARKER_START = "=== DataWeave launch diagnostics ==="
    const val MARKER_END = "=== end diagnostics ==="

    fun runDiagnose(args: Array<String>) {
        val lines = collectLines(args)
        lines.forEach { println(it) }
    }

    fun collectLines(args: Array<String>): List<String> {
        val out = mutableListOf<String>()
        out += MARKER_START
        out += "timestamp=${java.time.Instant.now()}"
        out += "java.version=${System.getProperty("java.version")}"
        out += "java.home=${System.getProperty("java.home")}"
        out += "java.vm.name=${System.getProperty("java.vm.name")}"
        out += "os.name=${System.getProperty("os.name")}"
        out += "os.arch=${System.getProperty("os.arch")}"
        out += "os.version=${System.getProperty("os.version")}"
        out += "user.dir=${System.getProperty("user.dir")}"
        out += "user.home=${System.getProperty("user.home")}"
        out += "env.JAVA_HOME=${System.getenv("JAVA_HOME") ?: "(not set)"}"
        out += "env.JAVA_TOOL_OPTIONS=${System.getenv("JAVA_TOOL_OPTIONS") ?: "(not set)"}"
        out += "env._JAVA_OPTIONS=${System.getenv("_JAVA_OPTIONS") ?: "(not set)"}"

        val appPath = System.getProperty("jpackage.app-path")
        out += "jpackage.app-path=${appPath ?: "(not set — dev/gradle run)"}"
        val appVersion = System.getProperty("jpackage.app-version")
        out += "jpackage.app-version=${appVersion ?: "(not set)"}"

        val installRoot = resolveInstallRoot(appPath)
        out += "install.root.resolved=${installRoot ?: "(unknown)"}"
        if (installRoot != null) {
            out += describeLayout(installRoot)
        }

        out += describeAccessibility()
        out += describeLaunchLog()
        out += "process.args=${args.joinToString(" ")}"
        out += MARKER_END
        return out
    }

    fun launchLogPath(): Path {
        val base =
            System.getenv("LOCALAPPDATA")?.let { Path.of(it, "DataWeave") }
                ?: Path.of(System.getProperty("user.home"), ".dataweave", "logs")
        return base.resolve("launch.log")
    }

    fun appendLaunchLog(message: String) {
        try {
            val path = launchLogPath()
            Files.createDirectories(path.parent)
            Files.writeString(
                path,
                "${java.time.Instant.now()} $message${System.lineSeparator()}",
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND,
            )
        } catch (_: Exception) {
            // Best-effort only.
        }
    }

    private fun resolveInstallRoot(appPath: String?): Path? {
        if (appPath.isNullOrBlank()) {
            return null
        }
        var dir =
            when {
                Path.of(appPath).isRegularFile() -> Path.of(appPath).parent
                Path.of(appPath).isDirectory() -> Path.of(appPath)
                else -> Path.of(appPath).parent
            } ?: return null
        // jpackage.app-path usually points at .../app; install root holds DataWeave.exe + runtime/.
        var current = dir
        for (unused in 0..2) {
            if (current.resolve("DataWeave.exe").isRegularFile() || current.resolve("runtime").isDirectory()) {
                return current
            }
            current = current.parent ?: return null
        }
        return current
    }

    private fun describeLayout(installRoot: Path): String {
        val sb = StringBuilder()
        fun line(key: String, value: Any?) {
            sb.appendLine("$key=$value")
        }
        val exe = installRoot.resolve("DataWeave.exe")
        val appDir = installRoot.resolve("app")
        val runtimeDir = installRoot.resolve("runtime")
        val jvmDll = runtimeDir.resolve("bin/server/jvm.dll")
        val javaExe = runtimeDir.resolve("bin/java.exe")
        val cfg = appDir.resolve("DataWeave.cfg")
        val release = runtimeDir.resolve("release")
        line("install.exe.exists", exe.exists())
        line("install.exe.path", exe)
        line("install.app.dir.exists", appDir.isDirectory())
        line("install.runtime.dir.exists", runtimeDir.isDirectory())
        line("install.jvm.dll.exists", jvmDll.isRegularFile())
        line("install.jvm.dll.path", jvmDll)
        if (System.getProperty("os.name").lowercase().contains("win")) {
            line("install.java.exe.exists", javaExe.isRegularFile())
            line("install.java.exe.path", javaExe)
        }
        line("install.cfg.exists", cfg.isRegularFile())
        if (cfg.isRegularFile()) {
            val cfgText = cfg.readText()
            line("install.cfg.skiko.library.path", cfgText.lines().firstOrNull { it.contains("skiko.library.path") })
            line("install.cfg.mainclass", cfgText.lines().firstOrNull { it.contains("app.mainclass") })
            line("install.cfg.runtime", cfgText.lines().firstOrNull { it.contains("app.runtime") })
        }
        if (release.isRegularFile()) {
            val releaseText = release.readText()
            val modules = releaseText.lineSequence().firstOrNull { it.startsWith("MODULES=") }?.substringAfter('=')?.trim('"')
            val javaVersion = releaseText.lineSequence().firstOrNull { it.startsWith("JAVA_VERSION=") }?.substringAfter('=')?.trim('"')
            line("install.runtime.modules", modules)
            line("install.runtime.java_version", javaVersion)
            line("install.runtime.has_java.desktop", modules?.contains("java.desktop") == true)
            line("install.runtime.has_jdk.accessibility", modules?.contains("jdk.accessibility") == true)
            line("install.runtime.has_jdk.crypto.ec", modules?.contains("jdk.crypto.ec") == true)
            val missing = RuntimeModuleRequirements.missingFrom(modules)
            line("install.runtime.missing_required_modules", if (missing.isEmpty()) "(none)" else missing.joinToString(","))
        }
        val skikoDll = appDir.resolve("skiko-windows-x64.dll")
        if (System.getProperty("os.name").lowercase().contains("win")) {
            line("install.skiko.dll.exists", skikoDll.isRegularFile())
            line("install.skiko.dll.path", skikoDll)
        }
        return sb.toString().trimEnd()
    }

    private fun describeAccessibility(): String {
        val sb = StringBuilder()
        val userProps = Path.of(System.getProperty("user.home"), ".accessibility.properties")
        sb.appendLine("accessibility.user.properties.path=$userProps")
        sb.appendLine("accessibility.user.properties.exists=${userProps.isRegularFile()}")
        if (userProps.isRegularFile()) {
            val text = userProps.readText().lines().take(20).joinToString(" | ")
            sb.appendLine("accessibility.user.properties.preview=$text")
        }
        val desktopProps = Path.of(System.getProperty("java.home"), "conf/accessibility.properties")
        sb.appendLine("accessibility.java.home.properties.exists=${desktopProps.isRegularFile()}")
        return sb.toString().trimEnd()
    }

    private fun describeLaunchLog(): String {
        val log = launchLogPath()
        val sb = StringBuilder()
        sb.appendLine("launch.log.path=$log")
        sb.appendLine("launch.log.exists=${log.isRegularFile()}")
        if (log.isRegularFile()) {
            val tail =
                Files.readAllLines(log).takeLast(30).joinToString(" | ")
            sb.appendLine("launch.log.tail=$tail")
        }
        val errLog = log.parent?.resolve("launch-error.log")
        if (errLog != null) {
            sb.appendLine("launch.error.log.path=$errLog")
            sb.appendLine("launch.error.log.exists=${errLog.isRegularFile()}")
            if (errLog.isRegularFile()) {
                sb.appendLine("launch.error.log.tail=${Files.readAllLines(errLog).takeLast(40).joinToString(" | ")}")
            }
        }
        return sb.toString().trimEnd()
    }

    fun writeLaunchError(throwable: Throwable) {
        try {
            val path = launchLogPath().parent.resolve("launch-error.log")
            Files.createDirectories(path.parent)
            val text =
                buildString {
                    appendLine("timestamp=${java.time.Instant.now()}")
                    appendLine("message=${throwable.message}")
                    appendLine(throwable.stackTraceToString())
                }
            Files.writeString(path, text, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            // Best-effort only.
        }
    }
}
