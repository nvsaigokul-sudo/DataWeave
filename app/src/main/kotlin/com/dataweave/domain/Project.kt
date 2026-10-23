package com.dataweave.domain

import java.time.Instant
import java.util.UUID

data class Project(
    val id: UUID,
    val name: String,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CreateProjectRequest(
    val name: String,
    val description: String,
)
