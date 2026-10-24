package com.dataweave.app.platform

import java.io.BufferedReader
import java.io.InputStreamReader

/** Best-effort detection of local PostgreSQL Windows services — never assumes a single service name. */
object WindowsPostgresServiceHelper {
    data class ServiceInfo(val name: String, val displayName: String, val status: String)

    fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("win")

    fun listPostgresServices(): List<ServiceInfo> {
        if (!isWindows()) return emptyList()
        return runCatching {
            val process =
                ProcessBuilder("sc.exe", "query", "type=", "service", "state=", "all")
                    .redirectErrorStream(true)
                    .start()
            val lines = BufferedReader(InputStreamReader(process.inputStream)).readLines()
            process.waitFor()
            parseScOutput(lines)
        }.getOrElse { emptyList() }
    }

    internal fun parseScOutput(lines: List<String>): List<ServiceInfo> {
        val services = mutableListOf<ServiceInfo>()
        var name: String? = null
        var display: String? = null
        var state: String? = null
        fun flush() {
            val n = name ?: return
            if (n.contains("postgres", ignoreCase = true) || (display ?: "").contains("postgres", ignoreCase = true)) {
                services.add(ServiceInfo(n, display ?: n, state ?: "unknown"))
            }
            name = null
            display = null
            state = null
        }
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("SERVICE_NAME:") -> {
                    flush()
                    name = trimmed.substringAfter(":").trim()
                }
                trimmed.startsWith("DISPLAY_NAME:") -> display = trimmed.substringAfter(":").trim()
                trimmed.startsWith("STATE") -> state = trimmed.substringAfter(":").trim()
            }
        }
        flush()
        return services.distinctBy { it.name }
    }

    fun startServiceMessage(serviceName: String): String =
        "Starting '$serviceName' may require Administrator privileges (UAC). " +
            "Open an elevated Command Prompt and run: sc start \"$serviceName\""
}
