package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DatasetGraphService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final DatasetGraphSqlGenerator sqlGenerator;
    private final ReadOnlyQueryService readOnlyQueryService;

    public DatasetGraphService(DatasetGraphSqlGenerator sqlGenerator, ReadOnlyQueryService readOnlyQueryService) {
        this.sqlGenerator = sqlGenerator;
        this.readOnlyQueryService = readOnlyQueryService;
    }

    public String serialize(DatasetGraphDefinition graph) throws Exception {
        return JSON.writeValueAsString(graph);
    }

    public DatasetGraphDefinition deserialize(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return DatasetGraphDefinition.empty("public");
        }
        return JSON.readValue(json, DatasetGraphDefinition.class);
    }

    public String generateSql(DatasetGraphDefinition graph, int limit, long offset) throws Exception {
        return sqlGenerator.toSelectSql(graph, limit, offset).sql();
    }

    public DataPage preview(UUID connectionId, DatasetGraphDefinition graph, long offset, int limit) throws Exception {
        var generated = sqlGenerator.toSelectSql(graph, limit, offset);
        List<Map<String, Object>> rows = readOnlyQueryService.runSelect(connectionId, generated.sql(), generated.params());
        var columns =
                rows.isEmpty()
                        ? List.<com.dataweave.backend.domain.ColumnMeta>of()
                        : rows.getFirst().keySet().stream()
                                .map(k -> new com.dataweave.backend.domain.ColumnMeta(k, "unknown", true))
                                .toList();
        return new DataPage(columns, rows, offset, limit, null, false, "Dataset graph preview");
    }
}
