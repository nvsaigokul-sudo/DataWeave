package com.dataweave.persistence

import com.dataweave.service.ProjectService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class DatabaseBootstrap private constructor(
    val dataSource: HikariDataSource,
    val projectService: ProjectService,
) {
    fun close() {
        dataSource.close()
    }

    companion object {
        fun open(config: DatabaseConfig): DatabaseBootstrap {
            val ds = createDataSource(config)
            migrate(ds)
            val repository = ProjectRepository(ds)
            return DatabaseBootstrap(ds, ProjectService(repository))
        }

        private fun createDataSource(config: DatabaseConfig): HikariDataSource {
            val hikari = HikariConfig().apply {
                jdbcUrl = config.jdbcUrl
                username = config.username
                password = config.password
                maximumPoolSize = 5
                poolName = "dataweave"
            }
            return HikariDataSource(hikari)
        }

        private fun migrate(dataSource: DataSource) {
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()
        }
    }
}
