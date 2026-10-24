package com.dataweave.backend;

import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.JoinPreviewService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JoinPreviewServiceTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired JoinPreviewService joins;

    @Test
    void innerJoinPreviewReturnsRows() throws Exception {
        seedJoinTables();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "join",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        var page =
                joins.previewInnerJoin(
                        profile.id(),
                        new TableRef("public", "orders"),
                        new TableRef("public", "customers"),
                        "customer_id",
                        "id",
                        List.of("left.id", "right.name"),
                        0,
                        50);
        Assertions.assertFalse(page.rows().isEmpty());
    }

    private static void seedJoinTables() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS customers (id INT PRIMARY KEY, name TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS orders (id INT PRIMARY KEY, customer_id INT)");
            st.execute("TRUNCATE orders, customers CASCADE");
            st.execute("INSERT INTO customers VALUES (1, 'Acme')");
            st.execute("INSERT INTO orders VALUES (10, 1)");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
