package com.dataweave.backend.service;

import com.dataweave.backend.domain.ColumnMeta;
import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.ForeignKeyRef;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.persistence.SchemaVersionRepository;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CatalogMetadataService {

    private final ConnectionProfileService connectionProfileService;
    private final PostgresConnectionManager connectionManager;
    private final SchemaVersionRepository schemaVersionRepository;

    public CatalogMetadataService(
            ConnectionProfileService connectionProfileService,
            PostgresConnectionManager connectionManager,
            SchemaVersionRepository schemaVersionRepository) {
        this.connectionProfileService = connectionProfileService;
        this.connectionManager = connectionManager;
        this.schemaVersionRepository = schemaVersionRepository;
    }

    @Cacheable(cacheNames = "metadata", key = "#connectionId + ':databases:' + @catalogMetadataService.schemaVersion(#connectionId)")
    public List<String> listDatabases(UUID connectionId) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        try (Connection conn = ds.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                """
                                SELECT datname FROM pg_database
                                WHERE datallowconn AND NOT datistemplate
                                ORDER BY datname
                                """);
                ResultSet rs = ps.executeQuery()) {
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
            return databases;
        }
    }

    @Cacheable(cacheNames = "metadata", key = "#connectionId + ':schemas:' + @catalogMetadataService.schemaVersion(#connectionId)")
    public List<String> listSchemas(UUID connectionId) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        try (Connection conn = ds.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                """
                                SELECT schema_name FROM information_schema.schemata
                                WHERE schema_name NOT IN ('pg_catalog','information_schema','pg_toast')
                                ORDER BY schema_name
                                """);
                ResultSet rs = ps.executeQuery()) {
            List<String> schemas = new ArrayList<>();
            while (rs.next()) {
                schemas.add(rs.getString(1));
            }
            return schemas;
        }
    }

    @Cacheable(cacheNames = "metadata", key = "#connectionId + ':tables:' + #schema + ':' + @catalogMetadataService.schemaVersion(#connectionId)")
    public List<TableRef> listTables(UUID connectionId, String schema) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        try (Connection conn = ds.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                """
                                SELECT table_schema, table_name
                                FROM information_schema.tables
                                WHERE table_schema = ? AND table_type = 'BASE TABLE'
                                ORDER BY table_name
                                """);
                ) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                List<TableRef> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(new TableRef(rs.getString(1), rs.getString(2)));
                }
                return tables;
            }
        }
    }

    @Cacheable(
            cacheNames = "metadata",
            key =
                    "#connectionId + ':columns:' + #schema + ':' + #table + ':' + @catalogMetadataService.schemaVersion(#connectionId)")
    public List<ColumnMeta> listColumns(UUID connectionId, String schema, String table) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        try (Connection conn = ds.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                """
                                SELECT column_name, data_type, is_nullable
                                FROM information_schema.columns
                                WHERE table_schema = ? AND table_name = ?
                                ORDER BY ordinal_position
                                """);
                ) {
            ps.setString(1, schema);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                List<ColumnMeta> columns = new ArrayList<>();
                while (rs.next()) {
                    columns.add(
                            new ColumnMeta(
                                    rs.getString(1),
                                    rs.getString(2),
                                    "YES".equalsIgnoreCase(rs.getString(3))));
                }
                return columns;
            }
        }
    }

    @Cacheable(
            cacheNames = "metadata",
            key =
                    "#connectionId + ':fks:' + #schema + ':' + @catalogMetadataService.schemaVersion(#connectionId)")
    public List<ForeignKeyRef> listForeignKeys(UUID connectionId, String schema) throws Exception {
        ConnectionProfile profile = connectionProfileService.require(connectionId);
        DataSource ds = connectionManager.getDataSource(profile);
        try (Connection conn = ds.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                """
                                SELECT
                                  tc.table_schema,
                                  tc.table_name,
                                  kcu.column_name,
                                  ccu.table_schema AS ref_schema,
                                  ccu.table_name AS ref_table,
                                  ccu.column_name AS ref_column
                                FROM information_schema.table_constraints tc
                                JOIN information_schema.key_column_usage kcu
                                  ON tc.constraint_name = kcu.constraint_name
                                  AND tc.table_schema = kcu.table_schema
                                JOIN information_schema.constraint_column_usage ccu
                                  ON ccu.constraint_name = tc.constraint_name
                                  AND ccu.table_schema = tc.table_schema
                                WHERE tc.constraint_type = 'FOREIGN KEY'
                                  AND tc.table_schema = ?
                                ORDER BY tc.table_name, kcu.ordinal_position
                                """)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                List<ForeignKeyRef> keys = new ArrayList<>();
                while (rs.next()) {
                    keys.add(
                            new ForeignKeyRef(
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4),
                                    rs.getString(5),
                                    rs.getString(6)));
                }
                return keys;
            }
        }
    }

    public long schemaVersion(UUID connectionId) {
        return schemaVersionRepository.getVersion(connectionId);
    }
}
