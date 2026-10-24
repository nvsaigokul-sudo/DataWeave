package com.dataweave.app.preferences

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

object UserPreferences {
    private val configDir: Path =
        Path.of(System.getProperty("user.home"), ".config", "dataweave").also { Files.createDirectories(it) }
    private val file: Path = configDir.resolve("preferences.properties")

    fun loadDarkTheme(): Boolean {
        if (!Files.exists(file)) return false
        return Properties().apply { Files.newInputStream(file).use { load(it) } }.getProperty("darkTheme", "false") == "true"
    }

    fun saveDarkTheme(enabled: Boolean) {
        val props =
            if (Files.exists(file)) {
                Properties().apply { Files.newInputStream(file).use { load(it) } }
            } else {
                Properties()
            }
        props.setProperty("darkTheme", enabled.toString())
        Files.newOutputStream(file).use { props.store(it, "DataWeave user preferences") }
    }
}
