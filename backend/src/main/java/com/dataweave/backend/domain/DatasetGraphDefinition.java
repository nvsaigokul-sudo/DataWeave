package com.dataweave.backend.domain;

import java.util.List;

/** Versioned logical dataset graph persisted in SQLite definition_json (v0.3.0). */
public record DatasetGraphDefinition(
        int version,
        String schema,
        List<DatasetSourceTable> sourceTables,
        List<DatasetJoinEdge> joins,
        List<DatasetOutputColumn> outputColumns,
        String filterText,
        String filterColumn,
        String sortColumn,
        boolean sortDescending) {

    public static DatasetGraphDefinition empty(String schema) {
        return new DatasetGraphDefinition(1, schema, List.of(), List.of(), List.of(), null, null, null, false);
    }

    public record DatasetSourceTable(String alias, String tableName, double canvasX, double canvasY) {}

    public record DatasetJoinEdge(
            String leftAlias,
            String leftColumn,
            JoinType joinType,
            String rightAlias,
            String rightColumn) {}

    public record DatasetOutputColumn(String alias, String sourceAlias, String columnName, String outputAlias) {}
}
