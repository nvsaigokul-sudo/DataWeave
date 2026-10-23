package com.dataweave.service

import com.dataweave.domain.CreateProjectRequest
import com.dataweave.persistence.DatabaseConfig
import com.dataweave.persistence.ProjectRepository
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProjectServiceTest {
    private lateinit var postgres: EmbeddedPostgres
    private lateinit var service: ProjectService

    @BeforeEach
    fun setUp() {
        postgres = EmbeddedPostgres.builder().setPort(0).start()
        val config = DatabaseConfig(
            jdbcUrl = postgres.getJdbcUrl("postgres", "postgres"),
            username = "postgres",
            password = "",
        )
        val ds = com.zaxxer.hikari.HikariDataSource(
            com.zaxxer.hikari.HikariConfig().apply {
                jdbcUrl = config.jdbcUrl
                username = config.username
                password = config.password
            },
        )
        org.flywaydb.core.Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        service = ProjectService(ProjectRepository(ds))
    }

    @AfterEach
    fun tearDown() {
        postgres.close()
    }

    @Test
    fun rejectsBlankName() {
        assertThrows(IllegalArgumentException::class.java) {
            service.create(CreateProjectRequest("  ", "x"))
        }
    }
}
