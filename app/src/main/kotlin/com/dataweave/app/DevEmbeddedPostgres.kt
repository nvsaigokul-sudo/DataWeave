package com.dataweave.app

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

data class EmbeddedPgHandle(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    private val postgres: EmbeddedPostgres,
) {
    fun close() {
        postgres.close()
    }
}

object DevEmbeddedPostgres {
    fun start(): EmbeddedPgHandle {
        val pg = EmbeddedPostgres.builder().setPort(0).start()
        return EmbeddedPgHandle(
            jdbcUrl = pg.getJdbcUrl("postgres", "postgres"),
            username = "postgres",
            password = "",
            postgres = pg,
        )
    }
}
