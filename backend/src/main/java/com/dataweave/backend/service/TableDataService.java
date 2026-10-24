package com.dataweave.backend.service;

import com.dataweave.backend.domain.ColumnMeta;
import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.persistence.SchemaVersionRepository;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class TableDataService {

    private final ConnectionProfileService connectionProfileService;
    private final PostgresConnectionManager connectionManager;
    private final CatalogMetadataService catalogMetadataService;
    private final SchemaVersionRepository schemaVersionRepository;
    private final TableDataCacheGateway tableDataCacheGateway;
    private final ExecutorService queryExecutor;
    private final int defaultPageSize;
    private final AtomicLong requestSequence = new AtomicLong();
    private final ConcurrentHashMap<String, Long> latestRequestByScope = new ConcurrentHashMap<>();

    public TableDataService(
            ConnectionProfileService connectionProfileService,
            PostgresConnectionManager connectionManager,
            CatalogMetadataService catalogMetadataService,
            SchemaVersionRepository schemaVersionRepository,
            @Lazy TableDataCacheGateway tableDataCacheGateway,
            ExecutorService queryExecutor,
            @Value("${dataweave.query.page-default-size}") int defaultPageSize) {
        this.connectionProfileService = connectionProfileService;
        this.connectionManager = connectionManager;
        this.catalogMetadataService = catalogMetadataService;
        this.schemaVersionRepository = schemaVersionRepository;
        this.tableDataCacheGateway = tableDataCacheGateway;
        this.queryExecutor = queryExecutor;
        this.defaultPageSize = defaultPageSize;
    }

    public CompletableFuture<DataPage> fetchPageAsync(
            UUID connectionId,
            TableRef table,
            long offset,
            Integer limit,
            String requestScope,
            com.dataweave.backend.domain.TableBrowseOptions options) {
        int pageSize = limit == null || limit <= 0 ? defaultPageSize : Math.min(limit, 500);
        com.dataweave.backend.domain.TableBrowseOptions browse =
                options == null ? com.dataweave.backend.domain.TableBrowseOptions.none() : options;
        long requestId = requestSequence.incrementAndGet();
        if (requestScope != null) {
            latestRequestByScope.put(requestScope, requestId);
        }
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        DataPage page =
                                tableDataCacheGateway.getPage(connectionId, table, offset, pageSize, browse);
                        if (requestScope != null && latestRequestByScope.get(requestScope) != requestId) {
                            throw new StaleResponseException("Superseded by a newer request");
                        }
                        return page;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                queryExecutor);
    }

    public DataPage fetchPage(UUID connectionId, TableRef table, long offset, int limit) throws Exception {
        return fetchPage(connectionId, table, offset, limit, com.dataweave.backend.domain.TableBrowseOptions.none());
    }

    public DataPage fetchPage(
            UUID connectionId,
            TableRef table,
            long offset,
            int limit,
            com.dataweave.backend.domain.TableBrowseOptions options)
            throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        List<ColumnMeta> columns = catalogMetadataService.listColumns(connectionId, table.schema(), table.name());
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Unknown table");
        }
        PrimaryKeyInfo pk = detectPrimaryKey(connectionId, table);
        com.dataweave.backend.domain.TableBrowseOptions browse =
                options == null ? com.dataweave.backend.domain.TableBrowseOptions.none() : options;
        String orderBy = resolveOrderBy(pk, columns, browse);
        String whereClause = resolveFilter(columns, browse);
        String sql =
                "SELECT * FROM "
                        + quoteIdentifier(table.schema())
                        + "."
                        + quoteIdentifier(table.name())
                        + whereClause
                        + " "
                        + orderBy
                        + " LIMIT ? OFFSET ?";
        DataSource ds = connectionManager.getDataSource(profile);
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int param = 1;
            if (browse.hasFilter()) {
                String pattern = "%" + browse.filterText().trim().replace("%", "\\%").replace("_", "\\_") + "%";
                for (int i = 0; i < columns.size(); i++) {
                    ps.setString(param++, pattern);
                }
            }
            ps.setInt(param++, limit);
            ps.setLong(param, offset);
            try (ResultSet rs = ps.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        Long estimate = estimateRows(connectionId, table);
        return new DataPage(
                columns,
                rows,
                offset,
                limit,
                estimate,
                pk.hasPrimaryKey(),
                pk.orderingDescription());
    }

    public long schemaVersion(UUID connectionId) {
        return schemaVersionRepository.getVersion(connectionId);
    }

    private Long estimateRows(UUID connectionId, TableRef table) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        String sql =
                "SELECT reltuples::bigint FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace "
                        + "WHERE n.nspname = ? AND c.relname = ?";
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table.schema());
            ps.setString(2, table.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    private PrimaryKeyInfo detectPrimaryKey(UUID connectionId, TableRef table) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        String sql =
                """
                SELECT kcu.column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema = kcu.table_schema
                WHERE tc.constraint_type = 'PRIMARY KEY'
                  AND tc.table_schema = ?
                  AND tc.table_name = ?
                ORDER BY kcu.ordinal_position
                """;
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table.schema());
            ps.setString(2, table.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<String> cols = new ArrayList<>();
                while (rs.next()) {
                    cols.add(rs.getString(1));
                }
                if (cols.isEmpty()) {
                    return new PrimaryKeyInfo(false, "OFFSET pagination (no primary key detected)", java.util.Optional.empty());
                }
                String clause =
                        "ORDER BY "
                                + cols.stream().map(TableDataService::quoteIdentifier).reduce((a, b) -> a + "," + b).orElse("1");
                return new PrimaryKeyInfo(true, "Keyset-friendly order: " + String.join(", ", cols), java.util.Optional.of(clause));
            }
        }
    }

    private static String resolveOrderBy(
            PrimaryKeyInfo pk, List<ColumnMeta> columns, com.dataweave.backend.domain.TableBrowseOptions browse) {
        if (browse.hasSort()) {
            String col = browse.sortColumn().trim();
            boolean known = columns.stream().anyMatch(c -> c.name().equalsIgnoreCase(col));
            if (!known) {
                throw new IllegalArgumentException("Unknown sort column: " + col);
            }
            String quoted = quoteIdentifier(columns.stream().filter(c -> c.name().equalsIgnoreCase(col)).findFirst().get().name());
            return "ORDER BY " + quoted + (browse.sortDescending() ? " DESC" : " ASC");
        }
        return pk.orderByClause().orElse("ORDER BY 1");
    }

    private static String resolveFilter(List<ColumnMeta> columns, com.dataweave.backend.domain.TableBrowseOptions browse) {
        if (!browse.hasFilter()) {
            return "";
        }
        String text = browse.filterText().trim();
        if (text.isEmpty()) {
            return "";
        }
        String pattern = "%" + text.replace("%", "\\%").replace("_", "\\_") + "%";
        List<String> parts = new ArrayList<>();
        for (ColumnMeta col : columns) {
            parts.add("CAST(" + quoteIdentifier(col.name()) + " AS TEXT) ILIKE ? ESCAPE '\\'");
        }
        return " WHERE (" + String.join(" OR ", parts) + ")";
    }

    private static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private record PrimaryKeyInfo(boolean hasPrimaryKey, String orderingDescription, java.util.Optional<String> orderByClause) {}

    public static class StaleResponseException extends RuntimeException {
        public StaleResponseException(String message) {
            super(message);
        }
    }
}
