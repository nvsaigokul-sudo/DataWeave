package com.dataweave.app

import com.dataweave.backend.DesktopSession
import com.dataweave.backend.domain.SslMode
import java.nio.file.Files

fun runHeadlessSmoke() {
    val embedded = DevEmbeddedPostgres.start()
    val sqlite = Files.createTempFile("dataweave-smoke-", ".db")
    sqlite.toFile().deleteOnExit()
    try {
        DesktopSession.start(
            "--dataweave.metadata.jdbc-url=jdbc:sqlite:${sqlite}",
            "--dataweave.test.mode=true",
        ).use { session ->
            val facade = session.facade()
            val project = facade.createProject("Smoke", "Headless")
            check(facade.listProjects().any { it.id == project.id })
            val conn =
                facade.saveConnection(
                    "embedded",
                    "127.0.0.1",
                    embedded.port,
                    "postgres",
                    "postgres",
                    "postgres".toCharArray(),
                    SslMode.DISABLE,
                    10,
                )
            val test = facade.testConnection(conn.id, null)
            check(test.success) { test.message }
            val schemas = facade.listSchemas(conn.id)
            check(schemas.isNotEmpty())
            println("HEADLESS_SMOKE_OK project=${project.id} connection=${conn.id} schemas=${schemas.size}")
        }
    } finally {
        embedded.close()
    }
}

fun main() {
    runHeadlessSmoke()
}
