package com.dataweave.app

/** jlink MODULES= tokens required for packaged Windows GUI + Spring (CI + --diagnose). */
object RuntimeModuleRequirements {
    val requiredTokens: List<String> =
        listOf(
            "java.base",
            "java.desktop",
            "java.sql",
            "java.xml",
            "java.naming",
            "java.net.http",
            "java.prefs",
            "jdk.accessibility",
            "jdk.crypto.ec",
            "jdk.unsupported",
        )

    fun missingFrom(modulesLine: String?): List<String> {
        if (modulesLine.isNullOrBlank()) {
            return requiredTokens
        }
        return requiredTokens.filter { token -> !modulesLine.contains(token) }
    }
}
