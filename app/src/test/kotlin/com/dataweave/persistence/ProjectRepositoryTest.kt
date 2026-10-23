package com.dataweave.persistence

import com.dataweave.domain.CreateProjectRequest
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProjectRepositoryTest {
    private lateinit var postgres: EmbeddedPostgres
    private lateinit var repository: ProjectRepository

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
        repository = ProjectRepository(ds)
    }

    @AfterEach
    fun tearDown() {
        postgres.close()
    }

    @Test
    fun createAndListProjects() {
        val created = repository.insert(CreateProjectRequest("Alpha", "First workspace"))
        val all = repository.list()
        assertEquals(1, all.size)
        assertEquals(created.id, all.first().id)
        assertEquals("Alpha", all.first().name)
    }

    @Test
    fun deleteProject() {
        val created = repository.insert(CreateProjectRequest("Beta", ""))
        assertTrue(repository.delete(created.id))
        assertTrue(repository.list().isEmpty())
    }
}
