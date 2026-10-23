package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableRef;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;

@Service
public class DatasetPreviewService {

    private final DatasetPreviewCacheGateway previewCacheGateway;
    private final PreviewScopeRegistry scopeRegistry;

    public DatasetPreviewService(DatasetPreviewCacheGateway previewCacheGateway, PreviewScopeRegistry scopeRegistry) {
        this.previewCacheGateway = previewCacheGateway;
        this.scopeRegistry = scopeRegistry;
    }

    public long beginPreviewScope(String scopeKey) {
        return scopeRegistry.beginScope(scopeKey);
    }

    public CompletableFuture<DataPage> previewAsync(
            String scopeKey, UUID connectionId, TableRef table, List<String> columns, long offset, int limit) {
        long generation = scopeRegistry.generationFor(scopeKey);
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return previewCacheGateway.preview(
                                scopeKey, generation, connectionId, table, columns, offset, limit);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                runnable -> Thread.startVirtualThread(runnable));
    }
}
