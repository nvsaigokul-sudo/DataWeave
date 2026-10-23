package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableRef;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DatasetPreviewCacheGateway {

    private final DatasetPreviewEngine engine;
    private final PreviewScopeRegistry scopeRegistry;

    public DatasetPreviewCacheGateway(DatasetPreviewEngine engine, PreviewScopeRegistry scopeRegistry) {
        this.engine = engine;
        this.scopeRegistry = scopeRegistry;
    }

    @Cacheable(
            cacheNames = "datasetPreview",
            key = "#scopeKey + ':' + #generation + ':' + #connectionId + ':' + #table.qualifiedName() + ':' + #selectedColumns")
    public DataPage preview(
            String scopeKey,
            long generation,
            UUID connectionId,
            TableRef table,
            List<String> selectedColumns,
            long offset,
            int limit) throws Exception {
        return engine.build(
                scopeKey, generation, scopeRegistry.scopeGeneration(), connectionId, table, selectedColumns, offset, limit);
    }
}
