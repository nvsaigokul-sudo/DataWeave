package com.dataweave.persistence

import com.dataweave.domain.CreateProjectRequest
import com.dataweave.domain.Project
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

class ProjectRepository(private val dataSource: DataSource) {

    fun list(): List<Project> = dataSource.connection.use { conn ->
        conn.prepareStatement(
            """
            SELECT id, name, description, created_at, updated_at
            FROM projects
            ORDER BY updated_at DESC
            """.trimIndent(),
        ).use { ps ->
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(rs.toProject())
                    }
                }
            }
        }
    }

    fun insert(request: CreateProjectRequest): Project {
        val id = UUID.randomUUID()
        val now = Instant.now()
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO projects (id, name, description, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { ps ->
                ps.setObject(1, id)
                ps.setString(2, request.name.trim())
                ps.setString(3, request.description.trim())
                ps.setTimestamp(4, Timestamp.from(now))
                ps.setTimestamp(5, Timestamp.from(now))
                ps.executeUpdate()
            }
        }
        return Project(id, request.name.trim(), request.description.trim(), now, now)
    }

    fun delete(id: UUID): Boolean = dataSource.connection.use { conn ->
        conn.prepareStatement("DELETE FROM projects WHERE id = ?").use { ps ->
            ps.setObject(1, id)
            ps.executeUpdate() > 0
        }
    }

    private fun java.sql.ResultSet.toProject() = Project(
        id = getObject("id", UUID::class.java),
        name = getString("name"),
        description = getString("description"),
        createdAt = getTimestamp("created_at").toInstant(),
        updatedAt = getTimestamp("updated_at").toInstant(),
    )
}
