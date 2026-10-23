package com.dataweave.backend.cache;

import java.util.UUID;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheInvalidationService {

    private final CacheManager cacheManager;

    public CacheInvalidationService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void invalidateConnection(UUID connectionId) {
        evictPrefix("metadata", connectionId.toString());
        evictPrefix("dataPages", connectionId.toString());
        evictPrefix("datasetPreview", connectionId.toString());
        evictPrefix("queryResults", connectionId.toString());
    }

    public void invalidateAll() {
        for (String name : new String[] {"metadata", "dataPages", "datasetPreview", "queryResults"}) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private void evictPrefix(String cacheName, String prefix) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine) {
            caffeine.asMap().keySet().removeIf(key -> key.toString().startsWith(prefix));
        } else {
            cache.clear();
        }
    }
}
