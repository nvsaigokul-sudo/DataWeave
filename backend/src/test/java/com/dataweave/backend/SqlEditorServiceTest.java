package com.dataweave.backend;

import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.service.SqlEditorService;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SqlEditorServiceTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired SqlEditorService sqlEditor;

    @Test
    void recordsHistoryOnSuccess() throws Exception {
        seedSampleTable();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "editor",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        Assertions.assertTrue(sessions.connect(profile.id()).state().name().equals("CONNECTED"));
        var page = sqlEditor.executeSelect(profile.id(), "SELECT id FROM dw_sample ORDER BY id");
        Assertions.assertFalse(page.rows().isEmpty());
        Assertions.assertFalse(sqlEditor.history(5).isEmpty());
    }

    private static void seedSampleTable() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS dw_sample (id INT PRIMARY KEY, name TEXT)");
            st.execute("TRUNCATE dw_sample");
            st.execute("INSERT INTO dw_sample VALUES (1, 'a'), (2, 'b')");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
