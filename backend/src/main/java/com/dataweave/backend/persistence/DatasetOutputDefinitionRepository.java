package com.dataweave.backend.persistence;

import com.dataweave.backend.domain.DatasetOutputDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetOutputDefinitionRepository {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final JdbcTemplate jdbc;

    public DatasetOutputDefinitionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DatasetOutputDefinition> findByProject(UUID projectId) {
        return jdbc.query(
                """
                SELECT id, project_id, connection_id, source_schema, source_table, columns_json, updated_at
                FROM dataset_output_definitions WHERE project_id = ? ORDER BY updated_at DESC
                """,
                mapper(),
                projectId.toString());
    }

    public Optional<DatasetOutputDefinition> findById(UUID id) {
        var rows =
                jdbc.query(
                        """
                        SELECT id, project_id, connection_id, source_schema, source_table, columns_json, updated_at
                        FROM dataset_output_definitions WHERE id = ?
                        """,
                        mapper(),
                        id.toString());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    public void upsert(DatasetOutputDefinition def) throws Exception {
        String columnsJson = JSON.writeValueAsString(def.outputColumns());
        jdbc.update(
                """
                INSERT INTO dataset_output_definitions
                (id, project_id, connection_id, source_schema, source_table, columns_json, updated_at)
                VALUES (?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET
                  connection_id = excluded.connection_id,
                  source_schema = excluded.source_schema,
                  source_table = excluded.source_table,
                  columns_json = excluded.columns_json,
                  updated_at = excluded.updated_at
                """,
                def.id().toString(),
                def.projectId().toString(),
                def.connectionId().toString(),
                def.sourceSchema(),
                def.sourceTable(),
                columnsJson,
                def.updatedAt().toString());
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM dataset_output_definitions WHERE id = ?", id.toString());
    }

    private RowMapper<DatasetOutputDefinition> mapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private DatasetOutputDefinition mapRow(ResultSet rs) throws java.sql.SQLException {
        List<String> columns;
        try {
            columns = JSON.readValue(rs.getString("columns_json"), new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid dataset columns_json", ex);
        }
        return new DatasetOutputDefinition(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                UUID.fromString(rs.getString("connection_id")),
                rs.getString("source_schema"),
                rs.getString("source_table"),
                columns,
                Instant.parse(rs.getString("updated_at")));
    }
}
