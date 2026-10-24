package com.dataweave.backend;

import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableBrowseOptions;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.service.TableDataCacheGateway;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TableBrowseColumnFilterIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired TableDataCacheGateway tablePages;

    @Test
    void perColumnFilterNarrowsRows() throws Exception {
        seed();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "col-filter",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(sessions.connect(id).state().name().equals("CONNECTED"));
        var table = new TableRef("public", "filter_demo");
        var broad = tablePages.getPage(id, table, 0, 50, TableBrowseOptions.none());
        var narrow =
                tablePages.getPage(
                        id,
                        table,
                        0,
                        50,
                        new TableBrowseOptions(null, false, "alpha", "name"));
        Assertions.assertTrue(broad.rows().size() > narrow.rows().size());
        Assertions.assertEquals(1, narrow.rows().size());
    }

    private static void seed() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS filter_demo (id INT, name TEXT)");
            st.execute("TRUNCATE filter_demo");
            st.execute("INSERT INTO filter_demo VALUES (1,'alpha'), (2,'beta')");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
