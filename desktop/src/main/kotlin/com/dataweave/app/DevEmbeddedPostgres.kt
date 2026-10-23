package com.dataweave.app

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.net.URI

data class EmbeddedPgHandle(
    val port: Int,
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
        val url = pg.getJdbcUrl("postgres", "postgres")
        val port = URI.create(url.replace("jdbc:", "")).port
        return EmbeddedPgHandle(port, url, "postgres", "", pg)
    }
}
