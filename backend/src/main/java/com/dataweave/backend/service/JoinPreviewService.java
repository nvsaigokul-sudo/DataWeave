package com.dataweave.backend.service;

import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.sql.SqlIdentifiers;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JoinPreviewService {

    private static final int MAX_ROWS = 500;

    private final ReadOnlyQueryService readOnlyQueryService;

    public JoinPreviewService(ReadOnlyQueryService readOnlyQueryService) {
        this.readOnlyQueryService = readOnlyQueryService;
    }

    public DataPage previewInnerJoin(
            UUID connectionId,
            TableRef left,
            TableRef right,
            String leftKey,
            String rightKey,
            List<String> selectedColumns,
            long offset,
            int limit) throws Exception {
        validateTable(left);
        validateTable(right);
        validateColumn(leftKey);
        validateColumn(rightKey);
        int cappedLimit = Math.min(Math.max(limit, 1), MAX_ROWS);
        long cappedOffset = Math.max(offset, 0);
        String lAlias = "l";
        String rAlias = "r";
        String sql =
                "SELECT "
                        + buildSelectList(selectedColumns, left, right, lAlias, rAlias)
                        + " FROM "
                        + SqlIdentifiers.quote(left.schema())
                        + "."
                        + SqlIdentifiers.quote(left.name())
                        + " "
                        + lAlias
                        + " INNER JOIN "
                        + SqlIdentifiers.quote(right.schema())
                        + "."
                        + SqlIdentifiers.quote(right.name())
                        + " "
                        + rAlias
                        + " ON "
                        + lAlias
                        + "."
                        + SqlIdentifiers.quote(leftKey)
                        + " = "
                        + rAlias
                        + "."
                        + SqlIdentifiers.quote(rightKey)
                        + " LIMIT "
                        + cappedLimit
                        + " OFFSET "
                        + cappedOffset;
        List<Map<String, Object>> rows = readOnlyQueryService.runSelect(connectionId, sql, List.of());
        var columns =
                rows.isEmpty()
                        ? List.<com.dataweave.backend.domain.ColumnMeta>of()
                        : rows.getFirst().keySet().stream()
                                .map(k -> new com.dataweave.backend.domain.ColumnMeta(k, "unknown", true))
                                .toList();
        return new DataPage(
                columns,
                rows,
                cappedOffset,
                cappedLimit,
                null,
                false,
                "Inner join on "
                        + left.schema()
                        + "."
                        + left.name()
                        + "."
                        + leftKey
                        + " = "
                        + right.schema()
                        + "."
                        + right.name()
                        + "."
                        + rightKey);
    }

    private static String buildSelectList(
            List<String> selectedColumns, TableRef left, TableRef right, String lAlias, String rAlias) {
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            return lAlias + ".*, " + rAlias + ".*";
        }
        List<String> parts = new ArrayList<>();
        for (String col : selectedColumns) {
            validateColumn(col);
            String[] split = col.split("\\.", 2);
            if (split.length == 2) {
                String side = split[0].trim();
                String name = split[1].trim();
                if ("left".equalsIgnoreCase(side)) {
                    parts.add(lAlias + "." + SqlIdentifiers.quote(name) + " AS " + SqlIdentifiers.quote("left_" + name));
                } else if ("right".equalsIgnoreCase(side)) {
                    parts.add(rAlias + "." + SqlIdentifiers.quote(name) + " AS " + SqlIdentifiers.quote("right_" + name));
                } else {
                    throw new IllegalArgumentException("Column side must be left or right: " + col);
                }
            } else {
                parts.add(lAlias + "." + SqlIdentifiers.quote(col));
            }
        }
        return String.join(", ", parts);
    }

    private static void validateTable(TableRef table) {
        if (table == null || table.schema() == null || table.name() == null) {
            throw new IllegalArgumentException("Table required");
        }
    }

    private static void validateColumn(String name) {
        if (name == null || name.isBlank() || name.contains(";") || name.contains("\"")) {
            throw new IllegalArgumentException("Invalid column identifier");
        }
    }
}
