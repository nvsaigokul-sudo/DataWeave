package com.dataweave.app

import com.dataweave.domain.CreateProjectRequest

/** CI-friendly smoke without opening a Compose window. */
fun main() {
    val embedded = DevEmbeddedPostgres.start()
    try {
        val bootstrap = com.dataweave.persistence.DatabaseBootstrap.open(
            com.dataweave.persistence.DatabaseConfig(
                embedded.jdbcUrl,
                embedded.username,
                embedded.password,
            ),
        )
        try {
            val project = bootstrap.projectService.create(
                CreateProjectRequest("Smoke project", "Headless startup check"),
            )
            check(project.name == "Smoke project")
            check(bootstrap.projectService.list().any { it.id == project.id })
            println("HEADLESS_SMOKE_OK project=${project.id}")
        } finally {
            bootstrap.close()
        }
    } finally {
        embedded.close()
    }
}
