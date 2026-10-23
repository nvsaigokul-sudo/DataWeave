package com.dataweave.backend.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService queryExecutor(@Value("${dataweave.query.pool-size}") int poolSize) {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "dataweave-query");
            t.setDaemon(true);
            return t;
        };
        return Executors.newFixedThreadPool(poolSize, factory);
    }
}
