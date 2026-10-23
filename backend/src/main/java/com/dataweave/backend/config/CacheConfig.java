package com.dataweave.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${dataweave.cache.metadata.max-entries}") long metadataMax,
            @Value("${dataweave.cache.metadata.ttl-seconds}") long metadataTtl,
            @Value("${dataweave.cache.data-page.max-entries}") long dataPageMax,
            @Value("${dataweave.cache.data-page.ttl-seconds}") long dataPageTtl,
            @Value("${dataweave.cache.dataset-preview.max-entries}") long previewMax,
            @Value("${dataweave.cache.dataset-preview.ttl-seconds}") long previewTtl,
            @Value("${dataweave.cache.query-result.max-entries}") long queryMax,
            @Value("${dataweave.cache.query-result.ttl-seconds}") long queryTtl) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                build("metadata", metadataMax, metadataTtl),
                build("dataPages", dataPageMax, dataPageTtl),
                build("datasetPreview", previewMax, previewTtl),
                build("queryResults", queryMax, queryTtl)));
        manager.initializeCaches();
        return manager;
    }

    private static CaffeineCache build(String name, long maxEntries, long ttlSeconds) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .maximumSize(maxEntries)
                        .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                        .recordStats()
                        .build());
    }
}
