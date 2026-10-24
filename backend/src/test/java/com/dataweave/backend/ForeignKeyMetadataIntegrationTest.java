package com.dataweave.backend;

import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.CatalogMetadataService;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ForeignKeyMetadataIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired CatalogMetadataService metadata;

    @Test
    void listsForeignKeysForSchema() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS fk_parent (id INT PRIMARY KEY)");
            st.execute("CREATE TABLE IF NOT EXISTS fk_child (id INT PRIMARY KEY, parent_id INT REFERENCES fk_parent(id))");
        }
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "fk",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(sessions.connect(id).state().name().equals("CONNECTED"));
        var keys = metadata.listForeignKeys(id, "public");
        Assertions.assertTrue(
                keys.stream()
                        .anyMatch(
                                k ->
                                        "fk_child".equals(k.table())
                                                && "parent_id".equals(k.column())
                                                && "fk_parent".equals(k.referencedTable())));
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
