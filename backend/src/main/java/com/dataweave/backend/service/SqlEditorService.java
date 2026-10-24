package com.dataweave.backend.service;

import com.dataweave.backend.domain.ColumnMeta;
import com.dataweave.backend.domain.SqlQueryHistoryEntry;
import com.dataweave.backend.domain.DataPage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SqlEditorService {

    private final ReadOnlyQueryService readOnlyQueryService;
    private final SqlQueryHistoryService historyService;

    public SqlEditorService(ReadOnlyQueryService readOnlyQueryService, SqlQueryHistoryService historyService) {
        this.readOnlyQueryService = readOnlyQueryService;
        this.historyService = historyService;
    }

    public DataPage executeSelect(UUID connectionId, String sql) throws Exception {
        long start = System.nanoTime();
        Optional<String> error = Optional.empty();
        List<Map<String, Object>> rows = List.of();
        try {
            rows = readOnlyQueryService.runSelect(connectionId, sql, List.of());
        } catch (Exception ex) {
            error = Optional.of(ex.getMessage() == null ? ex.toString() : ex.getMessage());
            long ms = (System.nanoTime() - start) / 1_000_000;
            historyService.record(connectionId, sql, 0, ms, error);
            throw ex;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        historyService.record(connectionId, sql, rows.size(), ms, Optional.empty());
        var columns =
                rows.isEmpty()
                        ? List.<ColumnMeta>of()
                        : rows.getFirst().keySet().stream()
                                .map(k -> new ColumnMeta(k, "unknown", true))
                                .toList();
        return new DataPage(columns, rows, 0, rows.size(), (long) rows.size(), false, "Ad hoc SELECT");
    }

    public List<SqlQueryHistoryEntry> history(int limit) {
        return historyService.listRecent(limit);
    }
}
