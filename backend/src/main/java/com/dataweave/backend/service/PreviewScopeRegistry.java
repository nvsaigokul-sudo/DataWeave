package com.dataweave.backend.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class PreviewScopeRegistry {
    private final AtomicLong previewGeneration = new AtomicLong();
    private final ConcurrentHashMap<String, Long> scopeGeneration = new ConcurrentHashMap<>();

    public long beginScope(String scopeKey) {
        long gen = previewGeneration.incrementAndGet();
        scopeGeneration.put(scopeKey, gen);
        return gen;
    }

    public long generationFor(String scopeKey) {
        return scopeGeneration.getOrDefault(scopeKey, previewGeneration.get());
    }

    ConcurrentHashMap<String, Long> scopeGeneration() {
        return scopeGeneration;
    }
}
