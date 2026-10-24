package com.dataweave.backend;

import com.dataweave.backend.cache.CacheDiagnosticsService;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.DatasetPreviewCacheGateway;
import com.dataweave.backend.support.IntegrationTestBase;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DatasetPreviewCacheIntegrationTest extends IntegrationTestBase {

    @Autowired ConnectionProfileService connections;
    @Autowired DatasetPreviewCacheGateway previewPages;
    @Autowired CacheDiagnosticsService cacheDiagnostics;

    @Test
    void previewCacheKeysIncludeOffsetAndLimit() throws Exception {
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
        var table = new TableRef("public", "dw_preview");
        String scope = "test-scope";
        long generation = 1L;
        List<String> columns = List.of("id", "name");

        var page0 = previewPages.preview(scope, generation, id, table, columns, 0, 1);
        Assertions.assertEquals(1, page0.rows().size());
        long missesAfterFirst =
                cacheDiagnostics.snapshot().get("datasetPreview").get("missCount").longValue();

        previewPages.preview(scope, generation, id, table, columns, 0, 1);
        long hitsAfterRepeat =
                cacheDiagnostics.snapshot().get("datasetPreview").get("hitCount").longValue();
        Assertions.assertTrue(hitsAfterRepeat >= 1, "same offset/limit should cache-hit");

        var page1 = previewPages.preview(scope, generation, id, table, columns, 1, 1);
        Assertions.assertEquals(1, page1.rows().size());
        long missesAfterSecondOffset =
                cacheDiagnostics.snapshot().get("datasetPreview").get("missCount").longValue();
        Assertions.assertEquals(
                missesAfterFirst + 1,
                missesAfterSecondOffset,
                "different offset must be a separate cache entry");
    }

    private void seedSampleTable() throws Exception {
        try (Connection conn = postgres.getPostgresDatabase().getConnection();
                Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS dw_preview");
            st.execute("CREATE TABLE dw_preview (id INT PRIMARY KEY, name TEXT)");
            st.execute("INSERT INTO dw_preview VALUES (1,'a'),(2,'b'),(3,'c')");
        }
    }

    private static int jdbcPort(String jdbcUrl) throws Exception {
        URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
        return uri.getPort();
    }
}
