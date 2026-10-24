package com.dataweave.backend.config;

import com.dataweave.backend.platform.PlatformInfo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqliteFlywayBackupConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SqliteFlywayBackupConfiguration.class);

    @Bean
    FlywayMigrationStrategy sqliteBackupBeforeMigrate() {
        return flyway -> {
            backupMetadataIfPresent(flyway);
            flyway.migrate();
        };
    }

    private static void backupMetadataIfPresent(Flyway flyway) {
        try {
            String url = flyway.getConfiguration().getDataSource().getConnection().getMetaData().getURL();
            if (url == null || !url.startsWith("jdbc:sqlite:")) {
                return;
            }
            Path db = Path.of(url.substring("jdbc:sqlite:".length()));
            if (!Files.isRegularFile(db)) {
                return;
            }
            Path backupDir = PlatformInfo.dataWeaveHome().resolve("backups");
            Files.createDirectories(backupDir);
            String stamp = Instant.now().toString().replace(":", "-");
            Path target = backupDir.resolve("metadata-" + stamp + ".db");
            Files.copy(db, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("SQLite metadata backup before migration: {}", target);
        } catch (Exception ex) {
            log.warn("Could not backup SQLite metadata before Flyway migrate: {}", ex.getMessage());
        }
    }
}
