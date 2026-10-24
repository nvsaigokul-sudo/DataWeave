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
import java.sql.PreparedStatement;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Verifies bounded paging: only page-sized row batches are loaded per request. */
class LargeTablePagingIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired TableDataCacheGateway tablePages;

    @Test
    void pagesLargeTableWithoutLoadingAllRows() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection()) {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS big_demo (id INT PRIMARY KEY, tag TEXT)");
            conn.createStatement().execute("TRUNCATE big_demo");
            try (PreparedStatement ps =
                    conn.prepareStatement("INSERT INTO big_demo (id, tag) VALUES (?, 'row-' || ?)")) {
                for (int i = 1; i <= 250; i++) {
                    ps.setInt(1, i);
                    ps.setInt(2, i);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "big",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(sessions.connect(id).state().name().equals("CONNECTED"));
        var table = new TableRef("public", "big_demo");
        var p0 = tablePages.getPage(id, table, 0, 100, TableBrowseOptions.none());
        var p1 = tablePages.getPage(id, table, 100, 100, TableBrowseOptions.none());
        var p2 = tablePages.getPage(id, table, 200, 100, TableBrowseOptions.none());
        Assertions.assertEquals(100, p0.rows().size());
        Assertions.assertEquals(100, p1.rows().size());
        Assertions.assertEquals(50, p2.rows().size());
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
