package com.dataweave.backend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource localMetadataDataSource(
            @Value("${dataweave.metadata.jdbc-url:}") String configuredUrl) throws Exception {
        String url = configuredUrl;
        if (url == null || url.isBlank()) {
            Path dir = Path.of(System.getProperty("user.home"), ".dataweave");
            Files.createDirectories(dir);
            url = "jdbc:sqlite:" + dir.resolve("metadata.db");
        }
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl(url);
        return ds;
    }

    @Bean
    public JdbcTemplate localJdbcTemplate(DataSource localMetadataDataSource) {
        return new JdbcTemplate(localMetadataDataSource);
    }
}
