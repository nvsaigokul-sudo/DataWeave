package com.dataweave.service

import com.dataweave.domain.CreateProjectRequest
import com.dataweave.domain.Project
import com.dataweave.persistence.ProjectRepository
import java.util.UUID

class ProjectService(private val repository: ProjectRepository) {

    fun list(): List<Project> = repository.list()

    fun create(request: CreateProjectRequest): Project {
        require(request.name.isNotBlank()) { "Project name is required." }
        return repository.insert(request)
    }

    fun delete(id: UUID): Boolean = repository.delete(id)
}
