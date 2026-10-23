package com.dataweave.backend;

import com.dataweave.backend.cache.CacheDiagnosticsService;
import com.dataweave.backend.cache.CacheInvalidationService;
import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.ConnectionTestResult;
import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.domain.WorkspaceProject;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import com.dataweave.backend.service.CatalogMetadataService;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.DatasetPreviewService;
import com.dataweave.backend.service.ReadOnlyQueryService;
import com.dataweave.backend.service.TableDataService;
import com.dataweave.backend.service.WorkspaceProjectService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

@Component
public class ApplicationFacade {

    private final WorkspaceProjectService workspaceProjectService;
    private final ConnectionProfileService connectionProfileService;
    private final CatalogMetadataService catalogMetadataService;
    private final TableDataService tableDataService;
    private final DatasetPreviewService datasetPreviewService;
    private final ReadOnlyQueryService readOnlyQueryService;
    private final CacheDiagnosticsService cacheDiagnosticsService;
    private final CacheInvalidationService cacheInvalidationService;
    private final PostgresConnectionManager postgresConnectionManager;

    public ApplicationFacade(
            WorkspaceProjectService workspaceProjectService,
            ConnectionProfileService connectionProfileService,
            CatalogMetadataService catalogMetadataService,
            TableDataService tableDataService,
            DatasetPreviewService datasetPreviewService,
            ReadOnlyQueryService readOnlyQueryService,
            CacheDiagnosticsService cacheDiagnosticsService,
            CacheInvalidationService cacheInvalidationService,
            PostgresConnectionManager postgresConnectionManager) {
        this.workspaceProjectService = workspaceProjectService;
        this.connectionProfileService = connectionProfileService;
        this.catalogMetadataService = catalogMetadataService;
        this.tableDataService = tableDataService;
        this.datasetPreviewService = datasetPreviewService;
        this.readOnlyQueryService = readOnlyQueryService;
        this.cacheDiagnosticsService = cacheDiagnosticsService;
        this.cacheInvalidationService = cacheInvalidationService;
        this.postgresConnectionManager = postgresConnectionManager;
    }

    public List<WorkspaceProject> listProjects() {
        return workspaceProjectService.list();
    }

    public WorkspaceProject createProject(String name, String description) {
        return workspaceProjectService.create(name, description);
    }

    public List<ConnectionProfile> listConnections() {
        return connectionProfileService.list();
    }

    public ConnectionProfile saveConnection(
            String name,
            String host,
            int port,
            String database,
            String username,
            char[] password,
            SslMode sslMode,
            int timeoutSeconds) {
        return connectionProfileService.create(name, host, port, database, username, password, sslMode, timeoutSeconds);
    }

    public ConnectionTestResult testConnection(UUID id, char[] passwordOverride) {
        return connectionProfileService.test(id, passwordOverride);
    }

    public void deleteConnection(UUID id) {
        connectionProfileService.delete(id);
    }

    public long refreshSchema(UUID connectionId) {
        return connectionProfileService.refreshSchema(connectionId);
    }

    public List<String> listSchemas(UUID connectionId) throws Exception {
        return catalogMetadataService.listSchemas(connectionId);
    }

    public List<TableRef> listTables(UUID connectionId, String schema) throws Exception {
        return catalogMetadataService.listTables(connectionId, schema);
    }

    public CompletableFuture<DataPage> fetchTablePage(
            UUID connectionId, TableRef table, long offset, Integer limit, String requestScope) {
        return tableDataService.fetchPageAsync(connectionId, table, offset, limit, requestScope);
    }

    public long beginDatasetPreview(String scopeKey) {
        return datasetPreviewService.beginPreviewScope(scopeKey);
    }

    public CompletableFuture<DataPage> previewDataset(
            String scopeKey, UUID connectionId, TableRef table, List<String> columns, long offset, int limit) {
        return datasetPreviewService.previewAsync(scopeKey, connectionId, table, columns, offset, limit);
    }

    public List<Map<String, Object>> runSelect(UUID connectionId, String sql, List<Object> params) throws Exception {
        return readOnlyQueryService.runSelect(connectionId, sql, params);
    }

    public Map<String, Map<String, Number>> cacheDiagnostics() {
        return cacheDiagnosticsService.snapshot();
    }

    public void invalidateAllCaches() {
        cacheInvalidationService.invalidateAll();
    }

    public void shutdown() {
        postgresConnectionManager.shutdown();
    }
}
