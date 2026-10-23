package com.dataweave.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dataweave.app.ui.AppShell
import com.dataweave.persistence.DatabaseBootstrap
import com.dataweave.persistence.DatabaseConfig

fun main(args: Array<String>) {
    val config = RuntimeConfig.fromArgs(args)
    val bootstrap = DatabaseBootstrap.open(config.toDatabaseConfig())
    Runtime.getRuntime().addShutdownHook(Thread { bootstrap.close() })

    application {
        Window(
            onCloseRequest = {
                bootstrap.close()
                exitApplication()
            },
            title = "DataWeave",
            state = rememberWindowState(width = 1200.dp, height = 800.dp),
        ) {
            AppShell(bootstrap.projectService)
        }
    }
}

data class RuntimeConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val devEmbedded: Boolean,
) {
    fun toDatabaseConfig() = DatabaseConfig(jdbcUrl, username, password)

    companion object {
        fun fromArgs(args: Array<String>): RuntimeConfig {
            val dev = args.contains("--dev")
            if (dev) {
                val embedded = DevEmbeddedPostgres.start()
                return RuntimeConfig(
                    jdbcUrl = embedded.jdbcUrl,
                    username = embedded.username,
                    password = embedded.password,
                    devEmbedded = true,
                )
            }
            val url = System.getenv("DATAWEAVE_JDBC_URL")
                ?: error(
                    "Set DATAWEAVE_JDBC_URL (and optional DATAWEAVE_DB_USER / DATAWEAVE_DB_PASSWORD) " +
                        "or pass --dev for embedded PostgreSQL.",
                )
            return RuntimeConfig(
                jdbcUrl = url,
                username = System.getenv("DATAWEAVE_DB_USER") ?: "dataweave",
                password = System.getenv("DATAWEAVE_DB_PASSWORD") ?: "",
                devEmbedded = false,
            )
        }
    }
}
