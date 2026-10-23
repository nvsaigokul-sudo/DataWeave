package com.dataweave.backend.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

@Service
public class CacheDiagnosticsService {

    private final CacheManager cacheManager;

    public CacheDiagnosticsService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Map<String, Map<String, Number>> snapshot() {
        Map<String, Map<String, Number>> out = new LinkedHashMap<>();
        for (String name : new String[] {"metadata", "dataPages", "datasetPreview", "queryResults"}) {
            Cache cache = cacheManager.getCache(name);
            if (cache instanceof CaffeineCache caffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        caffeineCache.getNativeCache();
                CacheStats stats = nativeCache.stats();
                Map<String, Number> row = new LinkedHashMap<>();
                row.put("estimatedSize", nativeCache.estimatedSize());
                row.put("hitCount", stats.hitCount());
                row.put("missCount", stats.missCount());
                row.put("evictionCount", stats.evictionCount());
                out.put(name, row);
            }
        }
        return out;
    }
}
