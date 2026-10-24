package com.dataweave.backend.persistence;

import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.SslMode;
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
public class ConnectionProfileRepository {

    private final JdbcTemplate jdbc;

    public ConnectionProfileRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ConnectionProfile> findAll() {
        return jdbc.query(
                """
                SELECT id, name, host, port, database_name, username, ssl_mode, credential_key,
                       connect_timeout_seconds, created_at, updated_at
                FROM connection_profiles ORDER BY name
                """,
                mapper());
    }

    public Optional<ConnectionProfile> findById(UUID id) {
        List<ConnectionProfile> rows =
                jdbc.query(
                        """
                        SELECT id, name, host, port, database_name, username, ssl_mode, credential_key,
                               connect_timeout_seconds, created_at, updated_at
                        FROM connection_profiles WHERE id = ?
                        """,
                        mapper(),
                        id.toString());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    public void insert(ConnectionProfile profile) {
        jdbc.update(
                """
                INSERT INTO connection_profiles
                (id, name, host, port, database_name, username, ssl_mode, credential_key,
                 connect_timeout_seconds, created_at, updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """,
                profile.id().toString(),
                profile.name(),
                profile.host(),
                profile.port(),
                profile.databaseName(),
                profile.username(),
                profile.sslMode().name(),
                profile.credentialKey(),
                profile.connectTimeoutSeconds(),
                profile.createdAt().toString(),
                profile.updatedAt().toString());
    }

    public void update(ConnectionProfile profile) {
        jdbc.update(
                """
                UPDATE connection_profiles SET
                  name = ?, host = ?, port = ?, database_name = ?, username = ?,
                  ssl_mode = ?, connect_timeout_seconds = ?, updated_at = ?
                WHERE id = ?
                """,
                profile.name(),
                profile.host(),
                profile.port(),
                profile.databaseName(),
                profile.username(),
                profile.sslMode().name(),
                profile.connectTimeoutSeconds(),
                profile.updatedAt().toString(),
                profile.id().toString());
    }

    public void delete(UUID id) {
        jdbc.update("DELETE FROM connection_profiles WHERE id = ?", id.toString());
    }

    private static RowMapper<ConnectionProfile> mapper() {
        return (rs, rowNum) -> map(rs);
    }

    private static ConnectionProfile map(ResultSet rs) throws SQLException {
        return new ConnectionProfile(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("host"),
                rs.getInt("port"),
                rs.getString("database_name"),
                rs.getString("username"),
                SslMode.fromString(rs.getString("ssl_mode")),
                rs.getString("credential_key"),
                rs.getInt("connect_timeout_seconds"),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at")));
    }
}
