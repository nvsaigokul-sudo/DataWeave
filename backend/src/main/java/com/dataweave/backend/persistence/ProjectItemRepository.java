package com.dataweave.backend.persistence;

import com.dataweave.backend.domain.ProjectItem;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectItemRepository {

    private final JdbcTemplate jdbc;

    public ProjectItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProjectItem> findByProject(UUID projectId) {
        return jdbc.query(
                """
                SELECT id, project_id, item_type, ref_id, sort_order, label, created_at
                FROM project_items WHERE project_id = ? ORDER BY sort_order, created_at
                """,
                (rs, rowNum) -> map(rs),
                projectId.toString());
    }

    public void insert(ProjectItem item) {
        jdbc.update(
                """
                INSERT INTO project_items (id, project_id, item_type, ref_id, sort_order, label, created_at)
                VALUES (?,?,?,?,?,?,?)
                """,
                item.id().toString(),
                item.projectId().toString(),
                item.itemType(),
                item.refId().toString(),
                item.sortOrder(),
                item.label(),
                item.createdAt().toString());
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM project_items WHERE id = ?", id.toString());
    }

    public void updateSortOrder(UUID id, int sortOrder) {
        jdbc.update("UPDATE project_items SET sort_order = ? WHERE id = ?", sortOrder, id.toString());
    }

    private static ProjectItem map(ResultSet rs) throws java.sql.SQLException {
        return new ProjectItem(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                rs.getString("item_type"),
                UUID.fromString(rs.getString("ref_id")),
                rs.getInt("sort_order"),
                rs.getString("label"),
                Instant.parse(rs.getString("created_at")));
    }
}
