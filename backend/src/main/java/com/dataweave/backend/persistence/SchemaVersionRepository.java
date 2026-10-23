package com.dataweave.backend.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SchemaVersionRepository {

    private final JdbcTemplate jdbc;

    public SchemaVersionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getVersion(UUID connectionId) {
        Optional<Long> version =
                jdbc.query(
                                """
                                SELECT version FROM schema_versions WHERE connection_id = ?
                                """,
                                rs -> rs.next() ? Optional.of(rs.getLong("version")) : Optional.empty(),
                                connectionId.toString());
        return version.orElse(0L);
    }

    public long bump(UUID connectionId) {
        long next = getVersion(connectionId) + 1;
        int updated =
                jdbc.update(
                        """
                        UPDATE schema_versions SET version = ?, refreshed_at = ? WHERE connection_id = ?
                        """,
                        next,
                        Instant.now().toString(),
                        connectionId.toString());
        if (updated == 0) {
            jdbc.update(
                    """
                    INSERT INTO schema_versions (connection_id, version, refreshed_at) VALUES (?,?,?)
                    """,
                    connectionId.toString(),
                    next,
                    Instant.now().toString());
        }
        return next;
    }
}
