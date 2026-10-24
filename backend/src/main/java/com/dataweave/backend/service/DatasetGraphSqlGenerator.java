package com.dataweave.backend.service;

import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.domain.JoinType;
import com.dataweave.backend.sql.SqlIdentifiers;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DatasetGraphSqlGenerator {

    public record GeneratedSql(String sql, java.util.List<Object> params) {}

    public GeneratedSql toSelectSql(DatasetGraphDefinition graph, int limit, long offset) {
        if (graph.sourceTables().isEmpty()) {
            throw new IllegalArgumentException("Add at least one source table");
        }
        List<DatasetGraphDefinition.DatasetSourceTable> tables = graph.sourceTables();
        DatasetGraphDefinition.DatasetSourceTable root = tables.getFirst();
        StringBuilder from =
                new StringBuilder()
                        .append(SqlIdentifiers.quote(graph.schema()))
                        .append(".")
                        .append(SqlIdentifiers.quote(root.tableName()))
                        .append(" ")
                        .append(root.alias());
        for (DatasetGraphDefinition.DatasetJoinEdge edge : graph.joins()) {
            DatasetGraphDefinition.DatasetSourceTable rightTable =
                    tables.stream()
                            .filter(t -> t.alias().equals(edge.rightAlias()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Missing table: " + edge.rightAlias()));
            JoinType type = edge.joinType() == null ? JoinType.INNER : edge.joinType();
            from.append(" ")
                    .append(type.sqlKeyword())
                    .append(" ")
                    .append(SqlIdentifiers.quote(graph.schema()))
                    .append(".")
                    .append(SqlIdentifiers.quote(rightTable.tableName()))
                    .append(" ")
                    .append(edge.rightAlias())
                    .append(" ON ")
                    .append(edge.leftAlias())
                    .append(".")
                    .append(SqlIdentifiers.quote(edge.leftColumn()))
                    .append(" = ")
                    .append(edge.rightAlias())
                    .append(".")
                    .append(SqlIdentifiers.quote(edge.rightColumn()));
        }
        List<String> selectParts = new ArrayList<>();
        if (graph.outputColumns().isEmpty()) {
            selectParts.add(root.alias() + ".*");
        } else {
            for (DatasetGraphDefinition.DatasetOutputColumn col : graph.outputColumns()) {
                String expr =
                        col.sourceAlias()
                                + "."
                                + SqlIdentifiers.quote(col.columnName());
                String label =
                        col.outputAlias() == null || col.outputAlias().isBlank()
                                ? col.columnName()
                                : col.outputAlias();
                selectParts.add(expr + " AS " + SqlIdentifiers.quote(label));
            }
        }
        java.util.List<Object> params = new java.util.ArrayList<>();
        StringBuilder sql =
                new StringBuilder("SELECT ")
                        .append(String.join(", ", selectParts))
                        .append(" FROM ")
                        .append(from);
        if (graph.filterText() != null && !graph.filterText().isBlank()) {
            String col = graph.filterColumn();
            if (col == null || col.isBlank()) {
                throw new IllegalArgumentException("Filter column required when filter text is set");
            }
            String filterExpr;
            if (col.contains(".")) {
                String[] parts = col.split("\\.", 2);
                filterExpr = parts[0] + "." + SqlIdentifiers.quote(parts[1]);
            } else {
                filterExpr = root.alias() + "." + SqlIdentifiers.quote(col);
            }
            sql.append(" WHERE CAST(").append(filterExpr).append(" AS TEXT) ILIKE ? ESCAPE '\\'");
            String pattern =
                    "%"
                            + graph.filterText().trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                            + "%";
            params.add(pattern);
        }
        if (graph.sortColumn() != null && !graph.sortColumn().isBlank()) {
            sql.append(" ORDER BY ")
                    .append(SqlIdentifiers.quote(graph.sortColumn()))
                    .append(graph.sortDescending() ? " DESC" : " ASC");
        }
        sql.append(" LIMIT ").append(Math.min(Math.max(limit, 1), 500));
        sql.append(" OFFSET ").append(Math.max(offset, 0));
        return new GeneratedSql(sql.toString(), params);
    }
}
