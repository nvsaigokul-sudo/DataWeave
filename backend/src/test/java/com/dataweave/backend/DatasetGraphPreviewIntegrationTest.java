package com.dataweave.backend;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.JoinType;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.service.DatasetGraphService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DatasetGraphPreviewIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired DatasetGraphService graphs;

    @Test
    void leftJoinPreviewIncludesUnmatchedLeftRow() throws Exception {
        seed();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "graph",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(sessions.connect(id).state().name().equals("CONNECTED"));
        var graph =
                new DatasetGraphDefinition(
                        1,
                        "public",
                        List.of(
                                new DatasetGraphDefinition.DatasetSourceTable("o", "orders", 0, 0),
                                new DatasetGraphDefinition.DatasetSourceTable("c", "customers", 1, 0)),
                        List.of(
                                new DatasetGraphDefinition.DatasetJoinEdge(
                                        "o", "customer_id", JoinType.LEFT, "c", "id")),
                        List.of(new DatasetGraphDefinition.DatasetOutputColumn("o.id", "o", "id", "order_id")),
                        null,
                        null,
                        null,
                        false);
        var page = graphs.preview(id, graph, 0, 50);
        Assertions.assertTrue(page.rows().size() >= 2);
    }

    private static void seed() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS customers (id INT PRIMARY KEY, name TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS orders (id INT PRIMARY KEY, customer_id INT)");
            st.execute("TRUNCATE orders, customers CASCADE");
            st.execute("INSERT INTO customers VALUES (1, 'A')");
            st.execute("INSERT INTO orders VALUES (10, 1), (11, NULL)");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
