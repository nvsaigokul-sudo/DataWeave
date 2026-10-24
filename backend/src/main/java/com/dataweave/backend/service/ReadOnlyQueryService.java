package com.dataweave.backend.service;

import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.persistence.SchemaVersionRepository;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ReadOnlyQueryService {

    private final ConnectionProfileService connectionProfileService;
    private final PostgresConnectionManager connectionManager;
    private final SchemaVersionRepository schemaVersionRepository;

    public ReadOnlyQueryService(
            ConnectionProfileService connectionProfileService,
            PostgresConnectionManager connectionManager,
            SchemaVersionRepository schemaVersionRepository) {
        this.connectionProfileService = connectionProfileService;
        this.connectionManager = connectionManager;
        this.schemaVersionRepository = schemaVersionRepository;
    }

    @Cacheable(
            cacheNames = "queryResults",
            condition = "#sql.trim().toLowerCase().startsWith('select')",
            key =
                    "#connectionId + ':' + #sql + ':' + T(String).valueOf(#params) + ':' + @readOnlyQueryService.schemaVersion(#connectionId)")
    public List<Map<String, Object>> runSelect(UUID connectionId, String sql, List<Object> params) throws Exception {
        String normalized = sql.trim();
        if (!normalized.toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Only read-only SELECT queries are cached");
        }
        if (normalized.contains(";")) {
            throw new IllegalArgumentException("Multi-statement SQL is not allowed");
        }
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(normalized)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }
            ps.setMaxRows(500);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public long schemaVersion(UUID connectionId) {
        return schemaVersionRepository.getVersion(connectionId);
    }
}
