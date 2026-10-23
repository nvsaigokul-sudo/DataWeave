package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableRef;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DatasetPreviewEngine {

    private final TableDataService tableDataService;

    public DatasetPreviewEngine(TableDataService tableDataService) {
        this.tableDataService = tableDataService;
    }

    public DataPage build(
            String scopeKey,
            long generation,
            java.util.concurrent.ConcurrentHashMap<String, Long> scopeGeneration,
            UUID connectionId,
            TableRef table,
            List<String> selectedColumns,
            long offset,
            int limit) throws Exception {
        long current = scopeGeneration.getOrDefault(scopeKey, generation);
        if (current != generation) {
            throw new TableDataService.StaleResponseException("Preview superseded");
        }
        DataPage base = tableDataService.fetchPage(connectionId, table, offset, limit);
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            return base;
        }
        List<Map<String, Object>> filteredRows =
                base.rows().stream()
                        .map(
                                row -> {
                                    Map<String, Object> subset = new LinkedHashMap<>();
                                    for (String col : selectedColumns) {
                                        subset.put(col, row.get(col));
                                    }
                                    return subset;
                                })
                        .toList();
        var filteredCols = base.columns().stream().filter(c -> selectedColumns.contains(c.name())).toList();
        return new DataPage(
                filteredCols,
                filteredRows,
                base.offset(),
                base.limit(),
                base.totalRowsEstimate(),
                base.hasPrimaryKey(),
                base.orderingDescription());
    }
}
