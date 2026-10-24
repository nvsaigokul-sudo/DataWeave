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

class ExplorerTableMetadataIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired CatalogMetadataService metadata;

    @Test
    void primaryKeyAndIndexesDiscoverable() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS meta_demo (id INT PRIMARY KEY, note TEXT)");
            st.execute("CREATE INDEX IF NOT EXISTS meta_demo_note_idx ON meta_demo(note)");
        }
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "meta",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(sessions.connect(id).state().name().equals("CONNECTED"));
        var pk = metadata.getPrimaryKey(id, "public", "meta_demo");
        Assertions.assertFalse(pk.columnNames().isEmpty());
        var indexes = metadata.listIndexes(id, "public", "meta_demo");
        Assertions.assertTrue(indexes.stream().anyMatch(i -> i.indexName().contains("note")));
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
