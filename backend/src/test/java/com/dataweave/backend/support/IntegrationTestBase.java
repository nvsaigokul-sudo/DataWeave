package com.dataweave.backend.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public abstract class IntegrationTestBase {

    protected static EmbeddedPostgres postgres;
    protected static Path sqliteFile;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) throws Exception {
        if (sqliteFile == null) {
            sqliteFile = Files.createTempFile("dataweave-test", ".db");
        }
        registry.add("dataweave.metadata.jdbc-url", () -> "jdbc:sqlite:" + sqliteFile);
        registry.add("dataweave.test.mode", () -> "true");
    }

    @BeforeAll
    static void startPostgres() throws Exception {
        if (postgres == null) {
            postgres = EmbeddedPostgres.builder().setPort(0).start();
        }
    }

    protected String pgJdbcUrl() {
        return postgres.getJdbcUrl("postgres", "postgres");
    }
}
