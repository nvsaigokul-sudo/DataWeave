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
        val props = loadProps()
        props.setProperty("darkTheme", enabled.toString())
        store(props)
    }

    fun loadLastConnectionId(): String? {
        if (!Files.exists(file)) return null
        return loadProps().getProperty("lastConnectionId")?.takeIf { it.isNotBlank() }
    }

    fun saveLastConnectionId(id: String?) {
        val props = loadProps()
        if (id.isNullOrBlank()) {
            props.remove("lastConnectionId")
        } else {
            props.setProperty("lastConnectionId", id)
        }
        store(props)
    }

    private fun loadProps(): Properties =
        if (Files.exists(file)) {
            Properties().apply { Files.newInputStream(file).use { load(it) } }
        } else {
            Properties()
        }

    private fun store(props: Properties) {
        Files.newOutputStream(file).use { props.store(it, "DataWeave user preferences") }
    }
}
