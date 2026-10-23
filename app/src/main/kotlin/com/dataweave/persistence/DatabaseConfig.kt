package com.dataweave.persistence

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
)
