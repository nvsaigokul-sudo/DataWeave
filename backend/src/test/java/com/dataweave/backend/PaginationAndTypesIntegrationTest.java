package com.dataweave.backend;

import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.domain.TableBrowseOptions;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.service.TableDataCacheGateway;
import com.dataweave.backend.support.IntegrationTestBase;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Date;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Embedded PostgreSQL fixture: multiple tables, NULLs, typed columns, pagination beyond one page. */
class PaginationAndTypesIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired ConnectionSessionService sessions;
    @Autowired TableDataCacheGateway tablePages;

    @Test
    void multiPageReadWithNullsAndTypes() throws Exception {
        seedFixture();
        int port = jdbcPort(pgJdbcUrl());
        var profile =
                connections.create(
                        "paginated",
                        "127.0.0.1",
                        port,
                        "postgres",
                        "postgres",
                        "postgres".toCharArray(),
                        SslMode.DISABLE,
                        5);
        UUID id = profile.id();
        Assertions.assertTrue(sessions.connect(id).state().name().equals("CONNECTED"));

        var typed = new TableRef("public", "dw_typed");
        var page0 = tablePages.getPage(id, typed, 0, 50, TableBrowseOptions.none());
        var page1 = tablePages.getPage(id, typed, 50, 50, TableBrowseOptions.none());
        var page2 = tablePages.getPage(id, typed, 100, 50, TableBrowseOptions.none());

        Assertions.assertEquals(50, page0.rows().size());
        Assertions.assertEquals(50, page1.rows().size());
        Assertions.assertEquals(20, page2.rows().size());
        Assertions.assertNull(page0.rows().getFirst().get("note"));
        Assertions.assertInstanceOf(BigDecimal.class, page0.rows().get(1).get("amount"));
        Assertions.assertInstanceOf(Date.class, page0.rows().get(1).get("created_on"));

        var other = new TableRef("public", "dw_other");
        var otherPage = tablePages.getPage(id, other, 0, 50, TableBrowseOptions.none());
        Assertions.assertEquals(3, otherPage.rows().size());
    }

    private void seedFixture() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS dw_typed");
            st.execute("DROP TABLE IF EXISTS dw_other");
            st.execute(
                    """
                    CREATE TABLE dw_typed (
                      id INT PRIMARY KEY,
                      note TEXT,
                      amount NUMERIC(10,2),
                      created_on DATE,
                      active BOOLEAN
                    )
                    """);
            st.execute(
                    """
                    CREATE TABLE dw_other (
                      id INT PRIMARY KEY,
                      label TEXT
                    )
                    """);
            StringBuilder insert = new StringBuilder("INSERT INTO dw_typed VALUES ");
            for (int i = 0; i < 120; i++) {
                if (i > 0) {
                    insert.append(',');
                }
                if (i == 0) {
                    insert.append(String.format("(%d, NULL, 0.00, '2024-01-01', false)", i));
                } else {
                    insert.append(
                            String.format(
                                    "(%d, 'row-%d', %d.%02d, '2024-06-15', %s)",
                                    i, i, i % 100, i % 97, i % 2 == 0 ? "true" : "false"));
                }
            }
            st.execute(insert.toString());
            st.execute("INSERT INTO dw_other VALUES (1,'alpha'),(2,'beta'),(3,NULL)");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
