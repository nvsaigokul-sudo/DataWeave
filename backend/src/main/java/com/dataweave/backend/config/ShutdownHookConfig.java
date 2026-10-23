package com.dataweave.backend.config;

import com.dataweave.backend.postgres.PostgresConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutdownHookConfig {

    @Bean
    public Runnable postgresShutdownHook(PostgresConnectionManager manager) {
        return manager::shutdown;
    }
}
