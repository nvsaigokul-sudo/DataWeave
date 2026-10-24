package com.dataweave.backend.persistence;

import com.dataweave.backend.domain.CsvSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CsvSourceRepository {

    private static final String HEADER_DELIM = "\u001f";

    private final JdbcTemplate jdbc;

    public CsvSourceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CsvSource> findByProject(UUID projectId) {
        return jdbc.query(
                """
                SELECT id, project_id, name, file_path, headers_json, row_estimate, created_at
                FROM csv_sources WHERE project_id = ? ORDER BY created_at DESC
                """,
                mapper(),
                projectId.toString());
    }

    public CsvSource require(UUID id) {
        List<CsvSource> rows =
                jdbc.query(
                        """
                        SELECT id, project_id, name, file_path, headers_json, row_estimate, created_at
                        FROM csv_sources WHERE id = ?
                        """,
                        mapper(),
                        id.toString());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("CSV source not found: " + id);
        }
        return rows.getFirst();
    }

    public void insert(CsvSource source) {
        jdbc.update(
                """
                INSERT INTO csv_sources (id, project_id, name, file_path, headers_json, row_estimate, created_at)
                VALUES (?,?,?,?,?,?,?)
                """,
                source.id().toString(),
                source.projectId().toString(),
                source.name(),
                source.filePath(),
                encodeHeaders(source.headers()),
                source.rowEstimate(),
                source.createdAt().toString());
    }

    private static String encodeHeaders(List<String> headers) {
        return String.join(HEADER_DELIM, headers);
    }

    private static List<String> decodeHeaders(String raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(raw.split(HEADER_DELIM, -1));
    }

    private static RowMapper<CsvSource> mapper() {
        return (rs, rowNum) -> map(rs);
    }

    private static CsvSource map(ResultSet rs) throws SQLException {
        long est = rs.getLong("row_estimate");
        Long rowEstimate = rs.wasNull() ? null : est;
        return new CsvSource(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("project_id")),
                rs.getString("name"),
                rs.getString("file_path"),
                decodeHeaders(rs.getString("headers_json")),
                rowEstimate,
                Instant.parse(rs.getString("created_at")));
    }
}
