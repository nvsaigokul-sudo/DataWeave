package com.dataweave.backend;

import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ReadOnlyQueryService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReadOnlyQueryServiceTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ReadOnlyQueryService queries;

    @Test
    void rejectsMultiStatementSql() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> queries.runSelect(UUID.randomUUID(), "SELECT 1; SELECT 2", List.of()));
    }

    @Test
    void distinctSelectsDoNotShareCacheEntry() throws Exception {
        seedSampleTable();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "ro",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(connections.test(id, null).success());

        var first = queries.runSelect(id, "SELECT id FROM dw_sample ORDER BY id", List.of());
        var second = queries.runSelect(id, "SELECT name FROM dw_sample ORDER BY id", List.of());

        Assertions.assertEquals(1, first.getFirst().size());
        Assertions.assertTrue(first.getFirst().containsKey("id"));
        Assertions.assertEquals(1, second.getFirst().size());
        Assertions.assertTrue(second.getFirst().containsKey("name"));
    }

    private void seedSampleTable() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS dw_sample");
            st.execute("CREATE TABLE dw_sample (id INT PRIMARY KEY, name TEXT)");
            st.execute("INSERT INTO dw_sample VALUES (1,'alpha')");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
