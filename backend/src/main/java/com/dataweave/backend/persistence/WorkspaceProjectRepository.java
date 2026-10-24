package com.dataweave.backend.persistence;

import com.dataweave.backend.domain.WorkspaceProject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceProjectRepository {

    private final JdbcTemplate jdbc;

    public WorkspaceProjectRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<WorkspaceProject> findAllOrderByUpdated() {
        return jdbc.query(
                """
                SELECT id, name, description, created_at, updated_at
                FROM workspace_projects ORDER BY updated_at DESC
                """,
                mapper());
    }

    public Optional<WorkspaceProject> findById(UUID id) {
        var rows =
                jdbc.query(
                        """
                        SELECT id, name, description, created_at, updated_at
                        FROM workspace_projects WHERE id = ?
                        """,
                        mapper(),
                        id.toString());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    public void update(UUID id, String name, String description, Instant updatedAt) {
        jdbc.update(
                "UPDATE workspace_projects SET name = ?, description = ?, updated_at = ? WHERE id = ?",
                name,
                description,
                updatedAt.toString(),
                id.toString());
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM workspace_projects WHERE id = ?", id.toString());
    }

    public void insert(WorkspaceProject project) {
        jdbc.update(
                """
                INSERT INTO workspace_projects (id, name, description, created_at, updated_at)
                VALUES (?,?,?,?,?)
                """,
                project.id().toString(),
                project.name(),
                project.description(),
                project.createdAt().toString(),
                project.updatedAt().toString());
    }

    private static RowMapper<WorkspaceProject> mapper() {
        return (rs, rowNum) -> map(rs);
    }

    private static WorkspaceProject map(ResultSet rs) throws SQLException {
        return new WorkspaceProject(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("description"),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at")));
    }
}
