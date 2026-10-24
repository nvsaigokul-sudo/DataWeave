package com.dataweave.app.preferences

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

object UserPreferences {
    private fun preferencesFile(): Path {
        val dir = Path.of(System.getProperty("user.home"), ".config", "dataweave")
        Files.createDirectories(dir)
        return dir.resolve("preferences.properties")
    }

    fun loadThemeMode(): ThemeMode {
        val file = preferencesFile()
        if (!Files.exists(file)) return ThemeMode.SYSTEM
        val props = loadProps()
        val stored = props.getProperty("themeMode")
        if (stored != null) return ThemeMode.fromStored(stored)
        return if (props.getProperty("darkTheme", "false") == "true") ThemeMode.DARK else ThemeMode.LIGHT
    }

    fun saveThemeMode(mode: ThemeMode) {
        val props = loadProps()
        props.setProperty("themeMode", mode.name)
        props.remove("darkTheme")
        store(props)
    }

    @Deprecated("Use loadThemeMode", ReplaceWith("loadThemeMode()"))
    fun loadDarkTheme(): Boolean = loadThemeMode() == ThemeMode.DARK

    @Deprecated("Use saveThemeMode", ReplaceWith("saveThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)"))
    fun saveDarkTheme(enabled: Boolean) {
        saveThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    fun loadLastConnectionId(): String? {
        if (!Files.exists(preferencesFile())) return null
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

    private fun loadProps(): Properties {
        val file = preferencesFile()
        return if (Files.exists(file)) {
            Properties().apply { Files.newInputStream(file).use { load(it) } }
        } else {
            Properties()
        }
    }

    private fun store(props: Properties) {
        Files.newOutputStream(preferencesFile()).use { props.store(it, "DataWeave user preferences") }
    }
}
