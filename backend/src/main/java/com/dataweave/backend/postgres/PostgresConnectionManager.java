package com.dataweave.backend.postgres;

import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.ConnectionTestResult;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.security.CredentialVault;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class PostgresConnectionManager {

    private final CredentialVault vault;
    private final Map<UUID, HikariDataSource> pools = new ConcurrentHashMap<>();

    public PostgresConnectionManager(CredentialVault vault) {
        this.vault = vault;
    }

    public ConnectionTestResult test(ConnectionProfile profile, char[] passwordOverride) {
        return probe(profile, passwordOverride);
    }

    /** Opens a live pool after a successful probe. Session state is tracked separately. */
    public ConnectionTestResult connect(ConnectionProfile profile, char[] passwordOverride) {
        UUID id = profile.id();
        ConnectionTestResult probe = probe(profile, passwordOverride);
        if (!probe.success()) {
            return probe;
        }
        disconnect(id);
        Optional<char[]> password = resolvePassword(profile, passwordOverride);
        pools.put(id, createPool(profile, password.orElseThrow(), false));
        return probe;
    }

    public boolean isConnected(UUID connectionId) {
        HikariDataSource ds = pools.get(connectionId);
        return ds != null && !ds.isClosed();
    }

    public DataSource getDataSource(ConnectionProfile profile) {
        HikariDataSource ds = pools.get(profile.id());
        if (ds == null || ds.isClosed()) {
            throw new IllegalStateException(
                    "Connection '" + profile.name() + "' is not active. Use Connect before running queries.");
        }
        return ds;
    }

    private ConnectionTestResult probe(ConnectionProfile profile, char[] passwordOverride) {
        Optional<char[]> password = resolvePassword(profile, passwordOverride);
        if (password.isEmpty()) {
            return ConnectionTestResult.fail(
                    "missing_credential",
                    "No saved password for this profile. Enter your password and press Save credentials, then Connect.");
        }
        try (HikariDataSource ds = createPool(profile, password.get(), true)) {
            try (Connection conn = ds.getConnection()) {
                conn.createStatement().execute("SELECT 1");
                return ConnectionTestResult.ok(
                        "Connected to PostgreSQL " + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (Exception ex) {
            return ConnectionTestResult.fail(classify(ex), sanitizeMessage(ex.getMessage()));
        }
    }

    public void disconnect(UUID connectionId) {
        HikariDataSource ds = pools.remove(connectionId);
        if (ds != null) {
            ds.close();
        }
    }

    public void shutdown() {
        pools.values().forEach(HikariDataSource::close);
        pools.clear();
    }

    private Optional<char[]> resolvePassword(ConnectionProfile profile, char[] passwordOverride) {
        if (passwordOverride != null) {
            return Optional.of(passwordOverride);
        }
        return vault.load(profile.credentialKey());
    }

    private HikariDataSource createPool(ConnectionProfile profile, char[] password, boolean forTest) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(profile));
        config.setUsername(profile.username());
        config.setPassword(new String(password));
        config.setMaximumPoolSize(forTest ? 1 : 3);
        config.setPoolName("pg-" + profile.id());
        config.setConnectionTimeout(profile.connectTimeoutSeconds() * 1000L);
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    static String buildJdbcUrl(ConnectionProfile profile) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:postgresql://")
                .append(profile.host())
                .append(":")
                .append(profile.port())
                .append("/")
                .append(profile.databaseName());
        url.append("?sslmode=").append(profile.sslMode().jdbcParam());
        url.append("&connectTimeout=").append(profile.connectTimeoutSeconds());
        url.append("&ApplicationName=DataWeave");
        return url.toString();
    }

    static String classify(Exception ex) {
        if (ex instanceof SQLException sql) {
            String state = Optional.ofNullable(sql.getSQLState()).orElse("");
            if (state.startsWith("28")) {
                return "authentication";
            }
            if (state.startsWith("08")) {
                return "network";
            }
        }
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("timeout")) {
            return "timeout";
        }
        if (msg.contains("ssl") || msg.contains("certificate")) {
            return "ssl";
        }
        if (msg.contains("password") || msg.contains("authentication")) {
            return "authentication";
        }
        return "unknown";
    }

    static String sanitizeMessage(String message) {
        if (message == null) {
            return "Connection failed";
        }
        return message.replaceAll("password=\\S+", "password=***");
    }
}
