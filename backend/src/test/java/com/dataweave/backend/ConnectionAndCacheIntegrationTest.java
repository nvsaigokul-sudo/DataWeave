package com.dataweave.backend;

import com.dataweave.backend.cache.CacheDiagnosticsService;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.TableDataCacheGateway;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ConnectionAndCacheIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired TableDataCacheGateway tablePages;
    @Autowired CacheDiagnosticsService cacheDiagnostics;

    @Test
    void pagedReadAndSchemaRefresh() throws Exception {
        seedSampleTable();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "local",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(connections.test(id, null).success());
        var table = new TableRef("public", "dw_sample");
        var page1 = tablePages.getPage(id, table, 0, 50);
        Assertions.assertEquals(2, page1.rows().size());
        tablePages.getPage(id, table, 0, 50);
        var stats = cacheDiagnostics.snapshot().get("dataPages");
        Assertions.assertNotNull(stats);
        connections.refreshSchema(id);
        var page3 = tablePages.getPage(id, table, 0, 50);
        Assertions.assertEquals(2, page3.rows().size());
    }

    private void seedSampleTable() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS dw_sample");
            st.execute("CREATE TABLE dw_sample (id INT PRIMARY KEY, name TEXT)");
            st.execute("INSERT INTO dw_sample VALUES (1,'a'),(2,'b')");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
