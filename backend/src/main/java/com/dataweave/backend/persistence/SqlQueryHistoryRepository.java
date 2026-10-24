package com.dataweave.backend.persistence;

import com.dataweave.backend.domain.SqlQueryHistoryEntry;
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
public class SqlQueryHistoryRepository {

    private static final int DEFAULT_LIMIT = 50;

    private final JdbcTemplate jdbc;

    public SqlQueryHistoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(SqlQueryHistoryEntry entry) {
        jdbc.update(
                """
                INSERT INTO sql_query_history
                (id, connection_id, sql_text, executed_at, row_count, duration_ms, error_message)
                VALUES (?,?,?,?,?,?,?)
                """,
                entry.id().toString(),
                entry.connectionId().map(UUID::toString).orElse(null),
                entry.sqlText(),
                entry.executedAt().toString(),
                entry.rowCount(),
                entry.durationMs(),
                entry.errorMessage().orElse(null));
    }

    public List<SqlQueryHistoryEntry> findRecent(int limit) {
        int capped = Math.min(Math.max(limit, 1), 200);
        return jdbc.query(
                """
                SELECT id, connection_id, sql_text, executed_at, row_count, duration_ms, error_message
                FROM sql_query_history ORDER BY executed_at DESC LIMIT ?
                """,
                mapper(),
                capped);
    }

    private static RowMapper<SqlQueryHistoryEntry> mapper() {
        return (rs, rowNum) -> map(rs);
    }

    private static SqlQueryHistoryEntry map(ResultSet rs) throws SQLException {
        String conn = rs.getString("connection_id");
        String err = rs.getString("error_message");
        return new SqlQueryHistoryEntry(
                UUID.fromString(rs.getString("id")),
                conn == null ? Optional.empty() : Optional.of(UUID.fromString(conn)),
                rs.getString("sql_text"),
                Instant.parse(rs.getString("executed_at")),
                rs.getInt("row_count"),
                rs.getLong("duration_ms"),
                err == null ? Optional.empty() : Optional.of(err));
    }
}
