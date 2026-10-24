package com.dataweave.backend;

import com.dataweave.backend.domain.ConnectionLifecycleState;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.support.ConnectionTestSupport;
import com.dataweave.backend.support.IntegrationTestBase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MetadataExplorerIntegrationTest extends IntegrationTestBase {

    @Autowired ApplicationFacade facade;
    @Autowired ConnectionProfileService profiles;
    @Autowired ConnectionSessionService sessions;

    @BeforeEach
    void seedTable() throws Exception {
        try (Connection conn = DriverManager.getConnection(pgJdbcUrl(), "postgres", "postgres");
                Statement st = conn.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS dw_meta_test");
            st.execute("DROP TABLE IF EXISTS dw_meta_test.sample");
            st.execute("CREATE TABLE dw_meta_test.sample (id INT PRIMARY KEY, label TEXT)");
            st.execute("INSERT INTO dw_meta_test.sample VALUES (1, 'a')");
        }
    }

    @Test
    void discoverDatabasesSchemasTablesAndColumns() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        UUID id = ConnectionTestSupport.createAndConnect(profiles, sessions, "127.0.0.1", port);

        var databases = facade.listDatabases(id);
        Assertions.assertTrue(databases.contains("postgres"));

        var schemas = facade.listSchemas(id);
        Assertions.assertTrue(schemas.contains("dw_meta_test"));

        var tables = facade.listTables(id, "dw_meta_test");
        Assertions.assertTrue(tables.stream().anyMatch(t -> "sample".equals(t.name())));

        var columns = facade.listColumns(id, "dw_meta_test", "sample");
        Assertions.assertEquals(2, columns.size());
        Assertions.assertTrue(columns.stream().anyMatch(c -> "id".equals(c.name())));
    }

    @Test
    void disconnectBlocksMetadataUntilReconnect() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        UUID id = ConnectionTestSupport.createAndConnect(profiles, sessions, "127.0.0.1", port);
        sessions.disconnect(id);
        Assertions.assertThrows(Exception.class, () -> facade.listSchemas(id));
        sessions.connect(id);
        Assertions.assertFalse(facade.listSchemas(id).isEmpty());
    }

    @Test
    void refreshSchemaBumpsVersionAndReloads() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        UUID id = ConnectionTestSupport.createAndConnect(profiles, sessions, "127.0.0.1", port);
        long v1 = facade.refreshSchema(id);
        try (Connection conn = DriverManager.getConnection(pgJdbcUrl(), "postgres", "postgres");
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS dw_meta_test.after_refresh (x INT)");
        }
        long v2 = facade.refreshSchema(id);
        Assertions.assertTrue(v2 > v1);
        var tables = facade.listTables(id, "dw_meta_test");
        Assertions.assertTrue(tables.stream().anyMatch(t -> "after_refresh".equals(t.name())));
    }

    @Test
    void switchDatabaseReconnectsToTarget() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        UUID id = ConnectionTestSupport.createAndConnect(profiles, sessions, "127.0.0.1", port);
        var status = facade.switchConnectionDatabase(id, "postgres");
        Assertions.assertEquals(ConnectionLifecycleState.CONNECTED, status.state());
        Assertions.assertEquals("postgres", profiles.require(id).databaseName());
        Assertions.assertFalse(facade.listSchemas(id).isEmpty());
    }

    @Test
    void metadataFailsWhenNeverConnected() throws Exception {
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                profiles.create(
                        "idle",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        Assertions.assertThrows(Exception.class, () -> facade.listDatabases(profile.id()));
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        return java.net.URI.create(jdbcUrl.replace("jdbc:", "")).getPort();
    }
}
